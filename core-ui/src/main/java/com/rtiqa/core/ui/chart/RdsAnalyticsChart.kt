package com.rtiqa.core.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartPoint(
    val label: String,
    val value: Float
)

/**
 * Enterprise Jetpack Compose Analytics Chart Component (RdsAnalyticsChart).
 * Renders smooth cubic Bezier gradient progress charts with native RTL support.
 */
@Composable
fun RdsAnalyticsChart(
    title: String,
    subtitle: String,
    dataPoints: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gradientStartColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isNotEmpty()) {
                val maxValue = dataPoints.maxOf { it.value }.coerceAtLeast(1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height
                        val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

                        // Draw Grid Lines
                        val gridLineColor = lineColor.copy(alpha = 0.1f)
                        val steps = 3
                        for (i in 0..steps) {
                            val y = height * (i / steps.toFloat())
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Compute Points
                        val points = dataPoints.mapIndexed { index, point ->
                            val x = index * spacing
                            val normalizedY = (point.value / maxValue) * progress.value
                            val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()
                            Offset(x, y)
                        }

                        // Build Cubic Bezier Curve
                        val linePath = Path()
                        val fillPath = Path()

                        if (points.isNotEmpty()) {
                            linePath.moveTo(points.first().x, points.first().y)
                            fillPath.moveTo(points.first().x, height)
                            fillPath.lineTo(points.first().x, points.first().y)

                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                                linePath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p2.x, p2.y
                                )
                                fillPath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p2.x, p2.y
                                )
                            }

                            fillPath.lineTo(points.last().x, height)
                            fillPath.close()

                            // Draw Gradient Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(gradientStartColor, Color.Transparent)
                                )
                            )

                            // Draw Stroke Line
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw Points Dots
                            points.forEach { point ->
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = point
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Render Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.forEach { point ->
                        Text(
                            text = point.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
