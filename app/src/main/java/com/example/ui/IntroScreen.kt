package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    onFinished: () -> Unit
) {
    var scene by remember { mutableIntStateOf(1) } // 1: Black/Particles, 2: City Reveal, 3: Creator Intro, 4: Logo Reveal
    val interactionSource = remember { MutableInteractionSource() }

    // Sequential cinematic timeline
    LaunchedEffect(Unit) {
        delay(1200) // Scene 1 -> 2
        scene = 2
        delay(1600) // Scene 2 -> 3
        scene = 3
        delay(2600) // Scene 3 -> 4
        scene = 4
        delay(2200) // Scene 4 -> Main Menu
        onFinished()
    }

    // Smooth animations
    val infiniteTransition = rememberInfiniteTransition(label = "intro")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitch"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Allow skipping immediately by tapping anywhere
                onFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        // Background Cyberpunk cityscape (reveals in scene >= 2)
        if (scene >= 2) {
            Image(
                painter = painterResource(id = R.drawable.bg_cyberpunk_city),
                contentDescription = "Cyberpunk City",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (scene == 2) 0.5f else 0.35f),
                contentScale = ContentScale.Crop
            )
        }

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepBlack.copy(alpha = 0.85f),
                            Color.Transparent,
                            DeepBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Scene 1: Initial neon glow
        if (scene == 1) {
            Text(
                text = "INITIALIZING CYBERSPACE...",
                color = ElectricCyan.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )
        }

        // Scene 2 & 3: CREATED BY SUBHON & SAYOD
        if (scene == 2 || scene == 3) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .scale(pulseScale)
            ) {
                Text(
                    text = "CREATED BY",
                    color = SoftLavender,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Futuristic Glitch text effect
                Box(contentAlignment = Alignment.Center) {
                    // Cyan glitch shadow
                    Text(
                        text = "SUBHON & SAYOD",
                        color = ElectricCyan.copy(alpha = 0.7f),
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(x = glitchOffset.dp)
                    )

                    // Pink glitch shadow
                    Text(
                        text = "SUBHON & SAYOD",
                        color = NeonPink.copy(alpha = 0.7f),
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(x = (-glitchOffset).dp)
                    )

                    // Main White & Neon Glow text
                    Text(
                        text = "SUBHON & SAYOD",
                        color = PureWhite,
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Electric Cyan accent line
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, ElectricCyan, NeonPurple, Color.Transparent)
                            )
                        )
                )
            }
        }

        // Scene 4: BEATBOUNCE Brand Reveal
        if (scene == 4) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .scale(pulseScale)
            ) {
                Text(
                    text = "BEATBOUNCE",
                    color = PureWhite,
                    fontSize = 44.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "LET THE MUSIC CONTROL THE UNIVERSE",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tap to skip prompt at bottom
        Text(
            text = "TAP ANYWHERE TO SKIP",
            color = MutedText.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}
