package com.smartbus360.app.ui.component

import android.os.Build
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.smartbus360.app.data.model.response.Notification
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

//@OptIn(ExperimentalPagerApi::class)
//@Composable
//fun NotificationPopup(
//    notifications: List<Notification>,
//    onDismiss: () -> Unit
//) {
//    Dialog(onDismissRequest = { /* Prevent dismiss without button */ }) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(12.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Notifications",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//
//                val pagerState = rememberPagerState()
//                HorizontalPager(
//                    state = pagerState,
//                    count = notifications.size,
//                    modifier = Modifier.height(200.dp)
//                ) { page ->
//                    val notification = notifications[page]
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = notification.message ?: "No message",
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = "Expiry: ${notification.expiryDate ?: "N/A"}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.Gray
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Button(
//                    onClick = { onDismiss() },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Seen")
//                }
//            }
//        }
//    }
//}





@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun NotificationPopup(
    notifications: List<Notification>,
    onNotificationSeen: (Notification) -> Unit,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState()
    val seenNotifications = remember { mutableStateListOf<String>() }
    val expandedMessages = remember { mutableStateMapOf<String, Boolean>() }

    Dialog(onDismissRequest = { /* Prevent dismiss without button */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    count = notifications.size,
                    modifier = Modifier.height(320.dp)
                ) { page ->
                    val notification = notifications[page]
                    val isExpanded = expandedMessages[notification.id.toString()] ?: false

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AndroidView(
                            factory = { context ->
                                TextView(context).apply {
                                    movementMethod = LinkMovementMethod.getInstance() // Enables clickable links
                                    setTextColor(android.graphics.Color.BLACK)
                                }
                            },
                            update = { textView ->
                                val rawHtml = notification.message ?: "No message"
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

                        if ((notification.message?.length ?: 0) > 100) {
                            TextButton(
                                onClick = { expandedMessages[notification.id.toString()] = !isExpanded }
                            ) {
                                Text(if (isExpanded) "Read Less" else "Read More")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Expiry: ${formatExpiryDate(notification.expiryDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!seenNotifications.contains(notification.id.toString())) {
                                    seenNotifications.add(notification.id.toString())
                                    onNotificationSeen(notification)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (seenNotifications.contains(notification.id.toString()))
                                    Color(0xFFB0B0B0) else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .alpha(if (seenNotifications.contains(notification.id.toString())) 0.6f else 1f)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = "Seen", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (seenNotifications.contains(notification.id.toString())) "Seen" else "Mark as Seen")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(notifications.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateDpAsState(if (isSelected) 16.dp else 6.dp, label = "dotWidth")
                        val dotHeight by animateDpAsState(if (isSelected) 6.dp else 6.dp, label = "dotHeight")

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = dotWidth, height = dotHeight)
                                .clip(RoundedCornerShape(50)) // Makes selected dot oval
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                        )
                    }
                }


                Text(
                    text = "Swipe left/right to view more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (seenNotifications.size == notifications.size) {
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}



// Function to format the date
fun formatExpiryDate(expiryDate: String?): String {
    if (expiryDate.isNullOrBlank()) return "N/A"

    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",     // Standard ISO 8601 format (UTC)
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // ISO format with milliseconds
        "yyyy-MM-dd HH:mm:ss",          // Common server datetime format
        "yyyy/MM/dd HH:mm:ss",          // Alternative format
    )

    for (format in formats) {
        try {
            val inputFormat = SimpleDateFormat(format, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC") // Convert from UTC if needed
            }
            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            val date = inputFormat.parse(expiryDate)
            if (date != null) return outputFormat.format(date)
        } catch (e: Exception) {
            // Ignore and try the next format
        }
    }

    return "N/A" // If all formats fail
}



// Notification Pager with Swiping Indicator
//                HorizontalPager(
//                    state = pagerState,
//                    count = notifications.size,
//                    modifier = Modifier.height(250.dp)
//                ) { page ->
//                    val notification = notifications[page]
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(12.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//
//
//                                AndroidView(
//                                    factory = { context ->
//                                        TextView(context).apply {
//                                            movementMethod = LinkMovementMethod.getInstance() // Enable clickable links
//                                        }
//                                    },
//                                    update = { textView ->
//                                        val rawHtml = notification.message ?: "No message"
//
//                                        // Step 1: Extract links and their colors
//                                        val colorLinkRegex = """<a\s+[^>]*href=["'](.*?)["'][^>]*style=["'][^"']*color:\s*([^;"']+)["'][^>]*>(.*?)</a>""".toRegex()
//                                        val matches = colorLinkRegex.findAll(rawHtml)
//
//                                        val colorMap = mutableMapOf<String, Int>()
//
//                                        for (match in matches) {
//                                            val url = match.groups[1]?.value ?: continue
//                                            val colorCode = match.groups[2]?.value ?: continue
//                                            val extractedColor = parseColor(colorCode)
//                                            colorMap[url] = extractedColor
//                                        }
//
//                                        // Step 2: Convert HTML to Spanned text
//                                        val spannedText: Spanned = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
//
//                                        // Step 3: Apply link colors
//                                        val spannable = SpannableString(spannedText)
//                                        val urlSpans = spannable.getSpans(0, spannable.length, android.text.style.URLSpan::class.java)
//
//                                        for (urlSpan in urlSpans) {
//                                            val start = spannable.getSpanStart(urlSpan)
//                                            val end = spannable.getSpanEnd(urlSpan)
//                                            val url = urlSpan.url
//                                            val color = colorMap[url] ?: Color.BLUE // Default to blue if not found
//
//                                            // Remove default URLSpan (to apply our custom one)
//                                            spannable.removeSpan(urlSpan)
//
//                                            // Apply custom clickable span with color
//                                            spannable.setSpan(object : ClickableSpan() {
//                                                override fun onClick(widget: View) {
//                                                    // Handle link click (open in browser)
//                                                    // Example: Open link using Intent (in Activity)
//                                                }
//                                            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                            // Apply extracted color
//                                            spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                                        }
//
//                                        textView.text = spannable
//                                        textView.setTextColor(Color.BLACK) // Default text color
//                                    },
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//
//                        /**
//                         * Parses a CSS color value (hex or rgb) into an Android color integer.
//                         */
//                        fun parseColor(color: String): Int {
//                            return when {
//                                color.startsWith("#") -> Color.parseColor(color) // Hex format
//                                color.startsWith("rgb") -> {
//                                    val rgbValues = color.substring(4, color.length - 1).split(",")
//                                    Color.rgb(rgbValues[0].trim().toInt(), rgbValues[1].trim().toInt(), rgbValues[2].trim().toInt()) // RGB format
//                                }
//                                else -> Color.BLUE // Default if parsing fails
//                            }
//                        }
//
//
//
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "Expiry: ${notification.expiryDate ?: "N/A"}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Mark individual notifications as Seen
//                        Button(
//                            onClick = {
//                                seenNotifications.add(notification.id.toString())
//                                onNotificationSeen(notification)
//                            },
//                            enabled = !seenNotifications.contains(notification.id.toString()),
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(if (seenNotifications.contains(notification.id.toString())) "Seen ✔" else "Mark as Seen")
//                        }
//                    }
//                }