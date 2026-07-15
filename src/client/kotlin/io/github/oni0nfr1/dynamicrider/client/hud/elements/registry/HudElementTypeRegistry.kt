package io.github.oni0nfr1.dynamicrider.client.hud.elements.registry

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.debug.EditorPropertyStressElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.DynNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.JiuStyleSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.*
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.*
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** 명시적으로 지원하는 모든 HUD 요소 타입의 중앙 registry다. */
object HudElementTypeRegistry {
    val EDITOR_PROPERTY_STRESS_TEST = HudElementType(
        EditorPropertyStressElement.Spec::class,
        EditorPropertyStressElement.Spec.serializer(),
        KartState::class.java,
        EditorPropertyStressElement::Spec,
    )
    val GRADIENT_GAUGE_BAR = HudElementType(
        GradientGaugeBar.Spec::class,
        GradientGaugeBar.Spec.serializer(),
        NitroKartState::class.java,
        GradientGaugeBar::Spec,
    )
    val PLAIN_NITRO_SLOT = HudElementType(
        PlainNitroSlot.Spec::class,
        PlainNitroSlot.Spec.serializer(),
        NitroKartState::class.java,
        PlainNitroSlot::Spec,
    )
    val NITRO_SLOT = HudElementType(
        DynNitroSlot.Spec::class,
        DynNitroSlot.Spec.serializer(),
        NitroKartState::class.java,
        DynNitroSlot::Spec,
    )
    val PLAIN_RANKING_TABLE = HudElementType(
        PlainRankingTable.Spec::class,
        PlainRankingTable.Spec.serializer(),
        KartState::class.java,
        PlainRankingTable::Spec,
    )
    val JIU_TACHOMETER_SIMPLE = HudElementType(
        JiuStyleSpdMeter.Spec::class,
        JiuStyleSpdMeter.Spec.serializer(),
        SpeedKartState::class.java,
        JiuStyleSpdMeter::Spec,
    )
    val RIDE_TIMER = HudElementType(
        HudTimer.Spec::class,
        HudTimer.Spec.serializer(),
        KartState::class.java,
        HudTimer::Spec,
    )
    val SPECTATE_TIMER = HudElementType(
        SpectateHudTimer.Spec::class,
        SpectateHudTimer.Spec.serializer(),
        KartState::class.java,
        SpectateHudTimer::Spec,
    )
    val CHARGE_TACHOMETER = HudElementType(
        ChargeTachometer.Spec::class,
        ChargeTachometer.Spec.serializer(),
        ChargeKartState::class.java,
        ChargeTachometer::Spec,
    )
    val CHARGE_SPEEDOMETER = HudElementType(
        ChargeSpdMeter.Spec::class,
        ChargeSpdMeter.Spec.serializer(),
        SpeedKartState::class.java,
        ChargeSpdMeter::Spec,
    )
    val CHARGE_GAUGE = HudElementType(
        ChargeGauge.Spec::class,
        ChargeGauge.Spec.serializer(),
        NitroKartState::class.java,
        ChargeGauge::Spec,
    )
    val CHARGER_GAUGE = HudElementType(
        ChargerGauge.Spec::class,
        ChargerGauge.Spec.serializer(),
        ChargeKartState::class.java,
        ChargerGauge::Spec,
    )
    val CHARGE_ICONS = HudElementType(
        ChargeIcons.Spec::class,
        ChargeIcons.Spec.serializer(),
        ChargeKartState::class.java,
        ChargeIcons::Spec,
    )
    val JIU_TACHOMETER = HudElementType(
        JiuTachometer.Spec::class,
        JiuTachometer.Spec.serializer(),
        JiuKartState::class.java,
        JiuTachometer::Spec,
    )
    val JIU_SPEEDOMETER = HudElementType(
        JiuSpdMeter.Spec::class,
        JiuSpdMeter.Spec.serializer(),
        SpeedKartState::class.java,
        JiuSpdMeter::Spec,
    )
    val JIU_ICONS = HudElementType(
        JiuIcons.Spec::class,
        JiuIcons.Spec.serializer(),
        JiuKartState::class.java,
        JiuIcons::Spec,
    )
    val JIU_GAUGE = HudElementType(
        JiuGauge.Spec::class,
        JiuGauge.Spec.serializer(),
        NitroKartState::class.java,
        JiuGauge::Spec,
    )
    val V1_TACHOMETER = HudElementType(
        V1Tachometer.Spec::class,
        V1Tachometer.Spec.serializer(),
        V1KartState::class.java,
        V1Tachometer::Spec,
    )

    val entries: List<HudElementType<*, *>> = listOf(
        EDITOR_PROPERTY_STRESS_TEST,
        GRADIENT_GAUGE_BAR,
        PLAIN_NITRO_SLOT,
        NITRO_SLOT,
        PLAIN_RANKING_TABLE,
        JIU_TACHOMETER_SIMPLE,
        RIDE_TIMER,
        SPECTATE_TIMER,
        CHARGE_TACHOMETER,
        CHARGE_SPEEDOMETER,
        CHARGE_GAUGE,
        CHARGER_GAUGE,
        CHARGE_ICONS,
        JIU_TACHOMETER,
        JIU_SPEEDOMETER,
        JIU_ICONS,
        JIU_GAUGE,
        V1_TACHOMETER,
    )

    private val byId = entries.associateBy(HudElementType<*, *>::id).also {
        require(it.size == entries.size) { "HUD element type IDs must be unique" }
    }
    private val bySpecClass = entries.associateBy(HudElementType<*, *>::specClass).also {
        require(it.size == entries.size) { "HUD element spec classes must be unique" }
    }

    val serializersModule: SerializersModule = SerializersModule {
        polymorphic(HudElementSpec::class) {
            registerAll()
        }
    }

    /** 안정적인 직렬화 [id]로 요소 타입을 조회한다. */
    fun byId(id: String): HudElementType<*, *>? = byId[id]

    /** [spec]의 구체 클래스에 등록된 요소 타입을 조회한다. */
    fun bySpec(spec: HudElementSpec<*, *>): HudElementType<*, *>? = bySpecClass[spec::class]

    /** [stateType]의 장면에 추가할 수 있는 요소 타입만 반환한다. */
    fun compatibleWith(stateType: KartStateType<out KartState>): List<HudElementType<*, *>> =
        entries.filter { it.accepts(stateType) }

    private fun PolymorphicModuleBuilder<HudElementSpec<*, *>>.registerAll() {
        register(EDITOR_PROPERTY_STRESS_TEST)
        register(GRADIENT_GAUGE_BAR)
        register(PLAIN_NITRO_SLOT)
        register(NITRO_SLOT)
        register(PLAIN_RANKING_TABLE)
        register(JIU_TACHOMETER_SIMPLE)
        register(RIDE_TIMER)
        register(SPECTATE_TIMER)
        register(CHARGE_TACHOMETER)
        register(CHARGE_SPEEDOMETER)
        register(CHARGE_GAUGE)
        register(CHARGER_GAUGE)
        register(CHARGE_ICONS)
        register(JIU_TACHOMETER)
        register(JIU_SPEEDOMETER)
        register(JIU_ICONS)
        register(JIU_GAUGE)
        register(V1_TACHOMETER)
    }

    private fun <S : KartState, SPEC : HudElementSpec<*, S>>
        PolymorphicModuleBuilder<HudElementSpec<*, *>>.register(type: HudElementType<S, SPEC>) {
        subclass(type.specClass, type.serializer)
    }
}
