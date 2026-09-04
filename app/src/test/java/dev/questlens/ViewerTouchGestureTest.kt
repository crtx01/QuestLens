package dev.questlens

import org.junit.Assert.*
import org.junit.Test

class ViewerTouchGestureTest {
    @Test fun distinguishesBothControllersAndToleratesObservedClickJitter() {
        val gesture = ViewerTouchGesture(24f)
        gesture.down(0x100002, 100f, 100f, 1000)
        assertEquals(1, gesture.up(0x100002, 111f, 113f, 1180))
        gesture.down(0x100001, 100f, 100f, 2000)
        assertEquals(-1, gesture.up(0x100001, 100f, 100f, 2200))
    }
    @Test fun dragNeverBecomesZoomEvenAfterReturningToStart() {
        val gesture = ViewerTouchGesture(24f)
        gesture.down(0x100002, 100f, 100f, 1000)
        assertNotNull(gesture.move(140f, 100f))
        assertEquals(0, gesture.up(0x100002, 100f, 100f, 1400))
    }
    @Test fun cancellationLongHoldAndOtherPointersCannotZoom() {
        val gesture = ViewerTouchGesture(24f)
        gesture.down(0x100002, 100f, 100f, 1000)
        gesture.cancel()
        assertEquals(0, gesture.up(0x100002, 100f, 100f, 1200))
        gesture.down(0x100002, 100f, 100f, 2000)
        assertEquals(0, gesture.up(0x100002, 100f, 100f, 2700))
        gesture.down(-1, 100f, 100f, 3000)
        assertEquals(0, gesture.up(-1, 100f, 100f, 3200))
        gesture.down(0x100002, 100f, 100f, 4000)
        assertEquals(0, gesture.up(0x100001, 100f, 100f, 4200))
    }
    @Test fun zoomStepsRespectBoundsAndIntermediateValues() {
        assertEquals(1f, steppedZoom(1f, -1))
        assertEquals(8f, steppedZoom(8f, 1))
        assertEquals(4f, steppedZoom(2.5f, 1))
        assertEquals(2f, steppedZoom(2.5f, -1))
    }
}
