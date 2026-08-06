package com.own.remindme.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.own.remindme.domain.model.AppTheme
import com.own.remindme.domain.model.Category
import com.own.remindme.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val preferences by viewModel.userPreferences.collectAsState()
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    
    var recordingCategory by remember { mutableStateOf<String?>(null) }
    var playingPath by remember { mutableStateOf<String?>(null) }
    
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    var expandedSection by remember { mutableStateOf<String?>(null) }
    
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    fun startRecording(fileName: String, onStart: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        val file = File(context.filesDir, "$fileName.m4a")
        recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        onStart(file.absolutePath)
    }

    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) { e.printStackTrace() }
            release()
        }
        recorder = null
    }

    fun togglePlaySound(path: String?) {
        if (path.isNullOrBlank()) return
        
        if (playingPath == path) {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
            activeMediaPlayer = null
            playingPath = null
            return
        }

        activeMediaPlayer?.stop()
        activeMediaPlayer?.release()
        activeMediaPlayer = null
        playingPath = null

        val file = File(path)
        if (!file.exists()) return

        try {
            activeMediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                playingPath = path
                setOnCompletionListener { 
                    it.release()
                    if (playingPath == path) {
                        playingPath = null
                    }
                    activeMediaPlayer = null
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (playingPath == path) {
                        playingPath = null
                    }
                    activeMediaPlayer = null
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playingPath = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeMediaPlayer?.release()
            activeMediaPlayer = null
        }
    }

    val emergencyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        if (!smsGranted || !callGranted) { }
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = onSurface,
                    titleContentColor = onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) 
                        Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd))
                    else
                        Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SettingItemCard(
                        title = "Profile",
                        subtitle = preferences.userName.ifBlank { "Not set" },
                        icon = Icons.Default.Person,
                        isExpanded = expandedSection == "Profile",
                        onClick = { expandedSection = if (expandedSection == "Profile") null else "Profile" }
                    ) {
                        SettingsEditableField(
                            value = preferences.userName,
                            onValueChange = viewModel::updateUserName,
                            title = "Your Name",
                            placeholder = "Enter your name"
                        )
                    }
                }

                item {
                    SettingItemCard(
                        title = "Emergency Contact",
                        subtitle = preferences.emergencyContact.ifBlank { "Not set" },
                        icon = Icons.Default.Call,
                        isExpanded = expandedSection == "Emergency",
                        onClick = { expandedSection = if (expandedSection == "Emergency") null else "Emergency" }
                    ) {
                        SettingsEditableField(
                            value = preferences.emergencyContact,
                            onValueChange = {
                                viewModel.updateEmergencyContact(it)
                                if (it.isNotBlank()) {
                                    emergencyPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE)
                                    )
                                }
                            },
                            title = "Phone Number",
                            placeholder = "Enter phone number",
                            keyboardType = KeyboardType.Phone
                        )
                        Text(
                            "This number will be messaged and called if a high priority medicine is missed 3 times.",
                            color = onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    SettingItemCard(
                        title = "Custom Sounds",
                        subtitle = if (preferences.categorySounds.isNotEmpty()) "Configured" else "Default",
                        icon = Icons.Default.MusicNote,
                        isExpanded = expandedSection == "Sounds",
                        onClick = { expandedSection = if (expandedSection == "Sounds") null else "Sounds" }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val soundItems = listOf(
                                "DEFAULT" to "General (Fallback)",
                                "MEDICINE_EXPIRY" to "Medicine Expiry"
                            ) + Category.entries.map { it.name to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                            
                            soundItems.forEach { (catId, label) ->
                                val soundPath = preferences.categorySounds[catId]
                                SoundRecorderRow(
                                    label = label,
                                    isRecording = recordingCategory == catId,
                                    isPlaying = soundPath != null && playingPath == soundPath,
                                    hasSound = soundPath != null,
                                    onRecordClick = {
                                        if (recordingCategory == catId) {
                                            stopRecording()
                                            recordingCategory = null
                                        } else {
                                            startRecording("sound_$catId") { path ->
                                                viewModel.updateCategorySound(catId, path)
                                                recordingCategory = catId
                                            }
                                        }
                                    },
                                    onPlayClick = {
                                        togglePlaySound(soundPath)
                                    },
                                    onDeleteClick = { viewModel.updateCategorySound(catId, null) }
                                )
                            }
                        }
                    }
                }

                item {
                    SettingItemCard(
                        title = "App Theme",
                        subtitle = when (preferences.appTheme) {
                            AppTheme.SYSTEM -> "System Default"
                            AppTheme.LIGHT -> "Light"
                            AppTheme.DARK -> "Dark"
                        },
                        icon = Icons.Default.Palette,
                        isExpanded = expandedSection == "Theme",
                        onClick = { expandedSection = if (expandedSection == "Theme") null else "Theme" }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AppTheme.entries.forEach { theme ->
                                val isSelected = preferences.appTheme == theme
                                val label = when (theme) {
                                    AppTheme.SYSTEM -> "System"
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.DARK -> "Dark"
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.updateAppTheme(theme) }
                                        .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (theme) {
                                            AppTheme.SYSTEM -> Icons.Default.SettingsSuggest
                                            AppTheme.LIGHT -> Icons.Default.LightMode
                                            AppTheme.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = label,
                                        tint = if (isSelected) Primary else onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = label,
                                        color = if (isSelected) Primary else onSurface.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SettingItemCard(
                        title = "App Features",
                        subtitle = "Everything ReminderHub can do",
                        icon = Icons.Default.Info,
                        isExpanded = expandedSection == "Features",
                        onClick = { expandedSection = if (expandedSection == "Features") null else "Features" }
                    ) {
                        val features = listOf(
                            "AI Voice Assistant" to "Create reminders naturally by speaking to the app.",
                            "Smart Categorization" to "Automatically organizes reminders into categories like Medicine, Vehicle, etc.",
                            "Custom Voice Alerts" to "Record your own voice or sounds for personalized reminder notifications.",
                            "Medicine Expiry Tracking" to "Proactive alerts before your medications expire.",
                            "Emergency Safety Net" to "Automatic SMS and calls to contact if high-priority medicine is missed.",
                            "Smart Recurrence" to "Support for Daily, Weekly, Monthly, and Yearly repeating schedules.",
                            "Attachments Support" to "Attach photos, videos, audio recordings, or documents directly to your reminders.",
                            "Adaptive Themes" to "Light, Dark, and System Default themes."
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            features.forEach { (title, desc) ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(onSurface.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(title, color = onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(desc, color = onSurface.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    val cardColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                shadowElevation = 2f
                shape = RoundedCornerShape(12.dp)
                clip = true
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = if (isExpanded) BorderStroke(0.6.dp, Brush.linearGradient(GradientPurple)) else null
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .then(
                            if (isExpanded) {
                                Modifier.background(Brush.linearGradient(GradientPurple))
                            } else {
                                Modifier.background(onSurface.copy(alpha = 0.05f))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isExpanded) Color.White else onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    if (!isExpanded) {
                        Text(
                            text = subtitle,
                            color = onSurface.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun SettingsEditableField(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by rememberSaveable(value) { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            singleLine = true,
            textStyle = TextStyle(
                color = onSurface,
                fontSize = 15.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (text.isEmpty()) {
                        Text(
                            placeholder,
                            color = onSurface.copy(alpha = 0.35f),
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            color = onSurface.copy(alpha = 0.12f),
            thickness = 1.dp
        )
    }
}

@Composable
fun SoundRecorderRow(
    label: String,
    isRecording: Boolean,
    isPlaying: Boolean,
    hasSound: Boolean,
    onRecordClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (isRecording) "Recording..." else if (isPlaying) "Playing..." else if (hasSound) "Sound Recorded" else "No custom sound",
                color = if (isRecording || isPlaying) Color.Red else onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasSound && !isRecording) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .then(
                            if (isPlaying) Modifier.background(Color.Transparent)
                            else Modifier.background(Brush.linearGradient(GradientBlue))
                        )
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = if (isDark) Color.White else Primary,
                            strokeWidth = 1.5.dp
                        )
                    }
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "playPause"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = if (playing) (if (isDark) Color.White else Primary) else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradientRed))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.Red.copy(alpha = 0.2f) else Primary)
                        .clickable { onRecordClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = if (isRecording) Color.Red else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
