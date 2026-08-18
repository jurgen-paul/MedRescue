package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TacticalTopBar(
  titleBadge: String = "SOS",
  statusText: String = "SYSTEM READY",
  statusColor: Color = SignalGreen,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {},
  onActionClick: () -> Unit = {}
) {
  Surface(
    color = Slate900,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Brand Lockup
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { onBackClick() }
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(SignalRedDark, Slate850)
                )
              )
              .border(1.dp, SignalRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HealthAndSafety,
              contentDescription = "MedRescue Mark",
              tint = SignalRed,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "MEDRESCUE",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp,
                  color = PureWhite
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(SignalRed)
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = titleBadge,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                  )
                )
              }
            }
            Text(
              text = "CLINICAL TELEMETRY ACCESS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = Slate400,
                letterSpacing = 1.2.sp
              )
            )
          }
        }

        // Topbar Actions & Live Indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Live indicator pill
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Slate850)
              .border(1.dp, Slate700, RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            PulsingDot(color = statusColor, size = 7.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = statusText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Slate300
              )
            )
          }

          if (showBackButton) {
            IconButton(
              onClick = onBackClick,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Slate850)
                .border(1.dp, Slate700, CircleShape)
                .testTag("topbar_back_button")
            ) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back to SOS HUD",
                tint = Slate300,
                modifier = Modifier.size(18.dp)
              )
            }
          } else {
            IconButton(
              onClick = onActionClick,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Slate850)
                .border(1.dp, Slate700, CircleShape)
                .testTag("topbar_action_button")
            ) {
              Icon(
                imageVector = Icons.Default.PhoneInTalk,
                contentDescription = "Emergency Call",
                tint = SignalAmber,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      HorizontalDivider(color = Slate800, thickness = 1.dp)
    }
  }
}

@Composable
fun StatusStrip(
  leftTag: String = "CHANNEL 07",
  midTag: String = "ENCRYPTED LINK",
  rightTag: String = "TELEMETRY NOMINAL",
  rightIcon: ImageVector = Icons.Default.Sensors,
  rightColor: Color = TelemetryCyan
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Slate950)
      .border(1.dp, Slate850)
      .padding(horizontal = 16.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = leftTag,
        style = MaterialTheme.typography.labelSmall.copy(
          color = Slate400,
          fontWeight = FontWeight.Bold
        )
      )
      Text(
        text = "·",
        color = Slate600
      )
      Text(
        text = midTag,
        style = MaterialTheme.typography.labelSmall.copy(
          color = Slate300,
          fontWeight = FontWeight.SemiBold
        )
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = rightIcon,
        contentDescription = null,
        tint = rightColor,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = rightTag,
        style = MaterialTheme.typography.labelSmall.copy(
          color = rightColor,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
      )
    }
  }
}

@Composable
fun PulsingDot(
  color: Color = SignalGreen,
  size: androidx.compose.ui.unit.Dp = 8.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Alpha"
  )

  Box(
    modifier = Modifier
      .size(size)
      .clip(CircleShape)
      .background(color.copy(alpha = alpha))
  )
}

@Composable
fun ToastNotification(message: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      color = Slate850.copy(alpha = 0.95f),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.5f)),
      shadowElevation = 8.dp
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = TelemetryCyan,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = message,
          style = MaterialTheme.typography.labelMedium.copy(
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        )
      }
    }
  }
}

@Composable
fun BottomNavigationBar(
  currentRoute: String,
  onNavigate: (String) -> Unit
) {
  Surface(
    color = Slate900,
    tonalElevation = 6.dp,
    border = BorderStroke(1.dp, Slate800),
    modifier = Modifier.fillMaxWidth()
  ) {
    NavigationBar(
      containerColor = Slate900,
      tonalElevation = 0.dp,
      modifier = Modifier.height(72.dp)
    ) {
      val items = listOf(
        Triple("sos", "SOS HUD", Icons.Default.Emergency),
        Triple("health_card", "Health Card", Icons.Default.AssignmentInd),
        Triple("emergency_instructions", "First Aid", Icons.Default.MedicalServices),
        Triple("er_directory", "ER Directory", Icons.Default.LocalHospital),
        Triple("incident_log", "Logs", Icons.Default.HistoryEdu),
        Triple("about", "About", Icons.Default.Info)
      )

      items.forEach { (route, label, icon) ->
        val selected = currentRoute == route
        NavigationBarItem(
          selected = selected,
          onClick = { onNavigate(route) },
          icon = {
            Icon(
              imageVector = icon,
              contentDescription = label,
              modifier = Modifier.size(20.dp),
              tint = if (selected) {
                if (route == "sos") SignalRed else TelemetryCyan
              } else Slate400
            )
          },
          label = {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = if (selected) PureWhite else Slate400
              ),
              maxLines = 1
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = if (route == "sos") SignalRed.copy(alpha = 0.2f) else TelemetryCyan.copy(alpha = 0.15f),
            selectedIconColor = PureWhite,
            unselectedIconColor = Slate400
          ),
          modifier = Modifier.testTag("nav_tab_$route")
        )
      }
    }
  }
}
