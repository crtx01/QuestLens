package dev.questlens

import org.junit.Assert.*
import org.junit.Test

class TemporalHistoryTest {
    private fun frame(time: Long, bytes: Int = 4) = EncodedFrame(ByteArray(bytes), time, time)
    @Test fun boundsBytesCountAndAge() {
        val history = TemporalHistory(maxBytes = 10, maxCount = 2, maxAgeMs = 100)
        history.add(frame(1)); history.add(frame(2)); history.add(frame(3))
        assertEquals(2, history.size)
        assertEquals(8, history.byteCount)
        assertEquals(2L, history[0].receivedAt)
        history.add(frame(200))
        assertEquals(1, history.size)
        history.add(frame(201, 11))
        assertEquals(1, history.size)
        history.clear()
        assertEquals(0, history.byteCount)
    }
    @Test fun selectsPreMenuFrameAndFallsBackToOldest() {
        val history = TemporalHistory()
        history.add(frame(1000)); history.add(frame(1500)); history.add(frame(2000))
        assertEquals(1, history.atOrBefore(1600))
        assertEquals(0, history.atOrBefore(500))
        assertEquals(2, history.atOrBefore(3000))
    }
}
