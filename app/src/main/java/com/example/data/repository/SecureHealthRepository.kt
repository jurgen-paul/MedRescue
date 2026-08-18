package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.entity.EncryptedHealthEntity
import com.example.data.db.entity.IncidentEntity
import com.example.data.security.CryptoManager
import com.example.model.EmergencyContact
import com.example.model.HealthProfile
import com.example.model.IncidentRecord
import com.example.model.IncidentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository providing secure local Room persistence with on-the-fly AES-256 GCM
 * encryption and decryption for patient health data, emergency contacts, and incident logs.
 */
class SecureHealthRepository(context: Context) {

  private val database = AppDatabase.getDatabase(context)
  private val healthDao = database.healthRecordDao()
  private val incidentDao = database.incidentDao()
  private val scope = CoroutineScope(Dispatchers.IO)

  init {
    // Seed initial health record in Room if database is empty
    scope.launch {
      val existing = healthDao.getHealthRecord()
      if (existing == null) {
        val defaultProfile = HealthProfile()
        saveHealthProfile(defaultProfile)
      }
    }
  }

  /**
   * Observe reactive Health Profile stream from Room, automatically decrypted into domain model.
   */
  val healthProfileFlow: Flow<HealthProfile> = healthDao.getHealthRecordFlow().map { entity ->
    if (entity == null) {
      HealthProfile()
    } else {
      decryptEntityToProfile(entity)
    }
  }

  /**
   * Observe reactive Incident logs from Room.
   */
  val incidentLogsFlow: Flow<List<IncidentRecord>> = incidentDao.getAllIncidentsFlow().map { entities ->
    entities.map { entity ->
      IncidentRecord(
        id = entity.id,
        timestamp = entity.timestamp,
        status = try { IncidentStatus.valueOf(entity.status) } catch (_: Exception) { IncidentStatus.RESOLVED },
        locationLabel = entity.locationLabel,
        coordinates = entity.coordinates,
        assignedUnit = entity.assignedUnit,
        targetFacility = entity.targetFacility,
        telemetryHash = entity.telemetryHash,
        stagesCompleted = entity.stagesCompleted
      )
    }
  }

  /**
   * Encrypts and persists updated health profile into Room database.
   */
  suspend fun saveHealthProfile(profile: HealthProfile) {
    val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
    val syncTimestamp = sdf.format(Date())

    // Encrypt individual sensitive fields with AES-256 GCM
    val (encName, iv) = CryptoManager.encrypt(profile.fullName)
    val (encDob, _) = CryptoManager.encrypt(profile.dob)
    val (encBlood, _) = CryptoManager.encrypt(profile.bloodType)
    val (encPronouns, _) = CryptoManager.encrypt(profile.pronouns)
    val (encAllergies, _) = CryptoManager.encrypt(profile.allergies.joinToString("|||"))
    val (encAllergyNote, _) = CryptoManager.encrypt(profile.allergyNote)
    val (encMeds, _) = CryptoManager.encrypt(profile.medications.joinToString("|||"))
    val (encContactName, _) = CryptoManager.encrypt(profile.emergencyContact.name)
    val (encContactRel, _) = CryptoManager.encrypt(profile.emergencyContact.relationship)
    val (encContactPhone, _) = CryptoManager.encrypt(profile.emergencyContact.phone)
    val (encPhysician, _) = CryptoManager.encrypt(profile.primaryPhysician)
    val (encNotes, _) = CryptoManager.encrypt(profile.medicalNotes)

    val signaturePayload = "${profile.id}|${profile.fullName}|${profile.bloodType}|${profile.allergies.joinToString(",")}|$syncTimestamp"
    val hash = CryptoManager.generateIntegrityHash(signaturePayload)

    val entity = EncryptedHealthEntity(
      id = "PRIMARY_HEALTH_PROFILE",
      fullNameEncrypted = encName,
      dobEncrypted = encDob,
      bloodTypeEncrypted = encBlood,
      pronounsEncrypted = encPronouns,
      organDonor = profile.organDonor,
      allergiesJsonEncrypted = encAllergies,
      allergyNoteEncrypted = encAllergyNote,
      medicationsJsonEncrypted = encMeds,
      emergencyContactNameEncrypted = encContactName,
      emergencyContactRelationshipEncrypted = encContactRel,
      emergencyContactPhoneEncrypted = encContactPhone,
      primaryPhysicianEncrypted = encPhysician,
      medicalNotesEncrypted = encNotes,
      cryptoIv = iv,
      dataSignatureHash = hash,
      lastUpdatedTimestamp = System.currentTimeMillis()
    )

    healthDao.insertOrUpdate(entity)
  }

  /**
   * Persists an emergency incident log in Room.
   */
  suspend fun recordIncident(record: IncidentRecord) {
    val entity = IncidentEntity(
      id = record.id,
      timestamp = record.timestamp,
      status = record.status.name,
      locationLabel = record.locationLabel,
      coordinates = record.coordinates,
      assignedUnit = record.assignedUnit,
      targetFacility = record.targetFacility,
      telemetryHash = record.telemetryHash,
      stagesCompleted = record.stagesCompleted,
      createdEpochMs = System.currentTimeMillis()
    )
    incidentDao.insertIncident(entity)
  }

  /**
   * Clears all incident logs from Room.
   */
  suspend fun clearIncidentLogs() {
    incidentDao.clearAllIncidents()
  }

  private fun decryptEntityToProfile(entity: EncryptedHealthEntity): HealthProfile {
    val iv = entity.cryptoIv
    val fullName = CryptoManager.decrypt(entity.fullNameEncrypted, iv).ifEmpty { "Alex Morgan" }
    val dob = CryptoManager.decrypt(entity.dobEncrypted, iv).ifEmpty { "14 JUN 1994" }
    val bloodType = CryptoManager.decrypt(entity.bloodTypeEncrypted, iv).ifEmpty { "O+" }
    val pronouns = CryptoManager.decrypt(entity.pronounsEncrypted, iv).ifEmpty { "THEY / THEM" }
    val allergiesRaw = CryptoManager.decrypt(entity.allergiesJsonEncrypted, iv)
    val allergies = if (allergiesRaw.isNotEmpty()) allergiesRaw.split("|||").filter { it.isNotBlank() } else listOf("Asthma", "Seasonal allergies")
    val allergyNote = CryptoManager.decrypt(entity.allergyNoteEncrypted, iv).ifEmpty { "No known medication allergies." }
    val medsRaw = CryptoManager.decrypt(entity.medicationsJsonEncrypted, iv)
    val meds = if (medsRaw.isNotEmpty()) medsRaw.split("|||").filter { it.isNotBlank() } else listOf("Albuterol inhaler · 90 mcg · as needed", "Cetirizine · 10 mg · daily")

    val contactName = CryptoManager.decrypt(entity.emergencyContactNameEncrypted, iv).ifEmpty { "Jordan Morgan" }
    val contactRel = CryptoManager.decrypt(entity.emergencyContactRelationshipEncrypted, iv).ifEmpty { "PARTNER" }
    val contactPhone = CryptoManager.decrypt(entity.emergencyContactPhoneEncrypted, iv).ifEmpty { "+1 415 555 0148" }

    val physician = CryptoManager.decrypt(entity.primaryPhysicianEncrypted, iv).ifEmpty { "Dr. S. Vance, MD · UCSF Health" }
    val medicalNotes = CryptoManager.decrypt(entity.medicalNotesEncrypted, iv).ifEmpty { "Carries rescue inhaler at all times. Prior mild concussion in 2022." }

    val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
    val syncTime = sdf.format(Date(entity.lastUpdatedTimestamp))

    return HealthProfile(
      id = "MED-AXM-0148",
      fullName = fullName,
      dob = dob,
      bloodType = bloodType,
      pronouns = pronouns,
      organDonor = entity.organDonor,
      allergies = allergies,
      allergyNote = allergyNote,
      medications = meds,
      emergencyContact = EmergencyContact(
        name = contactName,
        relationship = contactRel,
        phone = contactPhone
      ),
      primaryPhysician = physician,
      medicalNotes = medicalNotes,
      lastSync = syncTime
    )
  }
}
