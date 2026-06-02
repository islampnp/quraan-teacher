package com.quraan.teacher.app.di

import android.content.Context
import androidx.room.Room
import com.quraan.teacher.app.data.local.AppDatabase
import com.quraan.teacher.app.data.local.dao.*
import com.quraan.teacher.app.data.repository.*
import com.quraan.teacher.app.domain.usecase.*
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

    @Provides
    fun provideLearningPathDao(db: AppDatabase): LearningPathDao = db.learningPathDao()

    @Provides
    @Singleton
    fun provideStudentRepository(dao: StudentDao): StudentRepository = StudentRepository(dao)

    @Provides
    @Singleton
    fun provideProgressRepository(dao: ProgressDao): ProgressRepository = ProgressRepository(dao)

    @Provides
    @Singleton
    fun provideScheduleRepository(dao: ScheduleDao): ScheduleRepository = ScheduleRepository(dao)

    @Provides
    @Singleton
    fun provideAudioRepository(dao: AudioDao): AudioRepository = AudioRepository(dao)

    @Provides
    @Singleton
    fun provideQuizRepository(dao: QuizDao): QuizRepository = QuizRepository(dao)

    @Provides
    @Singleton
    fun provideLearningPathRepository(dao: LearningPathDao): LearningPathRepository = LearningPathRepository(dao)

    @Provides
    @Singleton
    fun provideGetStudentLearningPathUseCase(
        learningPathRepository: LearningPathRepository,
        studentRepository: StudentRepository
    ): GetStudentLearningPathUseCase = GetStudentLearningPathUseCase(learningPathRepository, studentRepository)

    @Provides
    @Singleton
    fun provideUpdateProgressUseCase(
        progressRepository: ProgressRepository,
        studentRepository: StudentRepository,
        learningPathRepository: LearningPathRepository
    ): UpdateProgressUseCase = UpdateProgressUseCase(progressRepository, studentRepository, learningPathRepository)

    @Provides
    @Singleton
    fun provideGenerateLearningPathUseCase(
        learningPathRepository: LearningPathRepository,
        studentRepository: StudentRepository
    ): GenerateLearningPathUseCase = GenerateLearningPathUseCase(learningPathRepository, studentRepository)

    @Provides
    @Singleton
    fun provideEvaluateStudentUseCase(
        studentRepository: StudentRepository,
        progressRepository: ProgressRepository,
        learningPathRepository: LearningPathRepository,
        quizRepository: QuizRepository
    ): EvaluateStudentUseCase = EvaluateStudentUseCase(
        studentRepository, progressRepository, learningPathRepository, quizRepository
    )

    @Provides
    @Singleton
    fun provideScheduleSessionUseCase(
        scheduleRepository: ScheduleRepository
    ): ScheduleSessionUseCase = ScheduleSessionUseCase(scheduleRepository)
}
