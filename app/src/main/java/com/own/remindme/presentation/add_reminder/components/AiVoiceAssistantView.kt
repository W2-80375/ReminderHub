package com.own.remindme.presentation.add_reminder.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.presentation.add_reminder.AddReminderViewModel.ConversationState
import com.own.remindme.ui.theme.Primary
import com.own.remindme.ui.theme.Success

@Composable
fun AiVoiceAssistantView(
    state: ConversationState,
    recognizedText: String,
    aiResponse: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val isListening = state == ConversationState.LISTENING
    val isProcessing = state == ConversationState.PROCESSING
    val isSpeaking = state == ConversationState.SPEAKING
    val isSuccess = state == ConversationState.SUCCESS

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Status Text
        Text(
            text = when (state) {
                ConversationState.IDLE -> "Tap the mic to start"
                ConversationState.LISTENING -> "I'm listening..."
                ConversationState.PROCESSING -> "Thinking..."
                ConversationState.SPEAKING -> "Speaking..."
                ConversationState.SUCCESS -> "Done! Reminder saved."
            },
            color = if (isSuccess) Success else onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            // Pulsing Background for Listening
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .background(Primary.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale * 0.8f)
                        .background(Primary.copy(alpha = 0.1f), CircleShape)
                )
            }

            // Central Icon/Button
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .scale(if (isSuccess) 1.1f else 1f),
                shape = CircleShape,
                color = when {
                    isSuccess -> Success
                    isListening -> Color.Red
                    isProcessing || isSpeaking -> Primary.copy(alpha = 0.1f)
                    else -> Primary
                },
                shadowElevation = if (isSuccess || isListening) 8.dp else 4.dp,
                onClick = {
                    if (isListening || isProcessing || isSpeaking) onStopListening() else onStartListening()
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isSuccess -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        isListening -> Icon(Icons.Default.Stop, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        isProcessing -> CircularProgressIndicator(color = Primary, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                        isSpeaking -> Icon(Icons.Default.Mic, null, tint = Primary, modifier = Modifier.size(40.dp)) // Visual hint of speaking
                        else -> Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }

        // Recognized Text Bubble
        AnimatedVisibility(
            visible = recognizedText.isNotEmpty() && !isSuccess,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "You said:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recognizedText,
                        color = onSurface.copy(alpha = 0.9f),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // AI Response Text
        AnimatedVisibility(
            visible = aiResponse.isNotEmpty(),
            enter = fadeIn()
        ) {
            Text(
                text = aiResponse,
                color = if (isSuccess) Success else onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 22.sp
            )
        }
    }
}
