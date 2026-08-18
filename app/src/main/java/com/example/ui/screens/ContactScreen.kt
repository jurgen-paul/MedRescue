package com.example.ui.screens

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
import com.example.model.FaqItem
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val isSubmitted by viewModel.contactSuccess.collectAsState()

  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var inquiryType by remember { mutableStateOf("Integration & API") }
  var message by remember { mutableStateOf("") }
  var typeDropdownExpanded by remember { mutableStateOf(false) }

  val inquiryOptions = listOf(
    "Integration & API",
    "Enterprise Dispatch",
    "Clinical Compliance",
    "Security & HIPAA",
    "General Support"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "OPERATIONS",
      statusText = "CHANNEL OPEN",
      statusColor = SignalGreen,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 06",
      midTag = "CONTACT & OPS",
      rightTag = "NON-EMERGENCY ONLY",
      rightColor = SignalAmber
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Emergency warning banner
      Surface(
        color = SignalAmberDark.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = SignalAmber,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "NON-EMERGENCY COMMUNICATION ONLY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SignalAmber,
                letterSpacing = 1.sp
              )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "If you are experiencing a medical emergency, call 911 immediately. This operations channel is for technical integration, EMS deployment, and institutional queries.",
              style = MaterialTheme.typography.bodySmall.copy(color = Slate300, lineHeight = 16.sp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Contact Form Card
      Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "TALK TO THE RESPONSE TEAM",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
          Text(
            text = "Connect with clinical engineers and CAD dispatch specialists.",
            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
          )

          Spacer(modifier = Modifier.height(16.dp))

          if (isSubmitted) {
            Surface(
              color = Slate850,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, SignalGreen.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = SignalGreen,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "INQUIRY TRANSMITTED",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Our operations desk will review your message within 1 business day.",
                  style = MaterialTheme.typography.bodySmall.copy(color = Slate300),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                  onClick = { viewModel.resetContactForm() },
                  border = BorderStroke(1.dp, Slate700)
                ) {
                  Text("SEND ANOTHER MESSAGE", color = Slate300)
                }
              }
            }
          } else {
            // Form Fields
            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text("Your Name", color = Slate400) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = Slate100,
                focusedBorderColor = TelemetryCyan,
                unfocusedBorderColor = Slate700
              ),
              modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email Address", color = Slate400) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = Slate100,
                focusedBorderColor = TelemetryCyan,
                unfocusedBorderColor = Slate700
              ),
              modifier = Modifier.fillMaxWidth().testTag("contact_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Inquiry Type Dropdown
            Box {
              OutlinedTextField(
                value = inquiryType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Inquiry Type", color = Slate400) },
                trailingIcon = {
                  IconButton(onClick = { typeDropdownExpanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate400)
                  }
                },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = Slate100,
                  focusedBorderColor = TelemetryCyan,
                  unfocusedBorderColor = Slate700
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { typeDropdownExpanded = true }
                  .testTag("contact_type_selector")
              )

              DropdownMenu(
                expanded = typeDropdownExpanded,
                onDismissRequest = { typeDropdownExpanded = false },
                modifier = Modifier.background(Slate900).border(1.dp, Slate700)
              ) {
                inquiryOptions.forEach { option ->
                  DropdownMenuItem(
                    text = { Text(option, color = PureWhite) },
                    onClick = {
                      inquiryType = option
                      typeDropdownExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = message,
              onValueChange = { message = it },
              label = { Text("Message & Objectives", color = Slate400) },
              minLines = 3,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = Slate100,
                focusedBorderColor = TelemetryCyan,
                unfocusedBorderColor = Slate700
              ),
              modifier = Modifier.fillMaxWidth().testTag("contact_message_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.submitContactForm(name, email, inquiryType, message)
              },
              enabled = name.isNotBlank() && email.isNotBlank(),
              colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("contact_submit_button")
            ) {
              Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("TRANSMIT MESSAGE", style = MaterialTheme.typography.labelMedium.copy(color = Slate950, fontWeight = FontWeight.Bold))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "FREQUENTLY ASKED QUESTIONS",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Slate400,
          letterSpacing = 1.sp
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // FAQ Accordions
      EmergencyRepository.faqList.forEach { faq ->
        FaqAccordionItem(faq)
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun FaqAccordionItem(faq: FaqItem) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    color = Slate900,
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, Slate800),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { expanded = !expanded }
      .testTag("faq_item_${faq.number}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = faq.number,
            style = MaterialTheme.typography.labelSmall.copy(
              color = TelemetryCyan,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = faq.question,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = PureWhite,
              fontSize = 13.sp
            )
          )
        }

        Icon(
          imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = Slate400,
          modifier = Modifier.size(20.dp)
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column {
          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Slate800, thickness = 1.dp)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = faq.answer,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Slate300,
              lineHeight = 18.sp
            )
          )
        }
      }
    }
  }
}
