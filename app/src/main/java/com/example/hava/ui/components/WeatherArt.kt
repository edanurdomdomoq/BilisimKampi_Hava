package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkyBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1425), Color(0xFF060E1A), Color(0xFF080F1C))
                )
            )
    ) {
        content()
    }
}

@Composable
fun WeatherArt(code: Int, modifier: Modifier = Modifier, isNight: Boolean = false) {
    val language = LocalAppLanguage.current
    Canvas(modifier.semantics { contentDescription = weatherDescription(code, language) }) {
        val unit = minOf(size.width, size.height) / 160f
        scale(unit, unit, pivot = Offset.Zero) {
            val sunny = code in 0..2
            val rain = code in 51..67 || code in 80..82 || code >= 95
            val snow = code in 71..77 || code in 85..86
            if (sunny) {
                val c = if (code == 0) Offset(80f, 75f) else Offset(103f, 52f)
                if (isNight) {
                    drawCircle(
                        Brush.radialGradient(listOf(Color(0xFFFFF4BD), Color(0xFFD5E3FF)), c, 34f),
                        34f,
                        c,
                    )
                    drawCircle(Color(0xFF0B1423), 31f, c + Offset(17f, -12f))
                } else {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEC7D).copy(alpha = .7f), Color.Transparent),
                            c,
                            59f,
                        ),
                        59f,
                        c,
                    )
                    repeat(10) { n ->
                        val a = n * Math.PI.toFloat() / 5f
                        val start = c + Offset(cos(a) * 40f, sin(a) * 40f)
                        val end = c + Offset(cos(a) * 51f, sin(a) * 51f)
                        drawLine(Color(0xFFFFE885), start, end, 4.5f, StrokeCap.Round)
                    }
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFF5A0), Color(0xFFFFD535), Color(0xFFF8B91B)),
                            c + Offset(-12f, -14f),
                            56f,
                        ),
                        34f,
                        c,
                    )
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = .65f), Color.Transparent),
                            c + Offset(-12f, -17f),
                            21f,
                        ),
                        21f,
                        c + Offset(-12f, -17f),
                    )
                }
            }
            if (code != 0) {
                cloud(if (sunny) 10f else 15f, if (sunny) 60f else 37f, code >= 95)
                if (code in 45..48) {
                    repeat(2) {
                        drawLine(
                            Color(0xFFA5BDD1),
                            Offset(30f, 121f + it * 12),
                            Offset(130f, 121f + it * 12),
                            5f,
                            StrokeCap.Round,
                        )
                    }
                }
            }
            if (rain)
                repeat(3) { n ->
                    drawLine(
                        Color(0xFF63ADFF),
                        Offset(52f + n * 28, 116f),
                        Offset(45f + n * 28, 131f),
                        6f,
                        StrokeCap.Round,
                    )
                }
            if (snow)
                repeat(3) { n ->
                    val c = Offset(45f + n * 32, 127f)
                    repeat(3) { i ->
                        val a = i * Math.PI.toFloat() / 3
                        val d = Offset(cos(a) * 6f, sin(a) * 6f)
                        drawLine(Color(0xFF6BB7EF), c - d, c + d, 2.5f, StrokeCap.Round)
                    }
                }
            if (code >= 95) {
                val bolt =
                    Path().apply {
                        moveTo(87f, 99f)
                        lineTo(68f, 123f)
                        lineTo(82f, 123f)
                        lineTo(74f, 146f)
                        lineTo(102f, 114f)
                        lineTo(88f, 114f)
                        close()
                    }
                drawPath(bolt, Color(0xFFFFD94A))
            }
        }
    }
}

private fun DrawScope.cloud(x: Float, y: Float, storm: Boolean) {
    val white = if (storm) Color(0xFFC4D6E8) else Color.White
    val bottom = if (storm) Color(0xFF7995B8) else Color(0xFFB8D2EA)
    drawOval(
        Brush.radialGradient(
            listOf(Color(0xFF5E8BB4).copy(alpha = .16f), Color.Transparent),
            Offset(x + 72, y + 57),
            65f,
        ),
        Offset(x + 4, y + 37),
        Size(137f, 38f),
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(white, bottom), y + 30, y + 66),
        Offset(x + 9, y + 28),
        Size(128f, 39f),
        CornerRadius(23f),
    )
    listOf(
            Triple(34f, 37f, 24f),
            Triple(63f, 23f, 31f),
            Triple(95f, 34f, 28f),
            Triple(120f, 45f, 20f),
        )
        .forEach { (cx, cy, r) ->
            val center = Offset(x + cx, y + cy)
            drawCircle(
                Brush.radialGradient(
                    listOf(white, white.copy(alpha = .98f), bottom),
                    center + Offset(-r * .32f, -r * .48f),
                    r * 1.9f,
                ),
                r,
                center,
            )
        }
}
