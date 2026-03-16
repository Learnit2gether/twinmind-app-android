package io.twinmind.app.data

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import io.twinmind.app.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class GeminiRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val generativeModel: GenerativeModel
) {

    // 1. Transcribe the 30-second chunk
    suspend fun transcribeAudioFile(file: File): String? {
        return try {
            val audioBytes = file.readBytes()
            val content = content {
                // Gemini supports PCM/WAV via blob
                blob("audio/wav", audioBytes)
                text("Transcribe this audio wav file. Do not summarize, just text.")
            }
            val response = generativeModel.generateContent(content)
            response.text
        } catch (e: Exception) {
            Log.d(TAG, "transcribeAudioFile: ${e.message}")
            null
        }
    }

    // 2. Generate Structured Summary with Streaming
    suspend fun summarizeTranscriptStream(transcript: String): Flow<String> = flow {
        val prompt = """
            convert meeting transcripts into structured Markdown notes.

            Rules:
            - Output only Markdown
            - Use these sections exactly:
              # Title
              ## Summary
              ## Key Points
              ## Action Items
            
            Formatting rules:
            - Use bullet points for key points
            - Use task list for action items
            - Keep sentences short
            - No explanations outside markdown
            
            Transcript: $transcript
        """.trimIndent()

        // Use generateContentStream to satisfy the "Stream in UI" requirement
        generativeModel.generateContentStream(prompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }
}