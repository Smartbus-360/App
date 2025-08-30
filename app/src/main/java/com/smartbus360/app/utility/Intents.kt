package com.smartbus360.app.utility

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast


fun bugReportEmail(context: Context, name: String, email: String, phoneNumber: String) {
    // Gather device details
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT

    // Compose the email body
    val emailBody = """
        Here are my details:
        
        Name: $name
        Email: $email
        Phone: $phoneNumber

        
        Device Details:
        Manufacturer: $manufacturer
        Model: $model
        Android Version: $androidVersion
        SDK Version: $sdkVersion
        
        Issue I'm facing:
        Please describe your issue here, including any steps to reproduce it, or specific error messages encountered.
    """.trimIndent()

    // First, try to open Gmail directly
    val gmailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822" // Restricts to email apps
        putExtra(Intent.EXTRA_EMAIL, arrayOf("Support@smartbus360"))
        putExtra(Intent.EXTRA_SUBJECT, "Bug Report")
        putExtra(Intent.EXTRA_TEXT, emailBody)
        setPackage("com.google.android.gm") // Direct to Gmail if available
    }

    // Check if Gmail is available
    if (gmailIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(gmailIntent)
    } else {
        // If Gmail isn't available, show the chooser for other email apps
        val chooserIntent = Intent.createChooser(gmailIntent, "Choose Email App")
        context.startActivity(chooserIntent)
    }
}

fun bugReportWhatsApp(context: Context, name: String, email: String, phoneNumber: String) {
    // Gather device details
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT

    // Compose the message body
    val messageBody = """
        Here are my details:
        
        Name: $name
        Email: $email
        Phone: $phoneNumber

        
        Device Details:
        Manufacturer: $manufacturer
        Model: $model
        Android Version: $androidVersion
        SDK Version: $sdkVersion
        
        Issue I'm facing:
        Please describe your issue here, including any steps to reproduce it, or specific error messages encountered.
    """.trimIndent()

    // Create the WhatsApp intent
    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/7717797775?text=${Uri.encode(messageBody)}")
    }
// Check for regular WhatsApp or WhatsApp Business
    val isWhatsAppInstalled = isAppInstalled(context, "com.whatsapp") ||
            isAppInstalled(context, "com.whatsapp.w4b")

    if (isWhatsAppInstalled) {
        // Launch WhatsApp
        context.startActivity(whatsappIntent)
    } else {
        // Notify user if WhatsApp is not installed
//        Toast.makeText(context, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show()
        bugReportEmail(context, name, email, phoneNumber)
    }

}



fun supportEmail(context: Context, name: String, email: String, phoneNumber: String) {
    // Gather device details
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT

    // Compose the email body
    val emailBody = """
        Here are my details:
        
        Name: $name
        Email: $email
        Phone: $phoneNumber

        
        Device Details:
        Manufacturer: $manufacturer
        Model: $model
        Android Version: $androidVersion
        SDK Version: $sdkVersion
        
        Issue I'm facing:
 
    """.trimIndent()

    // First, try to open Gmail directly
    val gmailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822" // Restricts to email apps
        putExtra(Intent.EXTRA_EMAIL, arrayOf("Support@smartbus360"))
        putExtra(Intent.EXTRA_SUBJECT, "Issue Report")
        putExtra(Intent.EXTRA_TEXT, emailBody)
        setPackage("com.google.android.gm") // Direct to Gmail if available
    }

    // Check if Gmail is available
    if (gmailIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(gmailIntent)
    } else {
        // If Gmail isn't available, show the chooser for other email apps
        val chooserIntent = Intent.createChooser(gmailIntent, "Choose Email App")
        context.startActivity(chooserIntent)
    }
}

fun supportWhatsApp(context: Context, name: String, email: String, phoneNumber: String) {
    // Gather device details
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT

    // Compose the message body
    val messageBody = """
        Here are my details:
        
        Name: $name
        Email: $email
        Phone: $phoneNumber

        
        Device Details:
        Manufacturer: $manufacturer
        Model: $model
        Android Version: $androidVersion
        SDK Version: $sdkVersion
        
        Issue I'm facing:
        Please describe your issue here.
    """.trimIndent()

    // Try launching WhatsApp
    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/7717797775?text=${Uri.encode(messageBody)}")
    }

    // Check for regular WhatsApp or WhatsApp Business
    val isWhatsAppInstalled = isAppInstalled(context, "com.whatsapp") ||
            isAppInstalled(context, "com.whatsapp.w4b")

    if (isWhatsAppInstalled) {
        // Launch WhatsApp
        context.startActivity(whatsappIntent)
    } else {
        // Notify user if WhatsApp is not installed
//        Toast.makeText(context, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show()
        supportEmail(context, name, email, phoneNumber)
    }
}

// Helper function to check if an app is installed
fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}


