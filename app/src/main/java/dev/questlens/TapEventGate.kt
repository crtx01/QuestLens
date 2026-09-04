package dev.questlens

/** Vendor timestamps need not share Android's clock origin. Prime with the retained first event. */
internal class TapEventGate(private val armedAtNs: Long) {
    private var lastEventNs: Long? = null
    private var lastAcceptedAtNs: Long? = null

    fun accept(eventNs: Long, nowNs: Long): Boolean {
        val previous = lastEventNs
        if (previous == null) {
            lastEventNs = eventNs
            return false
        }
        if (eventNs <= previous) {
            if (previous - eventNs > 1_000_000_000L) {
                lastEventNs = eventNs
                lastAcceptedAtNs = nowNs
            }
            return false
        }
        lastEventNs = eventNs
        if (nowNs - armedAtNs < 1_000_000_000L) return false
        val acceptedAt = lastAcceptedAtNs
        if (acceptedAt != null && nowNs - acceptedAt < 800_000_000L) return false
        lastAcceptedAtNs = nowNs
        return true
    }
}
