package dev.questlens

internal fun steppedZoom(current: Float, direction: Int): Float {
    val steps = listOf(1f, 2f, 4f, 6f, 8f)
    return when {
        direction > 0 -> steps.firstOrNull { it > current + .01f } ?: 8f
        direction < 0 -> steps.lastOrNull { it < current - .01f } ?: 1f
        else -> current.coerceIn(1f, 8f)
    }
}
