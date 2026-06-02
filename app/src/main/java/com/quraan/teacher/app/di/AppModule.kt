package com.quraan.teacher.app.di

import android.content.Context
import androidx.room.Room
import com.quraan.teacher.app.data.local.AppDatabase
import com.quraan.teacher.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "quraan_teacher_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()

    @Provides
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideAudioDao(db: AppDatabase): AudioDao = db.audioDao()

    @Provides
    fun provideQuizDao(db: AppDatabase): QuizDao = db.quizDao()

    fun provideLearningPathDao(db: AppDatabase): LearningPathDao = db.learningPathDao()
}
