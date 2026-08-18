package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.ToastNotification
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.MedRescueViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MedRescueViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val currentRoute by viewModel.currentRoute.collectAsState()
        val toastMessage by viewModel.toastMessage.collectAsState()

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
          bottomBar = {
            BottomNavigationBar(
              currentRoute = currentRoute,
              onNavigate = { route -> viewModel.navigateTo(route) }
            )
          },
          contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .background(Slate950)
          ) {
            // Main Screen Routing with Crossfade Animation
            Crossfade(
              targetState = currentRoute,
              label = "ScreenNavigation"
            ) { route ->
              when (route) {
                "sos" -> SosHudScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "health_card" -> HealthCardScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "first_responder_qr" -> FirstResponderQrScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "emergency_instructions" -> EmergencyInstructionsScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "er_directory" -> ErDirectoryScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "incident_log" -> IncidentLogScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "about" -> AboutScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                "contact" -> ContactScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
                else -> SosHudScreen(
                  viewModel = viewModel,
                  onNavigate = { viewModel.navigateTo(it) }
                )
              }
            }

            // Global Toast Notification
            toastMessage?.let { msg ->
              ToastNotification(message = msg)
            }
          }
        }
      }
    }
  }
}
