package com.example.data.db.dao

import androidx.room.*
import com.example.data.db.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

  @Query("SELECT * FROM incident_records ORDER BY createdEpochMs DESC")
  fun getAllIncidentsFlow(): Flow<List<IncidentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncident(incident: IncidentEntity)

  @Query("DELETE FROM incident_records WHERE id = :id")
  suspend fun deleteIncidentById(id: String)

  @Query("DELETE FROM incident_records")
  suspend fun clearAllIncidents()
}
