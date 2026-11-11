package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlin.math.sin
import kotlin.random.Random

class ProceduralLevelGenerator {

    /**
     * Generate platform sequence for a classic track or mode.
     */
    fun generateTrackPlatforms(
        track: MusicTrack,
        gameMode: GameMode,
        beatDistance: Float = 4.0f
    ): List<Platform3D> {
        val totalBeats = ((track.durationSec * track.bpm) / 60).coerceAtLeast(60)
        val platforms = mutableListOf<Platform3D>()
        val rng = Random(track.synthPatternId * 7919)

        var currentX = 0f
        val maxX = 2.2f
        val maxStepX = when (track.difficulty) {
            "Easy" -> 0.7f
            "Normal" -> 1.0f
            "Medium" -> 1.25f
            "Hard" -> 1.5f
            "Expert" -> 1.8f
            else -> 1.2f
        }

        val baseWidth = when (gameMode) {
            GameMode.PERFECT_RHYTHM -> 1.05f
            else -> if (track.difficulty == "Expert") 1.15f else 1.4f
        }

        val neonColors = listOf(NeonPurple, ElectricCyan, NeonPink, NeonEmerald, NeonGold)

        for (i in 0 until totalBeats) {
            // First 3 platforms are always dead center to let the player orient
            val x = if (i < 3) {
                0f
            } else {
                val pattern = (i / 8) % 4
                val target = when (pattern) {
                    0 -> sin(i * 0.5f) * 1.5f // Gentle wave
                    1 -> if (i % 2 == 0) -1.2f else 1.2f // Zigzag
                    2 -> sin(i * 0.8f) * 1.8f // Sweeping arc
                    else -> (rng.nextFloat() * 2f - 1f) * 1.6f // Random jump
                }
                val step = (target - currentX).coerceIn(-maxStepX, maxStepX)
                currentX = (currentX + step).coerceIn(-maxX, maxX)
                currentX
            }

            val z = i * beatDistance
            val color = neonColors[i % neonColors.size]
            val hasCoin = (i > 4) && (i % 5 == 0)

            platforms.add(
                Platform3D(
                    id = i,
                    x = x,
                    z = z,
                    width = baseWidth,
                    depth = 0.95f,
                    color = color,
                    beatIndex = i,
                    hasCoin = hasCoin
                )
            )
        }

        return platforms
    }

    /**
     * Generate an infinite extension of platforms for Endless Mode.
     */
    fun generateEndlessBatch(
        startIndex: Int,
        count: Int,
        beatDistance: Float = 4.0f,
        lastX: Float = 0f
    ): List<Platform3D> {
        val platforms = mutableListOf<Platform3D>()
        var currentX = lastX
        val maxX = 2.4f
        val rng = Random(startIndex * 31 + 17)
        val neonColors = listOf(NeonPurple, ElectricCyan, NeonPink, NeonEmerald, NeonGold)

        for (k in 0 until count) {
            val i = startIndex + k
            val difficultyScale = (i / 50f).coerceIn(0f, 1f) // ramps up over time
            val maxStep = 1.0f + difficultyScale * 0.7f
            val width = (1.4f - difficultyScale * 0.35f).coerceAtLeast(0.95f)

            val delta = (rng.nextFloat() * 2f - 1f) * maxStep
            currentX = (currentX + delta).coerceIn(-maxX, maxX)

            val z = i * beatDistance
            val color = neonColors[(i + k) % neonColors.size]
            val hasCoin = (i % 4 == 0)

            platforms.add(
                Platform3D(
                    id = i,
                    x = currentX,
                    z = z,
                    width = width,
                    depth = 0.95f,
                    color = color,
                    beatIndex = i,
                    hasCoin = hasCoin
                )
            )
        }
        return platforms
    }
}
