package dev.questlens

import kotlin.math.max
import kotlin.math.min

internal fun fittedScale(viewWidth: Float, viewHeight: Float, imageWidth: Int, imageHeight: Int): Float =
    min(viewWidth / imageWidth, viewHeight / imageHeight)

internal fun boundedPan(value: Float, imageExtent: Float, viewportExtent: Float): Float {
    val bound = max(0f, (imageExtent - viewportExtent) / 2f)
    return value.coerceIn(-bound, bound)
}
