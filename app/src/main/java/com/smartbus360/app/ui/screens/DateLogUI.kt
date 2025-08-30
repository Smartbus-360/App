package com.smartbus360.app.ui.screens


import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.StopX
import com.smartbus360.app.ui.theme.SmartBusPrimaryBlue
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.viewModels.ReachDateTimeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("StringFormatMatches")
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DateTimeLogScreen(  reachDateTimeViewModel: ReachDateTimeViewModel = getViewModel()) {
//    val reachDateTimeViewModel: ReachDateTimeViewModel = koinViewModel()
    val state = reachDateTimeViewModel.state.collectAsState()
    val context = LocalContext.current
    val tripTypes = listOf("morning", "afternoon", "evening")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    if (state.value.success == true){
    val dateOptions = state.value.data.map { it.reachDate }.distinct().sortedDescending()
    var selectedDate by remember { mutableStateOf(dateOptions.firstOrNull() ?: "") }
//    val logs = state.value.data.find { it.reachDate == selectedDate }?.stops ?: emptyList()
        val logs = state.value.data.find { it.reachDate == selectedDate }?.stops
            ?.sortedBy { it.stopOrder }
            ?: emptyList()


        Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
    ) {
//        Text(
//            text = "📅 Date-Time Log",
//            fontSize = 22.sp,
//            fontWeight = FontWeight.Bold,
//            color = SmartBusPrimaryBlue
//        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.scheduled_icon), // Replace with your actual drawable
                contentDescription = "Calendar Icon",
                tint = SmartBusPrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.date_time_log),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SmartBusPrimaryBlue
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // 📆 Dropdown for date selection
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text(stringResource(R.string.select_date)) },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SmartBusSecondaryBlue,
                    focusedLabelColor = SmartBusSecondaryBlue
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                dateOptions.forEach { date ->
                    DropdownMenuItem(
                        text = { Text(date) },
                        onClick = {
                            selectedDate = date
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔁 Swipeable TabRow synced with Pager
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tripTypes.forEachIndexed { index, trip ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = trip.replaceFirstChar { it.uppercase() },
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🌟 HorizontalPager with swipable trip shifts
        HorizontalPager(
            count = tripTypes.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val selectedTrip = tripTypes[page]
            val rounds = logs.filter { it.tripType == selectedTrip }
                .mapNotNull { it.round }
                .distinct()
                .sorted()


            var selectedRound by remember(selectedTrip, selectedDate) {
                mutableIntStateOf(rounds.firstOrNull() ?: 1)
            }

            val filteredStops = logs.filter {
                it.tripType == selectedTrip && it.round == selectedRound
            }
//            val filteredStops = logs.filter {
//                it.tripType == selectedTrip && it.round == selectedRound
//            }.sortedBy { it.stopOrder }


            Column(modifier = Modifier.fillMaxSize()) {
                Text(stringResource(R.string.rounds), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rounds) { round ->
                        val isSelected = selectedRound == round
                        FilterChip(text = context.getString(R.string.round, round), isSelected = isSelected) {
                            selectedRound = round
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (filteredStops.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_logs_found_for_selected_filters), color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredStops) { stop ->
                            StopCardModern(stop = stop)
                        }
                    }
                }
            }
        }
    }
}else if(state.value.success == false){
// 🛑 No logs found state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_logs_found), fontWeight = FontWeight.Medium, color = Color.Gray)
        }
    }

    else{
        // ⏳ Loading state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SmartBusPrimaryBlue)
        }
    }

}






@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) SmartBusPrimaryBlue else Color(0xFFE0E0E0),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 6.dp else 0.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StopCardModern(stop: StopX) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = "Stop Name", tint = SmartBusPrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stop.stopName.orEmpty(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartBusPrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(R.drawable.route_icon, contentDescription = "Route", tint = Color.DarkGray)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Route: ${stop.routeName}", fontSize = 14.sp, color = Color.DarkGray)
//            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.route_icon),
                    contentDescription = "Route",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp) // Optional: Set icon size
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Route: ${stop.routeName}", fontSize = 14.sp, color = Color.DarkGray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.reached_icon),
                    contentDescription = "Scheduled Time",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp) // Optional: Set icon size
                )
//                Icon(Icons.Default.DateRange, contentDescription = "Scheduled Time", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scheduled: ${formatTimeTo12Hour(stop.defaultArrivalTime?.substring(0, 5))}", color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.reached_icon),
                    contentDescription = "Reached Time",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp) // Optional: Set icon size
                )
//                Icon(Icons.Default.DateRange, contentDescription = "Reached Time", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reached: ${formatTimeTo12Hour(stop.reachDateTime?.substring(11, 16))}", color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    painter = painterResource(id = R.drawable.stop1_icon),
                    contentDescription = "Stop Order",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp) // Optional: Set icon size
                )

//                Icon(Icons.Default.DateRange, contentDescription = "Stop Order", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Order: ${stop.stopOrder}", color = Color.Gray)
            }

//            Row(verticalAlignment = Alignment.CenterVertically) {
//                val (statusIcon, statusText, statusColor) = when (stop.reached) {
//                    1 -> Triple(Icons.Default.CheckCircle, "Reached", Color(0xFF4CAF50))
////                    2 -> Triple(Icons.Default.Clear, "Missed", Color(0xFFF44336))
//                    else -> Triple(Icons.Default.Send, "Not Started", Color(0xFFF44336))
//                }
//
//                Icon(statusIcon, contentDescription = "Status", tint = statusColor)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Status: $statusText",
//                    fontWeight = FontWeight.SemiBold,
//                    color = statusColor
//                )
//            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val (statusIcon, statusText, statusColor) = when {
                    stop.reachDateTime.isNullOrEmpty() -> Triple(Icons.Default.Send, "Not Started", Color(0xFFF44336))
                    else -> Triple(Icons.Default.CheckCircle, "Reached", Color(0xFF4CAF50))
                }

                Icon(statusIcon, contentDescription = "Status", tint = statusColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Status: $statusText",
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }

        }
    }
}


fun formatTimeTo12Hour(time: String?): String {
    return try {
        val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        time?.let { formatter.format(parser.parse(it) ?: return "") } ?: ""
    } catch (e: Exception) {
        ""
    }
}



//@Composable
//fun StopCard(stop: StopLog) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = SmartBusTertiaryBlue),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("🛑 ${stop.stopName}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            Text("Route: ${stop.routeName}", fontSize = 14.sp)
//            Text("Default Arrival: ${stop.defaultArrivalTime?.substring(0, 5)}")
//            Text("Actual Reach: ${stop.reachDateTime?.substring(11, 16)}")
//            Text("Distance: ${stop.totalDistance} km")
//            Text("Status: ${if (stop.reached == 1) "✅ Reached" else "❌ Missed"}")
//        }
//    }
//}


//data class StopLog(
//    val logId: Int,
//    val round: Int,
//    val stopId: Int,
//    val reached: Int,
//    val latitude: Double,
//    val stopName: String,
//    val tripType: String,
//    val longitude: Double,
//    val routeName: String,
//    val stopOrder: Int,
//    val reachDateTime: String?,
//    val totalDistance: Int,
//    val defaultArrivalTime: String?,
//    val defaultDepartureTime: String?,
//)

data class ReachDateLogGroup(
    val reachDate: String,
    val stops: List<StopLog>
)

data class StopLog(
    val logId: Int,
    val round: Int,
    val stopId: Int,
    val reached: Int,
    val latitude: Double,
    val longitude: Double,
    val stopName: String,
    val tripType: String,
    val routeName: String,
    val stopOrder: Int,
    val reachDateTime: String?,
    val totalDistance: Int,
    val defaultArrivalTime: String?,
    val defaultDepartureTime: String?,
    val defaultAfternoonArrivalTime: String?,
    val defaultEveningArrivalTime: String?,
    val defaultAfternoonDepartureTime: String?,
    val defaultEveningDepartureTime: String?
)
