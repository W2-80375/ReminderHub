package com.own.remindme.presentation.detail

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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    navController: NavController,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val reminder = viewModel.reminder.value

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = "Reminder Details", color = DarkText) },
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
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd)))
        )

        reminder?.let { rem ->
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
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
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                is AppIcon.Drawable -> {
                                    Icon(
                                        painter = painterResource(icon.resId),
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = rem.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DetailItem(
                    icon = Icons.Default.Description,
                    label = "Description",
                    value = rem.description.ifBlank { "No description provided" }
                )

                if (rem.imageUris.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkText.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(rem.imageUris) { uriString ->
                                val isImage = uriString.contains("image") ||
                                        uriString.endsWith(".jpg") ||
                                        uriString.endsWith(".png") ||
                                        uriString.contains("media/external/images")

                                Card(
                                    modifier = Modifier
                                        .size(if (isImage) 150.dp else 100.dp)
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
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCard)
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
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
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
                                                color = DarkText,
                                                fontSize = 10.sp,
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
                            label = "Time",
                            value = timeFormat.format(Date(rem.reminderTime))
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.Repeat,
                            label = "Repeat",
                            value = rem.repeatType.label
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = rem.category.name
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            icon = Icons.Default.PriorityHigh,
                            label = "Priority",
                            value = rem.priority.name
                        )
                    }
                }

                rem.expiryDate?.let {
                    DetailItem(
                        icon = Icons.Default.CalendarMonth,
                        label = "Expiry Date",
                        value = dateFormat.format(Date(it))
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

@Composable
fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkText.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
