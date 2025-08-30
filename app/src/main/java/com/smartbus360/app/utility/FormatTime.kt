package com.smartbus360.app.utility

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

//fun formatTo12HourTime(isoTime: String): String {
//    // Define the input and output formats
//    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
//
//    // Set the time zone for input as UTC
//    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//    // Set the output format time zone to IST
//    outputFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
//
//    // Parse the input time and format it to 12-hour clock
//    val date = inputFormat.parse(isoTime)
//    return outputFormat.format(date ?: "")
//}

fun formatTo12HourTime(isoTime: String?): String {
    if (isoTime.isNullOrBlank()) {
        return "--:-- --"
    }

    return try {
        // Define the input and output formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        // Set both input and output formats to the same IST time zone
        inputFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")

        // Parse the input ISO time
        val date = inputFormat.parse(isoTime)
        // Format the parsed date to 12-hour IST time
        outputFormat.format(date ?: return "--:-- --")
    } catch (e: Exception) {
        e.printStackTrace() // Log the error for debugging
        "--:-- --"
    }
}


fun formatStopName(stopName: String?): String {
    // Safeguard against null or blank input
    if (stopName.isNullOrBlank()) return ""

    return stopName.split(",").joinToString(",\n") { segment ->
        val words = segment.split(" ").filter { it.isNotBlank() }
        val lineBuilder = StringBuilder()
        var currentLine = StringBuilder()

        for (word in words) {
            // Check if adding this word exceeds the 12-character limit
            if (currentLine.length + word.length > 12) {
                // If so, add the current line to the line builder and start a new line with this word
                if (lineBuilder.isNotEmpty()) lineBuilder.append("\n")
                lineBuilder.append(currentLine.toString().trim())
                currentLine = StringBuilder(word)
            } else {
                // Otherwise, add the word to the current line
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        // Append the remaining words in the line
        if (currentLine.isNotEmpty()) {
            if (lineBuilder.isNotEmpty()) lineBuilder.append("\n")
            lineBuilder.append(currentLine.toString().trim())
        }

        // Return the segment with the applied formatting
        lineBuilder.toString()
    }
}
