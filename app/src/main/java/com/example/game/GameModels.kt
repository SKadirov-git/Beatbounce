package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GameMode(val displayName: String, val description: String) {
    CLASSIC("CLASSIC", "Play full music tracks, follow platforms, and set high scores"),
    ENDLESS("ENDLESS", "Procedural infinite rhythm run with escalating challenge"),
    CHALLENGES("CHALLENGES", "Complete targeted missions and test your rhythm mastery"),
    PERFECT_RHYTHM("PERFECT RHYTHM", "Ultra-tight landing precision mode (±25ms accuracy)")
}

enum class EnvironmentType(
    val id: String,
    val displayName: String,
    val description: String,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val gridColor: Color,
    val buildingColor: Color,
    val price: Int,
    val unlockedByDefault: Boolean
) {
    NEON_CYBER_CITY(
        id = "cyber_city",
        displayName = "NEON CYBER CITY",
        description = "Towering skyscrapers, neon signs, and atmospheric purple mist",
        skyColorTop = DeepBlack,
        skyColorBottom = DarkViolet,
        gridColor = NeonPurple,
        buildingColor = Color(0xFF1E1035),
        price = 0,
        unlockedByDefault = true
    ),
    GALAXY_SPACE(
        id = "galaxy_space",
        displayName = "GALAXY SPACE",
        description = "Deep cosmic nebula, twinkling stellar clusters, and planetary rings",
        skyColorTop = Color(0xFF030014),
        skyColorBottom = Color(0xFF190033),
        gridColor = ElectricCyan,
        buildingColor = Color(0xFF120024),
        price = 200,
        unlockedByDefault = false
    ),
    SYNTHWAVE_SUNSET(
        id = "synthwave_sunset",
        displayName = "SYNTHWAVE SUNSET",
        description = "Retro 80s grid horizon, glowing sun, and dark mountain silhouettes",
        skyColorTop = Color(0xFF1B032E),
        skyColorBottom = Color(0xFF8B255D),
        gridColor = NeonPink,
        buildingColor = Color(0xFF2C0B3E),
        price = 400,
        unlockedByDefault = false
    ),
    DIGITAL_CYBER_WORLD(
        id = "digital_matrix",
        displayName = "DIGITAL MATRIX",
        description = "Emerald data streams, holographic pillars, and cyber architecture",
        skyColorTop = Color(0xFF02130C),
        skyColorBottom = Color(0xFF052B1B),
        gridColor = NeonEmerald,
        buildingColor = Color(0xFF083824),
        price = 600,
        unlockedByDefault = false
    ),
    CRYSTAL_UNIVERSE(
        id = "crystal_universe",
        displayName = "CRYSTAL UNIVERSE",
        description = "Prismatic crystal spires, dazzling light refractions, and icy glow",
        skyColorTop = Color(0xFF06152B),
        skyColorBottom = Color(0xFF0E3259),
        gridColor = ElectricCyan,
        buildingColor = Color(0xFF14477D),
        price = 800,
        unlockedByDefault = false
    ),
    FIRE_AND_ICE(
        id = "fire_and_ice",
        displayName = "FIRE & ICE",
        description = "Volcanic magma split with glacial permafrost aurora",
        skyColorTop = Color(0xFF280707),
        skyColorBottom = Color(0xFF0A1E38),
        gridColor = NeonGold,
        buildingColor = Color(0xFF38121E),
        price = 1200,
        unlockedByDefault = false
    )
}

enum class BallSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val coreColor: Color,
    val outerGlow: Color,
    val trailColor: Color,
    val price: Int,
    val unlockedByDefault: Boolean
) {
    NEON_CORE(
        id = "neon_core",
        displayName = "Neon Core",
        description = "Cybernetic core pulsing with electric cyan and purple energy",
        coreColor = ElectricCyan,
        outerGlow = NeonPurple,
        trailColor = ElectricCyan,
        price = 0,
        unlockedByDefault = true
    ),
    GALAXY_ORB(
        id = "galaxy_orb",
        displayName = "Galaxy Orb",
        description = "Stardust vortex containing the essence of distant galaxies",
        coreColor = Color(0xFF93C5FD),
        outerGlow = Color(0xFF6366F1),
        trailColor = Color(0xFFA855F7),
        price = 100,
        unlockedByDefault = false
    ),
    FIREBALL(
        id = "fireball",
        displayName = "Fireball",
        description = "Superheated solar plasma ball with roaring ember sparks",
        coreColor = Color(0xFFFEF08A),
        outerGlow = Color(0xFFEA580C),
        trailColor = Color(0xFFF97316),
        price = 250,
        unlockedByDefault = false
    ),
    ICEBALL(
        id = "iceball",
        displayName = "Iceball",
        description = "Glacial frost sphere trailing sharp crystalline ice shards",
        coreColor = Color(0xFFE0F2FE),
        outerGlow = Color(0xFF38BDF8),
        trailColor = Color(0xFF0284C7),
        price = 500,
        unlockedByDefault = false
    ),
    ELECTRIC_ORB(
        id = "electric_orb",
        displayName = "Electric Orb",
        description = "High-voltage lightning capacitor crackling with electrical charge",
        coreColor = PureWhite,
        outerGlow = ElectricCyan,
        trailColor = ElectricCyan,
        price = 750,
        unlockedByDefault = false
    ),
    CRYSTAL_BALL(
        id = "crystal_ball",
        displayName = "Crystal Ball",
        description = "Prismatic diamond sphere splitting light into spectral rainbows",
        coreColor = Color(0xFFFDF4FF),
        outerGlow = NeonPink,
        trailColor = Color(0xFFE879F9),
        price = 1000,
        unlockedByDefault = false
    ),
    CYBER_PULSE(
        id = "cyber_pulse",
        displayName = "Cyber Pulse",
        description = "Encrypted cyber matrix data core with quantum glitch trail",
        coreColor = Color(0xFF6EE7B7),
        outerGlow = NeonEmerald,
        trailColor = NeonEmerald,
        price = 1500,
        unlockedByDefault = false
    ),
    GOLDEN_ENERGY(
        id = "golden_energy",
        displayName = "Golden Energy",
        description = "Legendary solid aurum orb radiant with victorious celestial rays",
        coreColor = Color(0xFFFEF9C3),
        outerGlow = NeonGold,
        trailColor = Color(0xFFF59E0B),
        price = 2500,
        unlockedByDefault = false
    )
}

enum class LandingRating(val displayName: String, val points: Int, val color: Color) {
    MISS("MISS", 0, Color(0xFFEF4444)),
    GOOD("GOOD!", 10, NeonPurple),
    PERFECT("PERFECT!", 25, ElectricCyan),
    EXCELLENT("EXCELLENT!", 50, NeonPink)
}

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val genre: String,
    val bpm: Int,
    val difficulty: String, // Easy, Medium, Hard, Expert
    val durationSec: Int,
    val description: String,
    val synthPatternId: Int
)

data class Platform3D(
    val id: Int,
    val x: Float, // horizontal center (-2.5f to 2.5f)
    val z: Float, // forward distance from player start
    val width: Float = 1.35f,
    val depth: Float = 0.9f,
    val color: Color = NeonPurple,
    val beatIndex: Int = 0,
    val hasCoin: Boolean = false,
    var coinCollected: Boolean = false,
    var landed: Boolean = false,
    var landingRating: LandingRating? = null,
    var landingRingAnim: Float = 0f // 0 to 1 for visual shockwave
)

data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var color: Color,
    var life: Float, // 1f down to 0f
    var maxLife: Float = 1f,
    var size: Float = 6f
)

data class TrailPoint3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val alpha: Float
)

data class DailyChallenge(
    val id: String,
    val title: String,
    val target: Int,
    var current: Int,
    val rewardCoins: Int,
    var claimed: Boolean = false
) {
    val isCompleted: Boolean get() = current >= target
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    var unlocked: Boolean = false
)
