package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity storing AES-256 GCM encrypted health information
 * to ensure patient data remains strictly protected at rest on the local device.
 */
@Entity(tableName = "encrypted_health_records")
data class EncryptedHealthEntity(
  @PrimaryKey
  val id: String = "PRIMARY_HEALTH_PROFILE",
  val fullNameEncrypted: String,
  val dobEncrypted: String,
  val bloodTypeEncrypted: String,
  val pronounsEncrypted: String,
  val organDonor: Boolean = true,
  val allergiesJsonEncrypted: String,
  val allergyNoteEncrypted: String,
  val medicationsJsonEncrypted: String,
  val emergencyContactNameEncrypted: String,
  val emergencyContactRelationshipEncrypted: String,
  val emergencyContactPhoneEncrypted: String,
  val primaryPhysicianEncrypted: String,
  val medicalNotesEncrypted: String,
  val dnrStatus: Boolean = false,
  val insuranceProviderEncrypted: String = "",
  val policyNumberEncrypted: String = "",
  val cryptoIv: String,
  val dataSignatureHash: String,
  val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
  val encryptionScheme: String = "AES-256-GCM"
)
