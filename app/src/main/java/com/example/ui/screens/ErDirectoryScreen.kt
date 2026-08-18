package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HospitalFacility
import com.example.model.TravelMode
import com.example.ui.components.StatusStrip
import com.example.ui.components.TacticalRadarMap
import com.example.ui.components.TacticalTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedRescueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErDirectoryScreen(
  viewModel: MedRescueViewModel,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  val hospitals by viewModel.filteredHospitals.collectAsState()
  val travelMode by viewModel.travelMode.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedTraumaLevel by viewModel.selectedTraumaLevel.collectAsState()
  val viewMode by viewModel.directoryViewMode.collectAsState()
  val selectedLocation by viewModel.selectedLocation.collectAsState()

  var selectedFacilityForRadar by remember { mutableStateOf<HospitalFacility?>(null) }

  LaunchedEffect(hospitals) {
    if (selectedFacilityForRadar == null && hospitals.isNotEmpty()) {
      selectedFacilityForRadar = hospitals.first()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    TacticalTopBar(
      titleBadge = "FACILITIES",
      statusText = "${hospitals.size} ACTIVE",
      statusColor = SignalGreen,
      showBackButton = true,
      onBackClick = { onNavigate("sos") }
    )

    StatusStrip(
      leftTag = "MODULE 03",
      midTag = "ER DIRECTORY",
      rightTag = "${selectedLocation.neighborhood.uppercase()} · ${selectedLocation.lat}°N",
      rightColor = TelemetryCyan
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Header Info & Controls
      item {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "ER Directory",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              )
              Text(
                text = "${hospitals.size} facilities near ${selectedLocation.label.split(" ").first()}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
              )
            }

            // View Mode Toggle (List vs Radar Map)
            Surface(
              color = Slate900,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, Slate700)
            ) {
              Row(modifier = Modifier.padding(2.dp)) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == "list") TelemetryCyan else Color.Transparent)
                    .clickable { viewModel.setDirectoryViewMode("list") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("er_view_mode_list"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "List View",
                    tint = if (viewMode == "list") Slate950 else Slate400,
                    modifier = Modifier.size(16.dp)
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == "map") TelemetryCyan else Color.Transparent)
                    .clickable { viewModel.setDirectoryViewMode("map") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("er_view_mode_map"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar View",
                    tint = if (viewMode == "map") Slate950 else Slate400,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Search Bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search facility, specialty, or area...", color = Slate400) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                }
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = PureWhite,
              unfocusedTextColor = Slate100,
              focusedBorderColor = TelemetryCyan,
              unfocusedBorderColor = Slate700,
              focusedContainerColor = Slate900,
              unfocusedContainerColor = Slate900
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("er_search_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Trauma Level Filters
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("All levels", "Level 1", "Level 2", "Level 3").forEach { level ->
              val isSelected = selectedTraumaLevel == level
              Surface(
                color = if (isSelected) Slate800 else Slate900,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                  1.dp,
                  if (isSelected) {
                    if (level == "Level 1") SignalRed else TelemetryCyan
                  } else Slate800
                ),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { viewModel.setSelectedTraumaLevel(level) }
                  .testTag("filter_${level.lowercase().replace(" ", "_")}")
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = level,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) PureWhite else Slate400,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Travel Mode Selector Chips
          Surface(
            color = Slate900,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              TravelMode.values().forEach { mode ->
                val isSelected = travelMode == mode
                Surface(
                  color = if (isSelected) Slate800 else Color.Transparent,
                  shape = RoundedCornerShape(8.dp),
                  border = if (isSelected) BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.6f)) else null,
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.setTravelMode(mode) }
                    .testTag("mode_${mode.name.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = when (mode) {
                        TravelMode.WALKING -> Icons.Default.DirectionsWalk
                        TravelMode.DRIVING -> Icons.Default.DirectionsCar
                        TravelMode.AMBULANCE -> Icons.Default.Emergency
                      },
                      contentDescription = null,
                      tint = if (isSelected) (if (mode == TravelMode.AMBULANCE) SignalRed else TelemetryCyan) else Slate400,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = mode.label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else Slate400,
                        fontSize = 10.sp
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
        }
      }

      // Radar Map View or Hospital Cards
      if (viewMode == "map") {
        item {
          TacticalRadarMap(
            hospitals = hospitals,
            selectedFacility = selectedFacilityForRadar,
            travelMode = travelMode,
            onSelectFacility = { selectedFacilityForRadar = it },
            onNavigateFacility = { facility ->
              try {
                val gmmIntentUri = Uri.parse("geo:${facility.lat},${facility.lng}?q=${Uri.encode(facility.name)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                context.startActivity(mapIntent)
              } catch (_: Exception) {}
            },
            onCallFacility = { facility ->
              try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${facility.phone}"))
                context.startActivity(intent)
              } catch (_: Exception) {}
            },
            modifier = Modifier.testTag("tactical_radar_map_container")
          )
          Spacer(modifier = Modifier.height(20.dp))
        }
      }

      // List Items
      items(hospitals, key = { it.id }) { hospital ->
        HospitalFacilityCard(
          hospital = hospital,
          travelMode = travelMode,
          onDirectionsClick = {
            try {
              val gmmIntentUri = Uri.parse("google.navigation:q=${hospital.lat},${hospital.lng}")
              val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
              context.startActivity(mapIntent)
            } catch (_: Exception) {
              val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${hospital.lat},${hospital.lng}"))
              context.startActivity(browserIntent)
            }
          },
          onCallClick = {
            try {
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.phone}"))
              context.startActivity(intent)
            } catch (_: Exception) {}
          }
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      item {
        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }
}

@Composable
fun HospitalFacilityCard(
  hospital: HospitalFacility,
  travelMode: TravelMode,
  onDirectionsClick: () -> Unit,
  onCallClick: () -> Unit
) {
  val levelColor = when (hospital.level) {
    "Level 1" -> SignalRed
    "Level 2" -> SignalAmber
    else -> TelemetryCyan
  }

  Surface(
    color = Slate900,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, Slate800),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("hospital_card_${hospital.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Name + Trauma Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = hospital.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PureWhite
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${hospital.neighborhood} · ${hospital.address}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Slate400,
              fontSize = 11.sp
            )
          )
        }

        Surface(
          color = levelColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, levelColor.copy(alpha = 0.6f))
        ) {
          Text(
            text = hospital.level.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = levelColor,
              fontSize = 9.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Distance & Travel Mode ETA Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Slate850)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            tint = TelemetryCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${hospital.distance} mi away",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Slate300,
              fontWeight = FontWeight.SemiBold
            )
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when (travelMode) {
              TravelMode.WALKING -> Icons.Default.DirectionsWalk
              TravelMode.DRIVING -> Icons.Default.DirectionsCar
              TravelMode.AMBULANCE -> Icons.Default.Emergency
            },
            contentDescription = null,
            tint = levelColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${hospital.calculateEtaMinutes(travelMode)} min (${travelMode.shortLabel})",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PureWhite,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Specialties tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        hospital.specialties.forEach { specialty ->
          Surface(
            color = Slate800,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = specialty,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = Slate300
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = "STATUS: ${hospital.status}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (hospital.status == "TRAUMA CENTER" || hospital.status == "ACCEPTING") SignalGreen else SignalAmber
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = Slate800, thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onCallClick,
          border = BorderStroke(1.dp, Slate700),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("call_hospital_${hospital.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = SignalGreen,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "CALL ER",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PureWhite,
              fontWeight = FontWeight.Bold
            )
          )
        }

        Button(
          onClick = onDirectionsClick,
          colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("directions_hospital_${hospital.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = null,
            tint = Slate950,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "DIRECTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Slate950,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}
