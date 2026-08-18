package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HospitalFacility
import com.example.model.TravelMode
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MedicalQrCodeCanvas(
  modifier: Modifier = Modifier,
  payloadToken: String = "MED-AXM-0148"
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(PureWhite)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val gridSize = 25
      val cellSize = size.width / gridSize

      // Deterministic pseudo-random seed based on token
      val hash = payloadToken.hashCode()
      val random = java.util.Random(hash.toLong())

      // Draw QR Finder Patterns (Top-Left, Top-Right, Bottom-Left)
      fun drawFinderPattern(startX: Int, startY: Int) {
        // 7x7 outer black
        for (r in 0 until 7) {
          for (c in 0 until 7) {
            val isOuter = r == 0 || r == 6 || c == 0 || c == 6
            val isCenter = r in 2..4 && c in 2..4
            if (isOuter || isCenter) {
              drawRect(
                color = Slate950,
                topLeft = Offset((startX + c) * cellSize, (startY + r) * cellSize),
                size = Size(cellSize, cellSize)
              )
            }
          }
        }
      }

      drawFinderPattern(0, 0)
      drawFinderPattern(gridSize - 7, 0)
      drawFinderPattern(0, gridSize - 7)

      // Timing tracks
      for (i in 7 until gridSize - 7) {
        if (i % 2 == 0) {
          drawRect(
            color = Slate950,
            topLeft = Offset(6 * cellSize, i * cellSize),
            size = Size(cellSize, cellSize)
          )
          drawRect(
            color = Slate950,
            topLeft = Offset(i * cellSize, 6 * cellSize),
            size = Size(cellSize, cellSize)
          )
        }
      }

      // Draw Data modules
      for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
          val inTopLeftFinder = r < 8 && c < 8
          val inTopRightFinder = r < 8 && c >= gridSize - 8
          val inBottomLeftFinder = r >= gridSize - 8 && c < 8
          val inCenterBadge = r in 10..14 && c in 10..14

          if (!inTopLeftFinder && !inTopRightFinder && !inBottomLeftFinder && !inCenterBadge) {
            val fill = random.nextBoolean()
            if (fill) {
              drawRect(
                color = Slate950,
                topLeft = Offset(c * cellSize, r * cellSize),
                size = Size(cellSize, cellSize)
              )
            }
          }
        }
      }

      // Center Medical Cross Badge
      val centerPx = size.width / 2f
      val badgeRadius = cellSize * 2.8f
      drawCircle(
        color = SignalRed,
        radius = badgeRadius,
        center = Offset(centerPx, centerPx)
      )
      drawCircle(
        color = PureWhite,
        radius = badgeRadius * 0.9f,
        center = Offset(centerPx, centerPx)
      )
      // Red Cross inside
      val crossW = badgeRadius * 0.5f
      val crossL = badgeRadius * 1.4f
      drawRect(
        color = SignalRed,
        topLeft = Offset(centerPx - crossW / 2, centerPx - crossL / 2),
        size = Size(crossW, crossL)
      )
      drawRect(
        color = SignalRed,
        topLeft = Offset(centerPx - crossL / 2, centerPx - crossW / 2),
        size = Size(crossL, crossW)
      )
    }
  }
}

@Composable
fun TacticalRadarMap(
  hospitals: List<HospitalFacility>,
  selectedFacility: HospitalFacility?,
  travelMode: TravelMode,
  onSelectFacility: (HospitalFacility) -> Unit,
  onNavigateFacility: (HospitalFacility) -> Unit,
  onCallFacility: (HospitalFacility) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Sweep"
  )

  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 6f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Pulse"
  )

  Column(modifier = modifier) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Slate900)
        .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) * 0.45f

        // Radar grid circles
        drawCircle(
          color = Slate800,
          radius = maxRadius * 0.33f,
          center = center,
          style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        )
        drawCircle(
          color = Slate800,
          radius = maxRadius * 0.66f,
          center = center,
          style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        )
        drawCircle(
          color = Slate700,
          radius = maxRadius,
          center = center,
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Crosshairs
        drawLine(
          color = Slate800,
          start = Offset(center.x - maxRadius, center.y),
          end = Offset(center.x + maxRadius, center.y),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = Slate800,
          start = Offset(center.x, center.y - maxRadius),
          end = Offset(center.x, center.y + maxRadius),
          strokeWidth = 1.dp.toPx()
        )

        // Sweeper beam
        val sweepRad = Math.toRadians(sweepAngle.toDouble())
        val beamEnd = Offset(
          center.x + maxRadius * cos(sweepRad).toFloat(),
          center.y + maxRadius * sin(sweepRad).toFloat()
        )
        drawLine(
          brush = Brush.linearGradient(
            colors = listOf(TelemetryCyan.copy(alpha = 0.8f), Color.Transparent),
            start = center,
            end = beamEnd
          ),
          start = center,
          end = beamEnd,
          strokeWidth = 2.dp.toPx()
        )

        // User center location
        drawCircle(
          color = TelemetryCyan.copy(alpha = 0.3f),
          radius = pulseRadius,
          center = center
        )
        drawCircle(
          color = TelemetryCyan,
          radius = 6.dp.toPx(),
          center = center
        )
        drawCircle(
          color = PureWhite,
          radius = 2.5.dp.toPx(),
          center = center
        )

        // Reference center coordinates
        // Dolores Park: (37.7596, -122.4269)
        val userLat = 37.7596
        val userLng = -122.4269

        // Draw Hospital Markers
        hospitals.forEach { hospital ->
          // Map lat/lng delta to radar space
          val dLng = (hospital.lng - userLng) * 110.0 // East-West
          val dLat = (hospital.lat - userLat) * 110.0 // North-South (inverted in screen Y)

          val markerX = center.x + (dLng * (maxRadius / 5.5f)).toFloat()
          val markerY = center.y - (dLat * (maxRadius / 5.5f)).toFloat()
          val markerPos = Offset(markerX, markerY)

          val isSelected = selectedFacility?.id == hospital.id
          val markerColor = when (hospital.level) {
            "Level 1" -> SignalRed
            "Level 2" -> SignalAmber
            else -> TelemetryCyan
          }

          // Trajectory vector line if selected
          if (isSelected) {
            drawLine(
              color = markerColor.copy(alpha = 0.7f),
              start = center,
              end = markerPos,
              strokeWidth = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
          }

          // Marker aura & pin
          drawCircle(
            color = markerColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
            radius = if (isSelected) 14.dp.toPx() else 9.dp.toPx(),
            center = markerPos
          )
          drawCircle(
            color = markerColor,
            radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
            center = markerPos
          )
          drawCircle(
            color = PureWhite,
            radius = 2.dp.toPx(),
            center = markerPos
          )
        }
      }

      // Range indicators
      Text(
        text = "RADAR 6.0 MI RANGE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 8.sp,
          color = Slate400,
          fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(8.dp)
      )

      Text(
        text = "SIMULATED GPS LOCK",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 8.sp,
          color = TelemetryCyan,
          fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
      )
    }

    // Interactive Hospital Selector Chips under radar
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      hospitals.take(3).forEach { hospital ->
        val isSelected = selectedFacility?.id == hospital.id
        val levelColor = if (hospital.level == "Level 1") SignalRed else SignalAmber
        Surface(
          color = if (isSelected) Slate800 else Slate900,
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(
            1.dp,
            if (isSelected) levelColor else Slate800
          ),
          modifier = Modifier
            .weight(1f)
            .clickable { onSelectFacility(hospital) }
            .testTag("radar_chip_${hospital.id}")
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(
              text = hospital.name.split(" ").take(2).joinToString(" "),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                fontSize = 10.sp
              ),
              maxLines = 1
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "${hospital.distance} mi",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  color = Slate400
                )
              )
              Text(
                text = hospital.formatEta(travelMode),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = levelColor
                )
              )
            }
          }
        }
      }
    }

    // Selected Facility Quick Action Card
    selectedFacility?.let { facility ->
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        color = Slate850,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = facility.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = PureWhite,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              )
            }
            Text(
              text = "${facility.level} · ${facility.neighborhood} · ${facility.calculateEtaMinutes(travelMode)} min (${travelMode.shortLabel})",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Slate300,
                fontSize = 11.sp
              )
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
              onClick = { onCallFacility(facility) },
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Slate800)
                .border(1.dp, Slate700, CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call Facility",
                tint = SignalGreen,
                modifier = Modifier.size(16.dp)
              )
            }

            Button(
              onClick = { onNavigateFacility(facility) },
              colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = null,
                tint = Slate950,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "ROUTE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Slate950
                )
              )
            }
          }
        }
      }
    }
  }
}
