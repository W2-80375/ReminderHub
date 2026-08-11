package com.own.remindme.presentation.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.label
import com.own.remindme.ui.theme.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import com.own.remindme.domain.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    navController: NavController,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val reminder = viewModel.reminder.value
    
    ReminderDetailContent(
        reminder = reminder,
        onBackClick = { navController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailContent(
    reminder: Reminder?,
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    val context = LocalContext.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    val cardColor = if (isDark) DarkCard else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) 
                    Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd))
                else
                    Brush.verticalGradient(colors = listOf(Color.White, Color.White))
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(text = "Reminder Details", color = onSurface) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = onSurface,
                            titleContentColor = onSurface
                        ),
                        navigationIcon = {
                            if (onBackClick != null) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            reminder?.let { rem ->
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = if (isDark) null else BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rem.category) {
                                        Category.MEDICINE -> MedicineColor
                                        Category.VEHICLE -> VehicleColor
                                        Category.BILL -> BillsColor
                                        Category.DOCUMENT -> DocumentColor
                                        else -> Primary
                                    }.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (rem.category) {
                                Category.MEDICINE -> AppIcons.Medication
                                Category.VEHICLE -> AppIcons.DirectionsCar
                                Category.BILL -> AppIcons.Payments
                                Category.DOCUMENT -> AppIcons.Description
                                else -> AppIcons.Calendar
                            }

                            val tint = when (rem.category) {
                                Category.MEDICINE -> MedicineColor
                                Category.VEHICLE -> VehicleColor
                                Category.BILL -> BillsColor
                                Category.DOCUMENT -> DocumentColor
                                else -> Primary
                            }

                            when (icon) {
                                is AppIcon.Vector -> {
                                    Icon(
                                        imageVector = icon.imageVector,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                is AppIcon.Drawable -> {
                                    Icon(
                                        painter = painterResource(icon.resId),
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = rem.title,
                            fontSize = 18.sp,
                            color = onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DetailItem(
                    icon = Icons.Default.Description,
                    label = "Description",
                    value = rem.description.ifBlank { "No description provided" },
                    cardColor = cardColor
                )

                DetailItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Start Date",
                    value = dateFormat.format(Date(rem.reminderTimes.minOrNull() ?: 0L)),
                    cardColor = cardColor
                )

                if (rem.imageUris.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(rem.imageUris) { uriString ->
                                val isImage = uriString.contains("image") ||
                                        uriString.endsWith(".jpg") ||
                                        uriString.endsWith(".png") ||
                                        uriString.contains("media/external/images")

                                Card(
                                    modifier = Modifier
                                        .size(if (isImage) 130.dp else 80.dp)
                                        .border(
                                            width = if (isDark) 0.dp else 1.dp,
                                            color = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(Uri.parse(uriString), if (isImage) "image/*" else "*/*")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    if (isImage) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uriString),
                                            contentDescription = "Reminder Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val fileName = remember(uriString) {
                                                try {
                                                    val uri = Uri.parse(uriString)
                                                    if (uri.scheme == "content") {
                                                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                                            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                                            cursor.moveToFirst()
                                                            cursor.getString(nameIndex)
                                                        }
                                                    } else {
                                                        uri.path?.substringAfterLast('/')
                                                    }
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            } ?: "File"
                                            Text(
                                                fileName,
                                                color = onSurface,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.Schedule,
                            label = if (rem.reminderTimes.size > 1) "Times" else "Time",
                            value = rem.reminderTimes.sorted().joinToString(", ") { timeFormat.format(Date(it)) },
                            cardColor = cardColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.Repeat,
                            label = "Repeat",
                            value = rem.repeatType.label,
                            cardColor = cardColor
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = rem.category.name,
                            cardColor = cardColor
                        )
                    }
                    if (rem.category == Category.MEDICINE) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            DetailItem(
                                icon = Icons.Default.PriorityHigh,
                                label = "Priority",
                                value = rem.priority.name,
                                cardColor = cardColor
                            )
                        }
                    }
                }

                rem.expiryDate?.let {
                    val isExpired = it < System.currentTimeMillis()
                    DetailItem(
                        icon = Icons.Default.CalendarMonth,
                        label = if (isExpired) "Expired On" else "Expiry Date",
                        value = dateFormat.format(Date(it)),
                        cardColor = cardColor,
                        valueColor = if (isExpired) Color.Red else onSurface
                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    }
}
}

@Composable
fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    cardColor: Color = DarkCard,
    valueColor: Color? = null
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (isDark) null else BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = valueColor ?: onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
