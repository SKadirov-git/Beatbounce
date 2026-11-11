package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.*
import com.example.ui.theme.BeatBounceTheme
import com.example.ui.theme.DeepBlack

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeatBounceTheme {
                BeatBounceApp(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
    }

    override fun onStop() {
        super.onStop()
        viewModel.musicEngine.pause()
    }
}

@Composable
fun BeatBounceApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showTutorial by viewModel.showTutorialDialog.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        when (currentScreen) {
            AppScreen.INTRO -> {
                IntroScreen(
                    onFinished = {
                        viewModel.navigateTo(AppScreen.MAIN_MENU)
                    }
                )
            }
            AppScreen.MAIN_MENU -> {
                MainMenuScreen(viewModel = viewModel)
            }
            AppScreen.GAMEPLAY -> {
                GameplayScreen(viewModel = viewModel)
            }
            AppScreen.MUSIC_SELECT -> {
                MusicSelectScreen(viewModel = viewModel)
            }
            AppScreen.GAME_MODES -> {
                GameModesScreen(viewModel = viewModel)
            }
            AppScreen.SKINS -> {
                SkinsScreen(viewModel = viewModel)
            }
            AppScreen.ENVIRONMENTS -> {
                EnvironmentsScreen(viewModel = viewModel)
            }
            AppScreen.DAILY_CHALLENGES -> {
                DailyChallengesScreen(viewModel = viewModel)
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(viewModel = viewModel)
            }
            AppScreen.GAME_OVER -> {
                GameOverScreen(viewModel = viewModel)
            }
        }

        if (showTutorial) {
            TutorialDialog(
                onDismiss = {
                    viewModel.setTutorialShown()
                }
            )
        }
    }
}
