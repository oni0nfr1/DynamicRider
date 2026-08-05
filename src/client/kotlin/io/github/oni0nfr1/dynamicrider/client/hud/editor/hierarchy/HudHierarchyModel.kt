package io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath

/** 편집 document 안의 root 요소와 중첩 요소 슬롯 하나를 나타낸다. */
data class HudHierarchyNode(
    val path: HudPath,
    val nameKey: String,
    val kind: HudHierarchyNodeKind,
    val children: List<HudHierarchyNode> = emptyList(),
)

/** 하이어라키 노드가 root인지, 고정 또는 다형 자식 요소 슬롯인지 구분한다. */
sealed interface HudHierarchyNodeKind {
    val selectedTypeId: String?

    data class Root(
        override val selectedTypeId: String,
    ) : HudHierarchyNodeKind

    data class Fixed(
        val typeId: String,
        val nullable: Boolean,
        val present: Boolean,
    ) : HudHierarchyNodeKind {
        override val selectedTypeId: String?
            get() = typeId.takeIf { present }
    }

    data class Variant(
        override val selectedTypeId: String?,
        val nullable: Boolean,
        val variants: List<HudHierarchyVariant>,
    ) : HudHierarchyNodeKind
}

/** sealed 요소 슬롯에서 선택할 수 있는 구체 요소 타입이다. */
data class HudHierarchyVariant(
    val typeId: String,
    val nameKey: String,
)

/** GUI가 경로로 안정적으로 조회할 수 있는 HUD 요소 하이어라키 snapshot이다. */
class HudHierarchyModel(
    val roots: List<HudHierarchyNode>,
) {
    private val nodesByPath: Map<HudPath, HudHierarchyNode> = buildMap {
        fun add(node: HudHierarchyNode) {
            require(put(node.path, node) == null) { "Duplicate HUD hierarchy path '${node.path}'" }
            node.children.forEach(::add)
        }
        roots.forEach(::add)
    }

    val nodes: List<HudHierarchyNode> = nodesByPath.values.toList()

    /** [path]에 위치한 노드를 반환하며 없으면 `null`을 반환한다. */
    operator fun get(path: HudPath): HudHierarchyNode? = nodesByPath[path]

    /** [path]가 현재 snapshot에 포함되는지 반환한다. */
    operator fun contains(path: HudPath): Boolean = path in nodesByPath
}
