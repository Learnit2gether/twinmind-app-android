package io.twinmind.app.core

fun generateMeetingId(): String {
    return "meeting_${System.currentTimeMillis()}"
}