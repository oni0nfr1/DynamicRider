package io.github.oni0nfr1.dynamicrider.client.resource.element

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.atomic.AtomicBoolean

object ElementRegistry : SimpleSynchronousResourceReloadListener {

    private const val META_DIRECTORY = "element_meta"
    private const val JSON_EXTENSION = ".json"

    private val reloadListenerId = ResourceLocation.fromNamespaceAndPath(
        ResourceStore.MOD_ID,
        "element_registry",
    )

    private val initialized = AtomicBoolean(false)
    private val json = Json {
        ignoreUnknownKeys = false
    }

    @Volatile
    private var loadedMetadata: Map<ResourceLocation, JsonElement> = emptyMap()

    fun init() {
        if (!initialized.compareAndSet(false, true)) return

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(this)
    }

    inline fun <reified T : ElementMetaData> elementMeta(id: ResourceLocation): ElementMetaDelegate<T> =
        ElementMetaDelegate(id, serializer())

    fun <T : ElementMetaData> getElementMeta(
        id: ResourceLocation,
        deserializer: DeserializationStrategy<T>,
    ): T? = loadedMetadata[id]?.let { json.decodeFromJsonElement(deserializer, it) }

    fun <T : ElementMetaData> requireElementMeta(
        id: ResourceLocation,
        deserializer: DeserializationStrategy<T>,
    ): T = getElementMeta(id, deserializer)
        ?: error("Element metadata '$id' was not found")

    fun contains(id: ResourceLocation): Boolean = id in loadedMetadata

    override fun getFabricId(): ResourceLocation = reloadListenerId

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        reload(resourceManager)
    }

    fun reload(resourceManager: ResourceManager) {
        val loaded = HashMap<ResourceLocation, JsonElement>()
        val resources = resourceManager.listResources(META_DIRECTORY) { location ->
            location.path.endsWith(JSON_EXTENSION)
        }

        for ((resourceLocation, resource) in resources) {
            try {
                val metadataId = metadataIdOf(resourceLocation)
                val metadata = resource.openAsReader().use { reader ->
                    json.parseToJsonElement(reader.readText())
                }

                loaded[metadataId] = metadata
            } catch (exception: Exception) {
                ResourceStore.logger.error(
                    "[DynamicRider] Failed to load element metadata '{}'",
                    resourceLocation,
                    exception,
                )
            }
        }

        loadedMetadata = loaded.toMap()
        ResourceStore.logger.info(
            "[DynamicRider] Loaded {} element metadata file(s)",
            loadedMetadata.size,
        )
    }

    private fun metadataIdOf(resourceId: ResourceLocation): ResourceLocation {
        val path = resourceId.path
        val prefix = "$META_DIRECTORY/"

        require(path.startsWith(prefix) && path.endsWith(JSON_EXTENSION)) {
            "Invalid element metadata path: $resourceId"
        }

        val metadataPath = path
            .removePrefix(prefix)
            .removeSuffix(JSON_EXTENSION)

        require(metadataPath.isNotEmpty()) {
            "Element metadata path must contain an id: $resourceId"
        }

        return ResourceLocation.fromNamespaceAndPath(resourceId.namespace, metadataPath)
    }
}
