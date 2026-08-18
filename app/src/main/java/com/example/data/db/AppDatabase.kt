package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.HealthRecordDao
import com.example.data.db.dao.IncidentDao
import com.example.data.db.entity.EncryptedHealthEntity
import com.example.data.db.entity.IncidentEntity

@Database(
  entities = [EncryptedHealthEntity::class, IncidentEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun healthRecordDao(): HealthRecordDao
  abstract fun incidentDao(): IncidentDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "medrescue_secure_health.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
