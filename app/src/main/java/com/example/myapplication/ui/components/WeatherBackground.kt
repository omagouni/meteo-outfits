package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun WeatherBackground(
    condition: String,
    isNight: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val conditionLower = condition.lowercase()

    val gradientColors = getGradientForCondition(conditionLower, isNight)

    val animatedTopColor by animateColorAsState(
        targetValue = gradientColors.first,
        animationSpec = tween(durationMillis = 1500),
        label = "topColor"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = gradientColors.second,
        animationSpec = tween(durationMillis = 1500),
        label = "bottomColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedTopColor, animatedBottomColor)
                )
            )
    ) {
        // Particle effects
        when {
            conditionLower.contains("rain") || conditionLower.contains("drizzle") ->
                RainEffect()
            conditionLower.contains("snow") ->
                SnowEffect()
            conditionLower.contains("clear") && !isNight ->
                SunshineEffect()
            isNight ->
                StarsEffect()
        }
        content()
    }
}

private fun getGradientForCondition(condition: String, isNight: Boolean): Pair<Color, Color> {
    if (isNight) {
        return when {
            condition.contains("clear") -> Pair(Color(0xFF0F1C3F), Color(0xFF1A1A4E))
            condition.contains("cloud") -> Pair(Color(0xFF1A2332), Color(0xFF2C3E50))
            condition.contains("rain") || condition.contains("drizzle") -> Pair(Color(0xFF141E30), Color(0xFF243B55))
            condition.contains("thunderstorm") -> Pair(Color(0xFF0D0D1A), Color(0xFF1A1A33))
            condition.contains("snow") -> Pair(Color(0xFF1A2332), Color(0xFF34495E))
            else -> Pair(Color(0xFF0F1C3F), Color(0xFF1A1A4E))
        }
    }
    return when {
        condition.contains("clear") -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
        condition.contains("cloud") -> Pair(Color(0xFF89B4D4), Color(0xFFB8C6D4))
        condition.contains("rain") || condition.contains("drizzle") -> Pair(Color(0xFF4B6584), Color(0xFF778CA3))
        condition.contains("thunderstorm") -> Pair(Color(0xFF2C3A47), Color(0xFF596275))
        condition.contains("snow") -> Pair(Color(0xFFC8D6E5), Color(0xFFE8EDF1))
        condition.contains("mist") || condition.contains("fog") || condition.contains("haze") ->
            Pair(Color(0xFFA4B0BD), Color(0xFFC8D6E5))
        else -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
    }
}

@Composable
private fun RainEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainDrop"
    )

    val raindrops = remember {
        List(60) {
            RainDrop(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f,
                length = Random.nextFloat() * 30f + 15f,
                speed = Random.nextFloat() * 0.3f + 0.7f,
                alpha = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        raindrops.forEach { drop ->
            val y = ((drop.startY + offset * drop.speed) % 1.2f) * size.height
            drawLine(
                color = Color.White.copy(alpha = drop.alpha),
                start = Offset(drop.x * size.width, y),
                end = Offset(drop.x * size.width - 3f, y + drop.length),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
private fun SnowEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowFlake"
    )

    val snowflakes = remember {
        List(40) {
            SnowFlake(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.2f,
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.3f + 0.4f,
                wobble = Random.nextFloat() * 20f,
                alpha = Random.nextFloat() * 0.5f + 0.3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        snowflakes.forEach { flake ->
            val y = ((flake.startY + offset * flake.speed) % 1.2f) * size.height
            val wobbleX = kotlin.math.sin((offset * 6.28f + flake.wobble).toDouble()).toFloat() * 15f
            drawCircle(
                color = Color.White.copy(alpha = flake.alpha),
                radius = flake.radius,
                center = Offset(flake.x * size.width + wobbleX, y)
            )
        }
    }
}

@Composable
private fun SunshineEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x40FFD700),
                    Color(0x20FFD700),
                    Color(0x00FFD700)
                ),
                center = Offset(size.width * 0.8f, size.height * 0.08f),
                radius = 180f * pulse
            ),
            center = Offset(size.width * 0.8f, size.height * 0.08f),
            radius = 180f * pulse
        )
    }
}

@Composable
private fun StarsEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    val stars = remember {
        List(30) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f,
                radius = Random.nextFloat() * 2f + 0.5f,
                twinkleOffset = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val alpha = ((twinkle + star.twinkleOffset) % 1f) * 0.6f + 0.1f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.radius,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}

private data class RainDrop(
    val x: Float,
    val startY: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float
)

private data class SnowFlake(
    val x: Float,
    val startY: Float,
    val radius: Float,
    val speed: Float,
    val wobble: Float,
    val alpha: Float
)

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val twinkleOffset: Float
)
private data class Moon(
    val x: Float,
    val r: Float
)   