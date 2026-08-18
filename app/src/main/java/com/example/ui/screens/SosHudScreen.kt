package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyRepository
import com.example.model.LocationPreset
import com.example.ui.components.PulsingDot
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosHudScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  val isHolding by viewModel.isHolding.collectAsState()
  val holdProgress by viewModel.holdProgress.collectAsState()
  val isArmed by viewModel.isArmed.collectAsState()
  val dispatchStage by viewModel.dispatchStage.collectAsState()
  val selectedLocation by viewModel.selectedLocation.collectAsState()
  val showCancelDialog by viewModel.showCancelConfirmDialog.collectAsState()

  var locationDropdownExpanded by remember { mutableStateOf(false) }

  // Cancel Confirmation Dialog
  if (showCancelDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissCancelDialog() },
      containerColor = Slate900,
      titleContentColor = PureWhite,
      textContentColor = Slate300,
      icon = {
        Icon(
          imageVector = Icons.Default.WarningAmber,
          contentDescription = null,
          tint = SignalAmber,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "Cancel Emergency Dispatch?",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Text(
          text = "Are you sure you want to abort the active paramedic dispatch and stand down the emergency telemetry channel?",
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = { viewModel.confirmCancelDispatch() },
          colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
          modifier = Modifier.testTag("confirm_cancel_dispatch_button")
        ) {
          Text("ABORT DISPATCH", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { viewModel.dismissCancelDialog() },
          border = BorderStroke(1.dp, Slate700)
        ) {
          Text("KEEP ACTIVE", color = Slate300)
        }
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = if (isArmed) "ARMED" else "SOS",
      statusText = if (isArmed) "DISPATCH ACTIVE" else "SYSTEM READY",
      statusColor = if (isArmed) SignalRed else SignalGreen,
      onActionClick = {
        try {
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
          context.startActivity(intent)
        } catch (_: Exception) {}
      }
    )

    StatusStrip(
      leftTag = "CHANNEL 07",
      midTag = "ENCRYPTED LINK",
      rightTag = if (isArmed) "PARAMEDIC DISPATCHED" else "TELEMETRY NOMINAL",
      rightColor = if (isArmed) SignalRed else TelemetryCyan
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Intro Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isArmed) SignalRed.copy(alpha = 0.4f) else Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            PulsingDot(color = if (isArmed) SignalRed else SignalGreen, size = 8.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isArmed) "CRITICAL INCIDENT ACTIVE" else "SYSTEM READY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isArmed) SignalRed else SignalGreen,
                letterSpacing = 1.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "In the event of an accident, cardiac event, stroke, or injury, trigger SOS dispatch instantly. The system compiles your clinical medical card, captures GPS telemetry, discovers the closest trauma center, and notifies paramedics.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Slate300, lineHeight = 20.sp)
          )

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Slate800, thickness = 1.dp)
          Spacer(modifier = Modifier.height(12.dp))

          // Location Telemetry Box
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = TelemetryCyan,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "GPS COORDINATES",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${selectedLocation.lat}°N, ${selectedLocation.lng}°W",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
            }

            // Location Selector Dropdown
            Box {
              Surface(
                color = Slate850,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { locationDropdownExpanded = true }
                  .testTag("location_selector_dropdown")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = selectedLocation.label.take(18) + "...",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Slate300,
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Location",
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              DropdownMenu(
                expanded = locationDropdownExpanded,
                onDismissRequest = { locationDropdownExpanded = false },
                modifier = Modifier.background(Slate900).border(1.dp, Slate700)
              ) {
                EmergencyRepository.locationPresets.forEach { preset ->
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(
                          text = preset.label,
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                          )
                        )
                        Text(
                          text = "${preset.lat}°N, ${preset.lng}°W",
                          style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                      }
                    },
                    onClick = {
                      viewModel.setLocation(preset)
                      locationDropdownExpanded = false
                    },
                    modifier = Modifier.testTag("location_option_${preset.neighborhood}")
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Tactical SOS Trigger Button Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ArmedGlow")
        val armedPulse by infiniteTransition.animateFloat(
          initialValue = 0.85f,
          targetValue = 1.08f,
          animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
          ),
          label = "ArmedPulse"
        )

        // Outer Glow Rings
        val ringScale = if (isArmed) armedPulse else if (isHolding) 1.04f else 1f

        Box(
          modifier = Modifier
            .size((210 * ringScale).dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  if (isArmed) SignalRed.copy(alpha = 0.4f)
                  else if (isHolding) SignalAmber.copy(alpha = 0.35f)
                  else SignalRed.copy(alpha = 0.12f),
                  Color.Transparent
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          // Circular Progress Track Canvas
          Canvas(modifier = Modifier.size(190.dp)) {
            val strokeW = 6.dp.toPx()
            val canvasCenter = Offset(size.width / 2, size.height / 2)
            val canvasRadius = (size.width - strokeW) / 2

            // Background Track
            drawCircle(
              color = Slate800,
              radius = canvasRadius,
              style = Stroke(width = strokeW)
            )

            // Dynamic Progress Arc
            if (isHolding || isArmed) {
              val sweep = if (isArmed) 360f else (holdProgress * 360f)
              drawArc(
                brush = Brush.sweepGradient(
                  colors = listOf(SignalAmber, SignalRed, SignalRedDark)
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeW / 2, strokeW / 2),
                size = Size(size.width - strokeW, size.height - strokeW),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
              )
            }
          }

          // Main Interactive Center SOS Button
          Box(
            modifier = Modifier
              .size(160.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = if (isArmed) listOf(SignalRed, SignalRedDark)
                  else if (isHolding) listOf(SignalAmberDark, Slate900)
                  else listOf(Slate850, Slate900)
                )
              )
              .border(
                BorderStroke(
                  2.dp,
                  if (isArmed) SignalRed else if (isHolding) SignalAmber else Slate700
                ),
                CircleShape
              )
              .pointerInput(isArmed) {
                if (!isArmed) {
                  detectTapGestures(
                    onPress = {
                      viewModel.startHoldingSos()
                      tryAwaitRelease()
                      viewModel.releaseHoldingSos()
                    }
                  )
                }
              }
              .testTag("sos_main_button"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isArmed) Icons.Default.Campaign else Icons.Default.Emergency,
                contentDescription = null,
                tint = if (isArmed) PureWhite else if (isHolding) SignalAmber else SignalRed,
                modifier = Modifier.size(42.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (isArmed) "DISPATCHED" else if (isHolding) "HOLDING..." else "HOLD SOS",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.5.sp,
                  color = PureWhite
                )
              )
              if (isHolding && !isArmed) {
                Text(
                  text = "${(2.0 - holdProgress * 2.0).coerceAtLeast(0.0).let { String.format("%.1fs", it) }}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SignalAmber,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }
        }
      }

      // Status Note under SOS Button
      Text(
        text = if (isArmed) "Dispatch channel opened. Paramedics notified."
        else "Press and hold for 2 seconds to trigger emergency dispatch",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = if (isArmed) SignalRed else Slate400,
          fontWeight = if (isArmed) FontWeight.Bold else FontWeight.Normal,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
      )

      if (isArmed) {
        Spacer(modifier = Modifier.height(14.dp))
        Button(
          onClick = { viewModel.promptCancelDispatch() },
          colors = ButtonDefaults.buttonColors(containerColor = Slate800),
          border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(40.dp)
            .testTag("cancel_dispatch_trigger_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = SignalRed,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "CANCEL DISPATCH",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SignalRed,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Dispatch Protocol Checklist Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = TelemetryCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "DISPATCH PROTOCOL",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite,
                  letterSpacing = 1.sp
                )
              )
            }

            if (isArmed) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(SignalRed.copy(alpha = 0.2f))
                  .border(1.dp, SignalRed, RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "LIVE PROTOCOL",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SignalRed,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          EmergencyRepository.dispatchProtocolSteps.forEachIndexed { index, step ->
            val stepNumber = index + 1
            val isCompleted = isArmed && dispatchStage >= stepNumber
            val isCurrent = isArmed && dispatchStage == stepNumber

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Step number or check badge
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (isCompleted) SignalGreen
                    else if (isCurrent) SignalAmber
                    else Slate800
                  )
                  .border(
                    1.dp,
                    if (isCompleted) SignalGreen else if (isCurrent) SignalAmber else Slate700,
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isCompleted) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Slate950,
                    modifier = Modifier.size(14.dp)
                  )
                } else {
                  Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isCurrent) Slate950 else Slate300
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = step.title,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCompleted || isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCompleted) PureWhite else if (isCurrent) SignalAmber else Slate300
                  )
                )
                if (isCurrent || isCompleted) {
                  Text(
                    text = step.detail,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = Slate400
                    )
                  )
                }
              }
            }

            if (index < EmergencyRepository.dispatchProtocolSteps.size - 1) {
              Box(
                modifier = Modifier
                  .padding(start = 11.dp)
                  .width(2.dp)
                  .height(8.dp)
                  .background(if (isCompleted) SignalGreen.copy(alpha = 0.5f) else Slate800)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Quick Console Shortcuts
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = Slate900,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.4f)),
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigate("first_responder_qr") }
            .testTag("quick_shortcut_responder_qr")
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TelemetryCyan.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = TelemetryCyan,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "QR Pass",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = PureWhite,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              )
              Text(
                text = "Paramedic Scan",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 9.sp)
              )
            }
          }
        }

        Surface(
          color = Slate900,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.4f)),
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigate("emergency_instructions") }
            .testTag("quick_shortcut_first_aid")
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SignalRed.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = SignalRed,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "First Aid",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = PureWhite,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              )
              Text(
                text = "CPR Metronome",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 9.sp)
              )
            }
          }
        }

        Surface(
          color = Slate900,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.4f)),
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigate("er_directory") }
            .testTag("quick_shortcut_er_directory")
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SignalAmber.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = SignalAmber,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Trauma ER",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = PureWhite,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              )
              Text(
                text = "5 Centers",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 9.sp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
