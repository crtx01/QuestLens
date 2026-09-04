package dev.questlens

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer

class CaptureMathTest {
    @Test fun stripsPaddingAndAcceptsShortFinalRow() {
        val source = ByteBuffer.wrap(byteArrayOf(1,2,3,4,5,6,7,8,99,99,99,99,9,10,11,12,13,14,15,16))
        val output = ByteBuffer.allocate(16)
        packRgba(source, output, 2, 2, 12, 4)
        assertArrayEquals((1..16).map { it.toByte() }.toByteArray(), output.array())
        assertEquals(0, source.position())
    }
    @Test fun handlesPixelStrideAndCrop() {
        val source = ByteBuffer.allocate(48)
        for (i in 0 until 48) source.put(i, i.toByte())
        val output = ByteBuffer.allocate(8)
        packRgba(source, output, 1, 2, 24, 8, left = 1)
        assertArrayEquals(byteArrayOf(8,9,10,11,32,33,34,35), output.array())
    }
    @Test fun cropRespectsTopAndBufferOrigin() {
        val source = ByteBuffer.allocate(60)
        for (i in 0 until 60) source.put(i, i.toByte())
        source.position(4)
        val output = ByteBuffer.allocate(8)
        packRgba(source, output, 2, 1, 16, 4, left = 1, top = 1)
        assertArrayEquals(byteArrayOf(24,25,26,27,28,29,30,31), output.array())
    }
    @Test fun fitPreservesAspectRatio() {
        assertEquals(0.5f, fittedScale(960f, 800f, 1920, 1080), 0.001f)
    }
    @Test fun panReachesEveryEdgeWithoutLosingImage() {
        assertEquals(0f, boundedPan(100f, 400f, 800f), 0f)
        assertEquals(1200f, boundedPan(9999f, 3200f, 800f), 0f)
        assertEquals(-1200f, boundedPan(-9999f, 3200f, 800f), 0f)
        assertEquals(35f, boundedPan(35f, 3200f, 800f), 0f)
    }
}
