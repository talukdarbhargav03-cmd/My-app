package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.composed
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState

@Composable
fun Modifier.fadeInOnMount(duration: Int = 500, delayMillis: Int = 0): Modifier {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delayMillis,
                easing = LinearOutSlowInEasing
            )
        )
    }
    return this.graphicsLayer(alpha = animatable.value)
}

@Composable
fun Modifier.fadeInUpOnMount(duration: Int = 600, delayMillis: Int = 0): Modifier {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delayMillis,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val translationY = (1f - animatable.value) * 20f
    return this.graphicsLayer(
        alpha = animatable.value,
        translationY = translationY
    )
}

@Composable
fun Modifier.skeletonPulse(isDark: Boolean): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val baseColor = if (isDark) Color(0xFF23322B) else Color(0xFFE5EDE9)
    return this.background(baseColor.copy(alpha = alpha))
}

@Composable
fun LoadingCardSkeleton(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Image/Header placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .skeletonPulse(isDark)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .skeletonPulse(isDark)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Multi-line body placeholders
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .skeletonPulse(isDark)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .skeletonPulse(isDark)
        )
    }
}

@Composable
fun LoadingRecommendationsSkeleton(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .skeletonPulse(isDark)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .skeletonPulse(isDark)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .skeletonPulse(isDark)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .skeletonPulse(isDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .skeletonPulse(isDark)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .skeletonPulse(isDark)
        )
    }
}

@Composable
fun Modifier.hoverScaleAndShadow(
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    maxScale: Float = 1.015f,
    maxElevation: Dp = 8.dp
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) maxScale else 1f,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "hoverScale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isHovered) maxElevation else 0.dp,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "hoverShadow"
    )

    this
        .hoverable(interactionSource)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            clip = false
        )
}


