package com.example

import com.example.audio.SynthesizedMusicEngine
import com.example.game.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {
    @Test
    fun testMusicTracksConfigured() {
        val tracks = SynthesizedMusicEngine.ALL_TRACKS
        assertEquals(5, tracks.size)
        assertTrue(tracks.all { it.bpm in 100..160 })
        assertTrue(tracks.all { it.artist.contains("SUBHON") && it.artist.contains("SAYOD") })
    }

    @Test
    fun testBallSkinsAndEnvironments() {
        assertEquals(8, BallSkin.entries.size)
        assertEquals(6, EnvironmentType.entries.size)
        assertTrue(BallSkin.NEON_CORE.unlockedByDefault)
        assertTrue(EnvironmentType.NEON_CYBER_CITY.unlockedByDefault)
    }

    @Test
    fun testProceduralPlatformReachability() {
        val generator = ProceduralLevelGenerator()
        val track = SynthesizedMusicEngine.ALL_TRACKS[0]
        val platforms = generator.generateTrackPlatforms(track, GameMode.CLASSIC)

        assertTrue(platforms.isNotEmpty())
        assertEquals(0f, platforms[0].x, 0.01f)
        assertEquals(0f, platforms[1].x, 0.01f)

        // Verify reachability between consecutive platforms
        for (i in 0 until platforms.size - 1) {
            val deltaX = abs(platforms[i + 1].x - platforms[i].x)
            assertTrue("Platform $i to ${i+1} jump too far: $deltaX", deltaX <= 2.2f)
            assertTrue(platforms[i + 1].z > platforms[i].z)
        }
    }

    @Test
    fun testEndlessBatchGeneration() {
        val generator = ProceduralLevelGenerator()
        val batch1 = generator.generateEndlessBatch(0, 40)
        assertEquals(40, batch1.size)
        val batch2 = generator.generateEndlessBatch(40, 40, lastX = batch1.last().x)
        assertEquals(40, batch2.size)
        assertTrue(batch2.first().z > batch1.last().z)
    }

    @Test
    fun testLandingRatings() {
        assertEquals(25, LandingRating.PERFECT.points)
        assertEquals(50, LandingRating.EXCELLENT.points)
        assertEquals(10, LandingRating.GOOD.points)
        assertEquals(0, LandingRating.MISS.points)
    }
}
