package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.theme.*

@Composable
fun GameOverScreen(
    viewModel: GameViewModel
) {
    val score by viewModel.score.collectAsState()
    val maxCombo by viewModel.maxCombo.collectAsState()
    val perfects by viewModel.perfectCount.collectAsState()
    val excellents by viewModel.excellentCount.collectAsState()
    val goods by viewModel.goodCount.collectAsState()
    val totalJumps by viewModel.totalJumps.collectAsState()
    val earnedCoins by viewModel.earnedCoins.collectAsState()
    val isNewHigh by viewModel.isNewHighScore.collectAsState()
    val track by viewModel.selectedTrack.collectAsState()
    val mode by viewModel.selectedMode.collectAsState()

    val bestScore = remember(score) {
        viewModel.saveManager.getHighScore(track.id, mode.name)
    }

    val accuracy = remember(totalJumps, perfects, excellents, goods) {
        if (totalJumps > 0) {
            val weighted = (excellents * 1.0f + perfects * 0.8f + goods * 0.5f) / totalJumps
            (weighted * 100f).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    // Animated counting score
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "game_over_glow")
    val bannerPulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isNewHigh) {
                Surface(
                    color = NeonPink,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.scale(bannerPulse)
                ) {
                    Text(
                        text = "★ NEW HIGH SCORE! ★",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "GAME OVER",
                color = PureWhite,
                fontSize = 36.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Text(
                text = "${track.title} • ${mode.displayName}",
                color = ElectricCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Score Card
            Surface(
                color = CardSurface,
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(NeonPurple, ElectricCyan))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${animatedScore.value.toInt()}",
                        color = PureWhite,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "BEST",
                            value = "$bestScore",
                            icon = Icons.Default.EmojiEvents,
                            iconTint = NeonGold
                        )
                        StatItem(
                            label = "MAX COMBO",
                            value = "${maxCombo}x",
                            icon = Icons.Default.Whatshot,
                            iconTint = NeonPink
                        )
                        StatItem(
                            label = "PERFECTS",
                            value = "${perfects + excellents}",
                            icon = Icons.Default.Star,
                            iconTint = ElectricCyan
                        )
                        StatItem(
                            label = "ACCURACY",
                            value = "$accuracy%",
                            icon = Icons.Default.GpsFixed,
                            iconTint = NeonEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Coins reward banner
                    val totalAwarded = (score / 25).coerceAtLeast(5) + earnedCoins
                    Surface(
                        color = DarkViolet,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = NeonGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+$totalAwarded COINS EARNED",
                                color = NeonGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.restartGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("game_over_retry_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(ElectricCyan, NeonPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Retry",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RETRY TRACK",
                            color = PureWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("game_over_menu_button"),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.6f), ElectricCyan.copy(alpha = 0.6f)))
                )
            ) {
                Text(
                    text = "MAIN MENU",
                    color = SoftLavender,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
