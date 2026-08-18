package com.example.model

enum class TravelMode(val label: String, val shortLabel: String, val description: String) {
  WALKING("WALKING", "WALK", "Pedestrian route"),
  DRIVING("DRIVING", "DRIVE", "Road route"),
  AMBULANCE("AMBULANCE", "EMS", "Priority response")
}

data class LocationPreset(
  val label: String,
  val lat: Double,
  val lng: Double,
  val neighborhood: String
)

data class EmergencyContact(
  val name: String,
  val relationship: String,
  val phone: String
)

data class HealthProfile(
  val id: String = "MED-AXM-0148",
  val fullName: String = "Alex Morgan",
  val dob: String = "14 JUN 1994",
  val bloodType: String = "O+",
  val pronouns: String = "THEY / THEM",
  val organDonor: Boolean = true,
  val allergies: List<String> = listOf("Asthma", "Seasonal allergies"),
  val allergyNote: String = "No known medication allergies.",
  val medications: List<String> = listOf(
    "Albuterol inhaler · 90 mcg · as needed",
    "Cetirizine · 10 mg · daily"
  ),
  val emergencyContact: EmergencyContact = EmergencyContact(
    name = "Jordan Morgan",
    relationship = "PARTNER",
    phone = "+1 415 555 0148"
  ),
  val primaryPhysician: String = "Dr. S. Vance, MD · UCSF Health",
  val medicalNotes: String = "Carries rescue inhaler at all times. Prior mild concussion in 2022.",
  val lastSync: String = "06:47:33 UTC"
)

data class HospitalFacility(
  val id: String,
  val name: String,
  val level: String, // "Level 1", "Level 2", "Level 3"
  val distance: Double, // miles
  val driveMinutes: Int,
  val neighborhood: String,
  val status: String, // "TRAUMA CENTER", "ACCEPTING", "VERIFYING"
  val specialties: List<String>,
  val lat: Double,
  val lng: Double,
  val phone: String = "+1 (415) 206-8000",
  val address: String
) {
  fun calculateEtaMinutes(mode: TravelMode): Int {
    return when (mode) {
      TravelMode.WALKING -> kotlin.math.max(1, kotlin.math.ceil(distance * 20.0).toInt())
      TravelMode.AMBULANCE -> kotlin.math.max(2, kotlin.math.ceil(driveMinutes * 0.62).toInt())
      TravelMode.DRIVING -> driveMinutes
    }
  }

  fun formatEta(mode: TravelMode): String {
    return "${calculateEtaMinutes(mode)} min"
  }
}

enum class IncidentStatus {
  IDLE,
  ARMING,
  DISPATCHED,
  RESOLVED,
  CANCELLED
}

data class IncidentStageItem(
  val step: Int,
  val title: String,
  val detail: String
)

data class IncidentRecord(
  val id: String,
  val timestamp: String,
  val status: IncidentStatus,
  val locationLabel: String,
  val coordinates: String,
  val assignedUnit: String,
  val targetFacility: String,
  val telemetryHash: String,
  val stagesCompleted: Int
)

data class FaqItem(
  val number: String,
  val question: String,
  val answer: String
)

data class WorkflowStep(
  val number: String,
  val title: String,
  val description: String
)

data class DataLayerItem(
  val title: String,
  val copy: String
)
