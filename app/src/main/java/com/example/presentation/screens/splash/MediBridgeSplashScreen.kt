package com.example.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * MediBridge+ Official 5-Second Animated Splash Screen
 *
 * Sequence:
 * 0.0 - 0.5s: Clean dark premium background with ambient depth.
 * 0.5 - 2.0s: Bridge builds smoothly from both sides toward the center.
 * 2.0 - 3.0s: Medical cross with glowing ECG heartbeat pulse revealed above/integrated with bridge.
 * 3.0 - 4.0s: "MediBridge+" brand text reveals with smooth fade + upward translation.
 * 4.0 - 5.0s: Complete unified logo display with subtle heartbeat pulse & final smooth fade to app.
 */
@Composable
fun MediBridgeSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Master animation clock from 0ms to 5000ms
    val animTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animTime.animateTo(
            targetValue = 5000f,
            animationSpec = tween(
                durationMillis = 5000,
                easing = LinearEasing
            )
        )
        // Ensure a tiny delay for final frame then complete
        delay(50)
        onSplashFinished()
    }

    val currentMillis = animTime.value

    // Stage 1: Ambient background & glow (0 - 500ms)
    val backgroundGlowAlpha = ((currentMillis / 1000f).coerceIn(0f, 1f))

    // Stage 2: Bridge building from outer edges to center (500ms - 2000ms)
    val bridgeProgress = ((currentMillis - 500f) / 1500f).coerceIn(0f, 1f)
    val bridgeEasingProgress = remember(bridgeProgress) {
        FastOutSlowInEasing.transform(bridgeProgress)
    }

    // Stage 3: Medical Cross + Pulse + Caring Hands (2000ms - 3000ms)
    val medicalCrossProgress = ((currentMillis - 2000f) / 1000f).coerceIn(0f, 1f)
    val medicalCrossAlpha = remember(medicalCrossProgress) {
        FastOutSlowInEasing.transform(medicalCrossProgress)
    }
    val ecgPulseProgress = ((currentMillis - 2200f) / 800f).coerceIn(0f, 1f)

    // Stage 4: MediBridge+ text reveal (3000ms - 4000ms)
    val textRevealProgress = ((currentMillis - 3000f) / 1000f).coerceIn(0f, 1f)
    val textAlpha = remember(textRevealProgress) {
        FastOutSlowInEasing.transform(textRevealProgress)
    }
    val textOffsetY = (1.0f - textAlpha) * 20f

    // Stage 5: Final subtle pulse & fade-out preparation (4000ms - 5000ms)
    val finalPhaseProgress = ((currentMillis - 4000f) / 1000f).coerceIn(0f, 1f)
    val pulseScale = if (currentMillis >= 4000f) {
        1.0f + 0.025f * sin(finalPhaseProgress * Math.PI.toFloat())
    } else {
        1.0f
    }

    val splashExitAlpha = if (currentMillis >= 4750f) {
        (1.0f - ((currentMillis - 4750f) / 250f)).coerceIn(0f, 1f)
    } else {
        1.0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F2B48).copy(alpha = backgroundGlowAlpha),
                        Color(0xFF071424),
                        Color(0xFF030A14)
                    ),
                    center = Offset.Unspecified,
                    radius = 1200f
                )
            )
            .graphicsLayer {
                alpha = splashExitAlpha
                scaleX = pulseScale
                scaleY = pulseScale
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Canvas for Medical Cross + Suspension Bridge + Caring Hands
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val centerY = height / 2f

                    // 1. DRAW BRIDGE (0.5s - 2.0s)
                    if (bridgeEasingProgress > 0f) {
                        drawAnimatedBridge(
                            centerX = centerX,
                            centerY = centerY,
                            progress = bridgeEasingProgress
                        )
                    }

                    // 2. DRAW MEDICAL CROSS WITH ECG PULSE (2.0s - 3.0s)
                    if (medicalCrossAlpha > 0f) {
                        drawAnimatedHealthcareSymbols(
                            centerX = centerX,
                            centerY = centerY,
                            alpha = medicalCrossAlpha,
                            ecgProgress = ecgPulseProgress
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. BRANDING TEXT: MediBridge+ (3.0s - 4.0s)
            if (textAlpha > 0f) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = textOffsetY
                    }
                ) {
                    val brandedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFE0F7FA), // Crisp Ice-Blue for "Medi" on dark canvas
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                letterSpacing = 0.5.sp
                            )
                        ) {
                            append("Medi")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF00E5FF), // Vibrant Healthcare Teal for "Bridge"
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp,
                                letterSpacing = 0.5.sp
                            )
                        ) {
                            append("Bridge")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF00E5FF), // Teal "+"
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                baselineShift = BaselineShift(0.35f)
                            )
                        ) {
                            append("+")
                        }
                    }

                    Text(
                        text = brandedText,
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

/**
 * Draws the suspension bridge building symmetrically from both outer sides toward the center.
 */
private fun DrawScope.drawAnimatedBridge(
    centerX: Float,
    centerY: Float,
    progress: Float
) {
    val bridgeHalfWidth = size.width * 0.44f
    val deckY = centerY + 18f

    val currentSpan = bridgeHalfWidth * progress

    val deckLeft = centerX - currentSpan
    val deckRight = centerX + currentSpan

    val tealColor = Color(0xFF00E5FF)
    val navyBlue = Color(0xFF0A3981)
    val deepCyan = Color(0xFF00B4D8)

    // Bridge Deck Arch (Builds from outer edges inward)
    if (progress > 0.05f) {
        val deckPath = Path().apply {
            moveTo(centerX - currentSpan, deckY + 4f * (1f - (currentSpan / bridgeHalfWidth)))
            quadraticTo(
                centerX,
                deckY - 14f * progress,
                centerX + currentSpan,
                deckY + 4f * (1f - (currentSpan / bridgeHalfWidth))
            )
        }
        drawPath(
            path = deckPath,
            color = tealColor,
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round
            )
        )
    }

    // Suspension Towers
    val leftTowerX = centerX - bridgeHalfWidth * 0.52f
    val rightTowerX = centerX + bridgeHalfWidth * 0.52f
    val towerTopY = centerY - 62f
    val towerBaseY = deckY + 22f

    val towerProgress = ((progress - 0.15f) / 0.7f).coerceIn(0f, 1f)
    if (towerProgress > 0f) {
        val curTowerTop = towerBaseY - (towerBaseY - towerTopY) * towerProgress

        // Left Tower
        drawLine(
            brush = Brush.verticalGradient(listOf(tealColor, navyBlue)),
            start = Offset(leftTowerX, towerBaseY),
            end = Offset(leftTowerX, curTowerTop),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )
        // Right Tower
        drawLine(
            brush = Brush.verticalGradient(listOf(tealColor, navyBlue)),
            start = Offset(rightTowerX, towerBaseY),
            end = Offset(rightTowerX, curTowerTop),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )

        // Tower Cross Beams
        if (towerProgress > 0.6f) {
            val beamY1 = centerY - 20f
            val beamY2 = centerY + 4f
            drawLine(tealColor.copy(alpha = 0.8f), Offset(leftTowerX - 6f, beamY1), Offset(leftTowerX + 6f, beamY1), strokeWidth = 2f)
            drawLine(tealColor.copy(alpha = 0.8f), Offset(rightTowerX - 6f, beamY1), Offset(rightTowerX + 6f, beamY1), strokeWidth = 2f)
            drawLine(tealColor.copy(alpha = 0.8f), Offset(leftTowerX - 7f, beamY2), Offset(leftTowerX + 7f, beamY2), strokeWidth = 2f)
            drawLine(tealColor.copy(alpha = 0.8f), Offset(rightTowerX - 7f, beamY2), Offset(rightTowerX + 7f, beamY2), strokeWidth = 2f)
        }
    }

    // Main Suspension Cables (Left & Right swooping arches)
    val cableProgress = ((progress - 0.3f) / 0.7f).coerceIn(0f, 1f)
    if (cableProgress > 0f) {
        val cableAlpha = cableProgress

        // Left Suspension Cable: outer anchor -> tower top -> center connection
        val leftCablePath = Path().apply {
            moveTo(centerX - bridgeHalfWidth, deckY - 4f)
            quadraticTo(
                leftTowerX - 20f,
                towerTopY + 10f,
                leftTowerX,
                towerTopY
            )
            val centerConnectX = leftTowerX + (centerX - leftTowerX) * cableProgress
            val centerConnectY = towerTopY + (deckY - towerTopY - 10f) * cableProgress
            quadraticTo(
                leftTowerX + 24f,
                towerTopY + 36f,
                centerConnectX,
                centerConnectY
            )
        }
        drawPath(
            path = leftCablePath,
            color = deepCyan.copy(alpha = cableAlpha),
            style = Stroke(width = 2.4f, cap = StrokeCap.Round)
        )

        // Right Suspension Cable
        val rightCablePath = Path().apply {
            moveTo(centerX + bridgeHalfWidth, deckY - 4f)
            quadraticTo(
                rightTowerX + 20f,
                towerTopY + 10f,
                rightTowerX,
                towerTopY
            )
            val centerConnectX = rightTowerX - (rightTowerX - centerX) * cableProgress
            val centerConnectY = towerTopY + (deckY - towerTopY - 10f) * cableProgress
            quadraticTo(
                rightTowerX - 24f,
                towerTopY + 36f,
                centerConnectX,
                centerConnectY
            )
        }
        drawPath(
            path = rightCablePath,
            color = deepCyan.copy(alpha = cableAlpha),
            style = Stroke(width = 2.4f, cap = StrokeCap.Round)
        )

        // Vertical Suspenders
        if (progress > 0.6f) {
            val suspenderAlpha = ((progress - 0.6f) / 0.4f).coerceIn(0f, 0.75f)
            val suspenderCount = 10
            for (i in 1..suspenderCount) {
                val frac = i / (suspenderCount + 1f)
                val x = (centerX - bridgeHalfWidth) + (bridgeHalfWidth * 2f) * frac

                if (x >= deckLeft && x <= deckRight) {
                    val archY = deckY - 8f * (1f - ((x - centerX) / bridgeHalfWidth) * ((x - centerX) / bridgeHalfWidth))
                    val topY = if (x < centerX) {
                        towerTopY + 15f + 25f * ((x - leftTowerX) / 30f) * ((x - leftTowerX) / 30f)
                    } else {
                        towerTopY + 15f + 25f * ((x - rightTowerX) / 30f) * ((x - rightTowerX) / 30f)
                    }.coerceAtMost(archY - 4f)

                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = suspenderAlpha),
                        start = Offset(x, topY),
                        end = Offset(x, archY),
                        strokeWidth = 1.0f
                    )
                }
            }
        }
    }
}

/**
 * Draws the Medical Cross with ECG heartbeat pulse wave and the supportive caring hands.
 */
private fun DrawScope.drawAnimatedHealthcareSymbols(
    centerX: Float,
    centerY: Float,
    alpha: Float,
    ecgProgress: Float
) {
    val crossCenterY = centerY - 52f
    val crossSize = 34f
    val armWidth = 12f
    val halfArm = armWidth / 2f
    val halfCross = crossSize / 2f
    val cornerRadius = 4f

    // 1. MEDICAL CROSS WITH ROUNDED CORNERS
    val crossPath = Path().apply {
        // Top arm
        moveTo(centerX - halfArm + cornerRadius, crossCenterY - halfCross)
        lineTo(centerX + halfArm - cornerRadius, crossCenterY - halfCross)
        quadraticTo(centerX + halfArm, crossCenterY - halfCross, centerX + halfArm, crossCenterY - halfCross + cornerRadius)
        lineTo(centerX + halfArm, crossCenterY - halfArm)

        // Right arm
        lineTo(centerX + halfCross - cornerRadius, crossCenterY - halfArm)
        quadraticTo(centerX + halfCross, crossCenterY - halfArm, centerX + halfCross, crossCenterY - halfArm + cornerRadius)
        lineTo(centerX + halfCross, crossCenterY + halfArm - cornerRadius)
        quadraticTo(centerX + halfCross, crossCenterY + halfArm, centerX + halfCross - cornerRadius, crossCenterY + halfArm)
        lineTo(centerX + halfArm, crossCenterY + halfArm)

        // Bottom arm
        lineTo(centerX + halfArm, crossCenterY + halfCross - cornerRadius)
        quadraticTo(centerX + halfArm, crossCenterY + halfCross, centerX + halfArm - cornerRadius, crossCenterY + halfCross)
        lineTo(centerX - halfArm + cornerRadius, crossCenterY + halfCross)
        quadraticTo(centerX - halfArm, crossCenterY + halfCross, centerX - halfArm, crossCenterY + halfCross - cornerRadius)
        lineTo(centerX - halfArm, crossCenterY + halfArm)

        // Left arm
        lineTo(centerX - halfCross + cornerRadius, crossCenterY + halfArm)
        quadraticTo(centerX - halfCross, crossCenterY + halfArm, centerX - halfCross, crossCenterY + halfArm - cornerRadius)
        lineTo(centerX - halfCross, crossCenterY - halfArm + cornerRadius)
        quadraticTo(centerX - halfCross, crossCenterY - halfArm, centerX - halfCross + cornerRadius, crossCenterY - halfArm)
        lineTo(centerX - halfArm, crossCenterY - halfArm)
        lineTo(centerX - halfArm, crossCenterY - halfCross + cornerRadius)
        quadraticTo(centerX - halfArm, crossCenterY - halfCross, centerX - halfArm + cornerRadius, crossCenterY - halfCross)
        close()
    }

    // Gradient fill for cross
    drawPath(
        path = crossPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF00E5FF).copy(alpha = alpha),
                Color(0xFF00897B).copy(alpha = alpha)
            ),
            start = Offset(centerX - halfCross, crossCenterY - halfCross),
            end = Offset(centerX + halfCross, crossCenterY + halfCross)
        )
    )

    // Glowing outline for cross
    drawPath(
        path = crossPath,
        color = Color(0xFFE0F7FA).copy(alpha = alpha * 0.8f),
        style = Stroke(width = 1.5f)
    )

    // 2. ECG HEARTBEAT LINE
    if (ecgProgress > 0f) {
        val ecgStartX = centerX - halfCross - 6f
        val ecgEndX = centerX + halfCross + 6f
        val totalEcgWidth = ecgEndX - ecgStartX

        val currentEcgX = ecgStartX + totalEcgWidth * ecgProgress

        val ecgPoints = listOf(
            Offset(ecgStartX, crossCenterY),
            Offset(centerX - 14f, crossCenterY),
            Offset(centerX - 8f, crossCenterY + 4f),
            Offset(centerX - 4f, crossCenterY - 14f), // Q-R spike top
            Offset(centerX + 2f, crossCenterY + 12f),  // S dip
            Offset(centerX + 6f, crossCenterY - 4f),   // T wave
            Offset(centerX + 12f, crossCenterY),
            Offset(ecgEndX, crossCenterY)
        )

        val ecgPath = Path().apply {
            moveTo(ecgPoints[0].x, ecgPoints[0].y)
            for (pt in ecgPoints) {
                if (pt.x <= currentEcgX) {
                    lineTo(pt.x, pt.y)
                } else {
                    // interpolate current active point
                    lineTo(currentEcgX, crossCenterY)
                    break
                }
            }
        }

        drawPath(
            path = ecgPath,
            color = Color.White.copy(alpha = alpha),
            style = Stroke(
                width = 2.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Glowing active pulse dot at leading edge of ECG
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = alpha),
            radius = 3.5f,
            center = Offset(currentEcgX, crossCenterY)
        )
    }

    // 3. CARING HANDS EMBRACE AT BASE OF BRIDGE (2.0s - 3.0s)
    val handsY = centerY + 42f
    val handsPath = Path().apply {
        // Left hand curve sweeping to center
        moveTo(centerX - 72f, handsY - 16f)
        cubicTo(
            centerX - 60f, handsY + 18f,
            centerX - 24f, handsY + 34f,
            centerX, handsY + 34f
        )
        cubicTo(
            centerX + 24f, handsY + 34f,
            centerX + 60f, handsY + 18f,
            centerX + 72f, handsY - 16f
        )
        cubicTo(
            centerX + 50f, handsY + 24f,
            centerX + 20f, handsY + 28f,
            centerX, handsY + 28f
        )
        cubicTo(
            centerX - 20f, handsY + 28f,
            centerX - 50f, handsY + 24f,
            centerX - 72f, handsY - 16f
        )
        close()
    }

    drawPath(
        path = handsPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00B4D8).copy(alpha = alpha),
                Color(0xFF0A3981).copy(alpha = alpha),
                Color(0xFF00B4D8).copy(alpha = alpha)
            )
        )
    )

    // Central Heart with Plus symbol inside caring hands
    val heartCenterY = handsY + 12f
    val heartSize = 14f
    val heartPath = Path().apply {
        moveTo(centerX, heartCenterY + heartSize * 0.7f)
        cubicTo(
            centerX - heartSize, heartCenterY + heartSize * 0.2f,
            centerX - heartSize, heartCenterY - heartSize * 0.5f,
            centerX - heartSize * 0.3f, heartCenterY - heartSize * 0.5f
        )
        cubicTo(
            centerX, heartCenterY - heartSize * 0.5f,
            centerX, heartCenterY - heartSize * 0.2f,
            centerX, heartCenterY - heartSize * 0.2f
        )
        cubicTo(
            centerX, heartCenterY - heartSize * 0.2f,
            centerX, heartCenterY - heartSize * 0.5f,
            centerX + heartSize * 0.3f, heartCenterY - heartSize * 0.5f
        )
        cubicTo(
            centerX + heartSize, heartCenterY - heartSize * 0.5f,
            centerX + heartSize, heartCenterY + heartSize * 0.2f,
            centerX, heartCenterY + heartSize * 0.7f
        )
        close()
    }

    drawPath(
        path = heartPath,
        brush = Brush.verticalGradient(
            listOf(
                Color(0xFF00E5FF).copy(alpha = alpha),
                Color(0xFF00897B).copy(alpha = alpha)
            )
        )
    )

    // Small White Plus in center of Heart
    val plusSize = 3.5f
    drawLine(
        color = Color.White.copy(alpha = alpha),
        start = Offset(centerX - plusSize, heartCenterY - 0.5f),
        end = Offset(centerX + plusSize, heartCenterY - 0.5f),
        strokeWidth = 1.4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = alpha),
        start = Offset(centerX, heartCenterY - plusSize - 0.5f),
        end = Offset(centerX, heartCenterY + plusSize - 0.5f),
        strokeWidth = 1.4f,
        cap = StrokeCap.Round
    )
}
