package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmergencyRepository
import com.example.data.repository.SecureHealthRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MedRescueViewModel(application: Application) : AndroidViewModel(application) {

  private val secureHealthRepository = SecureHealthRepository(application)

  val healthProfile: StateFlow<HealthProfile> = secureHealthRepository.healthProfileFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthProfile())

  val incidentLogs: StateFlow<List<IncidentRecord>> = secureHealthRepository.incidentLogsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Navigation State
  private val _currentRoute = MutableStateFlow("sos")
  val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

  // Location State
  private val _selectedLocation = MutableStateFlow(EmergencyRepository.locationPresets[0])
  val selectedLocation: StateFlow<LocationPreset> = _selectedLocation.asStateFlow()

  // SOS & Dispatch State
  private val _isHolding = MutableStateFlow(false)
  val isHolding: StateFlow<Boolean> = _isHolding.asStateFlow()

  private val _holdProgress = MutableStateFlow(0f)
  val holdProgress: StateFlow<Float> = _holdProgress.asStateFlow()

  private val _isArmed = MutableStateFlow(false)
  val isArmed: StateFlow<Boolean> = _isArmed.asStateFlow()

  private val _dispatchStage = MutableStateFlow(0) // 0=None, 1..5
  val dispatchStage: StateFlow<Int> = _dispatchStage.asStateFlow()

  private var holdJob: Job? = null
  private var dispatchJob: Job? = null

  // ER Directory Filters & Mode
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedTraumaLevel = MutableStateFlow("All levels")
  val selectedTraumaLevel: StateFlow<String> = _selectedTraumaLevel.asStateFlow()

  private val _travelMode = MutableStateFlow(TravelMode.DRIVING)
  val travelMode: StateFlow<TravelMode> = _travelMode.asStateFlow()

  private val _directoryViewMode = MutableStateFlow("list") // "list" or "map"
  val directoryViewMode: StateFlow<String> = _directoryViewMode.asStateFlow()

  private val _sortBy = MutableStateFlow("distance") // "distance" or "name"
  val sortBy: StateFlow<String> = _sortBy.asStateFlow()

  // UI Feedback & Dialogs
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _showQrDialog = MutableStateFlow(false)
  val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

  private val _showEditProfileDialog = MutableStateFlow(false)
  val showEditProfileDialog: StateFlow<Boolean> = _showEditProfileDialog.asStateFlow()

  private val _showCancelConfirmDialog = MutableStateFlow(false)
  val showCancelConfirmDialog: StateFlow<Boolean> = _showCancelConfirmDialog.asStateFlow()

  // Contact Form State
  private val _contactSuccess = MutableStateFlow(false)
  val contactSuccess: StateFlow<Boolean> = _contactSuccess.asStateFlow()

  // Filtered Hospital List
  val filteredHospitals: StateFlow<List<HospitalFacility>> = combine(
    _searchQuery,
    _selectedTraumaLevel,
    _sortBy
  ) { query, level, sort ->
    EmergencyRepository.hospitalFacilities
      .filter { facility ->
        val matchesQuery = query.isBlank() ||
            facility.name.contains(query, ignoreCase = true) ||
            facility.neighborhood.contains(query, ignoreCase = true) ||
            facility.specialties.any { it.contains(query, ignoreCase = true) }
        val matchesLevel = level == "All levels" || facility.level.equals(level, ignoreCase = true)
        matchesQuery && matchesLevel
      }
      .sortedWith { a, b ->
        if (sort == "distance") a.distance.compareTo(b.distance) else a.name.compareTo(b.name)
      }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencyRepository.hospitalFacilities)

  fun navigateTo(route: String) {
    _currentRoute.value = route
  }

  fun setLocation(preset: LocationPreset) {
    _selectedLocation.value = preset
    showToast("LOCATION SET: ${preset.label.uppercase()}")
  }

  fun startHoldingSos() {
    if (_isArmed.value) return
    _isHolding.value = true
    _holdProgress.value = 0f
    vibrate(50)

    holdJob?.cancel()
    holdJob = viewModelScope.launch {
      val durationMs = 2000L
      val stepMs = 40L
      val steps = (durationMs / stepMs).toInt()
      for (i in 1..steps) {
        delay(stepMs)
        _holdProgress.value = i.toFloat() / steps
        if (i % 10 == 0) {
          vibrate(20)
        }
      }
      // Triggered!
      _isHolding.value = false
      _holdProgress.value = 1f
      triggerSosDispatch()
    }
  }

  fun releaseHoldingSos() {
    if (_isArmed.value) return
    holdJob?.cancel()
    _isHolding.value = false
    _holdProgress.value = 0f
  }

  private fun triggerSosDispatch() {
    _isArmed.value = true
    vibratePattern(longArrayOf(0, 200, 100, 200, 100, 300))
    showToast("EMERGENCY DISPATCH TRIGGERED")

    dispatchJob?.cancel()
    dispatchJob = viewModelScope.launch {
      for (stage in 1..5) {
        _dispatchStage.value = stage
        delay(600)
      }

      val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
      val newRecord = IncidentRecord(
        id = "INC-" + UUID.randomUUID().toString().take(8).uppercase(),
        timestamp = sdf.format(Date()),
        status = IncidentStatus.DISPATCHED,
        locationLabel = _selectedLocation.value.label,
        coordinates = "${_selectedLocation.value.lat}°N, ${_selectedLocation.value.lng}°W",
        assignedUnit = "Medic 14 - Unit Alpha",
        targetFacility = "Zuckerberg SF General",
        telemetryHash = "SHA256:" + UUID.randomUUID().toString().take(16),
        stagesCompleted = 5
      )
      secureHealthRepository.recordIncident(newRecord)
    }
  }

  fun promptCancelDispatch() {
    _showCancelConfirmDialog.value = true
  }

  fun confirmCancelDispatch() {
    _showCancelConfirmDialog.value = false
    _isArmed.value = false
    _isHolding.value = false
    _holdProgress.value = 0f
    _dispatchStage.value = 0
    dispatchJob?.cancel()
    vibrate(100)
    showToast("DISPATCH CANCELLED")
  }

  fun dismissCancelDialog() {
    _showCancelConfirmDialog.value = false
  }

  fun setTravelMode(mode: TravelMode) {
    _travelMode.value = mode
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedTraumaLevel(level: String) {
    _selectedTraumaLevel.value = level
  }

  fun setDirectoryViewMode(mode: String) {
    _directoryViewMode.value = mode
  }

  fun setSortBy(sort: String) {
    _sortBy.value = sort
  }

  fun setShowQrDialog(show: Boolean) {
    _showQrDialog.value = show
  }

  fun setShowEditProfileDialog(show: Boolean) {
    _showEditProfileDialog.value = show
  }

  fun saveHealthProfile(updated: HealthProfile) {
    viewModelScope.launch {
      secureHealthRepository.saveHealthProfile(updated)
    }
    _showEditProfileDialog.value = false
    showToast("HEALTH CARD ENCRYPTED & SAVED TO ROOM")
  }

  fun submitContactForm(name: String, email: String, type: String, message: String) {
    _contactSuccess.value = true
    showToast("MESSAGE TRANSMITTED")
  }

  fun resetContactForm() {
    _contactSuccess.value = false
  }

  fun clearAllIncidentLogs() {
    viewModelScope.launch {
      secureHealthRepository.clearIncidentLogs()
    }
    showToast("INCIDENT LOG CLEARED")
  }

  fun showToast(msg: String) {
    viewModelScope.launch {
      _toastMessage.value = msg
      delay(2600)
      if (_toastMessage.value == msg) {
        _toastMessage.value = null
      }
    }
  }

  private fun vibrate(durationMs: Long) {
    try {
      val context = getApplication<Application>()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(durationMs)
        }
      }
    } catch (_: Exception) {}
  }

  private fun vibratePattern(timings: LongArray) {
    try {
      val context = getApplication<Application>()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrator?.vibrate(timings, -1)
      }
    } catch (_: Exception) {}
  }
}
