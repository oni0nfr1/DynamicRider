package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.animation.OneShotTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.SpriteAtlas
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge.SkidNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudColor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudRange
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class StyledNitroSlot(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    NitroSlot by SkidNitroSlot(context.kartState) {
    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("nitro_slot/styled_nitro_slot"),
        )

        val ATLAS get() = AtlasRegistry.requireSprite(META.atlas)
        val NITRO_ATLAS get() = AtlasRegistry.requireSprite(META.nitroAtlas)
        val TEAM_NITRO_ATLAS get() = AtlasRegistry.requireSprite(META.teamNitroAtlas)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val atlas: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val nitroAtlas: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val teamNitroAtlas: ResourceLocation,
        val backgroundColumn: Int,
        val frameColumn: Int,
        val iconColumn: Int,

        val defaultStyleRow: Int,
        val jiuStyleRow: Int,
        val xStyleRow: Int,
        val v1StyleRow: Int,
        val chargeStyleRow: Int,
    ) : ElementMetaData

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    private val style = spec.style
    private val slotIndex = spec.slotIndex
    private val convertAnimation = OneShotTimer(1000, nanoTime = context::nanoTime)
    private var wasTeamBoost = false

    private val StyleSpec.row: Int
        get() = when (this) {
            is StyleSpec.Default -> META.defaultStyleRow
            is StyleSpec.Jiu -> META.jiuStyleRow
            is StyleSpec.X -> META.xStyleRow
            is StyleSpec.V1 -> META.v1StyleRow
            is StyleSpec.Charge -> META.chargeStyleRow
        }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        if (maxBoost < slotIndex) return

        val row = style.row
        val background = ATLAS.cellAt(row, META.backgroundColumn)
        val frame = ATLAS.cellAt(row, META.frameColumn)
        val decoration = ATLAS.cellAt(row, META.iconColumn)

        when (val style = style) {
            is StyleSpec.Default -> {
                background.draw(guiGraphics, 0, 0)
                frame.drawHueShifted(guiGraphics, 0, 0, style.frameHue)
                decoration.draw(guiGraphics, 0, 0)
            }
            is StyleSpec.Jiu -> {
                background.draw(guiGraphics, 0, 0)
                frame.drawHueShifted(guiGraphics, 0, 0, style.frameHue)
                decoration.drawHueShifted(guiGraphics, 0, 0, style.iconHue)
            }
            is StyleSpec.X -> {
                background.draw(guiGraphics, 0, 0, style.backgroundColor)
                frame.draw(guiGraphics, 0, 0)
                decoration.draw(guiGraphics, 0, 0, style.iconColor)
            }
            is StyleSpec.V1 -> {
                background.draw(guiGraphics, 0, 0, style.backgroundColor)
                frame.draw(guiGraphics, 0, 0)
                decoration.draw(guiGraphics, 0, 0, style.iconColor)
            }
            is StyleSpec.Charge -> {
                background.draw(guiGraphics, 0, 0, style.backgroundColor)
                frame.draw(guiGraphics, 0, 0)
                decoration.draw(guiGraphics, 0, 0, style.iconColor)
            }
        }

        // 일반 texture와 hue-shift texture는 서로 다른 RenderType buffer에 쌓인다. 장식 layer를 먼저
        // 확정하지 않으면 live HUD의 최종 flush 순서에 따라 nitro icon 위에 그려질 수 있다.
        guiGraphics.flush()
        renderNitroIcon(guiGraphics)
    }

    private fun renderNitroIcon(guiGraphics: GuiGraphics) {
        if (nitro < slotIndex) {
            wasTeamBoost = false
            return
        }

        val cell = if (teamNitro >= slotIndex) {
            if (!wasTeamBoost) {
                convertAnimation.start()
                wasTeamBoost = true
            }
            if ((convertAnimation.progress * 8).toInt() % 2 == 0) {
                TEAM_NITRO_ATLAS.cellAt(0, 0)
            } else {
                NITRO_ATLAS.cellAt(0, 0)
            }
        } else {
            wasTeamBoost = false
            NITRO_ATLAS.cellAt(0, 0)
        }

        cell.drawCentered(guiGraphics)
    }

    private fun SpriteAtlas.Cell.drawCentered(guiGraphics: GuiGraphics) {
        draw(
            guiGraphics,
            x = (this@StyledNitroSlot.width - width) / 2,
            y = (this@StyledNitroSlot.height - height) / 2,
        )
    }

    @Serializable
    @SerialName("STYLED_NITRO_SLOT")
    @HudElementInfo(category = "nitro")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val style: StyleSpec = StyleSpec.Default(),
        @HudRange(min = 1.0, max = 16.0, step = 1.0)
        val slotIndex: Int = 1,
    ) : HudElementSpec<StyledNitroSlot, NitroKartState> {
        override fun requiredStateClass() = NitroKartState::class.java

        override fun create(
            context: HudSceneContext<NitroKartState>,
            parent: ElementHolder,
        ) = StyledNitroSlot(this, context, parent)
    }

    @Serializable
    sealed interface StyleSpec {
        @Serializable
        @SerialName("default")
        data class Default(
            @HudRange(min = -180.0, max = 180.0)
            val frameHue: Float = 0f,
        ) : StyleSpec

        @Serializable
        @SerialName("jiu")
        data class Jiu(
            @HudRange(min = -180.0, max = 180.0)
            val frameHue: Float = 0f,
            @HudRange(min = -180.0, max = 180.0)
            val iconHue: Float = 0f,
        ) : StyleSpec

        @Serializable
        @SerialName("x")
        data class X(
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val backgroundColor: Int = 0xFFFFFFFF.toInt(),
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val iconColor: Int = 0xFFFFFFFF.toInt(),
        ) : StyleSpec

        @Serializable
        @SerialName("v1")
        data class V1(
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val backgroundColor: Int = 0xFFFFFFFF.toInt(),
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val iconColor: Int = 0xFFFFFFFF.toInt(),
        ) : StyleSpec

        @Serializable
        @SerialName("charge")
        data class Charge(
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val backgroundColor: Int = 0xFFFFFFFF.toInt(),
            @HudColor
            @Serializable(with = HexColorSerdes::class)
            val iconColor: Int = 0xFFFFFFFF.toInt(),
        ) : StyleSpec
    }
}
