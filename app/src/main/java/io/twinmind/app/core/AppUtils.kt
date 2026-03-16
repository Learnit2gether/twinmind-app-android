package io.twinmind.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateMeetingId(): String {
    return "meeting_${System.currentTimeMillis()}"
}


fun Long.toMeetingDateTime(): String {
    val date = Date(this)
    // Pattern: MMM (Month), d (Day), h:mm (Time), a (AM/PM)
    val formatter = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())

    return formatter.format(date)
        .replace("AM", "am")
        .replace("PM", "pm")
}

fun Long.toReadableDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        // e.g., "1 hr 15 min"
        hours > 0 -> {
            if (minutes > 0) "${hours} hr ${minutes} min" else "${hours} hr"
        }
        // e.g., "5 min 30s" or "5 min"
        minutes > 0 -> {
            if (seconds > 0) "${minutes} min ${seconds}s" else "${minutes} min"
        }
        // e.g., "45s"
        seconds > 0 -> "${seconds}s"

        // Handle zero case (matches your "0 min" screenshot)
        else -> "0 min"
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}