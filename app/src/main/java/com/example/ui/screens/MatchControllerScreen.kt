package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.theme.GoldAccent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScoreGoalViewModel

@Composable
fun MatchControllerScreen(
    match: Match,
    viewModel: ScoreGoalViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val activeMatchState by viewModel.activeMatch.collectAsState()
    val activeMatch = activeMatchState ?: match

    var showGoalDialogForTeam by remember { mutableStateOf<String?>(null) }
    var showEventDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPreMatchLineupDialog by remember { mutableStateOf(false) }

    // Stats temporary state for adjustments
    var homeShots by remember(activeMatch) { mutableLongStateOf(activeMatch.homeShots) }
    var awayShots by remember(activeMatch) { mutableLongStateOf(activeMatch.awayShots) }
    var homeFouls by remember(activeMatch) { mutableLongStateOf(activeMatch.homeFouls) }
    var awayFouls by remember(activeMatch) { mutableLongStateOf(activeMatch.awayFouls) }
    var homeCorners by remember(activeMatch) { mutableLongStateOf(activeMatch.homeCorners) }
    var awayCorners by remember(activeMatch) { mutableLongStateOf(activeMatch.awayCorners) }
    var homePossession by remember(activeMatch) { mutableFloatStateOf(activeMatch.homePossession.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("controller_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Live Match Console",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.testTag("controller_delete_match_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Match",
                    tint = LiveRed
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LiveStatusBadge(status = activeMatch.status, currentMinute = activeMatch.currentMinute)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${activeMatch.homeTeam} vs ${activeMatch.awayTeam}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Score Row with Fast Increment / Decrement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Home Controls
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = activeMatch.homeTeam,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Text(
                                    text = activeMatch.homeScore.toString(),
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = PitchGreen
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { showGoalDialogForTeam = "home" },
                                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("home_goal_button")
                                    ) {
                                        Text("+ GOAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    if (activeMatch.homeScore > 0) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateScore(activeMatch, activeMatch.homeScore - 1, activeMatch.awayScore)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("-", color = TextSecondary)
                                        }
                                    }
                                }
                            }

                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )

                            // Away Controls
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = activeMatch.awayTeam,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Text(
                                    text = activeMatch.awayScore.toString(),
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFF38BDF8)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { showGoalDialogForTeam = "away" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("away_goal_button")
                                    ) {
                                        Text("+ GOAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    if (activeMatch.awayScore > 0) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateScore(activeMatch, activeMatch.homeScore, activeMatch.awayScore - 1)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("-", color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Match Clock & Period Controller
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Match Period & Time Control",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Minute Steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Minute: ${activeMatch.currentMinute}'",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PitchGreen
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateMatchStatus(
                                            activeMatch.id,
                                            activeMatch.status,
                                            activeMatch.currentMinute + 1,
                                            activeMatch.summary
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+1'", color = TextPrimary)
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateMatchStatus(
                                            activeMatch.id,
                                            activeMatch.status,
                                            activeMatch.currentMinute + 5,
                                            activeMatch.summary
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+5'", color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Period buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (activeMatch.status == "upcoming") {
                                Button(
                                    onClick = { showPreMatchLineupDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("start_match_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lineup Setup & Kickoff", fontWeight = FontWeight.Bold)
                                }
                            } else if (activeMatch.status == "live_1h") {
                                Button(
                                    onClick = {
                                        viewModel.updateMatchStatus(
                                            activeMatch.id,
                                            "halftime",
                                            45,
                                            "Half Time whistle blown. ${activeMatch.homeTeam} ${activeMatch.homeScore} - ${activeMatch.awayScore} ${activeMatch.awayTeam}"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("halftime_button")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Blow Half Time", fontWeight = FontWeight.Bold)
                                }
                            } else if (activeMatch.status == "halftime") {
                                Button(
                                    onClick = {
                                        viewModel.updateMatchStatus(
                                            activeMatch.id,
                                            "live_2h",
                                            46,
                                            "Second Half kicked off!"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("start_2h_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start 2nd Half", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (activeMatch.isLive || activeMatch.status == "halftime") {
                                Button(
                                    onClick = {
                                        viewModel.updateMatchStatus(
                                            activeMatch.id,
                                            "finished",
                                            activeMatch.currentMinute.coerceAtLeast(90),
                                            "Full Time! Final score: ${activeMatch.homeTeam} ${activeMatch.homeScore} - ${activeMatch.awayScore} ${activeMatch.awayTeam}"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("finish_match_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("End Match (FT)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Add Event Button (Cards, Subs, Commentary)
            item {
                Button(
                    onClick = { showEventDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_event_dialog_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Card, Sub or Commentary Event", fontWeight = FontWeight.Bold)
                }
            }

            // Live Stats Editor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Live Match Stats Adjuster",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Possession Slider
                        Text(
                            text = "Possession: ${homePossession.toInt()}% - ${(100 - homePossession).toInt()}%",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = homePossession,
                            onValueChange = { homePossession = it },
                            valueRange = 10f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = PitchGreen,
                                activeTrackColor = PitchGreen
                            )
                        )

                        // Shots +/-
                        StatCounterRow(
                            label = "Shots",
                            homeCount = homeShots,
                            awayCount = awayShots,
                            onHomeChange = { homeShots = it },
                            onAwayChange = { awayShots = it }
                        )

                        // Corners +/-
                        StatCounterRow(
                            label = "Corners",
                            homeCount = homeCorners,
                            awayCount = awayCorners,
                            onHomeChange = { homeCorners = it },
                            onAwayChange = { awayCorners = it }
                        )

                        // Fouls +/-
                        StatCounterRow(
                            label = "Fouls",
                            homeCount = homeFouls,
                            awayCount = awayFouls,
                            onHomeChange = { homeFouls = it },
                            onAwayChange = { awayFouls = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateMatchStats(
                                    activeMatch.id,
                                    homeShots = homeShots,
                                    awayShots = awayShots,
                                    homeShotsOnTarget = homeShots / 2,
                                    awayShotsOnTarget = awayShots / 2,
                                    homeFouls = homeFouls,
                                    awayFouls = awayFouls,
                                    homeCorners = homeCorners,
                                    awayCorners = awayCorners,
                                    homePossession = homePossession.toLong(),
                                    awayPossession = (100 - homePossession).toLong()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                        ) {
                            Text("Save Stats to Live Stream", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Goal Dialog
    if (showGoalDialogForTeam != null) {
        val team = showGoalDialogForTeam!!
        val teamName = if (team == "home") activeMatch.homeTeam else activeMatch.awayTeam
        var playerName by remember { mutableStateOf("") }
        var assistName by remember { mutableStateOf("") }
        var minuteText by remember { mutableStateOf(activeMatch.currentMinute.toString()) }

        AlertDialog(
            onDismissRequest = { showGoalDialogForTeam = null },
            title = { Text("⚽ Goal for $teamName", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Goalscorer Name") },
                        placeholder = { Text("e.g. Marcus Johnson") },
                        modifier = Modifier.fillMaxWidth().testTag("goalscorer_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = assistName,
                        onValueChange = { assistName = it },
                        label = { Text("Assist By (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("Minute") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minute = minuteText.toLongOrNull() ?: activeMatch.currentMinute
                        viewModel.addMatchEvent(
                            match = activeMatch,
                            minute = minute,
                            team = team,
                            type = "goal",
                            player = playerName.ifBlank { "Player" },
                            secondary = assistName,
                            detail = "Goal scored for $teamName!"
                        )
                        showGoalDialogForTeam = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_goal_button")
                ) {
                    Text("Confirm Goal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialogForTeam = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // General Event Dialog (Cards, Subs, Commentary)
    if (showEventDialog) {
        var selectedType by remember { mutableStateOf("yellow_card") }
        var selectedTeam by remember { mutableStateOf("home") }
        var playerName by remember { mutableStateOf("") }
        var detailText by remember { mutableStateOf("") }
        var minuteText by remember { mutableStateOf(activeMatch.currentMinute.toString()) }

        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = { Text("Add Match Event", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("yellow_card" to "🟨 Yellow", "red_card" to "🟥 Red", "substitution" to "🔄 Sub", "commentary" to "💬 Text").forEach { (type, label) ->
                            OutlinedButton(
                                onClick = { selectedType = type },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedType == type) PitchGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
                                )
                            ) {
                                Text(label, fontSize = 10.sp, color = if (selectedType == type) PitchGreen else TextPrimary)
                            }
                        }
                    }

                    // Team selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTeam = "home" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTeam == "home") PitchGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
                            )
                        ) {
                            Text(activeMatch.homeTeam, maxLines = 1, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { selectedTeam = "away" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTeam == "away") PitchGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
                            )
                        ) {
                            Text(activeMatch.awayTeam, maxLines = 1, color = TextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Player Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("Minute") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = detailText,
                        onValueChange = { detailText = it },
                        label = { Text("Detail / Commentary Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minute = minuteText.toLongOrNull() ?: activeMatch.currentMinute
                        viewModel.addMatchEvent(
                            match = activeMatch,
                            minute = minute,
                            team = selectedTeam,
                            type = selectedType,
                            player = playerName,
                            detail = detailText
                        )
                        showEventDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                ) {
                    Text("Add Event", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Match?", color = TextPrimary) },
            text = { Text("Are you sure you want to delete this match broadcast? This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMatch(activeMatch.id) {
                            showDeleteConfirm = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showPreMatchLineupDialog) {
        PreMatchLineupDialog(
            match = activeMatch,
            viewModel = viewModel,
            onDismiss = { showPreMatchLineupDialog = false },
            onConfirmKickoff = { homeForm, awayForm, homeTac, awayTac, capName, gkName, groundVenue ->
                viewModel.updateMatchTactics(
                    activeMatch.id,
                    homeForm,
                    awayForm,
                    homeTac,
                    awayTac,
                    groundVenue
                )
                viewModel.updateMatchStatus(
                    activeMatch.id,
                    "live_1h",
                    1,
                    "Match kicked off at $groundVenue! 1st Half underway. ${activeMatch.homeTeam} vs ${activeMatch.awayTeam}. Captain: $capName (C), GK: $gkName (GK)"
                )
                viewModel.addMatchEvent(
                    match = activeMatch,
                    minute = 1,
                    team = "home",
                    type = "kickoff",
                    player = capName,
                    detail = "Kickoff at $groundVenue. Captain: $capName (C) led lineup out. Goalkeeper: $gkName (GK)"
                )
                showPreMatchLineupDialog = false
            }
        )
    }
}

data class StarterPlayerEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var mobileNumber: String = "",
    var jerseyNumber: Long = 10L,
    var position: String = "FWD"
)

@Composable
fun PreMatchLineupDialog(
    match: Match,
    viewModel: ScoreGoalViewModel,
    onDismiss: () -> Unit,
    onConfirmKickoff: (homeFormation: String, awayFormation: String, homeTactics: String, awayTactics: String, captainName: String, gkName: String, groundVenue: String) -> Unit
) {
    val allPlayers by viewModel.allPlayers.collectAsState()
    var groundVenue by remember { mutableStateOf(match.venue) }
    var selectedTeamTab by remember { mutableStateOf(0) } // 0: Home, 1: Away
    var homeFormation by remember { mutableStateOf(match.homeFormation.ifBlank { "4-3-3" }) }
    var awayFormation by remember { mutableStateOf(match.awayFormation.ifBlank { "4-4-2" }) }
    var homeTactics by remember { mutableStateOf(match.homeTactics.ifBlank { "High Press & Fast Attack" }) }
    var awayTactics by remember { mutableStateOf(match.awayTactics.ifBlank { "Counter Attack & Deep Defense" }) }

    // Starter Players state with registered mobile numbers
    var homePlayers by remember {
        mutableStateOf(
            listOf(
                StarterPlayerEntry(name = "David O'Connor", mobileNumber = "+91 9876543210", jerseyNumber = 1, position = "GK"),
                StarterPlayerEntry(name = "Kyle Walker", mobileNumber = "+91 9876543211", jerseyNumber = 2, position = "DEF"),
                StarterPlayerEntry(name = "John Stones", mobileNumber = "+91 9876543212", jerseyNumber = 5, position = "DEF"),
                StarterPlayerEntry(name = "Ruben Dias", mobileNumber = "+91 9876543213", jerseyNumber = 4, position = "DEF"),
                StarterPlayerEntry(name = "Rodri", mobileNumber = "+91 9876543214", jerseyNumber = 16, position = "MID"),
                StarterPlayerEntry(name = "Kevin De Bruyne", mobileNumber = "+91 9876543215", jerseyNumber = 17, position = "MID"),
                StarterPlayerEntry(name = "Bernardo Silva", mobileNumber = "+91 9876543216", jerseyNumber = 8, position = "MID"),
                StarterPlayerEntry(name = "Phil Foden", mobileNumber = "+91 9876543217", jerseyNumber = 47, position = "FWD"),
                StarterPlayerEntry(name = "Marcus Johnson", mobileNumber = "+91 9876543218", jerseyNumber = 9, position = "FWD"),
                StarterPlayerEntry(name = "Jack Grealish", mobileNumber = "+91 9876543219", jerseyNumber = 10, position = "FWD"),
                StarterPlayerEntry(name = "Julian Alvarez", mobileNumber = "+91 9876543220", jerseyNumber = 19, position = "FWD")
            )
        )
    }

    var awayPlayers by remember {
        mutableStateOf(
            listOf(
                StarterPlayerEntry(name = "Mark Davis", mobileNumber = "+1 555-0101", jerseyNumber = 1, position = "GK"),
                StarterPlayerEntry(name = "Ben White", mobileNumber = "+1 555-0102", jerseyNumber = 4, position = "DEF"),
                StarterPlayerEntry(name = "William Saliba", mobileNumber = "+1 555-0103", jerseyNumber = 2, position = "DEF"),
                StarterPlayerEntry(name = "Gabriel", mobileNumber = "+1 555-0104", jerseyNumber = 6, position = "DEF"),
                StarterPlayerEntry(name = "Thomas Partey", mobileNumber = "+1 555-0105", jerseyNumber = 5, position = "MID"),
                StarterPlayerEntry(name = "Declan Rice", mobileNumber = "+1 555-0106", jerseyNumber = 41, position = "MID"),
                StarterPlayerEntry(name = "Bukayo Saka", mobileNumber = "+1 555-0107", jerseyNumber = 7, position = "MID"),
                StarterPlayerEntry(name = "Martin Odegaard", mobileNumber = "+1 555-0108", jerseyNumber = 8, position = "MID"),
                StarterPlayerEntry(name = "Lucas Silva", mobileNumber = "+1 555-0109", jerseyNumber = 10, position = "FWD"),
                StarterPlayerEntry(name = "Kai Havertz", mobileNumber = "+1 555-0110", jerseyNumber = 29, position = "FWD"),
                StarterPlayerEntry(name = "Gabriel Jesus", mobileNumber = "+1 555-0111", jerseyNumber = 9, position = "FWD")
            )
        )
    }

    // Captain & GK ID selections
    var homeCaptainId by remember { mutableStateOf(homePlayers.getOrNull(8)?.id ?: "") }
    var homeGkId by remember { mutableStateOf(homePlayers.getOrNull(0)?.id ?: "") }
    var awayCaptainId by remember { mutableStateOf(awayPlayers.getOrNull(8)?.id ?: "") }
    var awayGkId by remember { mutableStateOf(awayPlayers.getOrNull(0)?.id ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val formationsList = listOf("4-3-3", "4-4-2", "3-5-2", "4-2-3-1")
    val strategiesList = listOf("High Press & Attack", "Counter Attack", "Possession Control", "Low Block Defense")

    val currentPlayers = if (selectedTeamTab == 0) homePlayers else awayPlayers
    val currentTeamName = if (selectedTeamTab == 0) match.homeTeam else match.awayTeam

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PitchGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pre-Match Lineup Setup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Player Mobile Numbers & Captain/GK Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchGreen
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Match Ground / Stadium Name (Required before kickoff)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PitchGreen)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Match Ground / Stadium * (Required before kickoff)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PitchGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = groundVenue,
                                onValueChange = { groundVenue = it },
                                label = { Text("Ground / Stadium Name *") },
                                placeholder = { Text("e.g. Santiago Bernabéu or City Arena Ground 1") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("lineup_ground_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }

                // Team Selector Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTeamTab = 0 },
                            modifier = Modifier.weight(1f).testTag("lineup_tab_home"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTeamTab == 0) PitchGreen.copy(alpha = 0.25f) else DarkSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTeamTab == 0) PitchGreen else DarkCardBorder)
                        ) {
                            Text(
                                text = match.homeTeam,
                                color = if (selectedTeamTab == 0) PitchGreen else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedTeamTab = 1 },
                            modifier = Modifier.weight(1f).testTag("lineup_tab_away"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTeamTab == 1) PitchGreen.copy(alpha = 0.25f) else DarkSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTeamTab == 1) PitchGreen else DarkCardBorder)
                        ) {
                            Text(
                                text = match.awayTeam,
                                color = if (selectedTeamTab == 1) PitchGreen else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Formation Selector
                item {
                    Text("Tactical Formation: ${if (selectedTeamTab == 0) homeFormation else awayFormation}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(formationsList) { f ->
                            val currentF = if (selectedTeamTab == 0) homeFormation else awayFormation
                            val isSel = currentF == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PitchGreen else DarkSurfaceVariant)
                                    .clickable {
                                        if (selectedTeamTab == 0) homeFormation = f else awayFormation = f
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = f,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Strategy Selector
                item {
                    Text("Strategic Approach: ${if (selectedTeamTab == 0) homeTactics else awayTactics}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(strategiesList) { strat ->
                            val currentStrat = if (selectedTeamTab == 0) homeTactics else awayTactics
                            val isSel = currentStrat == strat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PitchGreen else DarkSurfaceVariant)
                                    .clickable {
                                        if (selectedTeamTab == 0) homeTactics = strat else awayTactics = strat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = strat,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = DarkCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "$currentTeamName Starting Squad (${currentPlayers.size} Players)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Every starting player requires Name & Registered Mobile Number. Choose (C) and (GK):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Starting Players List
                itemsIndexed(currentPlayers) { index, player ->
                    val isCaptain = if (selectedTeamTab == 0) homeCaptainId == player.id else awayCaptainId == player.id
                    val isGk = if (selectedTeamTab == 0) homeGkId == player.id else awayGkId == player.id

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCaptain) GoldAccent else if (isGk) Color(0xFF38BDF8) else DarkCardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Starter #${index + 1} (${player.position})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PitchGreen
                                )

                                // Badges
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Captain selector button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCaptain) GoldAccent else DarkSurface)
                                            .clickable {
                                                if (selectedTeamTab == 0) homeCaptainId = player.id else awayCaptainId = player.id
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isCaptain) "★ Captain (C)" else "Set Captain",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCaptain) Color.Black else TextSecondary
                                        )
                                    }

                                    // Goalkeeper selector button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isGk) Color(0xFF38BDF8) else DarkSurface)
                                            .clickable {
                                                if (selectedTeamTab == 0) homeGkId = player.id else awayGkId = player.id
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isGk) "🧤 Goalkeeper (GK)" else "Set GK",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGk) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Player Name Input (Required)
                            OutlinedTextField(
                                value = player.name,
                                onValueChange = { newName ->
                                    val updated = currentPlayers.toMutableList()
                                    updated[index] = player.copy(name = newName)
                                    if (selectedTeamTab == 0) homePlayers = updated else awayPlayers = updated
                                },
                                label = { Text("Player Name *") },
                                placeholder = { Text("e.g. John Doe") },
                                modifier = Modifier.fillMaxWidth().testTag("starter_name_${index}"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Registered Mobile Number (Required)
                            val matchedPlayer = remember(player.mobileNumber, allPlayers) {
                                allPlayers.find {
                                    it.mobileNumber.isNotBlank() && it.mobileNumber.trim() == player.mobileNumber.trim()
                                }
                            }

                            OutlinedTextField(
                                value = player.mobileNumber,
                                onValueChange = { newMobile ->
                                    val updated = currentPlayers.toMutableList()
                                    val matchProfile = allPlayers.find {
                                        it.mobileNumber.isNotBlank() && it.mobileNumber.trim() == newMobile.trim()
                                    }
                                    val autoName = if (matchProfile != null && (player.name.isBlank() || player.name.startsWith("Starter") || player.name == "John Doe")) {
                                        matchProfile.name
                                    } else player.name
                                    val autoJersey = if (matchProfile != null) matchProfile.jerseyNumber else player.jerseyNumber

                                    updated[index] = player.copy(
                                        mobileNumber = newMobile,
                                        name = autoName,
                                        jerseyNumber = autoJersey
                                    )
                                    if (selectedTeamTab == 0) homePlayers = updated else awayPlayers = updated
                                },
                                label = { Text("Registered Mobile Number *") },
                                placeholder = { Text("+91 9876543210") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("starter_mobile_${index}"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            if (matchedPlayer != null) {
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Registered: ${matchedPlayer.name} • Rank: ${matchedPlayer.goalRankTitle} (${matchedPlayer.goals} Goals)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Add starter player button
                item {
                    TextButton(
                        onClick = {
                            val newEntry = StarterPlayerEntry(
                                name = "",
                                mobileNumber = "",
                                jerseyNumber = (currentPlayers.size + 1).toLong(),
                                position = "FWD"
                            )
                            if (selectedTeamTab == 0) {
                                homePlayers = homePlayers + newEntry
                            } else {
                                awayPlayers = awayPlayers + newEntry
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PitchGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Starter Player to Roster", color = PitchGreen, fontWeight = FontWeight.Bold)
                    }
                }

                if (validationError != null) {
                    item {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groundVenue.isBlank()) {
                        validationError = "Match Ground / Stadium name is strictly required before starting the match!"
                        return@Button
                    }

                    // Validate that all starter players have name and mobile number
                    val emptyHomePlayer = homePlayers.firstOrNull { it.name.isBlank() || it.mobileNumber.isBlank() }
                    if (emptyHomePlayer != null) {
                        validationError = "All ${match.homeTeam} starting players require both Name and Registered Mobile Number!"
                        selectedTeamTab = 0
                        return@Button
                    }

                    val emptyAwayPlayer = awayPlayers.firstOrNull { it.name.isBlank() || it.mobileNumber.isBlank() }
                    if (emptyAwayPlayer != null) {
                        validationError = "All ${match.awayTeam} starting players require both Name and Registered Mobile Number!"
                        selectedTeamTab = 1
                        return@Button
                    }

                    val homeCap = homePlayers.find { it.id == homeCaptainId }?.name ?: homePlayers.firstOrNull()?.name ?: "Captain"
                    val homeGk = homePlayers.find { it.id == homeGkId }?.name ?: homePlayers.firstOrNull()?.name ?: "Keeper"

                    onConfirmKickoff(homeFormation, awayFormation, homeTactics, awayTactics, homeCap, homeGk, groundVenue)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_kickoff_button")
            ) {
                Text("Confirm Lineup & Kickoff (1st Half)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
fun StatCounterRow(
    label: String,
    homeCount: Long,
    awayCount: Long,
    onHomeChange: (Long) -> Unit,
    onAwayChange: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Home stepper
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onHomeChange((homeCount - 1).coerceAtLeast(0L)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = homeCount.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { onHomeChange(homeCount + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )

        // Away stepper
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onAwayChange((awayCount - 1).coerceAtLeast(0L)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = awayCount.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { onAwayChange(awayCount + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
            }
        }
    }
}
