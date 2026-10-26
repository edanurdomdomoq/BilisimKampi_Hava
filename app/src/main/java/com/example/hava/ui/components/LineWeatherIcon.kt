package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LineWeatherIcon(code: Int, modifier: Modifier = Modifier) {
    val description = weatherDescription(code, LocalAppLanguage.current)
    Canvas(modifier.semantics { contentDescription = description }) {
        val unit = minOf(size.width, size.height) / 80f
        scale(unit, unit, pivot = Offset.Zero) {
            if (code in 0..2) {
                val c = if (code == 0) Offset(40f, 36f) else Offset(48f, 23f)
                val radius = if (code == 0) 16f else 13f
                val gold = Color(0xFFFFC747)
                drawCircle(gold, radius, c, style = Stroke(3.5f))
                repeat(8) { n ->
                    val a = n * Math.PI.toFloat() / 4
                    drawLine(
                        gold,
                        c + Offset(cos(a) * (radius + 8), sin(a) * (radius + 8)),
                        c + Offset(cos(a) * (radius + 14), sin(a) * (radius + 14)),
                        3f,
                        StrokeCap.Round,
                    )
                }
            }
            if (code != 0) {
                val path =
                    Path().apply {
                        moveTo(21f, 55f)
                        cubicTo(4f, 55f, 4f, 30f, 22f, 30f)
                        cubicTo(25f, 9f, 53f, 10f, 57f, 31f)
                        cubicTo(78f, 30f, 80f, 55f, 62f, 55f)
                        lineTo(21f, 55f)
                    }
                drawPath(path, Color(0xFF15233A).copy(alpha = .68f))
                drawPath(path, Color(0xFFF4F0FF), style = Stroke(3.5f, cap = StrokeCap.Round))
            }
            if (code in 51..67 || code in 80..82 || code >= 95)
                repeat(3) { n ->
                    drawLine(
                        Color(0xFF6ED1FF),
                        Offset(28f + n * 15, 62f),
                        Offset(24f + n * 15, 74f),
                        3.5f,
                        StrokeCap.Round,
                    )
                }
            if (code in 71..77 || code in 85..86)
                repeat(3) { n ->
                    val c = Offset(25f + n * 19, 68f)
                    drawLine(Color(0xFFB6E4FF), c - Offset(4f, 0f), c + Offset(4f, 0f), 2.5f)
                    drawLine(Color(0xFFB6E4FF), c - Offset(0f, 4f), c + Offset(0f, 4f), 2.5f)
                }
        }
    }
}
