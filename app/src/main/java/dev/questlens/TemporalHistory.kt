package dev.questlens

internal data class EncodedFrame(val bytes: ByteArray, val timestampNs: Long, val receivedAt: Long)

/** Compressed RAM only; bounded by time, count and bytes. Caller serializes access. */
internal class TemporalHistory(private val maxBytes: Int = 16 * 1024 * 1024,
                               private val maxCount: Int = 24, private val maxAgeMs: Long = 12_000) {
    private val frames = ArrayDeque<EncodedFrame>()
    var byteCount = 0; private set
    val size get() = frames.size
    operator fun get(index: Int) = frames.elementAt(index)
    fun add(frame: EncodedFrame) {
        while (frames.isNotEmpty() && frame.receivedAt - frames.first().receivedAt > maxAgeMs) removeFirst()
        if (frame.bytes.size > maxBytes) return
        while (frames.isNotEmpty() && (frames.size >= maxCount || byteCount + frame.bytes.size > maxBytes)) removeFirst()
        frames.addLast(frame)
        byteCount += frame.bytes.size
    }
    fun atOrBefore(time: Long): Int = frames.indexOfLast { it.receivedAt <= time }.coerceAtLeast(0)
    fun clear() { frames.clear(); byteCount = 0 }
    private fun removeFirst() { byteCount -= frames.removeFirst().bytes.size }
}
