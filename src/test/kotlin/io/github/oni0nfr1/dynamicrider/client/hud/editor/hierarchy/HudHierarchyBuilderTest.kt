package io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementSlotSchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudMetadataReader
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudValueSchema
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudHierarchyBuilderTest {
    private val elementIds = setOf("TEST_CHILD_A", "TEST_CHILD_B")
    private val rootType = type(TestRootSpec.serializer())
    private val childAType = type(TestChildA.serializer())
    private val childBType = type(TestChildB.serializer())
    private val typesById = listOf(rootType, childAType, childBType).associateBy(HudHierarchyElementType::id)

    @Test
    fun `metadata separates value and element properties`() {
        val properties = rootType.metadata.properties.associateBy { it.serialName }

        assertInstanceOf(
            HudValueSchema.Sealed::class.java,
            (properties.getValue("value").schema as HudPropertySchema.Value).schema,
        )
        assertInstanceOf(
            HudElementSlotSchema.Fixed::class.java,
            (properties.getValue("fixed").schema as HudPropertySchema.Element).schema,
        )
        assertInstanceOf(
            HudElementSlotSchema.Sealed::class.java,
            (properties.getValue("variant").schema as HudPropertySchema.Element).schema,
        )
        assertTrue(properties.getValue("nullableFixed").nullable)
        assertTrue(properties.getValue("nullableVariant").nullable)
    }

    @Test
    fun `metadata rejects a sealed property mixing element and value variants`() {
        val failure = assertThrows(IllegalArgumentException::class.java) {
            HudMetadataReader.read(TestMixedRootSpec.serializer(), elementIds::contains)
        }

        assertTrue(failure.message.orEmpty().contains("mixes HUD element and value variants"))
    }

    @Test
    fun `builder retains fixed sealed and absent nullable element slots`() {
        val model = builder(
            TestRootSpec(
                fixed = TestChildA(nested = TestChildB()),
                variant = TestChildA(),
                nullableFixed = null,
                nullableVariant = null,
            ),
        ).build()

        val root = model[HudPath.of("root")]
        val fixed = model[HudPath.of("root", "fixed")]
        val nested = model[HudPath.of("root", "fixed", "nested")]
        val variant = model[HudPath.of("root", "variant")]
        val nullableFixed = model[HudPath.of("root", "nullableFixed")]
        val nullableVariant = model[HudPath.of("root", "nullableVariant")]

        assertEquals(1, model.roots.size)
        assertInstanceOf(HudHierarchyNodeKind.Root::class.java, root?.kind)
        assertEquals("TEST_CHILD_A", (fixed?.kind as HudHierarchyNodeKind.Fixed).selectedTypeId)
        assertEquals("TEST_CHILD_B", (nested?.kind as HudHierarchyNodeKind.Fixed).selectedTypeId)
        val variantKind = variant?.kind as HudHierarchyNodeKind.Variant
        assertEquals("TEST_CHILD_A", variantKind.selectedTypeId)
        assertEquals(listOf("TEST_CHILD_A", "TEST_CHILD_B"), variantKind.variants.map { it.typeId })
        assertFalse((nullableFixed?.kind as HudHierarchyNodeKind.Fixed).present)
        assertNull(nullableFixed.kind.selectedTypeId)
        val nullableVariantKind = nullableVariant?.kind as HudHierarchyNodeKind.Variant
        assertNull(nullableVariantKind.selectedTypeId)
        assertTrue(nullableVariantKind.nullable)
    }

    @Test
    fun `model indexes every node by its absolute escaped path`() {
        val model = builder(TestRootSpec()).build()
        val root = model.roots.single()
        val childPath = HudPath.of("root.with.dot", "fixed")
        val escaped = HudPath.parse("root~1with~1dot.fixed")

        val dottedModel = builder(TestRootSpec(), "root.with.dot").build()

        assertTrue(HudPath.of("root", "fixed") in model)
        assertSame(
            root.children.first { it.path == HudPath.of("root", "fixed") },
            model[HudPath.of("root", "fixed")],
        )
        assertEquals(childPath, escaped)
        assertSame(dottedModel.roots.single().children.first(), dottedModel[escaped])
    }

    private fun builder(spec: TestRootSpec, id: String = "root"): HudHierarchyBuilder {
        val document = HudSceneDocument.from(HudSceneSpec(elementIds = listOf(id), elements = listOf(spec)))
        return HudHierarchyBuilder(
            document = document,
            catalog = object : HudHierarchyElementCatalog {
                override fun bySpec(spec: HudElementSpec<*, *>) = rootType.takeIf { spec is TestRootSpec }
                override fun byId(id: String) = typesById[id]
            },
        )
    }

    private fun <T : HudElementSpec<*, *>> type(serializer: KSerializer<T>): HudHierarchyElementType =
        HudHierarchyElementType(
            id = serializer.descriptor.serialName,
            serializer = serializer,
            metadata = HudMetadataReader.read(serializer, elementIds::contains),
        )
}

@Serializable
private sealed interface TestValue {
    @Serializable
    @SerialName("TEST_VALUE_A")
    data object A : TestValue

    @Serializable
    @SerialName("TEST_VALUE_B")
    data object B : TestValue
}

@Serializable
private sealed interface TestChildSpec : HudElementSpec<HudElement<KartState>, KartState>

@Serializable
@SerialName("TEST_CHILD_A")
private data class TestChildA(
    override val layout: HudLayoutSpec = HudLayoutSpec(),
    val nested: TestChildB? = null,
) : TestChildSpec {
    override fun requiredStateClass() = KartState::class.java
    override fun create(context: HudSceneContext<KartState>, parent: ElementHolder): HudElement<KartState> =
        error("Test spec is not instantiated")
}

@Serializable
@SerialName("TEST_CHILD_B")
private data class TestChildB(
    override val layout: HudLayoutSpec = HudLayoutSpec(),
) : TestChildSpec {
    override fun requiredStateClass() = KartState::class.java
    override fun create(context: HudSceneContext<KartState>, parent: ElementHolder): HudElement<KartState> =
        error("Test spec is not instantiated")
}

@Serializable
@SerialName("TEST_ROOT")
private data class TestRootSpec(
    override val layout: HudLayoutSpec = HudLayoutSpec(),
    val value: TestValue = TestValue.A,
    val fixed: TestChildA = TestChildA(),
    val variant: TestChildSpec = TestChildB(),
    val nullableFixed: TestChildB? = null,
    val nullableVariant: TestChildSpec? = null,
) : HudElementSpec<HudElement<KartState>, KartState> {
    override fun requiredStateClass() = KartState::class.java
    override fun create(context: HudSceneContext<KartState>, parent: ElementHolder): HudElement<KartState> =
        error("Test spec is not instantiated")
}

@Serializable
private sealed interface TestMixedSpec

@Serializable
@SerialName("TEST_CHILD_A")
private data class TestMixedElement(
    override val layout: HudLayoutSpec = HudLayoutSpec(),
) : TestMixedSpec, HudElementSpec<HudElement<KartState>, KartState> {
    override fun requiredStateClass() = KartState::class.java
    override fun create(context: HudSceneContext<KartState>, parent: ElementHolder): HudElement<KartState> =
        error("Test spec is not instantiated")
}

@Serializable
@SerialName("TEST_MIXED_VALUE")
private data object TestMixedValue : TestMixedSpec

@Serializable
@SerialName("TEST_MIXED_ROOT")
private data class TestMixedRootSpec(
    override val layout: HudLayoutSpec = HudLayoutSpec(),
    val mixed: TestMixedSpec = TestMixedValue,
) : HudElementSpec<HudElement<KartState>, KartState> {
    override fun requiredStateClass() = KartState::class.java
    override fun create(context: HudSceneContext<KartState>, parent: ElementHolder): HudElement<KartState> =
        error("Test spec is not instantiated")
}
