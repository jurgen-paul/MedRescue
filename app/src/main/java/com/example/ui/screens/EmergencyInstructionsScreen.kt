package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel
import kotlinx.coroutines.delay

data class EmergencyInstructionCategory(
  val id: String,
  val title: String,
  val urgencyBadge: String,
  val badgeColor: Color,
  val icon: ImageVector,
  val summary: String,
  val steps: List<String>,
  val clinicalNote: String
)

@Composable
fun EmergencyInstructionsScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedCategory by remember { mutableStateOf<String?>("cpr") }
  var isCprMetronomeActive by remember { mutableStateOf(false) }
  var cprBeatCount by remember { mutableStateOf(0) }

  // 110 BPM Metronome Loop (~545ms per beat)
  LaunchedEffect(isCprMetronomeActive) {
    if (isCprMetronomeActive) {
      while (true) {
        delay(545L)
        cprBeatCount = (cprBeatCount + 1) % 30
      }
    } else {
      cprBeatCount = 0
    }
  }

  val instructionsList = remember {
    listOf(
      EmergencyInstructionCategory(
        id = "cpr",
        title = "Adult CPR & Chest Compressions",
        urgencyBadge = "CRITICAL 110 BPM",
        badgeColor = SignalRed,
        icon = Icons.Default.Favorite,
        summary = "Immediate high-quality chest compressions to maintain cerebral and coronary perfusion.",
        steps = listOf(
          "Check scene safety & responsiveness. Tap shoulders and shout: 'Are you OK?'.",
          "Call 911 immediately and send someone to retrieve an automated external defibrillator (AED).",
          "Position hands centered on lower half of breastbone. Interlock fingers, keep elbows locked.",
          "Compress hard & fast: 2 inches (5 cm) deep at 100-120 BPM cadence (follow the interactive metronome below).",
          "Allow full chest recoil between compressions. Do NOT lean on the chest.",
          "Give 30 compressions followed by 2 rescue breaths (if trained) or maintain continuous Hands-Only CPR."
        ),
        clinicalNote = "Hands-Only CPR is clinically proven as effective as conventional CPR in the first critical minutes of out-of-hospital cardiac arrest."
      ),
      EmergencyInstructionCategory(
        id = "bleeding",
        title = "Severe Bleeding & Tourniquet",
        urgencyBadge = "STOP THE BLEED",
        badgeColor = SignalRed,
        icon = Icons.Default.Bloodtype,
        summary = "Arterial hemorrhage control protocol to prevent hemorrhagic shock.",
        steps = listOf(
          "Apply immediate direct, firm bilateral pressure on the wound using clean gauze or cloth.",
          "If bleeding is deep into a cavity, firmly pack gauze directly into the wound base.",
          "For severe extremity bleeding not controlled by direct pressure, apply a commercial Tourniquet (CAT/SOFTT).",
          "Place tourniquet 2-3 inches above the wound (never over a joint).",
          "Pull strap tight, twist windlass rod until bright red bleeding stops and distal pulse is absent.",
          "Lock windlass in clip and write exact application time (e.g. 'T: 14:32') on patient's forehead or tourniquet band."
        ),
        clinicalNote = "Do NOT loosen or remove a tourniquet once applied. Only surgical trauma staff should release arterial occlusions."
      ),
      EmergencyInstructionCategory(
        id = "anaphylaxis",
        title = "Anaphylaxis & Auto-Injector",
        urgencyBadge = "IMMEDIATE EPI",
        badgeColor = SignalAmber,
        icon = Icons.Default.Medication,
        summary = "Rapid epinephrine administration for severe systemic allergic reactions.",
        steps = listOf(
          "Recognize symptoms: Hives, facial swelling, difficulty breathing, wheezing, throat tightness, dizziness.",
          "Grasp Epinephrine Auto-Injector (EpiPen) with blue safety release pointing UP and orange tip DOWN.",
          "Pull off the blue safety cap with a firm straight pull.",
          "Position orange tip against the outer mid-thigh at a 90° right angle (can be given through clothing).",
          "Push firmly until a loud 'click' is heard. Hold firmly in place for 3 full seconds.",
          "Remove injector and massage the injection site for 10 seconds. Call 911 immediately even if symptoms improve."
        ),
        clinicalNote = "A second dose may be administered 5 to 15 minutes after the first if emergency EMS has not arrived and severe symptoms persist."
      ),
      EmergencyInstructionCategory(
        id = "choking",
        title = "Choking & Airway Obstruction",
        urgencyBadge = "AIRWAY BLOCKED",
        badgeColor = SignalAmber,
        icon = Icons.Default.AirlineSeatReclineExtra,
        summary = "Heimlich maneuver protocol for conscious vs unresponsive choking victims.",
        steps = listOf(
          "Ask: 'Are you choking? Can you speak?'. If patient can cough forcefully, encourage coughing.",
          "If patient cannot breathe, speak, or cough: Stand behind them and wrap arms around their waist.",
          "Make a fist with one hand, placing thumb side slightly above the navel and well below breastbone.",
          "Grasp your fist with your other hand. Perform quick, upward and inward abdominal thrusts.",
          "Continue thrusts until the foreign object is expelled or the person becomes unresponsive.",
          "If unresponsive: Lower person safely to floor, call 911, and immediately start CPR compressions (look in mouth before breaths)."
        ),
        clinicalNote = "For pregnant individuals or obese patients, perform chest thrusts positioned over the middle of the sternum instead of abdominal thrusts."
      ),
      EmergencyInstructionCategory(
        id = "stroke",
        title = "Stroke - F.A.S.T. Assessment",
        urgencyBadge = "TIME CRITICAL",
        badgeColor = TelemetryCyan,
        icon = Icons.Default.Psychology,
        summary = "Rapid neurological triage protocol for acute ischemic or hemorrhagic stroke.",
        steps = listOf(
          "F - FACE DROOPING: Ask the person to smile. Does one side of the face droop or feel numb?",
          "A - ARM WEAKNESS: Ask them to raise both arms for 10 seconds. Does one arm drift downward?",
          "S - SPEECH DIFFICULTY: Ask them to repeat: 'The sky is blue in California'. Is speech slurred or strange?",
          "T - TIME TO CALL 911: If any symptom is present, call 911 immediately. Note the exact time symptoms started.",
          "Keep patient lying flat with head slightly elevated (15-30°). Do NOT give food, water, or aspirin."
        ),
        clinicalNote = "Thrombolytic (tPA) therapies have a strict 3-to-4.5-hour therapeutic window from last known well time."
      ),
      EmergencyInstructionCategory(
        id = "seizure",
        title = "Seizure First Aid & Safety",
        urgencyBadge = "TRAUMA SHIELD",
        badgeColor = SignalGreen,
        icon = Icons.Default.Shield,
        summary = "Protection and post-ictal recovery protocol for active convulsive seizures.",
        steps = listOf(
          "Ease the person gently to the floor. Clear sharp, hard, or hot objects from the immediate area.",
          "Turn the person gently onto their side into the Recovery Position to keep airway clear of saliva/vomit.",
          "Place a soft jacket or folded towel under their head to prevent traumatic head strike.",
          "Loosen tight neckwear (collars, ties, scarves). Remove eyeglasses.",
          "NEVER hold the person down or attempt to stop their involuntary movements.",
          "NEVER put any objects, fingers, or medication into their mouth.",
          "Time the seizure. Call 911 if seizure lasts longer than 5 minutes or if a second seizure begins immediately."
        ),
        clinicalNote = "After the seizure ends, stay calmly with the person until they are fully awake and alert."
      )
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "EMERGENCY PROTOCOLS",
      statusText = "FIRST AID GUIDANCE",
      statusColor = SignalRed,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 07",
      midTag = "FIRST AID PROTOCOLS",
      rightTag = "AHA / ERC COMPLIANT",
      rightColor = SignalAmber
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      // Emergency Quick Dial Strip
      item {
        Surface(
          color = SignalRedDark.copy(alpha = 0.25f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SignalRed),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Emergency,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "LIFE-THREATENING EMERGENCY?",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SignalRed,
                    fontSize = 10.sp
                  )
                )
                Text(
                  text = "Call 911 immediately before or during first aid.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PureWhite,
                    fontSize = 11.sp
                  )
                )
              }
            }

            Button(
              onClick = {
                try {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                  context.startActivity(intent)
                } catch (_: Exception) {}
              },
              colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("dial_911_instruction_button")
            ) {
              Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("CALL 911", style = MaterialTheme.typography.labelSmall.copy(color = PureWhite, fontWeight = FontWeight.Bold))
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Interactive CPR Metronome Widget
      item {
        Surface(
          color = Slate900,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(
            1.dp,
            if (isCprMetronomeActive) SignalRed else Slate800
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cpr_metronome_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "MetronomePulse")
                val pulseScale by infiniteTransition.animateFloat(
                  initialValue = 1f,
                  targetValue = if (isCprMetronomeActive) 1.25f else 1f,
                  animationSpec = infiniteRepeatable(
                    animation = tween(272, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                  ),
                  label = "pulse"
                )

                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .scale(if (isCprMetronomeActive) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(if (isCprMetronomeActive) SignalRed else Slate800)
                    .border(
                      1.dp,
                      if (isCprMetronomeActive) SignalRed else TelemetryCyan.copy(alpha = 0.5f),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isCprMetronomeActive) PureWhite else TelemetryCyan,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Text(
                    text = "CPR CADENCE METRONOME",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = PureWhite,
                      fontSize = 13.sp
                    )
                  )
                  Text(
                    text = "AHA Standard: 110 Compressions / Minute",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = if (isCprMetronomeActive) SignalRed else Slate400,
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Button(
                onClick = { isCprMetronomeActive = !isCprMetronomeActive },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isCprMetronomeActive) SignalRed else Slate800
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isCprMetronomeActive) SignalRed else TelemetryCyan),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("toggle_cpr_metronome_button")
              ) {
                Icon(
                  imageVector = if (isCprMetronomeActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isCprMetronomeActive) "PAUSE" else "START",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                  )
                )
              }
            }

            if (isCprMetronomeActive) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Slate850)
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "COMPRESSION CYCLE:",
                  style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                )

                Text(
                  text = "${cprBeatCount + 1} / 30",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (cprBeatCount >= 28) SignalAmber else SignalRed,
                    fontSize = 15.sp
                  )
                )

                Text(
                  text = if (cprBeatCount >= 28) "PREPARE 2 BREATHS" else "PUSH 2 INCHES DEEP",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "EMERGENCY CLINICAL PROTOCOLS",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.sp
          )
        )

        Spacer(modifier = Modifier.height(10.dp))
      }

      // Protocols List
      items(instructionsList, key = { it.id }) { item ->
        val isExpanded = selectedCategory == item.id

        Surface(
          color = Slate900,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(
            1.dp,
            if (isExpanded) item.badgeColor.copy(alpha = 0.8f) else Slate800
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
              selectedCategory = if (isExpanded) null else item.id
            }
            .testTag("instruction_card_${item.id}")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(item.badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, item.badgeColor.copy(alpha = 0.5f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.badgeColor,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = PureWhite,
                      fontSize = 14.sp
                    )
                  )
                  Text(
                    text = item.urgencyBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = item.badgeColor,
                      fontSize = 9.sp
                    )
                  )
                }
              }

              Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = item.summary,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Slate300,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            )

            AnimatedVisibility(visible = isExpanded) {
              Column {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                  text = "ACTION STEPS:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TelemetryCyan,
                    letterSpacing = 0.5.sp
                  )
                )

                Spacer(modifier = Modifier.height(8.dp))

                item.steps.forEachIndexed { index, step ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Box(
                      modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(1.dp, item.badgeColor.copy(alpha = 0.5f), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = PureWhite,
                          fontSize = 9.sp
                        )
                      )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                      text = step,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate100,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                  color = Slate850,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, Slate700),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Icon(
                      imageVector = Icons.Default.Info,
                      contentDescription = null,
                      tint = TelemetryCyan,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = item.clinicalNote,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate300,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                      )
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
