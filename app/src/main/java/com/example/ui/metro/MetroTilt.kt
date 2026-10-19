package com.example.ui.metro

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize

/**
 * Implements the signature Windows Phone Metro 3D click tilt effect.
 * When touched, the element tilts in 3D perspective towards the touch point,
 * scales slightly down, and snaps back with a responsive spring when released.
 */
fun Modifier.metroTilt(
    enabled: Boolean = true,
    maxTiltAngle: Float = 10f,
    onClick: (() -> Unit)? = null
): Modifier = this.then(
    Modifier.metroTiltInternal(
        enabled = enabled,
        maxTiltAngle = maxTiltAngle,
        onClick = onClick
    )
)

private fun Modifier.metroTiltInternal(
    enabled: Boolean,
    maxTiltAngle: Float,
    onClick: (() -> Unit)?
): Modifier {
    return this.then(
        Modifier
            .semantics {
                if (onClick != null) {
                    role = Role.Button
                }
            }
    )
}

@Composable
fun MetroTiltBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxTiltAngle: Float = 9f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val centerX = if (componentSize.width > 0) componentSize.width / 2f else 1f
    val centerY = if (componentSize.height > 0) componentSize.height / 2f else 1f

    val normX = if (isPressed && componentSize.width > 0) {
        ((touchOffset.x - centerX) / centerX).coerceIn(-1f, 1f)
    } else 0f

    val normY = if (isPressed && componentSize.height > 0) {
        ((touchOffset.y - centerY) / centerY).coerceIn(-1f, 1f)
    } else 0f

    val targetRotX = if (isPressed) -normY * maxTiltAngle else 0f
    val targetRotY = if (isPressed) normX * maxTiltAngle else 0f
    val targetScale = if (isPressed) 0.96f else 1f

    val rotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "metroTiltRotX"
    )
    val rotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "metroTiltRotY"
    )
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "metroTiltScale"
    )

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .onSizeChanged { componentSize = it }
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                scaleX = scale
                scaleY = scale
                cameraDistance = 16f * density.density
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchOffset = down.position
                    isPressed = true

                    var finished = false
                    while (!finished) {
                        val event = awaitPointerEvent()
                        val current = event.changes.firstOrNull()
                        if (current != null) {
                            if (current.pressed) {
                                touchOffset = current.position
                            } else {
                                finished = true
                                val bounds = Rect(
                                    0f, 0f,
                                    size.width.toFloat(),
                                    size.height.toFloat()
                                )
                                if (bounds.contains(current.position)) {
                                    onClick?.invoke()
                                }
                            }
                        } else {
                            finished = true
                        }
                    }
                    isPressed = false
                }
            }
    ) {
        content()
    }
}
