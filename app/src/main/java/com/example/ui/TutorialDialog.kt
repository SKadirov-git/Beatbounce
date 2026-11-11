package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tut_anim")
    val handOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hand"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOverlay)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = CardSurface,
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(ElectricCyan, NeonPurple))
            ),
            modifier = Modifier.fillMaxWidth().testTag("tutorial_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Diagram of Ball & Drag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(DarkViolet, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height * 0.7f)

                        // Draw 3 floating platforms
                        for (i in -1..1) {
                            val px = c.x + i * 80f
                            drawRoundRect(
                                color = NeonPurple.copy(alpha = 0.6f),
                                topLeft = Offset(px - 30f, c.y),
                                size = Size(60f, 16f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }

                        // Ball following horizontal motion
                        val bx = c.x + handOffset
                        val by = c.y - 35f
                        drawCircle(
                            brush = Brush.radialGradient(listOf(PureWhite, ElectricCyan, NeonPurple), center = Offset(bx, by), radius = 18f),
                            radius = 16f,
                            center = Offset(bx, by)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = "Swipe gesture",
                        tint = ElectricCyan.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomCenter)
                            .offset(x = handOffset.dp, y = (-8).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TutorialStepItem(
                    number = "1",
                    title = "AUTOMATIC BOUNCING",
                    description = "The ball jumps automatically to the rhythm of the music."
                )

                Spacer(modifier = Modifier.height(10.dp))

                TutorialStepItem(
                    number = "2",
                    title = "DRAG TO STEER",
                    description = "Drag left or right on the screen to land on the next platform."
                )

                Spacer(modifier = Modifier.height(10.dp))

                TutorialStepItem(
                    number = "3",
                    title = "PERFECT & COMBOS",
                    description = "Land dead-center for PERFECT ratings and combo score multipliers!"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("tutorial_got_it_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text(
                        text = "GOT IT! LET'S BOUNCE",
                        color = DeepBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialStepItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = NeonPurple,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = SoftLavender,
                fontSize = 11.sp
            )
        }
    }
}
