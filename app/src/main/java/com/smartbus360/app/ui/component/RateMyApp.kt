 package com.smartbus360.app.ui.component

import android.app.Activity
import android.content.Context
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun RateThisAppButton() {
    val context = LocalContext.current
    val activity = context as? Activity // Ensure the context is an Activity

    Button(onClick = { launchInAppReview(activity, context) }) {
        Text("Rate This App")
    }
}

fun launchInAppReview(activity: Activity?, context: Context) {
//    if (activity == null) return
//
//    val reviewManager = ReviewManagerFactory.create(activity)
//    val request = reviewManager.requestReviewFlow()
//    request.addOnCompleteListener { task: Task<ReviewInfo> ->
//        if (task.isSuccessful) {
//            // Get the ReviewInfo instance
//            val reviewInfo = task.result
//            val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
//            flow.addOnCompleteListener {
//                // The review flow has completed
//            }
//        } else {
//            // Handle the error
//            val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
//            context.startActivity(intent)
//
//        }
//    }
}
