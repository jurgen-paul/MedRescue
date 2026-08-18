package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.CryptoManager
import com.example.model.HealthProfile
import com.example.ui.components.MedicalQrCodeCanvas
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@Composable
fun FirstResponderQrScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.healthProfile.collectAsState()
  var qrFormatMode by remember { mutableStateOf("triage") } // "triage" or "raw_fhir"
  var isHighContrastMode by remember { mutableStateOf(false) }

  val responderPayload = remember(profile, qrFormatMode) {
    if (qrFormatMode == "raw_fhir") {
      """{"resourceType":"EmergencyTriageSummary","id":"${profile.id}","patient":{"name":"${profile.fullName}","dob":"${profile.dob}","bloodType":"${profile.bloodType}"},"allergies":${profile.allergies.map { "\"$it\"" }},"medications":${profile.medications.map { "\"$it\"" }},"ice":{"name":"${profile.emergencyContact.name}","phone":"${profile.emergencyContact.phone}"},"security":{"storage":"Room-AES256-GCM","sig":"${CryptoManager.generateIntegrityHash(profile.id + profile.fullName)}"}}"""
    } else {
      CryptoManager.createEmergencyResponderPayload(
        id = profile.id,
        name = profile.fullName,
        bloodType = profile.bloodType,
        allergies = profile.allergies,
        medications = profile.medications,
        contactPhone = profile.emergencyContact.phone,
        contactName = profile.emergencyContact.name
      )
    }
  }

  val cryptoHash = remember(responderPayload) {
    CryptoManager.generateIntegrityHash(responderPayload)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (isHighContrastMode) Color(0xFF05070A) else Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "RESPONDER SCAN",
      statusText = "OFFLINE READY",
      statusColor = SignalGreen,
      showBackButton = true,
      onBackClick = { onNavigate("health_card") }
    )

    StatusStrip(
      leftTag = "SECURE PASS",
      midTag = "ROOM AES-256 ENCRYPTED",
      rightTag = "PARAMEDIC SCAN MODE",
      rightColor = SignalAmber
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header Banner
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "FIRST RESPONDER TRIAGE PASS",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                fontSize = 13.sp
              )
            )
            Text(
              text = "Present this scannable token to EMS or ER staff.",
              style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 10.sp)
            )
          }

          // Format Toggle
          Surface(
            color = Slate850,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Slate700)
          ) {
            Row(modifier = Modifier.padding(2.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (qrFormatMode == "triage") TelemetryCyan else Color.Transparent)
                  .clickable { qrFormatMode = "triage" }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("qr_format_triage"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "TRIAGE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (qrFormatMode == "triage") Slate950 else Slate400
                  )
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (qrFormatMode == "raw_fhir") TelemetryCyan else Color.Transparent)
                  .clickable { qrFormatMode = "raw_fhir" }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("qr_format_fhir"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "FHIR",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (qrFormatMode == "raw_fhir") Slate950 else Slate400
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Scannable QR Code Canvas Box (High Contrast White Container)
      Surface(
        color = PureWhite,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(3.dp, if (isHighContrastMode) TelemetryCyan else PureWhite),
        shadowElevation = 10.dp,
        modifier = Modifier
          .size(260.dp)
          .testTag("first_responder_qr_canvas_box")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          MedicalQrCodeCanvas(
            modifier = Modifier
              .size(210.dp)
              .testTag("responder_qr_code_view"),
            payloadToken = responderPayload
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Cryptographic Integrity Strip
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = SignalGreen,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "AES-256 ROOM PERSISTENCE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SignalGreen
              )
            )
          }

          Text(
            text = cryptoHash,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              color = TelemetryCyan
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Critical Clinical Glance Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Patient Identity Top
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = profile.fullName,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
              Text(
                text = "DOB: ${profile.dob} · ${profile.pronouns}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
              )
            }

            // Blood Type Pill
            Surface(
              color = SignalRedDark,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, SignalRed)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "BLOOD ",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Slate300)
                )
                Text(
                  text = profile.bloodType,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Slate800, thickness = 1.dp)
          Spacer(modifier = Modifier.height(10.dp))

          // Key Triage Flags
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Organ Donor
            Surface(
              color = Slate850,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, if (profile.organDonor) SignalGreen.copy(alpha = 0.4f) else Slate700),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "ORGAN DONOR",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Slate400)
                )
                Text(
                  text = if (profile.organDonor) "REGISTERED" else "NO",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (profile.organDonor) SignalGreen else Slate300
                  )
                )
              }
            }

            // Resuscitation Code
            Surface(
              color = Slate850,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.4f)),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "RESUSCITATION",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Slate400)
                )
                Text(
                  text = "FULL CODE",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TelemetryCyan
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Severe Allergies Warning
          Surface(
            color = SignalRedDark.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = SignalRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "ALLERGIES / CONTRAINDICATIONS:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SignalRed,
                    fontSize = 9.sp
                  )
                )
                Text(
                  text = profile.allergies.joinToString(", ") + " · " + profile.allergyNote,
                  style = MaterialTheme.typography.bodySmall.copy(color = PureWhite, fontSize = 10.sp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Emergency Contact Direct Tap
          Surface(
            color = Slate850,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Slate700),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "ICE CONTACT (${profile.emergencyContact.relationship})",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Slate400)
                )
                Text(
                  text = "${profile.emergencyContact.name} · ${profile.emergencyContact.phone}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                  )
                )
              }

              Button(
                onClick = {
                  try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.emergencyContact.phone}"))
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = SignalGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("dial_ice_from_qr_pass")
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = PureWhite, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CALL", style = MaterialTheme.typography.labelSmall.copy(color = PureWhite, fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("MedRescue FHIR Payload", responderPayload)
            clipboard.setPrimaryClip(clip)
            viewModel.showToast("FHIR PAYLOAD COPIED TO CLIPBOARD")
          },
          border = BorderStroke(1.dp, Slate700),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("copy_fhir_payload_button")
        ) {
          Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Slate300, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("COPY FHIR", style = MaterialTheme.typography.labelSmall.copy(color = Slate300, fontWeight = FontWeight.Bold))
        }

        Button(
          onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_SUBJECT, "MedRescue First Responder Emergency Pass")
              putExtra(
                Intent.EXTRA_TEXT,
                "MEDRESCUE EMERGENCY TRIAGE PASS\nPatient: ${profile.fullName}\nDOB: ${profile.dob}\nBlood: ${profile.bloodType}\nAllergies: ${profile.allergies.joinToString(", ")}\nMedications: ${profile.medications.joinToString(", ")}\nICE: ${profile.emergencyContact.name} (${profile.emergencyContact.phone})\nPayload: $responderPayload"
              )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Emergency Pass"))
          },
          colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("share_responder_pass_button")
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("SHARE PASS", style = MaterialTheme.typography.labelSmall.copy(color = Slate950, fontWeight = FontWeight.Bold))
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
