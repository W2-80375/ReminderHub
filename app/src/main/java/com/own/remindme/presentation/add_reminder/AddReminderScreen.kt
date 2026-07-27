package com.own.remindme.presentation.add_reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.border
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.domain.model.label
import com.own.remindme.presentation.add_reminder.components.AiVoiceAssistantView
import com.own.remindme.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    val cardColor = if (isDark) DarkCard else Color.White

    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val convState by viewModel.convState.collectAsState()
    val aiRecognizedText by viewModel.aiRecognizedText.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()

    var isVoiceMode by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val uriStrings = uris.map { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) { }
                    uri.toString()
                }
                viewModel.onEvent(AddReminderEvent.AddAttachments(uriStrings))
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

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onEvent(AddReminderEvent.ToggleAiListening)
            } else {
                Toast.makeText(context, "Microphone permission is required for AI Speak", Toast.LENGTH_SHORT).show()
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
                        text = if (viewModel.isEditMode) "Edit Reminder" else "Add Reminder", 
                        color = onSurface
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = onSurface,
                    titleContentColor = onSurface
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
                    if (viewModel.isEditMode) {
                        IconButton(onClick = { viewModel.onEvent(AddReminderEvent.DeleteReminder) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = { viewModel.onEvent(AddReminderEvent.SaveReminder) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = onSurface)
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
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Entry Mode Toggle - Only shown when adding a new reminder
            if (!viewModel.isEditMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor.copy(alpha = if (isDark) 0.5f else 0.8f))
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
                                if (mode) Icon(
                                    Icons.Default.Mic,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (selected) Color.White else onSurface.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isVoiceMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AiVoiceAssistantView(
                    state = convState,
                    recognizedText = aiRecognizedText,
                    aiResponse = aiResponse,
                    onStartListening = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.onEvent(AddReminderEvent.ToggleAiListening)
                        } else {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopListening = {
                        viewModel.onEvent(AddReminderEvent.ToggleAiListening)
                    }
                )
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

                    TopicText("Description")
                    EditField(
                        value = viewModel.reminderDescription.value,
                        onValueChange = { viewModel.onEvent(AddReminderEvent.EnteredDescription(it)) },
                        placeholder = "Enter description...",
                        singleLine = false,
                        minLines = 3
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            TopicText("Date")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardColor)
                                    .border(
                                        width = if (isDark) 0.dp else 1.dp,
                                        color = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showDatePicker = true }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale).format(Date(viewModel.reminderTimes.value.firstOrNull() ?: System.currentTimeMillis())),
                                        color = onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    TopicText("Remind at")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.reminderTimes.value.forEachIndexed { index, time ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardColor)
                                        .border(
                                            width = if (isDark) 0.dp else 1.dp,
                                            color = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { editingTimeIndex = index }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, null, tint = Primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = SimpleDateFormat("hh:mm a", LocalLocale.current.platformLocale).format(Date(time)),
                                            color = onSurface,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                if (viewModel.reminderTimes.value.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.onEvent(AddReminderEvent.RemoveTime(index)) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(cardColor, RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(Icons.Default.Remove, null, tint = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                                if (index == viewModel.reminderTimes.value.size - 1) {
                                    IconButton(
                                        onClick = { viewModel.onEvent(AddReminderEvent.AddTime(System.currentTimeMillis())) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(cardColor, RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Primary)
                                    }
                                }
                            }
                        }
                    }

                    TopicText("Category")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(Category.entries) { category ->
                            val selected = viewModel.reminderCategory.value == category
                            val categoryColor = when (category) {
                                Category.MEDICINE -> MedicineColor
                                Category.VEHICLE -> VehicleColor
                                Category.BILL -> BillsColor
                                Category.DOCUMENT -> DocumentColor
                                Category.HEALTH -> HealthColor
                                Category.BIRTHDAY -> BirthdayColor
                                Category.SHOPPING -> ShoppingColor
                                else -> Primary
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onEvent(AddReminderEvent.ChangeCategory(category)) },
                                label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = categoryColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = cardColor,
                                    labelColor = onSurface.copy(alpha = 0.6f)
                                ),
                                border = if (isDark) null else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Color.Black.copy(alpha = 0.05f)
                                )
                            )
                        }
                    }

                    TopicText("Frequency")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(RepeatType.entries.filter { it != RepeatType.NONE && it != RepeatType.CUSTOM }) { repeatType ->
                            val selected = viewModel.reminderRepeatType.value == repeatType
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onEvent(AddReminderEvent.ChangeRepeatType(repeatType)) },
                                label = { Text(repeatType.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = cardColor,
                                    labelColor = onSurface.copy(alpha = 0.6f)
                                ),
                                border = if (isDark) null else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Color.Black.copy(alpha = 0.05f)
                                )
                            )
                        }
                    }

                    if (viewModel.reminderCategory.value == Category.MEDICINE) {
                        TopicText("Priority")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(Priority.entries) { priority ->
                                val selected = viewModel.reminderPriority.value == priority
                                val priorityColor = when (priority) {
                                    Priority.LOW -> Success
                                    Priority.MEDIUM -> Warning
                                    Priority.HIGH -> Color.Red
                                }
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.onEvent(AddReminderEvent.ChangePriority(priority)) },
                                    label = { Text(priority.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = priorityColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = cardColor,
                                        labelColor = onSurface.copy(alpha = 0.6f)
                                    ),
                                    border = if (isDark) null else FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = Color.Black.copy(alpha = 0.05f)
                                    )
                                )
                            }
                        }
                    }

                    if (viewModel.reminderCategory.value == Category.MEDICINE) {
                        TopicText("Expiry Date")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardColor)
                                .border(
                                    width = if (isDark) 0.dp else 1.dp,
                                    color = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showExpiryDatePicker = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventBusy, null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = viewModel.expiryDate.value?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Set expiry date (Optional)",
                                    color = if (viewModel.expiryDate.value != null) onSurface else onSurface.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                                if (viewModel.expiryDate.value != null) {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { viewModel.onEvent(AddReminderEvent.ChangeExpiryDate(null)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = onSurface.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    TopicText("Attachments")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardColor)
                                    .border(
                                        width = if (isDark) 0.dp else 1.dp,
                                        color = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showAttachmentDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, null, tint = Primary)
                                    Text("Add", color = Primary, fontSize = 12.sp)
                                }
                            }
                        }
                        items(viewModel.attachmentUris.value) { uri ->
                            Box(modifier = Modifier.size(80.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.onEvent(AddReminderEvent.RemoveAttachment(uri)) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = { Text("Add Attachment", color = onSurface) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Take Photo", color = onSurface) },
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
                        headlineContent = { Text("Upload File", color = onSurface) },
                        supportingContent = { Text("Photo, Video, Audio, or Document", fontSize = 11.sp) },
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
            containerColor = cardColor
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.reminderTimes.value.firstOrNull() ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.reminderTimes.value.forEachIndexed { index, time ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = time
                                val hour = get(Calendar.HOUR_OF_DAY)
                                val minute = get(Calendar.MINUTE)
                                timeInMillis = millis
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                            }
                            viewModel.onEvent(AddReminderEvent.UpdateTime(index, calendar.timeInMillis))
                        }
                    }
                    showDatePicker = false
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Primary) }
            },
            colors = DatePickerDefaults.colors(containerColor = cardColor)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = cardColor,
                    titleContentColor = onSurface,
                    headlineContentColor = onSurface,
                    selectedDayContainerColor = Primary,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }

    editingTimeIndex?.let { index ->
        val currentTime = viewModel.reminderTimes.value[index]
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { editingTimeIndex = null },
            confirmButton = {
                TextButton(onClick = {
                    val newCalendar = Calendar.getInstance().apply {
                        timeInMillis = currentTime
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    viewModel.onEvent(AddReminderEvent.UpdateTime(index, newCalendar.timeInMillis))
                    editingTimeIndex = null
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { editingTimeIndex = null }) { Text("Cancel", color = Primary) }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = if (isDark) DarkBgEnd else Background,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = onSurface,
                        selectorColor = Primary,
                        periodSelectorSelectedContainerColor = Primary,
                        periodSelectorUnselectedContainerColor = cardColor,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = onSurface,
                        timeSelectorSelectedContainerColor = Primary,
                        timeSelectorUnselectedContainerColor = cardColor,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = onSurface
                    )
                )
            },
            containerColor = cardColor
        )
    }

    if (showExpiryDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.expiryDate.value ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onEvent(AddReminderEvent.ChangeExpiryDate(millis))
                    }
                    showExpiryDatePicker = false
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryDatePicker = false }) { Text("Cancel", color = Primary) }
            },
            colors = DatePickerDefaults.colors(containerColor = cardColor)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = cardColor,
                    titleContentColor = onSurface,
                    headlineContentColor = onSurface,
                    selectedDayContainerColor = Primary,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }
}

@Composable
fun TopicText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
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
    val isDark = isSystemInDarkTheme()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val cardColor = if (isDark) DarkCard else Color(0xFFF1F3F4)

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        placeholder = { Text(placeholder, color = onSurface.copy(alpha = 0.4f), fontSize = 14.sp) },
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        trailingIcon = trailingIcon,
        textStyle = TextStyle(fontSize = 14.sp, color = onSurface),
        colors = TextFieldDefaults.colors(
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface,
            focusedContainerColor = cardColor,
            unfocusedContainerColor = cardColor,
            disabledContainerColor = cardColor,
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
