package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import kotlinx.serialization.SerializationException
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 활성 리소스팩에서 HUD 장면 JSON을 읽어 검증된 명세와 로드 오류를 캐시한다.
 */
object HudSceneResourceRegistry : SimpleSynchronousResourceReloadListener {
    private const val SCENE_DIRECTORY = "hud"
    private const val JSON_EXTENSION = ".json"

    private val reloadListenerId = ResourceLocation.fromNamespaceAndPath(
        ResourceStore.MOD_ID,
        "hud_scene_registry",
    )
    private val initialized = AtomicBoolean(false)

    @Volatile
    private var loadedScenes: Map<ResourceLocation, HudSceneSpec> = emptyMap()

    @Volatile
    private var loadErrors: Map<ResourceLocation, SerializationException> = emptyMap()

    /** Fabric 클라이언트 리소스 reload listener를 한 번만 등록한다. */
    fun init() {
        if (!initialized.compareAndSet(false, true)) return
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this)
    }

    /**
     * 리소스 ID에 대응하는 검증된 장면 명세를 반환한다.
     *
     * @return 로드된 명세, 리소스가 없거나 역직렬화에 실패했으면 `null`
     */
    fun get(id: ResourceLocation): HudSceneSpec? = loadedScenes[id]

    /**
     * 리소스 ID를 읽는 중 발생한 역직렬화 오류를 반환한다.
     *
     * 리소스가 단순히 존재하지 않는 경우에는 `null`이다.
     */
    fun getLoadError(id: ResourceLocation): SerializationException? = loadErrors[id]

    override fun getFabricId(): ResourceLocation = reloadListenerId

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        val scenes = HashMap<ResourceLocation, HudSceneSpec>()
        val errors = HashMap<ResourceLocation, SerializationException>()
        val resources = resourceManager.listResources(SCENE_DIRECTORY) { location ->
            location.path.endsWith(JSON_EXTENSION)
        }

        for ((resourceLocation, resource) in resources) {
            val sceneId = sceneIdOf(resourceLocation)
            try {
                val spec = resource.openAsReader().use { HudSceneCodec.decode(it.readText()) }
                scenes[sceneId] = spec
            } catch (exception: Exception) {
                val serializationException = exception as? SerializationException
                    ?: SerializationException("Failed to read HUD scene $resourceLocation", exception)
                errors[sceneId] = serializationException
                ResourceStore.logger.error(
                    "[DynamicRider] Failed to load HUD scene '{}'",
                    resourceLocation,
                    exception,
                )
            }
        }

        loadedScenes = scenes.toMap()
        loadErrors = errors.toMap()
        ResourceStore.logger.info("[DynamicRider] Loaded {} HUD scene(s)", loadedScenes.size)
    }

    private fun sceneIdOf(resourceId: ResourceLocation): ResourceLocation {
        val path = resourceId.path
            .removePrefix("$SCENE_DIRECTORY/")
            .removeSuffix(JSON_EXTENSION)
        return ResourceLocation.fromNamespaceAndPath(resourceId.namespace, path)
    }
}
