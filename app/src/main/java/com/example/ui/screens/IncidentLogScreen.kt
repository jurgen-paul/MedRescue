package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.IncidentRecord
import com.example.model.IncidentStatus
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@Composable
fun IncidentLogScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val incidents by viewModel.incidentLogs.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "INCIDENT AUDIT",
      statusText = "${incidents.size} LOGGED",
      statusColor = TelemetryCyan,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 04",
      midTag = "INCIDENT LOG",
      rightTag = "TELEMETRY AUDIT",
      rightColor = TelemetryCyan
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Incident Log",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PureWhite
              )
            )
            Text(
              text = "Audit trail of telemetry transmissions.",
              style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
            )
          }

          if (incidents.isNotEmpty()) {
            OutlinedButton(
              onClick = { viewModel.clearAllIncidentLogs() },
              border = BorderStroke(1.dp, Slate700),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("clear_incident_logs_button")
            ) {
              Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("CLEAR", style = MaterialTheme.typography.labelSmall.copy(color = Slate300))
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      if (incidents.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 60.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.HistoryEdu,
                contentDescription = null,
                tint = Slate700,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "NO INCIDENTS RECORDED",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Slate400
                )
              )
              Text(
                text = "Emergency SOS dispatches and telemetry streams will appear here.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(incidents, key = { it.id }) { incident ->
          IncidentCard(incident)
          Spacer(modifier = Modifier.height(12.dp))
        }
      }

      item {
        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }
}

@Composable
fun IncidentCard(incident: IncidentRecord) {
  val statusColor = when (incident.status) {
    IncidentStatus.DISPATCHED -> SignalRed
    IncidentStatus.ARMING -> SignalAmber
    IncidentStatus.RESOLVED -> SignalGreen
    IncidentStatus.CANCELLED -> Slate400
    else -> TelemetryCyan
  }

  Surface(
    color = Slate900,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, Slate800),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("incident_record_${incident.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = incident.id,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
        }

        Surface(
          color = statusColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
          Text(
            text = incident.status.name,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = statusColor,
              fontSize = 9.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Slate850)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "TIMESTAMP",
            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 9.sp)
          )
          Text(
            text = incident.timestamp,
            style = MaterialTheme.typography.bodySmall.copy(color = PureWhite, fontWeight = FontWeight.Bold)
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "DISPATCH UNIT",
            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 9.sp)
          )
          Text(
            text = incident.assignedUnit,
            style = MaterialTheme.typography.bodySmall.copy(color = PureWhite, fontWeight = FontWeight.Bold)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "LOCATION: ${incident.locationLabel} (${incident.coordinates})",
        style = MaterialTheme.typography.bodySmall.copy(color = Slate300, fontSize = 11.sp)
      )

      Text(
        text = "TARGET ER: ${incident.targetFacility}",
        style = MaterialTheme.typography.bodySmall.copy(color = Slate300, fontSize = 11.sp)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = incident.telemetryHash,
        style = MaterialTheme.typography.labelSmall.copy(
          color = TelemetryCyan,
          fontSize = 9.sp
        )
      )
    }
  }
}
