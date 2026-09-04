package dev.questlens

import androidx.compose.foundation.Canvas
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZoomViewer(frame: FrozenFrame, zoom: Float, onZoom: (Float) -> Unit,
               resetKey: Int, modifier: Modifier = Modifier) {
    val image = remember(frame) { frame.bitmap.asImageBitmap() }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var pan by remember(frame, resetKey) { mutableStateOf(Offset.Zero) }
    val currentZoom by rememberUpdatedState(zoom)
    val currentOnZoom by rememberUpdatedState(onZoom)
    val slop = with(LocalDensity.current) { 24.dp.toPx() }
    val gesture = remember(frame, resetKey, slop) { ViewerTouchGesture(slop) }
    var panAtDown by remember { mutableStateOf(Offset.Zero) }
    fun clamp(offset: Offset, z: Float): Offset {
        val scale = fittedScale(viewport.width.toFloat(), viewport.height.toFloat(), image.width, image.height) * z
        return Offset(boundedPan(offset.x, image.width * scale, viewport.width.toFloat()),
            boundedPan(offset.y, image.height * scale, viewport.height.toFloat()))
    }
    LaunchedEffect(zoom, viewport) { pan = clamp(pan, zoom) }
    Canvas(modifier.clipToBounds().onSizeChanged { viewport = it }
        .semantics { contentDescription = "Frozen image. Click with the right trigger to zoom in or the left trigger to zoom out. Hold a trigger and drag to move." }
        .pointerInteropFilter { event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    panAtDown = pan
                    gesture.down(event.deviceId, event.x, event.y, event.eventTime)
                }
                MotionEvent.ACTION_MOVE -> gesture.move(event.x, event.y)?.let { (x, y) ->
                    pan = clamp(panAtDown + Offset(x, y), currentZoom)
                }
                MotionEvent.ACTION_UP -> {
                    val direction = gesture.up(event.deviceId, event.x, event.y, event.eventTime)
                    if (direction != 0) {
                        val newZoom = steppedZoom(currentZoom, direction)
                        val anchor = Offset(event.x - viewport.width / 2f, event.y - viewport.height / 2f)
                        pan = clamp((pan - anchor) * (newZoom / currentZoom) + anchor, newZoom)
                        currentOnZoom(newZoom)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> gesture.cancel()
            }
            true
        }) {
        val scale = fittedScale(size.width, size.height, image.width, image.height) * zoom
        val rendered = IntSize((image.width * scale).roundToInt().coerceAtLeast(1),
            (image.height * scale).roundToInt().coerceAtLeast(1))
        val offset = clamp(pan, zoom)
        drawImage(image, dstOffset = IntOffset(((size.width - rendered.width) / 2 + offset.x).roundToInt(),
            ((size.height - rendered.height) / 2 + offset.y).roundToInt()),
            dstSize = rendered, filterQuality = FilterQuality.None)
    }
}
