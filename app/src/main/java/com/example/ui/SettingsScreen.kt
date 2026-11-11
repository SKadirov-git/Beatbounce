package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel
) {
    var musicVol by remember { mutableFloatStateOf(viewModel.saveManager.musicVolume) }
    var sfxVol by remember { mutableFloatStateOf(viewModel.saveManager.sfxVolume) }
    var sensitivity by remember { mutableFloatStateOf(viewModel.saveManager.touchSensitivity) }
    var haptics by remember { mutableStateOf(viewModel.saveManager.hapticsEnabled) }
    var timingFeedback by remember { mutableStateOf(viewModel.saveManager.showTimingFeedback) }
    var graphics by remember { mutableStateOf(viewModel.saveManager.graphicsQuality) }

    var showResetDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Audio Controls Card
            SectionCard(title = "AUDIO", icon = Icons.Default.VolumeUp) {
                Text(
                    text = "Music Volume: ${(musicVol * 100).toInt()}%",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
                Slider(
                    value = musicVol,
                    onValueChange = {
                        musicVol = it
                        viewModel.saveManager.musicVolume = it
                        viewModel.musicEngine.setVolume(it)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = DarkViolet
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sound Effects Volume: ${(sfxVol * 100).toInt()}%",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
                Slider(
                    value = sfxVol,
                    onValueChange = {
                        sfxVol = it
                        viewModel.saveManager.sfxVolume = it
                        viewModel.soundEffects.setVolume(it)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPurple,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = DarkViolet
                    )
                )
            }

            // Gameplay Controls Card
            SectionCard(title = "CONTROLS & HAPTICS") {
                Text(
                    text = "Touch Sensitivity: ${String.format("%.1fx", sensitivity)}",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
                Slider(
                    value = sensitivity,
                    onValueChange = {
                        sensitivity = it
                        viewModel.saveManager.touchSensitivity = it
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPink,
                        activeTrackColor = NeonPink,
                        inactiveTrackColor = DarkViolet
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Haptic Vibration",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Vibrate on platform bounce",
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = haptics,
                        onCheckedChange = {
                            haptics = it
                            viewModel.saveManager.hapticsEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = ElectricCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Timing Accuracy Feedback",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Display PERFECT and EXCELLENT prompts",
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = timingFeedback,
                        onCheckedChange = {
                            timingFeedback = it
                            viewModel.saveManager.showTimingFeedback = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = NeonPurple
                        )
                    )
                }
            }

            // Graphics Quality
            SectionCard(title = "GRAPHICS PROFILE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { q ->
                        val isSel = graphics == q
                        OutlinedButton(
                            onClick = {
                                graphics = q
                                viewModel.saveManager.graphicsQuality = q
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSel) ElectricCyan else Color.Transparent
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(if (isSel) listOf(ElectricCyan, PureWhite) else listOf(DarkViolet, DarkViolet))
                            )
                        ) {
                            Text(
                                text = q,
                                color = if (isSel) DeepBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tutorial Replay
            SectionCard(title = "TUTORIAL") {
                Button(
                    onClick = { viewModel.openTutorial() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text(
                        text = "REPLAY HOW TO PLAY TUTORIAL",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Official Credits Card
            SectionCard(title = "CREDITS", icon = Icons.Default.Info) {
                Text(
                    text = "BEATBOUNCE",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "CREATED BY SUBHON & SAYOD",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Futuristic 3D Rhythm Arcade Engine\nTarget Performance: 60 FPS Neon Cyberpunk",
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            // Reset Game Progress
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text(
                    text = "RESET ALL PROGRESS",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Progress?", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = { Text("This will reset all high scores, unlocked skins, and coins back to default.", color = SoftLavender) },
                containerColor = CardSurface,
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.saveManager.resetAllProgress()
                            showResetDialog = false
                            viewModel.navigateTo(AppScreen.MAIN_MENU)
                        }
                    ) {
                        Text("RESET", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("CANCEL", color = SoftLavender)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), Color.Transparent))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    color = ElectricCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
