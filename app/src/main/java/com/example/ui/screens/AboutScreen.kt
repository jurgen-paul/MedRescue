package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyRepository
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@Composable
fun AboutScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "ARCHITECTURE",
      statusText = "SPECIFICATION",
      statusColor = TelemetryCyan,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 05",
      midTag = "ARCHITECTURE",
      rightTag = "5-STAGE PIPELINE",
      rightColor = TelemetryCyan
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Hero
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "SYSTEM ARCHITECTURE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TelemetryCyan,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Medical context, delivered faster.",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Traditional emergency calls rely on voice triage, manual address lookup, and verbal summaries of medical history. MedRescue demonstrates an emergency console where clinical telemetry, facility discovery, and encrypted health profiles are coordinated automatically in seconds.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Slate300, lineHeight = 20.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "5-STAGE DISPATCH PIPELINE",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Slate400,
          letterSpacing = 1.sp
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 5 Workflow Steps
      EmergencyRepository.workflowSteps.forEach { step ->
        Surface(
          color = Slate900,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Slate800),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("about_step_${step.number}")
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Slate800)
                .border(1.dp, TelemetryCyan.copy(alpha = 0.5f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = step.number,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = TelemetryCyan
                )
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite,
                  fontSize = 14.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, lineHeight = 17.sp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "INTEGRATED DATA LAYERS",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Slate400,
          letterSpacing = 1.sp
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Data Layers
      EmergencyRepository.dataLayers.forEach { layer ->
        Surface(
          color = Slate900,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Slate800),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = layer.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TelemetryCyan,
                fontSize = 13.sp
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = layer.copy,
              style = MaterialTheme.typography.bodySmall.copy(color = Slate300, lineHeight = 17.sp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
