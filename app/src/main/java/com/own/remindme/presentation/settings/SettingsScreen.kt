package com.own.remindme.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
    
    var isRecordingMedicine by remember { mutableStateOf(false) }
    var isRecordingOther by remember { mutableStateOf(false) }
    var isPlayingMedicine by remember { mutableStateOf(false) }
    var isPlayingOther by remember { mutableStateOf(false) }
    
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
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

    fun togglePlaySound(path: String?, onStateChange: (Boolean) -> Unit) {
        if (path.isNullOrBlank()) return
        
        if (activeMediaPlayer?.isPlaying == true) {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
            activeMediaPlayer = null
            isPlayingMedicine = false
            isPlayingOther = false
            return
        }

        val file = File(path)
        if (!file.exists()) return

        try {
            activeMediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                onStateChange(true)
                setOnCompletionListener { 
                    it.release()
                    activeMediaPlayer = null
                    onStateChange(false)
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    activeMediaPlayer = null
                    onStateChange(false)
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onStateChange(false)
        }
    }

    // Release player on dispose
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
        if (!smsGranted || !callGranted) {
            // Permission denied
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd)))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSection(title = "Profile") {
                    SettingsEditableField(
                        value = preferences.userName,
                        onValueChange = viewModel::updateUserName,
                        title = "",
                        placeholder = "Enter your name"
                    )
                }

                SettingsSection(title = "Emergency Contact") {
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
                        title = "",
                        placeholder = "Enter phone number",
                        keyboardType = KeyboardType.Phone
                    )
                    Text(
                        "This number will be messaged and called if a high priority medicine is missed 3 times.",
                        color = DarkText.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                SettingsSection(title = "Custom Sounds") {
                    SoundRecorderRow(
                        label = "Medicine Reminder",
                        isRecording = isRecordingMedicine,
                        isPlaying = isPlayingMedicine,
                        hasSound = preferences.medicineSoundPath != null,
                        onRecordClick = {
                            if (isRecordingMedicine) {
                                stopRecording()
                                isRecordingMedicine = false
                            } else {
                                startRecording("medicine_sound") { path ->
                                    viewModel.updateMedicineSound(path)
                                    isRecordingMedicine = true
                                }
                            }
                        },
                        onPlayClick = { 
                            togglePlaySound(preferences.medicineSoundPath) { isPlayingMedicine = it }
                        },
                        onDeleteClick = { viewModel.updateMedicineSound(null) }
                    )

                    SoundRecorderRow(
                        label = "Other Reminders",
                        isRecording = isRecordingOther,
                        isPlaying = isPlayingOther,
                        hasSound = preferences.otherSoundPath != null,
                        onRecordClick = {
                            if (isRecordingOther) {
                                stopRecording()
                                isRecordingOther = false
                            } else {
                                startRecording("other_sound") { path ->
                                    viewModel.updateOtherSound(path)
                                    isRecordingOther = true
                                }
                            }
                        },
                        onPlayClick = { 
                            togglePlaySound(preferences.otherSoundPath) { isPlayingOther = it }
                        },
                        onDeleteClick = { viewModel.updateOtherSound(null) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = Background,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = DarkText.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            singleLine = true,
            textStyle = TextStyle(
                color = DarkText,
                fontSize = 17.sp
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
                            color = DarkText.copy(alpha = 0.35f),
                            fontSize = 17.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            color = DarkText.copy(alpha = 0.12f),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = DarkText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (isRecording) "Recording..." else if (isPlaying) "Playing..." else if (hasSound) "Sound Recorded" else "No custom sound",
                color = if (isRecording || isPlaying) Color.Red else DarkText.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            if (hasSound && !isRecording) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isPlaying) {
                                Modifier.background(Color.White.copy(alpha = 0.2f))
                            } else {
                                Modifier.background(Brush.linearGradient(GradientBlue))
                            }
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp).background(Brush.linearGradient(GradientRed), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            IconButton(
                onClick = onRecordClick,
                modifier = Modifier.size(36.dp).background(if (isRecording) Color.Red.copy(alpha = 0.2f) else Primary, CircleShape)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Record",
                    tint = if (isRecording) Color.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
