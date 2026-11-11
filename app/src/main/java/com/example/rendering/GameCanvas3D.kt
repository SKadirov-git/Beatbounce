package com.example.rendering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.*
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun GameCanvas3D(
    ballX: Float,
    ballY: Float,
    ballZ: Float,
    ballRotation: Float,
    selectedSkin: BallSkin,
    selectedEnv: EnvironmentType,
    platforms: List<Platform3D>,
    particles: List<Particle3D>,
    trailPoints: List<TrailPoint3D>,
    bassEnergy: Float,
    recentRating: LandingRating?,
    ratingAnim: Float, // 0 to 1
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height * 0.58f
        val fov = width * 0.95f

        // Camera setup
        val cameraX = ballX * 0.40f
        val cameraY = 2.4f
        val cameraZ = ballZ - 4.2f

        fun project(wx: Float, wy: Float, wz: Float): Offset? {
            val rx = wx - cameraX
            val ry = wy - cameraY
            val rz = wz - cameraZ
            if (rz <= 0.35f) return null
            val scale = fov / rz
            val sx = centerX + rx * scale
            val sy = centerY - ry * scale
            return Offset(sx, sy)
        }

        fun getScale(wz: Float): Float {
            val rz = wz - cameraZ
            return if (rz > 0.35f) fov / rz else 0f
        }

        // 1. Draw Environment Sky & Parallax Cyber Horizon
        drawEnvironmentBackground(
            env = selectedEnv,
            width = width,
            height = height,
            centerY = centerY,
            bassEnergy = bassEnergy,
            ballZ = ballZ
        )

        // 2. Draw 3D Platforms (Back-to-front sorting)
        val visiblePlatforms = platforms.filter {
            it.z >= cameraZ - 1.0f && it.z <= cameraZ + 36.0f
        }.sortedByDescending { it.z }

        for (plat in visiblePlatforms) {
            draw3DPlatform(
                plat = plat,
                project = ::project,
                bassEnergy = bassEnergy
            )
        }

        // 3. Draw Ball Shadow onto platform/floor
        val shadowY = 0f
        val shadowCenter = project(ballX, shadowY, ballZ)
        if (shadowCenter != null && ballY >= 0f) {
            val shadowScale = (fov / (ballZ - cameraZ)).coerceAtLeast(0f)
            val shadowRadiusX = 28f * (shadowScale / 120f) * (1f - (ballY / 4f).coerceIn(0f, 0.6f))
            val shadowRadiusY = 12f * (shadowScale / 120f)
            val shadowAlpha = (0.6f - (ballY / 5f)).coerceIn(0.1f, 0.7f)
            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset(shadowCenter.x - shadowRadiusX, shadowCenter.y - shadowRadiusY),
                size = Size(shadowRadiusX * 2, shadowRadiusY * 2)
            )
        }

        // 4. Draw Ball Particle Trail
        for (i in 0 until trailPoints.size - 1) {
            val p1 = trailPoints[i]
            val p2 = trailPoints[i + 1]
            val s1 = project(p1.x, p1.y, p1.z)
            val s2 = project(p2.x, p2.y, p2.z)
            if (s1 != null && s2 != null) {
                val trailAlpha = (p1.alpha * 0.75f).coerceIn(0f, 1f)
                val scale = getScale(p1.z)
                val strokeW = (14f * (scale / 120f) * p1.alpha).coerceIn(2f, 22f)
                drawLine(
                    color = selectedSkin.trailColor.copy(alpha = trailAlpha),
                    start = s1,
                    end = s2,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }

        // 5. Draw 3D Glowing Ball
        val ballScreen = project(ballX, ballY, ballZ)
        if (ballScreen != null) {
            val ballScale = getScale(ballZ)
            val baseRadius = (32f * (ballScale / 120f)).coerceIn(12f, 75f)

            // Dynamic bounce squish & stretch
            val isNearGround = ballY < 0.35f
            val squishX = if (isNearGround) 1.25f else 0.95f
            val squishY = if (isNearGround) 0.80f else 1.05f
            val radiusX = baseRadius * squishX
            val radiusY = baseRadius * squishY

            // Outer pulse glow
            val glowRadius = radiusX * (1.6f + bassEnergy * 0.4f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        selectedSkin.outerGlow.copy(alpha = 0.65f),
                        selectedSkin.outerGlow.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = ballScreen,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = ballScreen
            )

            // Solid 3D Sphere with Specular Highlight
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PureWhite,
                        selectedSkin.coreColor,
                        selectedSkin.outerGlow,
                        selectedSkin.outerGlow.copy(alpha = 0.85f)
                    ),
                    center = Offset(ballScreen.x - radiusX * 0.3f, ballScreen.y - radiusY * 0.35f),
                    radius = radiusX * 1.1f
                ),
                topLeft = Offset(ballScreen.x - radiusX, ballScreen.y - radiusY),
                size = Size(radiusX * 2, radiusY * 2)
            )

            // Inner cyber orbital rings
            val ringRadius = radiusX * 0.7f
            drawOval(
                color = PureWhite.copy(alpha = 0.5f),
                topLeft = Offset(ballScreen.x - ringRadius, ballScreen.y - ringRadius * 0.35f),
                size = Size(ringRadius * 2, ringRadius * 0.7f),
                style = Stroke(width = 2.5f)
            )
        }

        // 6. Draw 3D Particles
        for (p in particles) {
            val sp = project(p.x, p.y, p.z)
            if (sp != null) {
                val pScale = getScale(p.z)
                val pSize = (p.size * (pScale / 120f) * p.life).coerceIn(1.5f, 18f)
                val pAlpha = (p.life).coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = pAlpha),
                    radius = pSize,
                    center = sp
                )
            }
        }

        // 7. Floating Rating Feedback ("PERFECT!", "EXCELLENT!", "GOOD!")
        if (recentRating != null && ratingAnim > 0.05f) {
            val textPos = project(ballX, ballY + 1.2f + (1f - ratingAnim) * 0.6f, ballZ)
            if (textPos != null) {
                val scale = (1.2f - (1f - ratingAnim) * 0.2f).coerceIn(0.8f, 1.4f)
                val alpha = (ratingAnim).coerceIn(0f, 1f)

                // Background glow pill
                val pillWidth = 160f * scale
                val pillHeight = 44f * scale
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            recentRating.color.copy(alpha = 0.15f * alpha),
                            recentRating.color.copy(alpha = 0.85f * alpha),
                            recentRating.color.copy(alpha = 0.15f * alpha)
                        )
                    ),
                    topLeft = Offset(textPos.x - pillWidth / 2f, textPos.y - pillHeight / 2f),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(22f * scale, 22f * scale)
                )

                drawRoundRect(
                    color = PureWhite.copy(alpha = 0.9f * alpha),
                    topLeft = Offset(textPos.x - pillWidth / 2f, textPos.y - pillHeight / 2f),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(22f * scale, 22f * scale),
                    style = Stroke(width = 2f * scale)
                )
            }
        }
    }
}

private fun DrawScope.drawEnvironmentBackground(
    env: EnvironmentType,
    width: Float,
    height: Float,
    centerY: Float,
    bassEnergy: Float,
    ballZ: Float
) {
    // Gradient sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(env.skyColorTop, env.skyColorBottom),
            startY = 0f,
            endY = height
        ),
        size = Size(width, height)
    )

    // Dynamic Horizon Cyber Skyline with illuminated windows
    val skylineY = centerY * 0.82f
    val buildingCount = 14
    val buildingWidth = width / 9f

    for (i in 0 until buildingCount) {
        val bIndex = i - 2
        val bHeight = ((abs(sin(bIndex * 1.7f + 0.5f)) * 0.35f + 0.15f) * height)
        val bx = bIndex * buildingWidth - ((ballZ * 2.5f) % buildingWidth)
        val by = skylineY - bHeight

        // Dark skyscraper body
        drawRect(
            color = env.buildingColor,
            topLeft = Offset(bx, by),
            size = Size(buildingWidth - 4f, bHeight + (height - skylineY))
        )

        // Skyscraper roof neon edge
        val edgeColor = if (i % 2 == 0) NeonPurple else ElectricCyan
        drawLine(
            color = edgeColor.copy(alpha = 0.6f + bassEnergy * 0.4f),
            start = Offset(bx, by),
            end = Offset(bx + buildingWidth - 4f, by),
            strokeWidth = 3f
        )

        // Glowing office windows pulsing with bass
        val rows = (bHeight / 24f).toInt()
        val cols = 3
        for (r in 0 until rows step 2) {
            for (c in 0 until cols) {
                val wx = bx + 6f + c * 10f
                val wy = by + 12f + r * 22f
                val isLit = (bIndex * 7 + r * 3 + c) % 3 != 0
                if (isLit) {
                    val winColor = if ((bIndex + r) % 2 == 0) ElectricCyan else NeonPink
                    val winAlpha = (0.35f + bassEnergy * 0.5f).coerceIn(0.2f, 0.95f)
                    drawRect(
                        color = winColor.copy(alpha = winAlpha),
                        topLeft = Offset(wx, wy),
                        size = Size(5f, 7f)
                    )
                }
            }
        }
    }

    // Distant cyber grid converging at horizon
    val gridLines = 10
    for (i in 0..gridLines) {
        val norm = i / gridLines.toFloat()
        val startX = norm * width
        drawLine(
            color = env.gridColor.copy(alpha = 0.18f + bassEnergy * 0.12f),
            start = Offset(width / 2f, skylineY),
            end = Offset(startX, height),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.draw3DPlatform(
    plat: Platform3D,
    project: (Float, Float, Float) -> Offset?,
    bassEnergy: Float
) {
    val halfW = plat.width / 2f
    val halfD = plat.depth / 2f
    val topY = 0f
    val bottomY = -0.32f // Extruded 3D depth

    // 4 corners of top surface
    val pNearL = project(plat.x - halfW, topY, plat.z - halfD)
    val pNearR = project(plat.x + halfW, topY, plat.z - halfD)
    val pFarR = project(plat.x + halfW, topY, plat.z + halfD)
    val pFarL = project(plat.x - halfW, topY, plat.z + halfD)

    // 2 bottom front corners for 3D extrusion
    val bNearL = project(plat.x - halfW, bottomY, plat.z - halfD)
    val bNearR = project(plat.x + halfW, bottomY, plat.z - halfD)

    if (pNearL == null || pNearR == null || pFarR == null || pFarL == null) return

    // 1. Draw Front Extrusion Face (3D depth thickness)
    if (bNearL != null && bNearR != null) {
        val frontPath = Path().apply {
            moveTo(pNearL.x, pNearL.y)
            lineTo(pNearR.x, pNearR.y)
            lineTo(bNearR.x, bNearR.y)
            lineTo(bNearL.x, bNearL.y)
            close()
        }
        drawPath(
            path = frontPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    plat.color.copy(alpha = 0.55f),
                    plat.color.copy(alpha = 0.15f)
                )
            )
        )
    }

    // 2. Draw Top Surface
    val topPath = Path().apply {
        moveTo(pNearL.x, pNearL.y)
        lineTo(pNearR.x, pNearR.y)
        lineTo(pFarR.x, pFarR.y)
        lineTo(pFarL.x, pFarL.y)
        close()
    }

    val surfaceColor = if (plat.landed) {
        plat.color.copy(alpha = 0.85f)
    } else {
        plat.color.copy(alpha = 0.35f + bassEnergy * 0.25f)
    }

    drawPath(
        path = topPath,
        brush = Brush.radialGradient(
            colors = listOf(
                PureWhite.copy(alpha = if (plat.landed) 0.7f else 0.35f),
                surfaceColor,
                plat.color.copy(alpha = 0.25f)
            ),
            center = Offset((pNearL.x + pFarR.x) / 2f, (pNearL.y + pFarR.y) / 2f)
        )
    )

    // Glowing Platform Edge Border
    drawPath(
        path = topPath,
        color = PureWhite.copy(alpha = if (plat.landed) 0.95f else 0.75f),
        style = Stroke(width = if (plat.landed) 3.5f else 2.0f)
    )

    // Landing Ring Pulse Animation
    if (plat.landingRingAnim > 0.05f) {
        val ringScale = 1.0f + (1f - plat.landingRingAnim) * 1.5f
        val ringAlpha = (plat.landingRingAnim).coerceIn(0f, 1f)
        val centerTop = project(plat.x, topY, plat.z)
        if (centerTop != null) {
            val ringW = (pNearR.x - pNearL.x).absoluteValue * ringScale
            val ringH = ringW * 0.38f
            drawOval(
                color = plat.color.copy(alpha = ringAlpha * 0.8f),
                topLeft = Offset(centerTop.x - ringW / 2f, centerTop.y - ringH / 2f),
                size = Size(ringW, ringH),
                style = Stroke(width = 3.5f * ringAlpha)
            )
        }
    }

    // Floating Collectible Coin
    if (plat.hasCoin && !plat.coinCollected) {
        val coinCenter = project(plat.x, topY + 0.85f, plat.z)
        if (coinCenter != null) {
            val coinW = 16f
            val coinH = 22f
            // Outer golden glow
            drawCircle(
                color = NeonGold.copy(alpha = 0.45f + bassEnergy * 0.3f),
                radius = 18f,
                center = coinCenter
            )
            // Coin body
            drawOval(
                brush = Brush.radialGradient(
                    listOf(PureWhite, NeonGold, Color(0xFFD97706)),
                    center = coinCenter
                ),
                topLeft = Offset(coinCenter.x - coinW / 2f, coinCenter.y - coinH / 2f),
                size = Size(coinW, coinH)
            )
        }
    }
}
