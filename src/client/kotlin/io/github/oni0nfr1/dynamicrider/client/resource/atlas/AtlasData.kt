package io.github.oni0nfr1.dynamicrider.client.resource.atlas

import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.SpriteAtlas
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation

@Serializable
sealed interface AtlasData {
    @Serializable(with = ResourceLocationSerializer::class)
    val texture: ResourceLocation
    val width: Int
    val height: Int
    val cellWidth: Int
    val cellHeight: Int
    val rowCount: Int
        get() {
            require(height > 0) { "height must be positive: $height" }
            require(cellHeight > 0) { "cellHeight must be positive: $cellHeight" }
            require(height % cellHeight == 0) {
                "height must be divisible by cellHeight: height=$height, cellHeight=$cellHeight"
            }
            return height / cellHeight
        }

    val colCount: Int
        get() {
            require(width > 0) { "width must be positive: $width" }
            require(cellWidth > 0) { "cellWidth must be positive: $cellWidth" }
            require(width % cellWidth == 0) {
                "width must be divisible by cellWidth: width=$width, cellWidth=$cellWidth"
            }
            return width / cellWidth
        }

    fun create(): Atlas

    @Serializable
    @SerialName("SPRITE")
    data class Sprite(
        @Serializable(with = ResourceLocationSerializer::class)
        override val texture: ResourceLocation,
        override val width: Int,
        override val height: Int,
        override val cellWidth: Int,
        override val cellHeight: Int,
    ): AtlasData {

        override fun create() = SpriteAtlas(
            texture,
            width,
            height,
            cellWidth,
            cellHeight,
        )
    }

    @Serializable
    @SerialName("NUMBER")
    data class Number(
        @Serializable(with = ResourceLocationSerializer::class)
        override val texture: ResourceLocation,
        val digitWidth: Int,
        val digitHeight: Int,
        val digitOrder: String = "0123456789",
    ) : AtlasData {

        override val width: Int
            get() = digitWidth * digitOrder.length
        override val height: Int
            get() = digitHeight
        override val cellWidth: Int
            get() = digitWidth
        override val cellHeight: Int
            get() = digitHeight

        override fun create() = NumberAtlas(
            texture,
            digitWidth,
            digitHeight,
            digitOrder
        )
    }

}
