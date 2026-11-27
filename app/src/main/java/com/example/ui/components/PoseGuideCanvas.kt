package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.PoseStyle
import com.example.ui.theme.SiluEmerald

/**
 * Full-screen live viewfinder dashed human silhouette overlay.
 * Outlines head oval + optical crosshair, raised hand, shoulder, torso, and waistline.
 */
@Composable
fun PoseOverlayCanvas(
    poseStyle: PoseStyle,
    isAligned: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val dashEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(7.dp.toPx(), 5.dp.toPx()),
            phase = 0f
        )
        val fineDashEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
            phase = 0f
        )

        val outerGlowColor = if (isAligned) SiluEmerald.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f)
        val primaryLineColor = Color.White.copy(alpha = if (isAligned) 0.92f else 0.72f)
        val guideLineColor = if (isAligned) SiluEmerald.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.65f)
        val nodeColor = if (isAligned) SiluEmerald else Color.White.copy(alpha = 0.85f)

        val glowStroke = Stroke(
            width = 7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = dashEffect
        )
        val contourStroke = Stroke(
            width = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = dashEffect
        )
        val skeletonStroke = Stroke(
            width = 1.25.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = fineDashEffect
        )

        when (poseStyle) {
            PoseStyle.CAFE_DAILY -> {
                // Calibrated to camera_cafe_portrait proportions:
                // Head center ~(0.525w, 0.355h), raised right hand ~(0.37w, 0.33h),
                // bent elbow ~(0.25w, 0.48h), left shoulder ~(0.67w, 0.48h), waist ~0.69h
                val contour = Path().apply {
                    // Start near bottom left hip/blazer hem
                    moveTo(w * 0.33f, h * 0.82f)
                    cubicTo(w * 0.35f, h * 0.70f, w * 0.36f, h * 0.60f, w * 0.37f, h * 0.52f)
                    // Raised right sleeve & bent elbow
                    cubicTo(w * 0.27f, h * 0.50f, w * 0.23f, h * 0.49f, w * 0.24f, h * 0.45f)
                    cubicTo(w * 0.26f, h * 0.40f, w * 0.31f, h * 0.35f, w * 0.35f, h * 0.31f)
                    // Raised hand near forehead
                    cubicTo(w * 0.38f, h * 0.28f, w * 0.43f, h * 0.28f, w * 0.46f, h * 0.30f)
                    // Crown of head & flowing hair back
                    cubicTo(w * 0.51f, h * 0.26f, w * 0.60f, h * 0.27f, w * 0.63f, h * 0.34f)
                    cubicTo(w * 0.66f, h * 0.40f, w * 0.66f, h * 0.44f, w * 0.69f, h * 0.47f)
                    // Left shoulder & sleeve outer contour
                    cubicTo(w * 0.72f, h * 0.52f, w * 0.71f, h * 0.68f, w * 0.66f, h * 0.85f)
                }

                // Soft emerald alignment halo + crisp white dashed outline
                drawPath(contour, color = outerGlowColor, style = glowStroke)
                drawPath(contour, color = primaryLineColor, style = contourStroke)

                // Head oval + Leica optical crosshair
                val headLeft = w * 0.44f
                val headTop = h * 0.285f
                val headW = w * 0.175f
                val headH = h * 0.142f
                drawOval(
                    color = outerGlowColor,
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headW, headH),
                    style = Stroke(width = 5.dp.toPx())
                )
                drawOval(
                    color = if (isAligned) SiluEmerald.copy(alpha = 0.92f) else primaryLineColor,
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headW, headH),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                // Crosshair inside head oval
                val cx = headLeft + headW * 0.48f
                val cy = headTop + headH * 0.48f
                drawLine(
                    color = Color.White.copy(alpha = 0.82f),
                    start = Offset(headLeft + 4.dp.toPx(), cy),
                    end = Offset(headLeft + headW - 4.dp.toPx(), cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.82f),
                    start = Offset(cx, headTop + 4.dp.toPx()),
                    end = Offset(cx, headTop + headH - 4.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )

                // Internal posture guide skeleton (raised arm + left arm + waistline)
                val rightArmGuide = Path().apply {
                    moveTo(w * 0.415f, h * 0.32f)
                    lineTo(w * 0.36f, h * 0.37f)
                    lineTo(w * 0.28f, h * 0.47f)
                    lineTo(w * 0.43f, h * 0.50f)
                }
                drawPath(rightArmGuide, color = guideLineColor, style = skeletonStroke)

                val leftArmGuide = Path().apply {
                    moveTo(w * 0.62f, h * 0.46f)
                    lineTo(w * 0.66f, h * 0.51f)
                    lineTo(w * 0.64f, h * 0.67f)
                    lineTo(w * 0.605f, h * 0.84f)
                }
                drawPath(leftArmGuide, color = guideLineColor, style = skeletonStroke)

                // Subtle waistline reference dashed line
                drawLine(
                    color = primaryLineColor.copy(alpha = 0.65f),
                    start = Offset(w * 0.37f, h * 0.695f),
                    end = Offset(w * 0.59f, h * 0.695f),
                    strokeWidth = 1.1.dp.toPx(),
                    pathEffect = fineDashEffect
                )

                // Key posture tracking nodes
                val joints = listOf(
                    Offset(w * 0.415f, h * 0.32f),
                    Offset(w * 0.28f, h * 0.47f),
                    Offset(w * 0.66f, h * 0.51f),
                    Offset(w * 0.64f, h * 0.67f),
                    Offset(w * 0.605f, h * 0.84f)
                )
                joints.forEach { pt ->
                    drawCircle(color = outerGlowColor, radius = 5.5.dp.toPx(), center = pt)
                    drawCircle(color = nodeColor, radius = 2.6.dp.toPx(), center = pt)
                }
            }

            PoseStyle.STREET_SIDE -> {
                val contour = Path().apply {
                    moveTo(w * 0.33f, h * 0.90f)
                    cubicTo(w * 0.30f, h * 0.72f, w * 0.30f, h * 0.56f, w * 0.35f, h * 0.48f)
                    cubicTo(w * 0.39f, h * 0.43f, w * 0.41f, h * 0.38f, w * 0.42f, h * 0.32f)
                    cubicTo(w * 0.44f, h * 0.23f, w * 0.58f, h * 0.23f, w * 0.62f, h * 0.32f)
                    cubicTo(w * 0.64f, h * 0.40f, w * 0.63f, h * 0.47f, w * 0.67f, h * 0.54f)
                    cubicTo(w * 0.72f, h * 0.66f, w * 0.74f, h * 0.79f, w * 0.75f, h * 0.90f)
                }
                drawPath(contour, color = outerGlowColor, style = glowStroke)
                drawPath(contour, color = primaryLineColor, style = contourStroke)
                drawHeadTarget(w * 0.43f, h * 0.245f, w * 0.19f, h * 0.155f, isAligned, outerGlowColor, primaryLineColor)
                // Shoulder & waist alignment lines
                drawLine(guideLineColor, Offset(w * 0.35f, h * 0.51f), Offset(w * 0.64f, h * 0.53f), 1.2.dp.toPx(), pathEffect = fineDashEffect)
                drawLine(primaryLineColor.copy(alpha = 0.6f), Offset(w * 0.33f, h * 0.74f), Offset(w * 0.70f, h * 0.74f), 1.1.dp.toPx(), pathEffect = fineDashEffect)
            }

            PoseStyle.STREET_WALK -> {
                val contour = Path().apply {
                    // Full-stride coat & walking silhouette
                    moveTo(w * 0.36f, h * 0.88f)
                    lineTo(w * 0.41f, h * 0.69f)
                    cubicTo(w * 0.36f, h * 0.57f, w * 0.38f, h * 0.44f, w * 0.43f, h * 0.38f)
                    cubicTo(w * 0.44f, h * 0.28f, w * 0.55f, h * 0.28f, w * 0.57f, h * 0.38f)
                    cubicTo(w * 0.62f, h * 0.45f, w * 0.65f, h * 0.58f, w * 0.61f, h * 0.71f)
                    lineTo(w * 0.66f, h * 0.88f)
                }
                drawPath(contour, color = outerGlowColor, style = glowStroke)
                drawPath(contour, color = primaryLineColor, style = contourStroke)
                drawHeadTarget(w * 0.445f, h * 0.285f, w * 0.125f, h * 0.105f, isAligned, outerGlowColor, primaryLineColor)
                // Stride axis line
                drawLine(guideLineColor, Offset(w * 0.50f, h * 0.41f), Offset(w * 0.49f, h * 0.70f), 1.2.dp.toPx(), pathEffect = fineDashEffect)
                drawLine(guideLineColor, Offset(w * 0.49f, h * 0.70f), Offset(w * 0.40f, h * 0.88f), 1.2.dp.toPx(), pathEffect = fineDashEffect)
                drawLine(guideLineColor, Offset(w * 0.49f, h * 0.70f), Offset(w * 0.62f, h * 0.88f), 1.2.dp.toPx(), pathEffect = fineDashEffect)
            }

            PoseStyle.SEASIDE_BACK -> {
                // Horizon reference rule at golden third
                drawLine(
                    color = guideLineColor.copy(alpha = 0.5f),
                    start = Offset(w * 0.08f, h * 0.44f),
                    end = Offset(w * 0.92f, h * 0.44f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = fineDashEffect
                )
                val contour = Path().apply {
                    moveTo(w * 0.37f, h * 0.90f)
                    cubicTo(w * 0.38f, h * 0.72f, w * 0.39f, h * 0.58f, w * 0.37f, h * 0.49f)
                    cubicTo(w * 0.38f, h * 0.43f, w * 0.42f, h * 0.41f, w * 0.43f, h * 0.36f)
                    cubicTo(w * 0.44f, h * 0.27f, w * 0.56f, h * 0.27f, w * 0.57f, h * 0.36f)
                    cubicTo(w * 0.58f, h * 0.41f, w * 0.62f, h * 0.43f, w * 0.63f, h * 0.49f)
                    cubicTo(w * 0.61f, h * 0.58f, w * 0.62f, h * 0.72f, w * 0.63f, h * 0.90f)
                }
                drawPath(contour, color = outerGlowColor, style = glowStroke)
                drawPath(contour, color = primaryLineColor, style = contourStroke)
                drawHeadTarget(w * 0.44f, h * 0.28f, w * 0.125f, h * 0.11f, isAligned, outerGlowColor, primaryLineColor)
                drawLine(primaryLineColor.copy(alpha = 0.6f), Offset(w * 0.39f, h * 0.64f), Offset(w * 0.61f, h * 0.64f), 1.1.dp.toPx(), pathEffect = fineDashEffect)
            }

            PoseStyle.TALL_FULL -> {
                // Elongating low-angle full-body guides + golden 0.382 waistline
                val contour = Path().apply {
                    moveTo(w * 0.41f, h * 0.92f)
                    lineTo(w * 0.43f, h * 0.58f)
                    cubicTo(w * 0.39f, h * 0.49f, w * 0.39f, h * 0.40f, w * 0.43f, h * 0.35f)
                    cubicTo(w * 0.45f, h * 0.24f, w * 0.55f, h * 0.24f, w * 0.57f, h * 0.35f)
                    cubicTo(w * 0.61f, h * 0.40f, w * 0.61f, h * 0.49f, w * 0.57f, h * 0.58f)
                    lineTo(w * 0.59f, h * 0.92f)
                }
                drawPath(contour, color = outerGlowColor, style = glowStroke)
                drawPath(contour, color = primaryLineColor, style = contourStroke)
                drawHeadTarget(w * 0.45f, h * 0.255f, w * 0.105f, h * 0.092f, isAligned, outerGlowColor, primaryLineColor)
                // Waistline marker & bottom foot anchor
                drawLine(guideLineColor, Offset(w * 0.34f, h * 0.53f), Offset(w * 0.66f, h * 0.53f), 1.2.dp.toPx(), pathEffect = fineDashEffect)
                drawLine(primaryLineColor.copy(alpha = 0.55f), Offset(w * 0.36f, h * 0.92f), Offset(w * 0.64f, h * 0.92f), 1.2.dp.toPx())
            }
        }
    }
}

private fun DrawScope.drawHeadTarget(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    isAligned: Boolean,
    glowColor: Color,
    primaryColor: Color
) {
    drawOval(
        color = glowColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(width = 5.dp.toPx())
    )
    drawOval(
        color = if (isAligned) SiluEmerald.copy(alpha = 0.92f) else primaryColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(width = 1.5.dp.toPx())
    )
    val cx = left + width * 0.5f
    val cy = top + height * 0.5f
    drawLine(Color.White.copy(alpha = 0.78f), Offset(left + 4.dp.toPx(), cy), Offset(left + width - 4.dp.toPx(), cy), 1.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.78f), Offset(cx, top + 4.dp.toPx()), Offset(cx, top + height - 4.dp.toPx()), 1.dp.toPx())
}

/**
 * Subtle Leica-style left and right tilt/horizon scale rulers on the camera viewfinder.
 */
@Composable
fun TiltRulerOverlay(
    tiltDegrees: Float,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerY = h * 0.49f
        val rulerHeight = h * 0.34f
        val topY = centerY - rulerHeight / 2f
        val tickCount = 28
        val step = rulerHeight / tickCount
        val accent = if (isAligned) SiluEmerald else Color.White.copy(alpha = 0.85f)

        // Left & Right graduated fine ticks
        for (i in 0..tickCount) {
            val y = topY + i * step
            val isCenter = i == tickCount / 2
            val isMajor = i % 7 == 0
            val tickLen = when {
                isCenter -> 12.dp.toPx()
                isMajor -> 8.dp.toPx()
                else -> 4.5.dp.toPx()
            }
            val tickColor = if (isCenter) accent else Color.White.copy(alpha = if (isMajor) 0.50f else 0.28f)
            val strokeW = if (isCenter) 1.3.dp.toPx() else 0.8.dp.toPx()

            // Left edge ruler
            drawLine(
                color = tickColor,
                start = Offset(14.dp.toPx(), y),
                end = Offset(14.dp.toPx() + tickLen, y),
                strokeWidth = strokeW
            )
            // Right edge ruler
            drawLine(
                color = tickColor,
                start = Offset(w - 14.dp.toPx() - tickLen, y),
                end = Offset(w - 14.dp.toPx(), y),
                strokeWidth = strokeW
            )
        }

        // Dynamic tilt pointer triangles moving subtly with tiltDegrees
        val offsetPx = (tiltDegrees.coerceIn(-6f, 6f) / 6f) * (rulerHeight * 0.32f)
        val leftPointerY = centerY + offsetPx
        val rightPointerY = centerY - offsetPx

        val leftTri = Path().apply {
            moveTo(10.dp.toPx(), leftPointerY)
            lineTo(5.dp.toPx(), leftPointerY - 3.5.dp.toPx())
            lineTo(5.dp.toPx(), leftPointerY + 3.5.dp.toPx())
            close()
        }
        drawPath(leftTri, color = accent)

        val rightTri = Path().apply {
            moveTo(w - 10.dp.toPx(), rightPointerY)
            lineTo(w - 5.dp.toPx(), rightPointerY - 3.5.dp.toPx())
            lineTo(w - 5.dp.toPx(), rightPointerY + 3.5.dp.toPx())
            close()
        }
        drawPath(rightTri, color = accent)
    }
}

/**
 * Compact minimalist line-art pose silhouette icon for camera pose cards
 * and template card top-right dashed badges.
 */
@Composable
fun PoseSilhouetteIcon(
    poseStyle: PoseStyle,
    dashed: Boolean = false,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.35.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(2.8.dp.toPx(), 2.2.dp.toPx())) else null
        )

        when (poseStyle) {
            PoseStyle.CAFE_DAILY -> {
                // Head circle
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.54f, h * 0.26f), style = stroke)
                // Raised arm + torso silhouette
                val body = Path().apply {
                    moveTo(w * 0.32f, h * 0.86f)
                    lineTo(w * 0.36f, h * 0.50f)
                    lineTo(w * 0.24f, h * 0.44f)
                    lineTo(w * 0.38f, h * 0.27f)
                    moveTo(w * 0.42f, h * 0.44f)
                    cubicTo(w * 0.56f, h * 0.40f, w * 0.68f, h * 0.46f, w * 0.72f, h * 0.58f)
                    lineTo(w * 0.68f, h * 0.86f)
                }
                drawPath(body, color = tint, style = stroke)
            }

            PoseStyle.STREET_SIDE -> {
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.52f, h * 0.24f), style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.30f, h * 0.88f)
                    cubicTo(w * 0.30f, h * 0.56f, w * 0.36f, h * 0.44f, w * 0.48f, h * 0.43f)
                    cubicTo(w * 0.64f, h * 0.44f, w * 0.70f, h * 0.58f, w * 0.72f, h * 0.88f)
                }
                drawPath(body, color = tint, style = stroke)
            }

            PoseStyle.STREET_WALK -> {
                drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.20f), style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.50f, h * 0.34f)
                    lineTo(w * 0.48f, h * 0.60f)
                    lineTo(w * 0.34f, h * 0.88f)
                    moveTo(w * 0.48f, h * 0.60f)
                    lineTo(w * 0.66f, h * 0.88f)
                    moveTo(w * 0.49f, h * 0.42f)
                    lineTo(w * 0.34f, h * 0.54f)
                    moveTo(w * 0.49f, h * 0.42f)
                    lineTo(w * 0.65f, h * 0.56f)
                }
                drawPath(body, color = tint, style = stroke)
            }

            PoseStyle.SEASIDE_BACK -> {
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.25f), style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.34f, h * 0.88f)
                    cubicTo(w * 0.35f, h * 0.60f, w * 0.35f, h * 0.46f, w * 0.44f, h * 0.43f)
                    lineTo(w * 0.56f, h * 0.43f)
                    cubicTo(w * 0.65f, h * 0.46f, w * 0.65f, h * 0.60f, w * 0.66f, h * 0.88f)
                }
                drawPath(body, color = tint, style = stroke)
            }

            PoseStyle.TALL_FULL -> {
                drawCircle(tint, radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.18f), style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.38f, h * 0.90f)
                    lineTo(w * 0.41f, h * 0.40f)
                    lineTo(w * 0.59f, h * 0.40f)
                    lineTo(w * 0.62f, h * 0.90f)
                    moveTo(w * 0.34f, h * 0.56f)
                    lineTo(w * 0.66f, h * 0.56f)
                }
                drawPath(body, color = tint, style = stroke)
            }
        }
    }
}
