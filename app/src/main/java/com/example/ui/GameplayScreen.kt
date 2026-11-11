package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.rendering.GameCanvas3D
import com.example.ui.theme.*

@Composable
fun GameplayScreen(
    viewModel: GameViewModel
) {
    val ballX by viewModel.ballX.collectAsState()
    val ballY by viewModel.ballY.collectAsState()
    val ballZ by viewModel.ballZ.collectAsState()
    val ballRot by viewModel.ballRotation.collectAsState()
    val selectedSkin by viewModel.selectedSkin.collectAsState()
    val selectedEnv by viewModel.selectedEnv.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val trailPoints by viewModel.trailPoints.collectAsState()
    val bassEnergy by viewModel.bassEnergy.collectAsState()
    val recentRating by viewModel.recentRating.collectAsState()
    val ratingAnim by viewModel.ratingAnim.collectAsState()

    val score by viewModel.score.collectAsState()
    val combo by viewModel.combo.collectAsState()
    val multiplier by viewModel.multiplier.collectAsState()
    val songProgress by viewModel.songProgress.collectAsState()
    val showPauseDialog by viewModel.showPauseDialog.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()

    // Handle back button to pause game safely
    BackHandler {
        viewModel.pauseGame()
    }

    val comboScale = remember(combo) {
        if (combo > 0) Animatable(1.35f) else Animatable(1.0f)
    }

    LaunchedEffect(combo) {
        if (combo > 0) {
            comboScale.snapTo(1.35f)
            comboScale.animateTo(1.0f, tween(180, easing = EaseOutBack))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // 1. Core 3D Rhythm Canvas
        GameCanvas3D(
            ballX = ballX,
            ballY = ballY,
            ballZ = ballZ,
            ballRotation = ballRot,
            selectedSkin = selectedSkin,
            selectedEnv = selectedEnv,
            platforms = platforms,
            particles = particles,
            trailPoints = trailPoints,
            bassEnergy = bassEnergy,
            recentRating = recentRating,
            ratingAnim = ratingAnim,
            onDragDelta = { delta -> viewModel.onDragDelta(delta) },
            modifier = Modifier.fillMaxSize()
        )

        // 2. In-Game HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar: Progress, Multiplier, Score, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Multiplier
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$score",
                            color = PureWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (multiplier > 1) {
                            Surface(
                                color = NeonPink,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "x$multiplier",
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${selectedTrack.title} • ${selectedMode.displayName}",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Pause Button (min 48dp touch target)
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardSurface.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, NeonPurple.copy(alpha = 0.6f), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Song Progress Bar (in Classic mode)
            if (selectedMode == GameMode.CLASSIC) {
                LinearProgressIndicator(
                    progress = { songProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricCyan,
                    trackColor = DarkViolet.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Combo Streak Indicator
            if (combo >= 2) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .scale(comboScale.value)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    CardSurface.copy(alpha = 0.9f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Combo",
                        tint = if (combo >= 20) NeonPink else NeonGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$combo COMBO",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Drag Helper
            Text(
                text = "◄ DRAG TO MOVE BALL ►",
                color = MutedText.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }

        // 3. Pause Dialog
        if (showPauseDialog) {
            PauseDialog(
                viewModel = viewModel,
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                onMainMenu = {
                    viewModel.resumeGame() // unpause internal
                    viewModel.navigateTo(AppScreen.MAIN_MENU)
                }
            )
        }
    }
}

@Composable
private fun PauseDialog(
    viewModel: GameViewModel,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOverlay)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = CardSurface,
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(ElectricCyan, NeonPurple))
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text(
                        text = "RESUME",
                        color = DeepBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("restart_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(NeonPurple, NeonPink))
                    )
                ) {
                    Text(
                        text = "RESTART TRACK",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quit_menu_button")
                ) {
                    Text(
                        text = "MAIN MENU",
                        color = SoftLavender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
