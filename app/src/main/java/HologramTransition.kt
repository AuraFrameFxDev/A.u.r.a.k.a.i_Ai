package auraframefx.api.client.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * HologramTransition composable for futuristic lockscreen or UI transitions.
 * Combines fade, scale, and optional color/blur for a holographic effect.
 */
@Composable
fun HologramTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 700,
    startScale: Float = 0.85f,
    endScale: Float = 1f,
    startAlpha: Float = 0.3f,
    endAlpha: Float = 1f,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(if (visible) startScale else endScale) }
    val alpha = remember { Animatable(if (visible) startAlpha else endAlpha) }

    LaunchedEffect(visible) {
        scale.animateTo(
            if (visible) endScale else startScale,
            animationSpec = tween(durationMillis)
        )
        alpha.animateTo(
            if (visible) endAlpha else startAlpha,
            animationSpec = tween(durationMillis)
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
                this.alpha = alpha.value
                // Optionally add blur or color filter for more hologram effect
            }
    ) {
        content()
    }
}
