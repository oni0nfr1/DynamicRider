package io.github.oni0nfr1.dynamicrider.client.resource.atlas

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.SpriteAtlas
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.iterator

object AtlasRegistry : SimpleSynchronousResourceReloadListener {

    private const val META_DIRECTORY = "atlas_meta"
    private const val JSON_EXTENSION = ".json"

    private val reloadListenerId = ResourceLocation.fromNamespaceAndPath(
        ResourceStore.MOD_ID,
        "atlas_registry",
    )

    private val initialized = AtomicBoolean(false)
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = false
    }

    @Volatile
    private var loadedAtlases: Map<ResourceLocation, Atlas> = emptyMap()

    fun init() {
        if (!initialized.compareAndSet(false, true)) return

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(this)
    }

    fun atlas(id: ResourceLocation): AtlasDelegate = AtlasDelegate(id)

    fun sprite(id: ResourceLocation): SpriteAtlasDelegate = SpriteAtlasDelegate(id)

    fun number(id: ResourceLocation): NumberAtlasDelegate = NumberAtlasDelegate(id)

    fun getAtlas(id: ResourceLocation): Atlas? = loadedAtlases[id]

    fun getSprite(id: ResourceLocation): SpriteAtlas? = getAtlas(id) as? SpriteAtlas

    fun getNumber(id: ResourceLocation): NumberAtlas? = getAtlas(id) as? NumberAtlas

    fun requireAtlas(id: ResourceLocation): Atlas = getAtlas(id)
        ?: error("Atlas '$id' was not found")

    fun requireSprite(id: ResourceLocation): SpriteAtlas = requireType(id, "SpriteAtlas")

    fun requireNumber(id: ResourceLocation): NumberAtlas = requireType(id, "NumberAtlas")

    fun contains(id: ResourceLocation): Boolean = id in loadedAtlases

    override fun getFabricId(): ResourceLocation = reloadListenerId

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        reload(resourceManager)
    }

    fun reload(resourceManager: ResourceManager) {
        val loaded = HashMap<ResourceLocation, Atlas>()
        val resources = resourceManager.listResources(META_DIRECTORY) { location ->
            location.path.endsWith(JSON_EXTENSION)
        }

        for ((resourceLocation, resource) in resources) {
            try {
                val atlasId = atlasIdOf(resourceLocation)
                val data = resource.openAsReader().use { reader ->
                    json.decodeFromString<AtlasData>(reader.readText())
                }

                validate(data)
                loaded[atlasId] = data.create()
            } catch (exception: Exception) {
                ResourceStore.logger.error(
                    "[DynamicRider] Failed to load atlas metadata '{}'",
                    resourceLocation,
                    exception,
                )
            }
        }

        loadedAtlases = loaded.toMap()
        ResourceStore.logger.info(
            "[DynamicRider] Loaded {} atlas metadata file(s)",
            loadedAtlases.size,
        )
    }

    private inline fun <reified T : Atlas> requireType(
        id: ResourceLocation,
        expectedType: String,
    ): T {
        val atlas = requireAtlas(id)
        return atlas as? T ?: error(
            "Atlas '$id' is ${atlas::class.java.simpleName}, expected $expectedType"
        )
    }

    private fun atlasIdOf(metadataId: ResourceLocation): ResourceLocation {
        val path = metadataId.path
        val prefix = "$META_DIRECTORY/"

        require(path.startsWith(prefix) && path.endsWith(JSON_EXTENSION)) {
            "Invalid atlas metadata path: $metadataId"
        }

        val atlasPath = path
            .removePrefix(prefix)
            .removeSuffix(JSON_EXTENSION)

        require(atlasPath.isNotEmpty()) {
            "Atlas metadata path must contain an id: $metadataId"
        }

        return ResourceLocation.fromNamespaceAndPath(metadataId.namespace, atlasPath)
    }

    private fun validate(data: AtlasData) {
        // Accessing these properties performs the shared dimension and grid validation.
        data.rowCount
        data.colCount
    }
}
