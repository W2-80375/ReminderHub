package com.own.remindme.presentation.add_reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.ui.theme.DarkBgEnd
import com.own.remindme.ui.theme.DarkBgStart
import com.own.remindme.ui.theme.DarkCard
import com.own.remindme.ui.theme.DarkText
import com.own.remindme.ui.theme.Primary
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Reminder", color = DarkText) },
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
                    IconButton(onClick = { viewModel.onEvent(AddReminderEvent.SaveReminder) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = DarkText)
                    }
                }
            )
        }
    ) { padding ->
        val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopicText("Title")
            EditField(
                value = viewModel.reminderTitle.value,
                onValueChange = { viewModel.onEvent(AddReminderEvent.EnteredTitle(it)) },
                placeholder = "Enter reminder title..."
            )

            Spacer(modifier = Modifier.height(20.dp))

            TopicText("Description")
            EditField(
                value = viewModel.reminderDescription.value,
                onValueChange = { viewModel.onEvent(AddReminderEvent.EnteredDescription(it)) },
                placeholder = "Add details (optional)",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            TopicText("Category")
            var categoryExpanded by remember { mutableStateOf(false) }
            Box {
                EditField(
                    value = viewModel.reminderCategory.value.name,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "Select Category",
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkText)
                        }
                    },
                    modifier = Modifier.clickable { categoryExpanded = true }
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name, color = DarkText) },
                            onClick = {
                                viewModel.onEvent(AddReminderEvent.ChangeCategory(category))
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (viewModel.reminderCategory.value == Category.MEDICINE) {
                TopicText("Expiry Date")
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()
                val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

                EditField(
                    value = viewModel.expiryDate.value?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "Select Expiry Date",
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = DarkText)
                        }
                    },
                    modifier = Modifier.clickable { showDatePicker = true }
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.onEvent(AddReminderEvent.ChangeExpiryDate(datePickerState.selectedDateMillis))
                                showDatePicker = false
                            }) {
                                Text("OK", color = Primary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel", color = DarkText.copy(alpha = 0.6f))
                            }
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = DarkCard
                        )
                    ) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                containerColor = DarkCard,
                                titleContentColor = DarkText,
                                headlineContentColor = DarkText,
                                weekdayContentColor = DarkText,
                                subheadContentColor = DarkText,
                                yearContentColor = DarkText,
                                currentYearContentColor = Primary,
                                selectedYearContentColor = Color.White,
                                selectedYearContainerColor = Primary,
                                dayContentColor = DarkText,
                                selectedDayContainerColor = Primary,
                                selectedDayContentColor = Color.White,
                                todayContentColor = Primary,
                                todayDateBorderColor = Primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            TopicText("Priority")
            var priorityExpanded by remember { mutableStateOf(false) }
            Box {
                EditField(
                    value = viewModel.reminderPriority.value.name,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "Select Priority",
                    trailingIcon = {
                        IconButton(onClick = { priorityExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkText)
                        }
                    },
                    modifier = Modifier.clickable { priorityExpanded = true }
                )
                DropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    Priority.entries.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.name, color = DarkText) },
                            onClick = {
                                viewModel.onEvent(AddReminderEvent.ChangePriority(priority))
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TopicText("Timing")
            var showTimePicker by remember { mutableStateOf(false) }
            val timePickerState = rememberTimePickerState(
                initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                initialMinute = Calendar.getInstance().get(Calendar.MINUTE)
            )

            EditField(
                value = timeFormatter.format(Date(viewModel.reminderTime.value)),
                onValueChange = {},
                readOnly = true,
                placeholder = "Set Time",
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Select Time", tint = DarkText)
                    }
                },
                modifier = Modifier.clickable { showTimePicker = true }
            )

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            calendar.set(Calendar.MINUTE, timePickerState.minute)
                            viewModel.onEvent(AddReminderEvent.ChangeTime(calendar.timeInMillis))
                            showTimePicker = false
                        }) {
                            Text("OK", color = Primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = DarkText.copy(alpha = 0.6f))
                        }
                    },
                    containerColor = DarkCard,
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = DarkBgStart,
                                    selectorColor = Primary,
                                    containerColor = DarkCard,
                                    periodSelectorSelectedContainerColor = Primary,
                                    periodSelectorUnselectedContainerColor = DarkBgStart,
                                    periodSelectorSelectedContentColor = Color.White,
                                    periodSelectorUnselectedContentColor = DarkText,
                                    timeSelectorSelectedContainerColor = Primary,
                                    timeSelectorUnselectedContainerColor = DarkBgStart,
                                    timeSelectorSelectedContentColor = Color.White,
                                    timeSelectorUnselectedContentColor = DarkText
                                )
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TopicText("Frequency")
            var frequencyExpanded by remember { mutableStateOf(false) }
            Box {
                EditField(
                    value = when (viewModel.reminderRepeatType.value) {
                        RepeatType.NONE -> "None"
                        RepeatType.DAILY -> "Daily"
                        RepeatType.ALTERNATE -> "Alternate"
                        RepeatType.WEEKLY -> "In a week"
                        RepeatType.MONTHLY -> "In a month"
                        RepeatType.THREE_MONTHS -> "In 3 months"
                        RepeatType.SIX_MONTHS -> "In 6 months"
                        RepeatType.YEARLY -> "In a year"
                        else -> viewModel.reminderRepeatType.value.name
                    },
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "Select Frequency",
                    trailingIcon = {
                        IconButton(onClick = { frequencyExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkText)
                        }
                    },
                    modifier = Modifier.clickable { frequencyExpanded = true }
                )
                DropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    val frequencies = listOf(
                        RepeatType.DAILY to "Daily",
                        RepeatType.ALTERNATE to "Alternate",
                        RepeatType.WEEKLY to "In a week",
                        RepeatType.MONTHLY to "In a month",
                        RepeatType.THREE_MONTHS to "In 3 months",
                        RepeatType.SIX_MONTHS to "In 6 months",
                        RepeatType.YEARLY to "In a year"
                    )
                    frequencies.forEach { (type, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = DarkText) },
                            onClick = {
                                viewModel.onEvent(AddReminderEvent.ChangeRepeatType(type))
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TopicText(text: String) {
    Text(
        text = text,
        color = DarkText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
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
            .background(DarkCard, RoundedCornerShape(12.dp)),
        placeholder = { Text(placeholder, color = DarkText.copy(alpha = 0.4f)) },
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
