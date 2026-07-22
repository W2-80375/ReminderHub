package com.own.remindme.presentation.add_reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.domain.model.label
import com.own.remindme.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var isVoiceMode by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    viewModel.onEvent(AddReminderEvent.AddAttachment(it.toString()))
                } catch (_: Exception) {
                    // Handle case where permission cannot be taken
                    viewModel.onEvent(AddReminderEvent.AddAttachment(it.toString()))
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let {
                    viewModel.onEvent(AddReminderEvent.AddAttachment(it.toString()))
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createImageUri(context)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddReminderViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddReminderViewModel.UiEvent.SaveReminder -> {
                    navController.navigateUp()
                }
                is AddReminderViewModel.UiEvent.DeleteReminder -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (viewModel.reminderTitle.value.isNotEmpty() && viewModel.reminderTime.value != System.currentTimeMillis()) 
                            "Edit Reminder" else "Add Reminder", 
                        color = DarkText
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = DarkText,
                    titleContentColor = DarkText
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (viewModel.reminderTitle.value.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(AddReminderEvent.DeleteReminder) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = { viewModel.onEvent(AddReminderEvent.SaveReminder) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = DarkText)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Entry Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(false to "Manual", true to "AI Speak").forEach { (mode, label) ->
                    val selected = isVoiceMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Primary else Color.Transparent)
                            .clickable { isVoiceMode = mode }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mode) Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            Text(
                                text = label,
                                color = if (selected) Color.White else DarkText.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isVoiceMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
//                AiVoiceAssistantView(
//                    onReminderCreated = {
//                        // Handle voice-created reminder
//                    }
//                )
            }

            AnimatedVisibility(
                visible = !isVoiceMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopicText("Title")
                    EditField(
                        value = viewModel.reminderTitle.value,
                        onValueChange = { viewModel.onEvent(AddReminderEvent.EnteredTitle(it)) },
                        placeholder = "Enter reminder title..."
                    )
                    
                    // ... (rest of the fields will be moved here)
                }
            }
        }
    }

    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = { Text("Add Attachment", color = DarkText) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Take Photo", color = DarkText) },
                        leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Primary) },
                        modifier = Modifier.clickable {
                            showAttachmentDialog = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val uri = createImageUri(context)
                                tempImageUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        headlineContent = { Text("Upload File", color = DarkText) },
                        leadingContent = { Icon(Icons.Default.AttachFile, contentDescription = null, tint = Primary) },
                        modifier = Modifier.clickable {
                            showAttachmentDialog = false
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Cancel", color = Primary)
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun TopicText(text: String) {
    Text(
        text = text,
        color = DarkText,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        placeholder = { Text(placeholder, color = DarkText.copy(alpha = 0.4f), fontSize = 14.sp) },
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        trailingIcon = trailingIcon,
        textStyle = TextStyle(fontSize = 14.sp, color = DarkText),
        colors = TextFieldDefaults.colors(
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            focusedContainerColor = DarkCard,
            unfocusedContainerColor = DarkCard,
            disabledContainerColor = DarkCard,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun createImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "Pictures")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "temp_image_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "com.own.remindme.fileprovider",
        file
    )
}
