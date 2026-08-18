package com.example.data.db.dao

import androidx.room.*
import com.example.data.db.entity.EncryptedHealthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

  @Query("SELECT * FROM encrypted_health_records WHERE id = :id LIMIT 1")
  fun getHealthRecordFlow(id: String = "PRIMARY_HEALTH_PROFILE"): Flow<EncryptedHealthEntity?>

  @Query("SELECT * FROM encrypted_health_records WHERE id = :id LIMIT 1")
  suspend fun getHealthRecord(id: String = "PRIMARY_HEALTH_PROFILE"): EncryptedHealthEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(record: EncryptedHealthEntity)

  @Query("DELETE FROM encrypted_health_records WHERE id = :id")
  suspend fun deleteHealthRecord(id: String = "PRIMARY_HEALTH_PROFILE")

  @Query("DELETE FROM encrypted_health_records")
  suspend fun clearAll()
}
