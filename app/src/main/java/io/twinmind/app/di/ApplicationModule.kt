package io.twinmind.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.twinmind.app.BuildConfig
import io.twinmind.app.core.database.TwinMindDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun provideGeminiModel(): GenerativeModel {
        // in production app, key will be stored in NDK files
        val apiKey = BuildConfig.GEMINI_API_KEY
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, TwinMindDatabase::class.java, "twinmind.db")
            // for prototyping
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideMeetingDao(db: TwinMindDatabase) = db.meetingDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}