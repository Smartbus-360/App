package com.smartbus360.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.smartbus360.app.data.model.response.StopX
import com.smartbus360.app.viewModels.ReachDateTimeViewModel
import org.koin.androidx.compose.getViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReachedStopagesScreen(
    navController: NavHostController,
    viewModel: ReachDateTimeViewModel = getViewModel(),
) {
    val state = viewModel.state.collectAsState()

    val today = LocalDate.now()
    val dateOptions = listOf(
        today.format(DateTimeFormatter.ISO_LOCAL_DATE),
        today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
        today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
    )

    var selectedDate by remember { mutableStateOf(dateOptions[0]) }

    // Extract all stops and group by tripType
    val filteredStops = state.value.data
        .filter { it.reachDate.startsWith(selectedDate) }
        .flatMap { it.stops } // Flatten stops from the main list
        .groupBy { it.tripType }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Date Dropdown
        Text(text = "Select Date", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuComponent(
            options = dateOptions,
            selectedOption = selectedDate,
            onOptionSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Categorized Stops
        if (filteredStops.isNotEmpty()) {
            filteredStops.forEach { (tripType, stops) ->
                Text(
                    text = (tripType ?: "UNKNOWN").uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Blue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                stops.forEach { stop ->
                    StopItem(stop)
                }
            }
        } else {
            Text(
                text = "No reached stoppages available",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

// Dropdown Menu Component
@Composable
fun DropdownMenuComponent(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = selectedOption,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Stop Item UI
@Composable
fun StopItem(stop: StopX) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Stop: ${stop.stopName}", fontWeight = FontWeight.Bold)
            Text(text = "Reach Time: ${stop.reachDateTime ?: "Not Reached"}")
            Text(text = "Landmark: ${stop.routeName ?: "N/A"}")
        }
    }
}
