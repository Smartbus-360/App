package com.smartbus360.app.ui.screens

import android.graphics.Typeface
import android.text.Spanned
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.viewModels.NotificationViewModel
import com.smartbus360.app.viewModels.StudentScreenViewModel
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.smartbus360.app.ui.theme.SmartBusPrimaryBlue
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue

//@Composable
//fun NotificationItem(message: String, instituteType: String, createdAt: String) {
//    val timeAgo = remember(createdAt) { getTimeAgo(createdAt) }
//
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
////            Text(
////                text = message,
////                fontSize = 18.sp,
////                fontWeight = FontWeight.Bold,
////                color = Color(0xFF1E3A8A)
////            )
//
//            AndroidView(
//                factory = { context ->
//                    TextView(context).apply {
//                        textSize = 18f
//                        setTypeface(null, Typeface.BOLD)
////                        setTextColor(Color.parseColor("#1E3A8A")) // ✅ Correct
//
//                    }
//                },
//                update = { textView ->
//                    textView.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
//                }
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
////            Text(
////                text = "Institute: $instituteType",
////                fontSize = 14.sp,
////                color = Color(0xFF64748B)
////            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = timeAgo,
//                fontSize = 12.sp,
//                color = Color.Gray
//            )
//        }
//    }
//}
//
fun getTimeAgo(createdAt: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        val date = format.parse(createdAt)
        if (date != null) {
            DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
        } else {
            "Unknown time"
        }
    } catch (e: Exception) {
        "Unknown time"
    }
}
//
//
//
//@Composable
//fun NotificationList(
//    navController: NavHostController,
//    notificationViewModel: NotificationViewModel = getViewModel(),
//    studentScreenViewModel: StudentScreenViewModel = getViewModel()
//) {
//    val state = notificationViewModel.state.collectAsState()
//    val state1 = studentScreenViewModel.state.collectAsState()
//
//    val filteredNotifications = state.value.notifications.filter { notification ->
//        notification.instituteType == state1.value.user?.institute_type
//    }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp))
//            {
//                if (state.value.success == true) {
//                    if (filteredNotifications.isNotEmpty()) {
//                        item {
//                            Text(
//                                text = "Notifications",
//                                fontSize = 21.sp,
//                                fontFamily = InterMedium,
//                                fontWeight = FontWeight.Medium,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//                        }
//                    }
//
//                    items(filteredNotifications) { notification ->
//                        NotificationItem(
//                            message = notification.message?: "",
//                            instituteType = notification.instituteType?:"",
//                            createdAt = notification.createdAt?:""
//                        )
//                    }
//
//                    if (filteredNotifications.isEmpty()) {
//                        item {
//                            Text(
//                                text = "Notifications",
//                                fontSize = 21.sp,
//                                fontFamily = InterMedium,
//                                fontWeight = FontWeight.Medium,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//                            Text(
//                                text = "No notifications available",
//                                fontSize = 16.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(top = 16.dp)
//                            )
//                        }
//                    }
//                }
//            }
//}

   //latest below

//@Composable
//fun NotificationItem(message: String, createdAt: String, mandatory: Int?) {
//    val timeAgo = remember(createdAt) { getTimeAgo(createdAt) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp, horizontal = 8.dp)
//    ) {
//        Card(
//            shape = RoundedCornerShape(12.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                AndroidView(
//                    factory = { context ->
//                        TextView(context).apply {
//                            textSize = 18f
//                            setTypeface(null, Typeface.BOLD)
//                        }
//                    },
//                    update = { textView ->
//                        textView.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
//                    }
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = timeAgo,
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//            }
//        }
//
//        // ⭐ Marker in Bottom-Right Corner
//        if (mandatory == 1) {
//            Text(
//                text = "⭐ Important",
//                color = Color.Red,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(8.dp)
//            )
//        }
//    }
//}
//
//
//@Composable
//fun NotificationList(
//    navController: NavHostController,
//    notificationViewModel: NotificationViewModel = getViewModel(),
//    studentScreenViewModel: StudentScreenViewModel = getViewModel()
//) {
//    val state = notificationViewModel.state.collectAsState()
//    val stateBusNoti = notificationViewModel.stateBusNoti.collectAsState()
//    val state1 = studentScreenViewModel.state.collectAsState()
//
//    val filteredNotifications = state.value.notifications.filter { notification ->
//        notification.instituteType == state1.value.user?.institute_type
//    }
//
//    val filteredBusNotifications = stateBusNoti.value.notifications.filter { notification ->
//        notification.busId == state1.value.user?.busId
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Sticky Header
//        Text(
//            text = "Notifications",
//            fontSize = 21.sp,
//            fontFamily = InterMedium,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        if (state.value.success == true || stateBusNoti.value.success == true) {
//            if (filteredNotifications.isNotEmpty() || filteredBusNotifications.isNotEmpty()) {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(filteredNotifications) { notification ->
//                        NotificationItem(
//                            message = notification.message ?: "",
//                            createdAt = notification.createdAt ?: "",
//                            mandatory = notification.isMandatory ?: 0
//                        )
//                    }
//                    items(filteredBusNotifications) { notification ->
//                        NotificationItem(
//                            message = notification.message ?: "",
//                            createdAt = notification.createdAt ?: "",
//                            mandatory = notification.isMandatory ?: 0
//                        )
//                    }
//
//                }
//            } else {
//                Text(
//                    text = "No notifications available",
//                    fontSize = 16.sp,
//                    color = Color.Gray,
//                    modifier = Modifier.padding(top = 16.dp)
//                )
//            }
//        }
//    }
//}



@Composable
fun NotificationItem(message: String, createdAt: String, mandatory: Int?) {
    val timeAgo = remember(createdAt) { getTimeAgo(createdAt) }
    var isExpanded by remember { mutableStateOf(false) }

    // Convert HTML string to Spannable format
    val annotatedMessage = message

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)), // Light background
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Notification Message
//                Text(
//                    text = annotatedMessage,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF333366), // SmartBus360 primary color
//                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
//                    overflow = TextOverflow.Ellipsis
//                )
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            movementMethod = LinkMovementMethod.getInstance() // Enables clickable links
                            setTextColor(android.graphics.Color.BLACK)
                        }
                    },
                    update = { textView ->
                        val rawHtml = annotatedMessage ?: "No message"
                        val spannedText: Spanned = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

                        val shortText = if (spannedText.length > 100 && !isExpanded) {
                            spannedText.subSequence(0, 100).toString() + "..."
                        } else {
                            spannedText
                        }

                        textView.text = shortText
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Read More / Read Less
                if (annotatedMessage.length > 100) {
                    Text(
                        text = if (isExpanded) "Read Less" else "Read More",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SmartBusSecondaryBlue,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Label
                Text(
                    text = timeAgo,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // "Important" Badge with Floating Effect
        if (mandatory == 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SmartBusSecondaryBlue) // Blue badge for importance
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Important",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Important",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}



@Composable
fun NotificationList(
    navController: NavHostController,
    notificationViewModel: NotificationViewModel = getViewModel(),
    studentScreenViewModel: StudentScreenViewModel = getViewModel()
) {
    val state = notificationViewModel.state.collectAsState()
    val stateBusNoti = notificationViewModel.stateBusNoti.collectAsState()
    val state1 = studentScreenViewModel.state.collectAsState()

    val filteredNotifications = state.value.notifications.filter {
        it.instituteType == state1.value.user?.institute_type
    }

    val filteredBusNotifications = stateBusNoti.value.notifications.filter {
        it.busId == state1.value.user?.busId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Title with better spacing
        Text(
            text = "Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (state.value.success == true || stateBusNoti.value.success == true) {
            if (filteredNotifications.isNotEmpty() || filteredBusNotifications.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationItem(
                            message = notification.message ?: "",
                            createdAt = notification.createdAt ?: "",
                            mandatory = notification.isMandatory ?: 0
                        )
                    }
                    items(filteredBusNotifications) { notification ->
                        NotificationItem(
                            message = notification.message ?: "",
                            createdAt = notification.createdAt ?: "",
                            mandatory = notification.isMandatory ?: 0
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications available",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun ImportantTag() {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = "Important",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Important",
                tint = MaterialTheme.colorScheme.error
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.padding(start = 8.dp)
    )
}

//@Composable
//fun NotificationList(
//    navController: NavHostController,
//    notificationViewModel: NotificationViewModel = getViewModel(),
//    studentScreenViewModel: StudentScreenViewModel = getViewModel()
//) {
//    val state = notificationViewModel.state.collectAsState()
//    val stateBusNoti = notificationViewModel.stateBusNoti.collectAsState()
//    val state1 = studentScreenViewModel.state.collectAsState()
//
//    val filteredNotifications = state.value.notifications.filter {
//        it.instituteType == state1.value.user?.institute_type
//    }
//
//    val filteredBusNotifications = stateBusNoti.value.notifications.filter {
//        it.busId == state1.value.user?.busId
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // 🔹 Sticky Header
//        Text(
//            text = "Notifications",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        if (state.value.success == true || stateBusNoti.value.success == true) {
//            if (filteredNotifications.isNotEmpty() || filteredBusNotifications.isNotEmpty()) {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(filteredNotifications) { notification ->
//                        NotificationItem(
//                            message = notification.message ?: "",
//                            createdAt = notification.createdAt ?: "",
//                            mandatory = notification.isMandatory ?: 0
//                        )
//                    }
//                    items(filteredBusNotifications) { notification ->
//                        NotificationItem(
//                            message = notification.message ?: "",
//                            createdAt = notification.createdAt ?: "",
//                            mandatory = notification.isMandatory ?: 0
//                        )
//                    }
//                }
//            } else {
//                EmptyStateMessage("No notifications available")
//            }
//        }
//    }
//}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}




//data class Notification(
//    val message: String,
//    val instituteType: String,
//    val createdAt: String
//)
