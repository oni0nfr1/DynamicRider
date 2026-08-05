package io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementType
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HUD_CLASS_DISCRIMINATOR
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementSlotSchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** 현재 document의 중첩 [HudElementSpec] property를 GUI용 하이어라키 snapshot으로 만든다. */
class HudHierarchyBuilder(
    private val document: HudSceneDocument,
    private val catalog: HudHierarchyElementCatalog = RegistryHudHierarchyElementCatalog,
) {
    private val json = Json {
        classDiscriminator = HUD_CLASS_DISCRIMINATOR
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }

    /** 호출 시점의 document 내용으로 새 하이어라키 snapshot을 생성한다. */
    fun build(): HudHierarchyModel = HudHierarchyModel(
        document.elements.map { element ->
            val type = requireNotNull(catalog.bySpec(element.spec)) {
                "HUD element spec '${element.spec::class.qualifiedName}' is not registered"
            }
            val encoded = type.encode(json, element.spec)
            val path = HudPath.of(element.id)
            HudHierarchyNode(
                path = path,
                nameKey = type.metadata.nameKey,
                kind = HudHierarchyNodeKind.Root(type.id),
                children = buildChildren(type.metadata.properties, encoded, path),
            )
        },
    )

    private fun buildChildren(
        properties: List<HudPropertyMetadata>,
        encoded: JsonObject,
        parentPath: HudPath,
    ): List<HudHierarchyNode> = properties.mapNotNull { property ->
        if (property.hidden) return@mapNotNull null
        val schema = property.schema as? HudPropertySchema.Element ?: return@mapNotNull null
        val path = parentPath.child(property.serialName)
        val value = encoded[property.serialName]
            ?: error("Encoded HUD element property '$path' is missing")

        when (val slot = schema.schema) {
            is HudElementSlotSchema.Fixed -> buildFixedNode(property, slot, value, path)
            is HudElementSlotSchema.Sealed -> buildVariantNode(property, slot, value, path)
        }
    }

    private fun buildFixedNode(
        property: HudPropertyMetadata,
        slot: HudElementSlotSchema.Fixed,
        value: kotlinx.serialization.json.JsonElement,
        path: HudPath,
    ): HudHierarchyNode {
        val present = value !is JsonNull
        require(property.nullable || present) { "Non-null HUD element property '$path' is null" }
        val children = if (present) {
            val type = requireNotNull(catalog.byId(slot.serialName)) {
                "HUD element type '${slot.serialName}' at '$path' is not registered"
            }
            buildChildren(
                type.metadata.properties,
                value as? JsonObject ?: error("Encoded HUD element property '$path' is not an object"),
                path,
            )
        } else {
            emptyList()
        }
        return HudHierarchyNode(
            path = path,
            nameKey = property.nameKey,
            kind = HudHierarchyNodeKind.Fixed(slot.serialName, property.nullable, present),
            children = children,
        )
    }

    private fun buildVariantNode(
        property: HudPropertyMetadata,
        slot: HudElementSlotSchema.Sealed,
        value: kotlinx.serialization.json.JsonElement,
        path: HudPath,
    ): HudHierarchyNode {
        val selected = if (value is JsonNull) {
            require(property.nullable) { "Non-null sealed HUD element property '$path' is null" }
            null
        } else {
            val objectValue = value as? JsonObject
                ?: error("Encoded sealed HUD element property '$path' is not an object")
            (objectValue[slot.discriminator] as? JsonPrimitive)?.content
                ?: error("Encoded sealed HUD element property '$path' has no '${slot.discriminator}'")
        }
        require(selected == null || slot.variants.any { it.serialName == selected }) {
            "Encoded sealed HUD element property '$path' has unknown variant '$selected'"
        }
        val children = if (selected != null) {
            val type = requireNotNull(catalog.byId(selected)) {
                "HUD element type '$selected' at '$path' is not registered"
            }
            buildChildren(type.metadata.properties, value as JsonObject, path)
        } else {
            emptyList()
        }
        return HudHierarchyNode(
            path = path,
            nameKey = property.nameKey,
            kind = HudHierarchyNodeKind.Variant(
                selectedTypeId = selected,
                nullable = property.nullable,
                variants = slot.variants.map { HudHierarchyVariant(it.serialName, it.nameKey) },
            ),
            children = children,
        )
    }
}

/** 하이어라키를 만들 때 필요한 등록 요소 타입의 직렬화 및 메타데이터 view다. */
data class HudHierarchyElementType(
    val id: String,
    val serializer: KSerializer<out HudElementSpec<*, *>>,
    val metadata: HudElementMetadata,
) {
    @Suppress("UNCHECKED_CAST")
    fun encode(json: Json, spec: HudElementSpec<*, *>): JsonObject =
        json.encodeToJsonElement(serializer as KSerializer<HudElementSpec<*, *>>, spec) as? JsonObject
            ?: error("HUD element spec '$id' must serialize as an object")
}

/** 하이어라키 빌더가 root 및 중첩 요소 타입을 조회하는 경계다. */
interface HudHierarchyElementCatalog {
    /** [spec]의 구체 요소 타입을 반환한다. */
    fun bySpec(spec: HudElementSpec<*, *>): HudHierarchyElementType?

    /** 직렬화 타입 [id]에 해당하는 구체 요소 타입을 반환한다. */
    fun byId(id: String): HudHierarchyElementType?
}

private object RegistryHudHierarchyElementCatalog : HudHierarchyElementCatalog {
    override fun bySpec(spec: HudElementSpec<*, *>): HudHierarchyElementType? =
        HudElementTypeRegistry.bySpec(spec)?.toHierarchyType()

    override fun byId(id: String): HudHierarchyElementType? =
        HudElementTypeRegistry.byId(id)?.toHierarchyType()
}

private fun HudElementType<*, *>.toHierarchyType() = HudHierarchyElementType(id, serializer, metadata)
