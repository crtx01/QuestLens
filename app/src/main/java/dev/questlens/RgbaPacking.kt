package dev.questlens

import java.nio.ByteBuffer

/** Copies only actual pixels, including when the final row has no trailing padding. */
internal fun packRgba(source: ByteBuffer, target: ByteBuffer, width: Int, height: Int,
                      rowStride: Int, pixelStride: Int, left: Int = 0, top: Int = 0) {
    require(width > 0 && height > 0 && pixelStride >= 4)
    require(rowStride >= (left + width - 1) * pixelStride + 4)
    val input = source.duplicate()
    val origin = input.position()
    target.clear()
    for (row in 0 until height) {
        val start = origin + (top + row) * rowStride + left * pixelStride
        if (pixelStride == 4) {
            input.limit(source.limit())
            input.position(start)
            input.limit(start + width * 4)
            target.put(input)
        } else {
            for (col in 0 until width) {
                val offset = start + col * pixelStride
                repeat(4) { target.put(source.get(offset + it)) }
            }
        }
    }
    target.flip()
}
