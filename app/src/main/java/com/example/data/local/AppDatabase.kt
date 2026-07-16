package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Tournament::class,
        JoinedPlayer::class,
        WalletTransaction::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dls_tournament_db"
                )
                .fallbackToDestructiveMigration() // safe for rapid development and testing
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
