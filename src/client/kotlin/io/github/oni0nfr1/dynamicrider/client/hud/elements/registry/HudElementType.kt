package io.github.oni0nfr1.dynamicrider.client.hud.elements.registry

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudMetadataReader
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/** 단일 HUD 요소 타입의 직렬화, 호환성, 기본 생성 및 runtime 생성 정보를 묶는다. */
@OptIn(ExperimentalSerializationApi::class)
class HudElementType<S : KartState, SPEC : HudElementSpec<*, S>>(
    val specClass: KClass<SPEC>,
    val serializer: KSerializer<SPEC>,
    val requiredStateClass: Class<S>,
    private val defaultSpecFactory: () -> SPEC,
    /** `false`면 codec과 검증에는 등록하되 editor palette에는 노출하지 않는다. */
    val visibleInEditor: Boolean = true,
) {
    val id: String = serializer.descriptor.serialName
    val metadata: HudElementMetadata by lazy {
        HudMetadataReader.read(serializer) { HudElementTypeRegistry.byId(it) != null }
    }

    init {
        require(id.isNotBlank()) { "HUD element type ID must not be blank" }
        val defaultSpec = defaultSpecFactory()
        require(specClass.isInstance(defaultSpec)) {
            "Default HUD element spec for '$id' is not an instance of ${specClass.qualifiedName}"
        }
        require(defaultSpec.requiredStateClass() == requiredStateClass) {
            "Default HUD element spec for '$id' requires ${defaultSpec.requiredStateClass().name}, " +
                "but the registry declares ${requiredStateClass.name}"
        }
    }

    /** 새로운 기본 spec을 생성한다. */
    fun createDefaultSpec(): SPEC = defaultSpecFactory()

    /** 이 요소가 [stateType]의 장면에서 사용 가능한지 반환한다. */
    fun accepts(stateType: KartStateType<out KartState>): Boolean =
        requiredStateClass.isAssignableFrom(stateType.stateClass)

    /** 등록된 spec과 context를 사용해 runtime HUD 요소를 생성한다. */
    fun create(
        spec: SPEC,
        context: HudSceneContext<S>,
        parent: ElementHolder,
    ): HudElement<S> = spec.create(context, parent)
}
