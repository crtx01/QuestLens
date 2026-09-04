package dev.questlens

/** Horizon 2D virtual controller IDs observed on Quest 3S; unknown pointers only pan. */
internal fun controllerZoomDirection(deviceId: Int): Int = when (deviceId) {
    0x100002 -> 1 // Right Touch controller
    0x100001 -> -1 // Left Touch controller
    else -> 0
}

/** A trigger click and a drag must never both change zoom. Coordinates are local to the image. */
internal class ViewerTouchGesture(private val slop: Float) {
    private var deviceId = 0
    private var startedAt = 0L
    private var originX = 0f
    private var originY = 0f
    private var active = false
    private var dragged = false

    fun down(device: Int, x: Float, y: Float, time: Long) {
        deviceId = device
        originX = x
        originY = y
        startedAt = time
        active = true
        dragged = false
    }

    fun move(x: Float, y: Float): Pair<Float, Float>? {
        if (!active) return null
        val dx = x - originX
        val dy = y - originY
        if (dx * dx + dy * dy > slop * slop) dragged = true
        return if (dragged) dx to dy else null
    }

    fun up(device: Int, x: Float, y: Float, time: Long): Int {
        if (!active) return 0
        move(x, y)
        val click = !dragged && device == deviceId && time - startedAt in 0..500
        active = false
        return if (click) controllerZoomDirection(deviceId) else 0
    }

    fun cancel() { active = false }
}
