package com.example.game

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffects
import com.example.audio.SynthesizedMusicEngine
import com.example.data.SaveManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

enum class AppScreen {
    INTRO,
    MAIN_MENU,
    GAMEPLAY,
    MUSIC_SELECT,
    GAME_MODES,
    SKINS,
    ENVIRONMENTS,
    DAILY_CHALLENGES,
    SETTINGS,
    GAME_OVER
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val saveManager = SaveManager(application)
    val musicEngine = SynthesizedMusicEngine()
    val soundEffects = SoundEffects()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.INTRO)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _showPauseDialog = MutableStateFlow(false)
    val showPauseDialog: StateFlow<Boolean> = _showPauseDialog.asStateFlow()

    private val _showTutorialDialog = MutableStateFlow(false)
    val showTutorialDialog: StateFlow<Boolean> = _showTutorialDialog.asStateFlow()

    // Active Selections
    private val _selectedTrack = MutableStateFlow(SynthesizedMusicEngine.ALL_TRACKS[0])
    val selectedTrack: StateFlow<MusicTrack> = _selectedTrack.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.CLASSIC)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _selectedSkin = MutableStateFlow(saveManager.selectedSkin)
    val selectedSkin: StateFlow<BallSkin> = _selectedSkin.asStateFlow()

    private val _selectedEnv = MutableStateFlow(saveManager.selectedEnvironment)
    val selectedEnv: StateFlow<EnvironmentType> = _selectedEnv.asStateFlow()

    private val _coins = MutableStateFlow(saveManager.coins)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    // Gameplay Loop State
    private val _ballX = MutableStateFlow(0f)
    val ballX: StateFlow<Float> = _ballX.asStateFlow()

    private val _ballY = MutableStateFlow(0f)
    val ballY: StateFlow<Float> = _ballY.asStateFlow()

    private val _ballZ = MutableStateFlow(0f)
    val ballZ: StateFlow<Float> = _ballZ.asStateFlow()

    private val _ballRotation = MutableStateFlow(0f)
    val ballRotation: StateFlow<Float> = _ballRotation.asStateFlow()

    private val _platforms = MutableStateFlow<List<Platform3D>>(emptyList())
    val platforms: StateFlow<List<Platform3D>> = _platforms.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle3D>>(emptyList())
    val particles: StateFlow<List<Particle3D>> = _particles.asStateFlow()

    private val _trailPoints = MutableStateFlow<List<TrailPoint3D>>(emptyList())
    val trailPoints: StateFlow<List<TrailPoint3D>> = _trailPoints.asStateFlow()

    private val _bassEnergy = MutableStateFlow(0.2f)
    val bassEnergy: StateFlow<Float> = _bassEnergy.asStateFlow()

    private val _recentRating = MutableStateFlow<LandingRating?>(null)
    val recentRating: StateFlow<LandingRating?> = _recentRating.asStateFlow()

    private val _ratingAnim = MutableStateFlow(0f)
    val ratingAnim: StateFlow<Float> = _ratingAnim.asStateFlow()

    // Score & HUD
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo: StateFlow<Int> = _maxCombo.asStateFlow()

    private val _multiplier = MutableStateFlow(1)
    val multiplier: StateFlow<Int> = _multiplier.asStateFlow()

    private val _perfectCount = MutableStateFlow(0)
    val perfectCount: StateFlow<Int> = _perfectCount.asStateFlow()

    private val _excellentCount = MutableStateFlow(0)
    val excellentCount: StateFlow<Int> = _excellentCount.asStateFlow()

    private val _goodCount = MutableStateFlow(0)
    val goodCount: StateFlow<Int> = _goodCount.asStateFlow()

    private val _totalJumps = MutableStateFlow(0)
    val totalJumps: StateFlow<Int> = _totalJumps.asStateFlow()

    private val _songProgress = MutableStateFlow(0f)
    val songProgress: StateFlow<Float> = _songProgress.asStateFlow()

    private val _earnedCoins = MutableStateFlow(0)
    val earnedCoins: StateFlow<Int> = _earnedCoins.asStateFlow()

    private val _isNewHighScore = MutableStateFlow(false)
    val isNewHighScore: StateFlow<Boolean> = _isNewHighScore.asStateFlow()

    // Internal simulation fields
    private var ballTargetX = 0f
    private var currentBeatIndex = 0
    private var beatTimer = 0f
    private var beatDuration = 0.5f
    private var beatDistance = 4.0f
    private var isPlayingGame = false
    private var isPaused = false
    private var gameLoopJob: Job? = null
    private val levelGenerator = ProceduralLevelGenerator()

    init {
        // Sync audio volume settings
        musicEngine.setVolume(saveManager.musicVolume)
        soundEffects.setVolume(saveManager.sfxVolume)
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value == AppScreen.GAMEPLAY && screen != AppScreen.GAMEPLAY) {
            pauseGame()
            musicEngine.stop()
        }
        _currentScreen.value = screen
    }

    fun selectTrack(track: MusicTrack) {
        _selectedTrack.value = track
        soundEffects.playClick()
    }

    fun selectGameMode(mode: GameMode) {
        _selectedMode.value = mode
        soundEffects.playClick()
    }

    fun selectSkin(skin: BallSkin) {
        if (saveManager.isSkinUnlocked(skin)) {
            saveManager.selectedSkin = skin
            _selectedSkin.value = skin
            soundEffects.playClick()
        } else if (saveManager.spendCoins(skin.price)) {
            saveManager.unlockSkin(skin)
            saveManager.selectedSkin = skin
            _selectedSkin.value = skin
            _coins.value = saveManager.coins
            soundEffects.playCoin()
        }
    }

    fun selectEnvironment(env: EnvironmentType) {
        if (saveManager.isEnvironmentUnlocked(env)) {
            saveManager.selectedEnvironment = env
            _selectedEnv.value = env
            soundEffects.playClick()
        } else if (saveManager.spendCoins(env.price)) {
            saveManager.unlockEnvironment(env)
            saveManager.selectedEnvironment = env
            _selectedEnv.value = env
            _coins.value = saveManager.coins
            soundEffects.playCoin()
        }
    }

    fun setTutorialShown() {
        saveManager.tutorialCompleted = true
        _showTutorialDialog.value = false
    }

    fun openTutorial() {
        _showTutorialDialog.value = true
        soundEffects.playClick()
    }

    // --- GAMEPLAY LIFECYCLE ---

    fun startGame(track: MusicTrack = _selectedTrack.value, mode: GameMode = _selectedMode.value) {
        _selectedTrack.value = track
        _selectedMode.value = mode
        _showPauseDialog.value = false
        _isNewHighScore.value = false
        _score.value = 0
        _combo.value = 0
        _maxCombo.value = 0
        _multiplier.value = 1
        _perfectCount.value = 0
        _excellentCount.value = 0
        _goodCount.value = 0
        _totalJumps.value = 0
        _earnedCoins.value = 0
        _songProgress.value = 0f
        _recentRating.value = null
        _ratingAnim.value = 0f

        ballTargetX = 0f
        _ballX.value = 0f
        _ballY.value = 0f
        _ballZ.value = 0f
        _ballRotation.value = 0f
        currentBeatIndex = 0
        beatTimer = 0f

        beatDuration = 60f / track.bpm
        beatDistance = 4.0f

        // Generate platforms
        val generated = if (mode == GameMode.ENDLESS) {
            levelGenerator.generateEndlessBatch(0, 80, beatDistance)
        } else {
            levelGenerator.generateTrackPlatforms(track, mode, beatDistance)
        }
        _platforms.value = generated
        _particles.value = emptyList()
        _trailPoints.value = emptyList()

        isPlayingGame = true
        isPaused = false
        _currentScreen.value = AppScreen.GAMEPLAY

        // Start real synthesized music
        musicEngine.play(track)
        soundEffects.playJump()

        // Start 60 FPS Coroutine Game Loop
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            while (isActive && isPlayingGame) {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                if (!isPaused) {
                    updateGamePhysics(delta)
                }

                delay(16) // Target 60 FPS
            }
        }
    }

    fun pauseGame() {
        if (!isPlayingGame || isPaused) return
        isPaused = true
        musicEngine.pause()
        _showPauseDialog.value = true
        soundEffects.playClick()
    }

    fun resumeGame() {
        if (!isPlayingGame || !isPaused) return
        isPaused = false
        _showPauseDialog.value = false
        musicEngine.resume()
        soundEffects.playClick()
    }

    fun restartGame() {
        _showPauseDialog.value = false
        startGame(_selectedTrack.value, _selectedMode.value)
    }

    fun onDragDelta(deltaPx: Float) {
        if (!isPlayingGame || isPaused) return
        // Map touch drag pixels to 3D world units with sensitivity
        val sensitivity = saveManager.touchSensitivity
        val worldDelta = (deltaPx / 180f) * sensitivity
        ballTargetX = (ballTargetX + worldDelta).coerceIn(-2.5f, 2.5f)
    }

    private fun updateGamePhysics(delta: Float) {
        // Smooth horizontal ball follow
        val curX = _ballX.value
        val newX = curX + (ballTargetX - curX) * (14f * delta).coerceIn(0f, 1f)
        _ballX.value = newX

        // Advance beat timer
        beatTimer += delta
        val jumpApex = 2.4f

        if (beatTimer < beatDuration) {
            // Parabolic jump arc: 4 * H * t * (1 - t)
            val t = beatTimer / beatDuration
            _ballY.value = 4f * jumpApex * t * (1f - t)

            // Forward Z movement smoothly tracking beat distance
            val prevZ = currentBeatIndex * beatDistance
            _ballZ.value = prevZ + (beatDistance * t)

            // Ball rotation
            _ballRotation.value = (_ballRotation.value + delta * 240f) % 360f
        } else {
            // Completed a bounce: Land on platform!
            handleLanding()
        }

        // Bass energy reaction
        _bassEnergy.value = musicEngine.getCurrentBassEnergy()

        // Rating animation fade
        if (_ratingAnim.value > 0f) {
            _ratingAnim.value = max(0f, _ratingAnim.value - delta * 2.2f)
        }

        // Update trail particles
        val curTrail = _trailPoints.value.toMutableList()
        curTrail.add(0, TrailPoint3D(_ballX.value, _ballY.value, _ballZ.value, 1f))
        if (curTrail.size > 14) curTrail.removeAt(curTrail.size - 1)
        _trailPoints.value = curTrail.mapIndexed { idx, pt ->
            pt.copy(alpha = (1f - idx / 14f))
        }

        // Update 3D particles
        val curParticles = _particles.value.mapNotNull { p ->
            p.x += p.vx * delta
            p.y += p.vy * delta
            p.z += p.vz * delta
            p.life -= delta * 1.8f
            if (p.life > 0f) p else null
        }
        _particles.value = curParticles

        // Update platform landing ring animations
        val plats = _platforms.value
        for (plat in plats) {
            if (plat.landingRingAnim > 0f) {
                plat.landingRingAnim = max(0f, plat.landingRingAnim - delta * 2.5f)
            }
        }

        // Check track completion in Classic mode
        val track = _selectedTrack.value
        if (_selectedMode.value == GameMode.CLASSIC) {
            val progress = (_ballZ.value / (track.durationSec * (track.bpm / 60f) * beatDistance)).coerceIn(0f, 1f)
            _songProgress.value = progress
            if (currentBeatIndex >= plats.size - 1) {
                // Completed song!
                finishRun(true)
            }
        }
    }

    private fun handleLanding() {
        val plats = _platforms.value
        if (currentBeatIndex >= plats.size) {
            if (_selectedMode.value == GameMode.ENDLESS) {
                // Procedural generation extension
                val extension = levelGenerator.generateEndlessBatch(
                    currentBeatIndex,
                    50,
                    beatDistance,
                    plats.lastOrNull()?.x ?: 0f
                )
                _platforms.value = plats + extension
            } else {
                finishRun(true)
                return
            }
        }

        val targetPlat = _platforms.value.getOrNull(currentBeatIndex)
        if (targetPlat == null) {
            finishRun(false)
            return
        }

        val deltaX = abs(_ballX.value - targetPlat.x)
        val halfW = targetPlat.width / 2f

        // Check if landed within platform bounds
        if (deltaX <= halfW + 0.15f) {
            // SUCCESSFUL LANDING!
            targetPlat.landed = true
            targetPlat.landingRingAnim = 1.0f

            // Determine precision rating
            val rating = when {
                deltaX <= halfW * 0.28f -> LandingRating.EXCELLENT
                deltaX <= halfW * 0.55f -> LandingRating.PERFECT
                else -> LandingRating.GOOD
            }

            targetPlat.landingRating = rating
            _recentRating.value = rating
            _ratingAnim.value = 1.0f

            // Update stats & scores
            val newCombo = _combo.value + 1
            _combo.value = newCombo
            if (newCombo > _maxCombo.value) _maxCombo.value = newCombo

            val newMult = when {
                newCombo >= 40 -> 5
                newCombo >= 25 -> 4
                newCombo >= 15 -> 3
                newCombo >= 5 -> 2
                else -> 1
            }
            _multiplier.value = newMult
            _score.value += rating.points * newMult

            when (rating) {
                LandingRating.EXCELLENT -> {
                    _excellentCount.value += 1
                    soundEffects.playExcellentLanding()
                    triggerHaptic(50)
                }
                LandingRating.PERFECT -> {
                    _perfectCount.value += 1
                    soundEffects.playPerfectLanding()
                    triggerHaptic(35)
                }
                LandingRating.GOOD -> {
                    _goodCount.value += 1
                    soundEffects.playNormalLanding()
                    triggerHaptic(20)
                }
                else -> {}
            }

            // Milestone combos
            if (newCombo in listOf(10, 25, 50, 100)) {
                soundEffects.playComboMilestone()
            }

            // Check coin collection
            if (targetPlat.hasCoin && !targetPlat.coinCollected) {
                targetPlat.coinCollected = true
                _earnedCoins.value += 5
                soundEffects.playCoin()
            }

            // Spawn neon particles
            spawnLandingParticles(targetPlat.x, targetPlat.z, rating)

            // Update daily challenge & achievement milestones
            _totalJumps.value += 1
            saveManager.updateDailyChallengeProgress(0, _totalJumps.value)
            saveManager.updateDailyChallengeProgress(1, _perfectCount.value + _excellentCount.value)
            saveManager.updateDailyChallengeProgress(2, _maxCombo.value)

            if (_totalJumps.value == 1) saveManager.unlockAchievement("first_bounce")
            if (newCombo >= 25) saveManager.unlockAchievement("combo_master")
            if (_score.value >= 5000) saveManager.unlockAchievement("high_scorer")

            // Advance to next beat!
            currentBeatIndex++
            beatTimer = 0f
            soundEffects.playJump()
        } else {
            // MISSED PLATFORM! Ball falls into neon void!
            _recentRating.value = LandingRating.MISS
            _ratingAnim.value = 1.0f
            soundEffects.playGameOver()
            triggerHaptic(120)
            finishRun(false)
        }
    }

    private fun spawnLandingParticles(x: Float, z: Float, rating: LandingRating) {
        val count = when (rating) {
            LandingRating.EXCELLENT -> 32
            LandingRating.PERFECT -> 22
            else -> 12
        }
        val rng = Random
        val newParticles = List(count) {
            val angle = rng.nextFloat() * 2f * Math.PI.toFloat()
            val speed = rng.nextFloat() * 3.5f + 1.5f
            Particle3D(
                x = x,
                y = 0.1f,
                z = z,
                vx = cos(angle) * speed,
                vy = rng.nextFloat() * 4.5f + 2.5f,
                vz = sin(angle) * speed,
                color = rating.color,
                life = 1.0f,
                size = rng.nextFloat() * 6f + 3f
            )
        }
        _particles.value = _particles.value + newParticles
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!saveManager.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun finishRun(isVictory: Boolean) {
        isPlayingGame = false
        gameLoopJob?.cancel()
        musicEngine.stop()

        // Calculate final coins
        val baseCoins = (_score.value / 25).coerceAtLeast(5)
        val finalCoins = baseCoins + _earnedCoins.value
        saveManager.addCoins(finalCoins)
        _coins.value = saveManager.coins

        // High score check
        val track = _selectedTrack.value
        val mode = _selectedMode.value.name
        val isNewHigh = saveManager.setHighScore(track.id, mode, _score.value)
        _isNewHighScore.value = isNewHigh

        _currentScreen.value = AppScreen.GAME_OVER
    }

    override fun onCleared() {
        super.onCleared()
        musicEngine.stop()
    }
}
