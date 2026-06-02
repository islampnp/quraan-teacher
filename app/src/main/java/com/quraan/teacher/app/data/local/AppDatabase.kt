package com.quraan.teacher.app.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quraan.teacher.app.data.local.dao.*
import com.quraan.teacher.app.data.local.entities.*

@Database(
    entities = [
        StudentEntity::class,
        ProgressEntity::class,
        ScheduleEntity::class,
        AudioEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizAttemptEntity::class,
        LearningPathEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun progressDao(): ProgressDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun audioDao(): AudioDao
    abstract fun quizDao(): QuizDao
    abstract fun learningPathDao(): LearningPathDao

    companion object {
        private const val DATABASE_NAME = "quraan_teacher_db"

        fun build(context: android.content.Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(SeedDatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        private class SeedDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed data is handled by SeedData class after DB creation
            }
        }
    }
}
