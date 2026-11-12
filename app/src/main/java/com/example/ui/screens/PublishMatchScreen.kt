package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScoreGoalViewModel

@Composable
fun PublishMatchScreen(
    viewModel: ScoreGoalViewModel,
    onBack: () -> Unit,
    onMatchPublished: (String) -> Unit
) {
    BackHandler { onBack() }

    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var tournamentName by remember { mutableStateOf("Local League") }
    var matchTitle by remember { mutableStateOf("") }
    var kickoffTime by remember { mutableStateOf("18:30") }
    var matchDate by remember { mutableStateOf("2026-11-12") }
    var venue by remember { mutableStateOf("City Sports Complex - Pitch 1") }
    var format by remember { mutableStateOf("11v11 (90m)") }

    var homeColorHex by remember { mutableStateOf("#E53935") }
    var awayColorHex by remember { mutableStateOf("#1E88E5") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetColors = listOf(
        "#E53935" to "Red",
        "#1E88E5" to "Blue",
        "#43A047" to "Green",
        "#FDD835" to "Yellow",
        "#FFFFFF" to "White",
        "#000000" to "Black",
        "#8E24AA" to "Purple",
        "#FB8C00" to "Orange"
    )

    val formatOptions = listOf("11v11 (90m)", "7v7 (50m)", "5v5 Futsal", "9v9 Youth")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("publish_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Publish Match",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = PitchGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Broadcast to Community",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Publish your match publicly so fans, parents, and teammates can track the score and receive push notifications in real time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Teams Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Teams & Colors",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PitchGreen
                        )

                        // Home Team
                        OutlinedTextField(
                            value = homeTeam,
                            onValueChange = { homeTeam = it },
                            label = { Text("Home Team Name *") },
                            placeholder = { Text("e.g. Red Hawks FC") },
                            modifier = Modifier.fillMaxWidth().testTag("publish_home_team_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Text("Home Jersey Color", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetColors) { (hex, _) ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = homeColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isSelected) PitchGreen else Color.DarkGray, CircleShape)
                                        .clickable { homeColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Away Team
                        OutlinedTextField(
                            value = awayTeam,
                            onValueChange = { awayTeam = it },
                            label = { Text("Away Team Name *") },
                            placeholder = { Text("e.g. Blue Waves SC") },
                            modifier = Modifier.fillMaxWidth().testTag("publish_away_team_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Text("Away Jersey Color", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetColors) { (hex, _) ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = awayColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isSelected) PitchGreen else Color.DarkGray, CircleShape)
                                        .clickable { awayColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tournament & Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Tournament & Schedule",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PitchGreen
                        )

                        OutlinedTextField(
                            value = tournamentName,
                            onValueChange = { tournamentName = it },
                            label = { Text("Tournament / League / Cup Name") },
                            placeholder = { Text("e.g. City Summer Cup 2026") },
                            modifier = Modifier.fillMaxWidth().testTag("publish_tournament_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            label = { Text("Ground / Stadium / Pitch Name * (Required)") },
                            placeholder = { Text("e.g. Santiago Bernabéu or City Arena Ground 1") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PitchGreen) },
                            modifier = Modifier.fillMaxWidth().testTag("publish_ground_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = kickoffTime,
                                onValueChange = { kickoffTime = it },
                                label = { Text("Kickoff Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = matchDate,
                                onValueChange = { matchDate = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Text("Match Format", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(formatOptions) { opt ->
                                val isSelected = format == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PitchGreen else DarkSurfaceVariant)
                                        .clickable { format = opt }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (homeTeam.isBlank() || awayTeam.isBlank()) {
                            errorMessage = "Please enter both Home Team and Away Team names"
                            return@Button
                        }
                        if (venue.isBlank()) {
                            errorMessage = "Ground / Stadium name is required before starting or publishing a match!"
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        viewModel.publishMatch(
                            title = matchTitle.ifBlank { "$homeTeam vs $awayTeam" },
                            tournamentName = tournamentName.ifBlank { "Local Match" },
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            homeTeamColor = homeColorHex,
                            awayTeamColor = awayColorHex,
                            kickoffTime = kickoffTime,
                            matchDate = matchDate,
                            venue = venue,
                            format = format,
                            onSuccess = { matchId ->
                                isSubmitting = false
                                onMatchPublished(matchId)
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_publish_match_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PitchGreen,
                        contentColor = Color.Black
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Publish Live Match",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}
