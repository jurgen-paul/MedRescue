package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmergencyRepository {

  val locationPresets = listOf(
    LocationPreset("Dolores Park (Outdoors)", 37.7596, -122.4269, "Mission / Dolores"),
    LocationPreset("Mission District (Mobile)", 37.7599, -122.4148, "Mission District"),
    LocationPreset("Civic Center (Transit)", 37.7793, -122.4192, "Civic Center"),
    LocationPreset("Parnassus Heights", 37.7635, -122.4580, "Parnassus Heights")
  )

  val hospitalFacilities = listOf(
    HospitalFacility(
      id = "sfgh-01",
      name = "Zuckerberg San Francisco General",
      level = "Level 1",
      distance = 1.8,
      driveMinutes = 8,
      neighborhood = "Mission",
      status = "TRAUMA CENTER",
      specialties = listOf("Stroke", "Cardiac", "Pediatric"),
      lat = 37.7557,
      lng = -122.4054,
      phone = "+1 (415) 206-8000",
      address = "1001 Potrero Ave, San Francisco, CA 94110"
    ),
    HospitalFacility(
      id = "ucsf-02",
      name = "UCSF Medical Center at Parnassus",
      level = "Level 1",
      distance = 3.4,
      driveMinutes = 14,
      neighborhood = "Parnassus Heights",
      status = "ACCEPTING",
      specialties = listOf("Cardiac", "Burn", "Neuro"),
      lat = 37.7631,
      lng = -122.4576,
      phone = "+1 (415) 476-1000",
      address = "505 Parnassus Ave, San Francisco, CA 94143"
    ),
    HospitalFacility(
      id = "cpmc-03",
      name = "California Pacific Medical Center",
      level = "Level 2",
      distance = 4.1,
      driveMinutes = 17,
      neighborhood = "Pacific Heights",
      status = "ACCEPTING",
      specialties = listOf("Stroke", "Orthopedic"),
      lat = 37.7913,
      lng = -122.4333,
      phone = "+1 (415) 600-6000",
      address = "1101 Van Ness Ave, San Francisco, CA 94109"
    ),
    HospitalFacility(
      id = "kaiser-04",
      name = "Kaiser Permanente San Francisco",
      level = "Level 2",
      distance = 5.7,
      driveMinutes = 22,
      neighborhood = "Anza Vista",
      status = "VERIFYING",
      specialties = listOf("Cardiac", "Stroke"),
      lat = 37.7794,
      lng = -122.4425,
      phone = "+1 (415) 833-2000",
      address = "2425 Geary Blvd, San Francisco, CA 94115"
    ),
    HospitalFacility(
      id = "stmary-05",
      name = "St. Mary's Medical Center",
      level = "Level 3",
      distance = 6.2,
      driveMinutes = 25,
      neighborhood = "Inner Richmond",
      status = "ACCEPTING",
      specialties = listOf("Orthopedic", "Pediatric"),
      lat = 37.7827,
      lng = -122.4494,
      phone = "+1 (415) 668-1000",
      address = "450 Stanyan St, San Francisco, CA 94117"
    )
  )

  val dispatchProtocolSteps = listOf(
    IncidentStageItem(1, "Capture GPS coordinates & timestamp", "Locking precision coordinates and satellite fix timestamp."),
    IncidentStageItem(2, "Compile encrypted medical profile bundle", "Assembling blood type, allergy list, and primary contact."),
    IncidentStageItem(3, "Locate nearest Level-1 trauma center", "Cross-referencing real-time receiving facility availability."),
    IncidentStageItem(4, "Dispatch paramedic unit via secure channel", "Transmitting emergency token packet to Unit Alpha EMS."),
    IncidentStageItem(5, "Stream live telemetry to ER intake", "Establishing live bidirectional link with triage station.")
  )

  val workflowSteps = listOf(
    WorkflowStep("01", "User confirms SOS", "The press-and-hold control is designed to reduce accidental activation. A production deployment would create an incident only after the hold threshold is completed."),
    WorkflowStep("02", "Context is assembled", "The system prepares the Health Card, current location, telemetry packet, known allergies, medications, and other responder-approved context."),
    WorkflowStep("03", "Receiving facilities are ranked", "The ER Directory compares facilities near the simulated location, shows trauma level and specialties, and recalculates ETA for the selected travel mode."),
    WorkflowStep("04", "A dispatch channel is opened", "The console is designed to pass a concise incident bundle to an authorized dispatch or emergency response service rather than asking responders to interpret a long form."),
    WorkflowStep("05", "Responders receive structured context", "The Health Card QR flow provides a fast responder access path, while the incident interface is designed to keep location, medical context, and destination information together.")
  )

  val dataLayers = listOf(
    DataLayerItem("Location telemetry", "GPS coordinates, a location label, timestamp, and the selected travel origin. The current build uses a simulated Dolores Park location."),
    DataLayerItem("Health Card", "Mock patient identity, blood type, allergies, medications, emergency contact, and responder-sharing status."),
    DataLayerItem("ER Directory", "Mock receiving facilities with trauma level, specialties, acceptance status, coordinates, distance, and mode-specific ETA."),
    DataLayerItem("Response channel", "A production integration would connect the prepared incident bundle to a dispatch center, clinical intake system, or approved emergency service.")
  )

  val faqList = listOf(
    FaqItem("01", "Does this prototype contact emergency services?", "No. The current build does not create a real dispatch, contact 911 paramedics, send an actual medical alert, or transmit real telemetry to municipal CAD. It is an interface prototype demonstrating how those integrations are coordinated."),
    FaqItem("02", "What would be required for a real emergency integration?", "A production integration would require PSAP / CAD (Computer-Aided Dispatch) connectors, authenticated responder networks, HIPAA/GDPR clinical data safeguards, audited transmission logs, and carrier-certified emergency telemetry handshakes."),
    FaqItem("03", "How would location sharing work?", "On actual mobile devices, high-accuracy GNSS hardware with background wake locks captures coordinates and timestamp, resolves a reverse-geocoded location, and shares the minimum necessary telemetry with an authorized response channel."),
    FaqItem("04", "Can responders use the QR code during an incident?", "The QR flow demonstrates a controlled responder-access pattern. A production version would generate short-lived authenticated tokens with consent rules, tamper seals, expiry, and an audit trail for every scanned access."),
    FaqItem("05", "Is the Contact form monitored for urgent medical requests?", "No. This form is for non-emergency support and integration inquiries only. For a real emergency, call your local emergency number (e.g., 911) first and follow the instructions of emergency operators.")
  )

  private val _healthProfile = MutableStateFlow(HealthProfile())
  val healthProfile: StateFlow<HealthProfile> = _healthProfile.asStateFlow()

  private val _incidentLogs = MutableStateFlow(
    listOf(
      IncidentRecord(
        id = "INC-2026-0818-0941",
        timestamp = "09:41:12 UTC",
        status = IncidentStatus.RESOLVED,
        locationLabel = "Dolores Park (Outdoors)",
        coordinates = "37.7596°N, 122.4269°W",
        assignedUnit = "Medic 14 - Unit Alpha",
        targetFacility = "Zuckerberg SF General",
        telemetryHash = "SHA256:7f4a9b2c8e1d...",
        stagesCompleted = 5
      ),
      IncidentRecord(
        id = "INC-2026-0817-1822",
        timestamp = "18:22:45 UTC",
        status = IncidentStatus.RESOLVED,
        locationLabel = "Mission District (Mobile)",
        coordinates = "37.7599°N, 122.4148°W",
        assignedUnit = "Medic 08 - Unit Bravo",
        targetFacility = "UCSF Parnassus",
        telemetryHash = "SHA256:3e81fc04b12a...",
        stagesCompleted = 5
      )
    )
  )
  val incidentLogs: StateFlow<List<IncidentRecord>> = _incidentLogs.asStateFlow()

  fun updateHealthProfile(profile: HealthProfile) {
    val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
    _healthProfile.value = profile.copy(lastSync = sdf.format(Date()))
  }

  fun recordIncident(record: IncidentRecord) {
    _incidentLogs.value = listOf(record) + _incidentLogs.value
  }

  fun clearIncidents() {
    _incidentLogs.value = emptyList()
  }
}
