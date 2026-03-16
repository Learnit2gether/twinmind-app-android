package io.twinmind.app.core

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