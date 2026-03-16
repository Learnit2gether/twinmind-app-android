package io.twinmind.app.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.twinmind.app.core.database.ChunkEntity
import io.twinmind.app.ui.theme.TwinMindDarkBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TranscriptTab(startTime: Long, chunks: List<ChunkEntity>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chunks.size) { id ->
            val chunk = chunks.get(id)

            val regex = Regex("\\d+") // Matches one or more digits
            val index = regex.find(chunk.chunkIndex)?.value?.toIntOrNull() ?: 0

            // Calculate actual clock time: startTime + (index * 30s)
            val timestamp = startTime + (index* 30_000L)
            val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = timeLabel, color = TwinMindDarkBlue, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = chunk.transcript ?: "Transcribing chunk...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (chunk.transcript == null) Color.LightGray else Color.Black
                )
                Spacer(Modifier.height(16.dp))
                Divider(color = Color(0xFFF0F0F0))
            }
        }
    }
}
