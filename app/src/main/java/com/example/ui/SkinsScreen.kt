package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AppScreen
import com.example.game.BallSkin
import com.example.game.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinsScreen(
    viewModel: GameViewModel
) {
    val selectedSkin by viewModel.selectedSkin.collectAsState()
    val coins by viewModel.coins.collectAsState()

    var previewSkin by remember { mutableStateOf(selectedSkin) }

    val infiniteTransition = rememberInfiniteTransition(label = "skin_rot")
    val rot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "r"
    )

    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BALL CUSTOMIZATION",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("skins_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    Surface(
                        color = CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = NeonGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$coins",
                                color = NeonGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Interactive 3D Sphere Preview Showcase
            item {
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(previewSkin.outerGlow, previewSkin.coreColor))
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(140.dp).clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                val r = size.width * 0.35f

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            previewSkin.outerGlow.copy(alpha = 0.6f),
                                            previewSkin.outerGlow.copy(alpha = 0.15f),
                                            Color.Transparent
                                        ),
                                        center = c,
                                        radius = r * 1.5f
                                    ),
                                    radius = r * 1.5f,
                                    center = c
                                )

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(PureWhite, previewSkin.coreColor, previewSkin.outerGlow),
                                        center = Offset(c.x - r * 0.35f, c.y - r * 0.35f),
                                        radius = r * 1.1f
                                    ),
                                    radius = r,
                                    center = c
                                )

                                val ringR = r * 1.35f
                                drawOval(
                                    color = previewSkin.trailColor.copy(alpha = 0.7f),
                                    topLeft = Offset(c.x - ringR, c.y - ringR * 0.3f),
                                    size = Size(ringR * 2, ringR * 0.6f),
                                    style = Stroke(width = 2.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = previewSkin.displayName,
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = previewSkin.description,
                            color = SoftLavender,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            items(BallSkin.entries) { skin ->
                val isUnlocked = viewModel.saveManager.isSkinUnlocked(skin)
                val isEquipped = selectedSkin == skin

                Surface(
                    onClick = { previewSkin = skin },
                    color = CardSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isEquipped) listOf(skin.outerGlow, PureWhite) else listOf(skin.outerGlow.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("skin_item_${skin.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(46.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val c = Offset(size.width / 2f, size.height / 2f)
                                    val r = size.width * 0.4f
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(PureWhite, skin.coreColor, skin.outerGlow),
                                            center = Offset(c.x - r * 0.3f, c.y - r * 0.3f),
                                            radius = r * 1.1f
                                        ),
                                        radius = r,
                                        center = c
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = skin.displayName,
                                    color = PureWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isUnlocked) "UNLOCKED" else "${skin.price} Coins",
                                    color = if (isUnlocked) NeonEmerald else NeonGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isEquipped) {
                            Surface(
                                color = NeonPurple,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Equipped",
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "EQUIPPED",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (isUnlocked) {
                            OutlinedButton(
                                onClick = { viewModel.selectSkin(skin) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, NeonPurple)))
                            ) {
                                Text(
                                    text = "EQUIP",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val canAfford = coins >= skin.price
                            Button(
                                onClick = { viewModel.selectSkin(skin) },
                                enabled = canAfford,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGold,
                                    disabledContainerColor = Color(0xFF332A15)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = if (canAfford) DeepBlack else MutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UNLOCK",
                                    color = if (canAfford) DeepBlack else MutedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
