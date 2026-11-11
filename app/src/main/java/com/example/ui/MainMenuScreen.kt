package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.theme.*

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val selectedSkin by viewModel.selectedSkin.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val overallHighScore = remember(coins) { viewModel.saveManager.getOverallHighScore() }

    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")
    val ballRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ball_rot"
    )

    val playPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // 1. Cyberpunk Skyline Background
        Image(
            painter = painterResource(id = R.drawable.bg_cyberpunk_city),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DeepBlack.copy(alpha = 0.85f),
                            DeepBlack.copy(alpha = 0.50f),
                            DeepBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Bar: Coins & High Score ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Badge
                Surface(
                    color = CardSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonPurple, ElectricCyan)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "High Score",
                            tint = NeonGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$overallHighScore",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Coins Badge
                Surface(
                    color = CardSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonGold, ElectricCyan)))
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins",
                            color = NeonGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Official Brand Logo ---
            Text(
                text = "BEATBOUNCE",
                fontSize = 38.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "CREATED BY SUBHON & SAYOD",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Center: 3D Rotating Glowing Ball Display ---
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.width * 0.35f

                    // Outer ambient neon glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                selectedSkin.outerGlow.copy(alpha = 0.5f),
                                selectedSkin.outerGlow.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = c,
                            radius = r * 1.6f
                        ),
                        radius = r * 1.6f,
                        center = c
                    )

                    // 3D Ball Sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PureWhite,
                                selectedSkin.coreColor,
                                selectedSkin.outerGlow
                            ),
                            center = Offset(c.x - r * 0.35f, c.y - r * 0.35f),
                            radius = r * 1.1f
                        ),
                        radius = r,
                        center = c
                    )

                    // Orbital neon rings
                    val ringR = r * 1.35f
                    drawOval(
                        color = ElectricCyan.copy(alpha = 0.6f),
                        topLeft = Offset(c.x - ringR, c.y - ringR * 0.3f),
                        size = Size(ringR * 2, ringR * 0.6f),
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            Text(
                text = selectedSkin.displayName,
                color = SoftLavender,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Current Mode & Song Tag
            Surface(
                color = DarkViolet.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonPurple, ElectricCyan)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Track",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${selectedTrack.title} • ${selectedMode.displayName}",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PRIMARY ACTION: PLAY BUTTON ---
            Button(
                onClick = {
                    viewModel.startGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(playPulse)
                    .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = ElectricCyan)
                    .testTag("play_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricCyan, NeonPurple, NeonPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAY",
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- Menu Buttons Grid ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    title = "MUSIC",
                    subtitle = "Select EDM Track",
                    icon = Icons.Default.LibraryMusic,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    testTag = "music_button",
                    onClick = { viewModel.navigateTo(AppScreen.MUSIC_SELECT) }
                )
                MenuGridButton(
                    title = "GAME MODES",
                    subtitle = "4 Rhythm Modes",
                    icon = Icons.Default.Gamepad,
                    accentColor = NeonPink,
                    modifier = Modifier.weight(1f),
                    testTag = "modes_button",
                    onClick = { viewModel.navigateTo(AppScreen.GAME_MODES) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    title = "BALL SKINS",
                    subtitle = "8 Cyber Spheres",
                    icon = Icons.Default.Palette,
                    accentColor = NeonGold,
                    modifier = Modifier.weight(1f),
                    testTag = "skins_button",
                    onClick = { viewModel.navigateTo(AppScreen.SKINS) }
                )
                MenuGridButton(
                    title = "ENVIRONMENTS",
                    subtitle = "6 Worlds",
                    icon = Icons.Default.LocationCity,
                    accentColor = NeonEmerald,
                    modifier = Modifier.weight(1f),
                    testTag = "environments_button",
                    onClick = { viewModel.navigateTo(AppScreen.ENVIRONMENTS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    title = "DAILY QUESTS",
                    subtitle = "Earn Free Coins",
                    icon = Icons.Default.Assignment,
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f),
                    testTag = "daily_button",
                    onClick = { viewModel.navigateTo(AppScreen.DAILY_CHALLENGES) }
                )
                MenuGridButton(
                    title = "SETTINGS",
                    subtitle = "Audio & Controls",
                    icon = Icons.Default.Settings,
                    accentColor = SoftLavender,
                    modifier = Modifier.weight(1f),
                    testTag = "settings_button",
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // How to Play Tutorial trigger
            OutlinedButton(
                onClick = { viewModel.openTutorial() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("tutorial_button"),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.5f), ElectricCyan.copy(alpha = 0.5f))))
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "How to play",
                    tint = SoftLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HOW TO PLAY TUTORIAL",
                    color = SoftLavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuGridButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(84.dp)
            .testTag(testTag),
        color = CardSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
