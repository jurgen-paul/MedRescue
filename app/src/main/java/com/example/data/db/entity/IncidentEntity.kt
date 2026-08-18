package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity storing telemetry dispatch incident history.
 */
@Entity(tableName = "incident_records")
data class IncidentEntity(
  @PrimaryKey
  val id: String,
  val timestamp: String,
  val status: String,
  val locationLabel: String,
  val coordinates: String,
  val assignedUnit: String,
  val targetFacility: String,
  val telemetryHash: String,
  val stagesCompleted: Int,
  val createdEpochMs: Long = System.currentTimeMillis()
)
