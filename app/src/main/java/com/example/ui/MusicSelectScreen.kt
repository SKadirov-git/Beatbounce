package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.audio.SynthesizedMusicEngine
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.game.MusicTrack
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSelectScreen(
    viewModel: GameViewModel
) {
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val isMusicPlaying = viewModel.musicEngine.isMusicPlaying()
    val currentlyPlayingTrack = viewModel.musicEngine.getCurrentTrack()

    BackHandler {
        viewModel.musicEngine.stop()
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MUSIC LIBRARY",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.musicEngine.stop()
                            viewModel.navigateTo(AppScreen.MAIN_MENU)
                        },
                        modifier = Modifier.testTag("music_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a track to launch into cyberspace rhythm:",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
            }

            items(SynthesizedMusicEngine.ALL_TRACKS) { track ->
                val isSelected = selectedTrack.id == track.id
                val isPreviewing = isMusicPlaying && currentlyPlayingTrack?.id == track.id
                val highScore = viewModel.saveManager.getHighScore(track.id, viewModel.selectedMode.value.name)

                TrackCard(
                    track = track,
                    isSelected = isSelected,
                    isPreviewing = isPreviewing,
                    highScore = highScore,
                    onSelect = {
                        viewModel.selectTrack(track)
                    },
                    onTogglePreview = {
                        if (isPreviewing) {
                            viewModel.musicEngine.stop()
                        } else {
                            viewModel.selectTrack(track)
                            viewModel.musicEngine.play(track)
                        }
                    },
                    onPlay = {
                        viewModel.selectTrack(track)
                        viewModel.startGame(track)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: MusicTrack,
    isSelected: Boolean,
    isPreviewing: Boolean,
    highScore: Int,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit,
    onPlay: () -> Unit
) {
    val borderColor = if (isSelected) ElectricCyan else NeonPurple.copy(alpha = 0.35f)

    Surface(
        onClick = onSelect,
        color = CardSurface,
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isSelected) listOf(ElectricCyan, NeonPurple) else listOf(borderColor, Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("track_card_${track.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${track.genre} • ${track.artist}",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Difficulty badge
                Surface(
                    color = when (track.difficulty) {
                        "Easy" -> NeonEmerald.copy(alpha = 0.2f)
                        "Normal" -> NeonPurple.copy(alpha = 0.2f)
                        "Medium" -> ElectricCyan.copy(alpha = 0.2f)
                        "Hard" -> NeonGold.copy(alpha = 0.2f)
                        else -> NeonPink.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = track.difficulty.uppercase(),
                        color = when (track.difficulty) {
                            "Easy" -> NeonEmerald
                            "Normal" -> NeonPurple
                            "Medium" -> ElectricCyan
                            "Hard" -> NeonGold
                            else -> NeonPink
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = track.description,
                color = MutedText,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${track.bpm} BPM",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • ${track.durationSec}s",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                    if (highScore > 0) {
                        Text(
                            text = " • High: $highScore",
                            color = NeonGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Preview Audio Button
                    OutlinedButton(
                        onClick = onTogglePreview,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, NeonPurple)))
                    ) {
                        Icon(
                            imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Preview",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPreviewing) "STOP" else "PREVIEW",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Play Button
                    Button(
                        onClick = onPlay,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PLAY",
                            color = DeepBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
