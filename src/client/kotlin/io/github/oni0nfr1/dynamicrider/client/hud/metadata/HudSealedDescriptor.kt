package io.github.oni0nfr1.dynamicrider.client.hud.metadata

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor

const val HUD_CLASS_DISCRIMINATOR: String = "type"

data class HudSealedVariantDescriptor(
    val serialName: String,
    val descriptor: SerialDescriptor,
)

/** sealed serializer descriptor에 등록된 구체 subtype descriptor를 반환한다. */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.hudSealedVariants(): List<HudSealedVariantDescriptor> {
    require(kind == PolymorphicKind.SEALED) { "Descriptor '$serialName' is not sealed" }
    val valueIndex = getElementIndex("value")
    require(valueIndex >= 0) { "Sealed descriptor '$serialName' has no value descriptor" }
    val valueDescriptor = getElementDescriptor(valueIndex)
    return List(valueDescriptor.elementsCount) { index ->
        HudSealedVariantDescriptor(
            serialName = valueDescriptor.getElementName(index),
            descriptor = valueDescriptor.getElementDescriptor(index),
        )
    }
}
