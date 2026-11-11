package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.game.Achievement
import com.example.game.BallSkin
import com.example.game.DailyChallenge
import com.example.game.EnvironmentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaveManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("beatbounce_save_v1", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COINS = "total_coins"
        private const val KEY_SELECTED_SKIN = "selected_skin"
        private const val KEY_SELECTED_ENV = "selected_env"
        private const val KEY_MUSIC_VOL = "music_volume"
        private const val KEY_SFX_VOL = "sfx_volume"
        private const val KEY_TOUCH_SENSITIVITY = "touch_sensitivity"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_GRAPHICS_QUALITY = "graphics_quality" // Low, Medium, High
        private const val KEY_SHOW_TIMING_FEEDBACK = "show_timing_feedback"
        private const val KEY_TUTORIAL_COMPLETED = "tutorial_completed"
        private const val KEY_DAILY_DATE = "daily_challenge_date"
        private const val KEY_DAILY_PROGRESS_PREFIX = "daily_prog_"
        private const val KEY_DAILY_CLAIMED_PREFIX = "daily_claimed_"
        private const val KEY_HIGH_SCORE_PREFIX = "high_score_"
    }

    // --- Coins ---
    var coins: Int
        get() = prefs.getInt(KEY_COINS, 250) // Starting bonus
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    fun addCoins(amount: Int) {
        coins += amount
    }

    fun spendCoins(amount: Int): Boolean {
        if (coins >= amount) {
            coins -= amount
            return true
        }
        return false
    }

    // --- High Scores ---
    fun getHighScore(trackId: String, mode: String): Int {
        return prefs.getInt("${KEY_HIGH_SCORE_PREFIX}${trackId}_$mode", 0)
    }

    fun setHighScore(trackId: String, mode: String, score: Int): Boolean {
        val current = getHighScore(trackId, mode)
        if (score > current) {
            prefs.edit().putInt("${KEY_HIGH_SCORE_PREFIX}${trackId}_$mode", score).apply()
            return true
        }
        return false
    }

    fun getOverallHighScore(): Int {
        var maxScore = 0
        for (entry in prefs.all) {
            if (entry.key.startsWith(KEY_HIGH_SCORE_PREFIX) && entry.value is Int) {
                maxScore = maxOf(maxScore, entry.value as Int)
            }
        }
        return maxScore
    }

    // --- Skins ---
    var selectedSkin: BallSkin
        get() {
            val id = prefs.getString(KEY_SELECTED_SKIN, BallSkin.NEON_CORE.id)
            return BallSkin.entries.find { it.id == id } ?: BallSkin.NEON_CORE
        }
        set(value) = prefs.edit().putString(KEY_SELECTED_SKIN, value.id).apply()

    fun isSkinUnlocked(skin: BallSkin): Boolean {
        if (skin.unlockedByDefault) return true
        return prefs.getBoolean("skin_unlocked_${skin.id}", false)
    }

    fun unlockSkin(skin: BallSkin) {
        prefs.edit().putBoolean("skin_unlocked_${skin.id}", true).apply()
    }

    // --- Environments ---
    var selectedEnvironment: EnvironmentType
        get() {
            val id = prefs.getString(KEY_SELECTED_ENV, EnvironmentType.NEON_CYBER_CITY.id)
            return EnvironmentType.entries.find { it.id == id } ?: EnvironmentType.NEON_CYBER_CITY
        }
        set(value) = prefs.edit().putString(KEY_SELECTED_ENV, value.id).apply()

    fun isEnvironmentUnlocked(env: EnvironmentType): Boolean {
        if (env.unlockedByDefault) return true
        return prefs.getBoolean("env_unlocked_${env.id}", false)
    }

    fun unlockEnvironment(env: EnvironmentType) {
        prefs.edit().putBoolean("env_unlocked_${env.id}", true).apply()
    }

    // --- Settings ---
    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOL, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOL, value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOL, 0.90f)
        set(value) = prefs.edit().putFloat(KEY_SFX_VOL, value).apply()

    var touchSensitivity: Float
        get() = prefs.getFloat(KEY_TOUCH_SENSITIVITY, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TOUCH_SENSITIVITY, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var graphicsQuality: String
        get() = prefs.getString(KEY_GRAPHICS_QUALITY, "High") ?: "High"
        set(value) = prefs.edit().putString(KEY_GRAPHICS_QUALITY, value).apply()

    var showTimingFeedback: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TIMING_FEEDBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TIMING_FEEDBACK, value).apply()

    var tutorialCompleted: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETED, value).apply()

    // --- Daily Challenges ---
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getDailyChallenges(): List<DailyChallenge> {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_DAILY_DATE, "")

        if (savedDate != today) {
            // New day: reset daily challenge tracking
            val editor = prefs.edit()
            editor.putString(KEY_DAILY_DATE, today)
            for (i in 0..2) {
                editor.putInt("${KEY_DAILY_PROGRESS_PREFIX}$i", 0)
                editor.putBoolean("${KEY_DAILY_CLAIMED_PREFIX}$i", false)
            }
            editor.apply()
        }

        return listOf(
            DailyChallenge(
                id = "daily_0",
                title = "Jump onto 50 Platforms",
                target = 50,
                current = prefs.getInt("${KEY_DAILY_PROGRESS_PREFIX}0", 0),
                rewardCoins = 150,
                claimed = prefs.getBoolean("${KEY_DAILY_CLAIMED_PREFIX}0", false)
            ),
            DailyChallenge(
                id = "daily_1",
                title = "Score 15 PERFECT Landings",
                target = 15,
                current = prefs.getInt("${KEY_DAILY_PROGRESS_PREFIX}1", 0),
                rewardCoins = 200,
                claimed = prefs.getBoolean("${KEY_DAILY_CLAIMED_PREFIX}1", false)
            ),
            DailyChallenge(
                id = "daily_2",
                title = "Reach a 20x Combo Streak",
                target = 20,
                current = prefs.getInt("${KEY_DAILY_PROGRESS_PREFIX}2", 0),
                rewardCoins = 250,
                claimed = prefs.getBoolean("${KEY_DAILY_CLAIMED_PREFIX}2", false)
            )
        )
    }

    fun updateDailyChallengeProgress(index: Int, progress: Int) {
        val current = prefs.getInt("${KEY_DAILY_PROGRESS_PREFIX}$index", 0)
        prefs.edit().putInt("${KEY_DAILY_PROGRESS_PREFIX}$index", maxOf(current, progress)).apply()
    }

    fun claimDailyReward(index: Int, rewardCoins: Int): Boolean {
        val isClaimed = prefs.getBoolean("${KEY_DAILY_CLAIMED_PREFIX}$index", false)
        if (!isClaimed) {
            prefs.edit().putBoolean("${KEY_DAILY_CLAIMED_PREFIX}$index", true).apply()
            addCoins(rewardCoins)
            return true
        }
        return false
    }

    // --- Achievements ---
    fun getAchievements(): List<Achievement> {
        val list = listOf(
            Achievement("first_bounce", "First Bounce", "Land your first successful platform bounce", 50),
            Achievement("perfect_start", "Perfect Start", "Achieve 3 consecutive PERFECT landings", 100),
            Achievement("combo_master", "Combo Master", "Reach a 25x landing combo in a single song", 200),
            Achievement("rhythm_legend", "Rhythm Legend", "Complete a song with over 90% accuracy", 300),
            Achievement("endless_survivor", "Endless Survivor", "Reach 100 jumps in Endless Mode", 400),
            Achievement("high_scorer", "Cyber Maestro", "Score over 5,000 points in any mode", 500)
        )
        return list.map { ach ->
            ach.copy(unlocked = prefs.getBoolean("achievement_${ach.id}", false))
        }
    }

    fun unlockAchievement(id: String): Boolean {
        if (!prefs.getBoolean("achievement_$id", false)) {
            prefs.edit().putBoolean("achievement_$id", true).apply()
            val reward = when (id) {
                "first_bounce" -> 50
                "perfect_start" -> 100
                "combo_master" -> 200
                "rhythm_legend" -> 300
                "endless_survivor" -> 400
                "high_scorer" -> 500
                else -> 50
            }
            addCoins(reward)
            return true
        }
        return false
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}
