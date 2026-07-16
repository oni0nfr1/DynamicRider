package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudPreviewTransformTest {
    @Test
    fun `fit preserves aspect ratio and centers letterbox`() {
        val transform = HudPreviewTransform.fit(160, 90, 10f, 20f, 100f, 100f)

        assertEquals(0.625f, transform.scale)
        assertEquals(10f, transform.contentX)
        assertEquals(41.875f, transform.contentY)
        assertEquals(100f, transform.contentWidth)
        assertEquals(56.25f, transform.contentHeight)
    }

    @Test
    fun `logical and screen coordinate conversion round trips`() {
        val transform = HudPreviewTransform.fit(320, 180, 8f, 38f, 160f, 120f)

        val screen = transform.logicalToScreen(123.5f, 72.25f)
        val logical = transform.screenToLogical(screen.x.toDouble(), screen.y.toDouble())

        assertEquals(123.5f, logical.x, 0.0001f)
        assertEquals(72.25f, logical.y, 0.0001f)
    }

    @Test
    fun `content hit test excludes letterbox`() {
        val transform = HudPreviewTransform.fit(160, 90, 0f, 0f, 100f, 100f)

        assertFalse(transform.containsScreenPoint(50.0, 10.0))
        assertTrue(transform.containsScreenPoint(50.0, 50.0))
        assertFalse(transform.containsScreenPoint(100.0, 50.0))
    }

    @Test
    fun `matching display and logical dimensions produce identity transform`() {
        val transform = HudPreviewTransform.fit(320, 180, 0f, 0f, 320f, 180f)

        assertEquals(1f, transform.scale)
        assertEquals(HudPreviewTransform.Point(41f, 73f), transform.logicalToScreen(41f, 73f))
    }

    @Test
    fun `changing display area does not change logical coordinates`() {
        val editing = HudPreviewTransform.fit(320, 180, 8f, 38f, 190f, 120f)
        val preview = HudPreviewTransform.fit(320, 180, 0f, 0f, 320f, 180f)

        val editingPoint = editing.logicalToScreen(240f, 135f)
        val previewPoint = preview.logicalToScreen(240f, 135f)

        assertEquals(240f, editing.screenToLogical(editingPoint.x.toDouble(), editingPoint.y.toDouble()).x, 0.0001f)
        assertEquals(135f, editing.screenToLogical(editingPoint.x.toDouble(), editingPoint.y.toDouble()).y, 0.0001f)
        assertEquals(240f, previewPoint.x)
        assertEquals(135f, previewPoint.y)
    }

    @Test
    fun `screen drag is converted back to logical distance`() {
        val transform = HudPreviewTransform.fit(320, 180, 10f, 20f, 160f, 90f)
        val start = transform.logicalToScreen(40f, 30f)
        val end = transform.screenToLogical((start.x + 25f).toDouble(), (start.y + 10f).toDouble())

        assertEquals(90f, end.x, 0.0001f)
        assertEquals(50f, end.y, 0.0001f)
    }
}
