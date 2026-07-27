package com.own.remindme.presentation.components.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.ui.theme.GradientPurple

@Composable
fun AnimatedFab(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D Press Depth Animation
    val depth by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "depth"
    )

    // Infinite Motion Transition
    val infiniteTransition = rememberInfiniteTransition(label = "3DFloatingMotion")
    
    // Vertical Bobbing (Hover)
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Horizontal Sway
    val swayOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Breathing Scale
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Floating 3D Tilt
    val tiltX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tiltX"
    )
    val tiltY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltY"
    )

    Box(
        modifier = Modifier
            .padding(bottom = 24.dp, end = 16.dp) 
            .graphicsLayer {
                translationY = bobbingOffset
                translationX = swayOffset
                scaleX = breathingScale
                scaleY = breathingScale
                rotationX = tiltX
                rotationY = tiltY
                cameraDistance = 19f * density
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // The "Thickness" / 3D Bottom Layer (Edge of the button)
        Box(
            modifier = Modifier
                .offset(y = 4.dp)
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF4338CA)) // Darker shade for depth effect
        )

        // The Main Button Layer
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset { IntOffset(0, (6.dp - depth).roundToPx()) }
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(colors = GradientPurple))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 4.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    tint = Color.White,
                    modifier = Modifier.size(35.dp)
                )
                
//                AnimatedVisibility(visible = expanded) {
//                    Row {
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "New Reminder",
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 16.sp
//                        )
//                    }
//                }
            }
        }
    }
}
