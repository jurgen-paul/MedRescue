package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EmergencyContact
import com.example.model.HealthProfile
import com.example.ui.components.MedicalQrCodeCanvas
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCardScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.healthProfile.collectAsState()
  val showQrDialog by viewModel.showQrDialog.collectAsState()
  val showEditDialog by viewModel.showEditProfileDialog.collectAsState()

  // Edit Health Profile Dialog
  if (showEditDialog) {
    EditHealthProfileDialog(
      profile = profile,
      onDismiss = { viewModel.setShowEditProfileDialog(false) },
      onSave = { updated -> viewModel.saveHealthProfile(updated) }
    )
  }

  // Responder QR Modal Dialog
  if (showQrDialog) {
    Dialog(onDismissRequest = { viewModel.setShowQrDialog(false) }) {
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "RESPONDER QR PASS",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
              Text(
                text = "ACCESS TOKEN: ${profile.id}",
                style = MaterialTheme.typography.labelSmall.copy(color = TelemetryCyan)
              )
            }

            IconButton(
              onClick = { viewModel.setShowQrDialog(false) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Slate400
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // QR Canvas
          MedicalQrCodeCanvas(
            modifier = Modifier
              .size(220.dp)
              .testTag("health_card_qr_canvas"),
            payloadToken = profile.id
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Emergency medical responders can scan this code to access triage allergies, medications, and contact info.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Slate300,
              fontSize = 11.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("MedRescue Token", "https://medrescue.local/responder?token=${profile.id}")
                clipboard.setPrimaryClip(clip)
                viewModel.showToast("ACCESS LINK COPIED")
              },
              border = BorderStroke(1.dp, Slate700),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("copy_access_link_button")
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Slate300, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("COPY LINK", style = MaterialTheme.typography.labelSmall.copy(color = Slate300, fontWeight = FontWeight.Bold))
            }

            Button(
              onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_SUBJECT, "MedRescue Emergency Health Card")
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "MedRescue Emergency Health Card for ${profile.fullName}\nBlood: ${profile.bloodType}\nToken: ${profile.id}\nEmergency Contact: ${profile.emergencyContact.name} (${profile.emergencyContact.phone})"
                  )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Responder Card"))
              },
              colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("share_health_card_button")
            ) {
              Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("SHARE", style = MaterialTheme.typography.labelSmall.copy(color = Slate950, fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "HEALTH CARD",
      statusText = "ENCRYPTED",
      statusColor = SignalGreen,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 02",
      midTag = "HEALTH CARD",
      rightTag = "ENCRYPTED PROFILE",
      rightColor = TelemetryCyan
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Top Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Health Card",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
          Text(
            text = "Clinical context for first responders.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
            onClick = { viewModel.setShowEditProfileDialog(true) },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Slate850)
              .border(1.dp, Slate700, CircleShape)
              .testTag("edit_health_profile_button")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Profile",
              tint = Slate300,
              modifier = Modifier.size(18.dp)
            )
          }

          Button(
            onClick = { onNavigate("first_responder_qr") },
            colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier
              .height(38.dp)
              .testTag("open_first_responder_pass_button")
          ) {
            Icon(
              imageVector = Icons.Default.QrCode,
              contentDescription = null,
              tint = Slate950,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "RESPONDER QR",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate950
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Quick First Aid & Encryption Security Banner
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SignalGreen.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigate("emergency_instructions") }
          .testTag("first_aid_banner_shortcut")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MedicalServices,
              contentDescription = null,
              tint = SignalGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "ROOM DB SECURED · AES-256 GCM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = SignalGreen,
                  fontSize = 9.sp
                )
              )
              Text(
                text = "Tap to review Emergency CPR & First Aid Protocols →",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Slate300,
                  fontSize = 10.sp
                )
              )
            }
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Patient Identity Hero Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column {
              Text(
                text = "PATIENT IDENTITY",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Slate400,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
              Text(
                text = "DOB: ${profile.dob} · ${profile.pronouns}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Slate300,
                  fontWeight = FontWeight.Medium
                )
              )
            }

            // Blood Type Badge
            Surface(
              color = SignalRedDark.copy(alpha = 0.5f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.6f))
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "BLOOD",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = Slate300
                  )
                )
                Text(
                  text = profile.bloodType,
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Slate800, thickness = 1.dp)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "ORGAN DONOR",
                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
              )
              Text(
                text = if (profile.organDonor) "YES (REGISTERED)" else "NO",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (profile.organDonor) SignalGreen else Slate300
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "TOKEN ID",
                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
              )
              Text(
                text = profile.id,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = TelemetryCyan,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Critical Allergies Panel
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = SignalRed,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "KNOWN ALLERGIES & RESPIRATORY",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = 1.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            profile.allergies.forEach { allergy ->
              Surface(
                color = SignalRedDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.5f))
              ) {
                Text(
                  text = "⚠️ $allergy",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = profile.allergyNote,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Medications Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Medication,
              contentDescription = null,
              tint = SignalAmber,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ACTIVE MEDICATIONS",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = 1.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          profile.medications.forEach { med ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(SignalAmber)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = med,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = Slate300,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Emergency Contact Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "PRIMARY EMERGENCY CONTACT",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Slate400,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = profile.emergencyContact.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
              Text(
                text = "${profile.emergencyContact.relationship} · ${profile.emergencyContact.phone}",
                style = MaterialTheme.typography.bodySmall.copy(color = TelemetryCyan)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = {
                  try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.emergencyContact.phone}"))
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Slate850)
                  .border(1.dp, SignalGreen.copy(alpha = 0.5f), CircleShape)
                  .testTag("call_emergency_contact_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Call,
                  contentDescription = "Call Contact",
                  tint = SignalGreen,
                  modifier = Modifier.size(16.dp)
                )
              }

              IconButton(
                onClick = {
                  try {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${profile.emergencyContact.phone}")).apply {
                      putExtra("sms_body", "MedRescue SOS Alert: I am triggering an emergency notification with my clinical location bundle.")
                    }
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Slate850)
                  .border(1.dp, TelemetryCyan.copy(alpha = 0.5f), CircleShape)
                  .testTag("sms_emergency_contact_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Message,
                  contentDescription = "SMS Contact",
                  tint = TelemetryCyan,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Medical Notes & Physician Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "CLINICAL NOTES & PRIMARY PHYSICIAN",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Slate400,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = profile.primaryPhysician,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = profile.medicalNotes,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun EditHealthProfileDialog(
  profile: HealthProfile,
  onDismiss: () -> Unit,
  onSave: (HealthProfile) -> Unit
) {
  var name by remember { mutableStateOf(profile.fullName) }
  var dob by remember { mutableStateOf(profile.dob) }
  var blood by remember { mutableStateOf(profile.bloodType) }
  var pronouns by remember { mutableStateOf(profile.pronouns) }
  var contactName by remember { mutableStateOf(profile.emergencyContact.name) }
  var contactPhone by remember { mutableStateOf(profile.emergencyContact.phone) }
  var notes by remember { mutableStateOf(profile.medicalNotes) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      color = Slate900,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, Slate700),
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Edit Emergency Health Card",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = PureWhite
          )
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name", color = Slate400) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = Slate100,
            focusedBorderColor = TelemetryCyan,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = blood,
            onValueChange = { blood = it },
            label = { Text("Blood Type", color = Slate400) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = PureWhite,
              unfocusedTextColor = Slate100,
              focusedBorderColor = TelemetryCyan,
              unfocusedBorderColor = Slate700
            ),
            modifier = Modifier.weight(1f).testTag("edit_profile_blood_input")
          )

          OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("DOB", color = Slate400) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = PureWhite,
              unfocusedTextColor = Slate100,
              focusedBorderColor = TelemetryCyan,
              unfocusedBorderColor = Slate700
            ),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = pronouns,
          onValueChange = { pronouns = it },
          label = { Text("Pronouns", color = Slate400) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = Slate100,
            focusedBorderColor = TelemetryCyan,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = contactName,
          onValueChange = { contactName = it },
          label = { Text("Emergency Contact Name", color = Slate400) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = Slate100,
            focusedBorderColor = TelemetryCyan,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = contactPhone,
          onValueChange = { contactPhone = it },
          label = { Text("Emergency Contact Phone", color = Slate400) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = Slate100,
            focusedBorderColor = TelemetryCyan,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Clinical Notes", color = Slate400) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = Slate100,
            focusedBorderColor = TelemetryCyan,
            unfocusedBorderColor = Slate700
          ),
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("CANCEL", color = Slate400)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val updated = profile.copy(
                fullName = name,
                bloodType = blood,
                dob = dob,
                pronouns = pronouns,
                emergencyContact = profile.emergencyContact.copy(name = contactName, phone = contactPhone),
                medicalNotes = notes
              )
              onSave(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
            modifier = Modifier.testTag("save_profile_button")
          ) {
            Text("SAVE CARD", color = Slate950, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
