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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MatchEvent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScoreGoalViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MatchDetailScreen(
    match: Match,
    viewModel: ScoreGoalViewModel,
    onBack: () -> Unit,
    onOpenController: (Match) -> Unit
) {
    BackHandler { onBack() }

    val activeMatch by viewModel.activeMatch.collectAsState()
    val displayMatch = activeMatch ?: match
    val events by viewModel.matchEvents.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isFollowing = subscriptions.any { it.matchId == displayMatch.id }
    val currentUid = Firebase.auth.currentUser?.uid
    val isAuthor = displayMatch.authorId == currentUid

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedLineupPlayer by remember { mutableStateOf<LineupPlayerInfo?>(null) }
    val tabs = listOf("Timeline", "Stats", "Tactics & Lineup", "Info")

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
                modifier = Modifier.testTag("match_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = displayMatch.tournamentName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.toggleSubscription(displayMatch) },
                modifier = Modifier.testTag("detail_toggle_follow_button")
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = "Follow Alerts",
                    tint = if (isFollowing) PitchGreen else TextSecondary
                )
            }
        }

        // Hero Scoreboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LiveStatusBadge(
                    status = displayMatch.status,
                    currentMinute = displayMatch.currentMinute
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        TeamJerseyCircle(
                            teamName = displayMatch.homeTeam,
                            colorHex = displayMatch.homeTeamColor,
                            sizeDp = 56
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayMatch.homeTeam,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Score / Time
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        if (displayMatch.status == "upcoming") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = displayMatch.kickoffTime,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PitchGreen
                                    )
                                )
                                Text(
                                    text = displayMatch.matchDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            Text(
                                text = "${displayMatch.homeScore}  -  ${displayMatch.awayScore}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = if (displayMatch.isLive) PitchGreen else TextPrimary
                            )
                        }
                    }

                    // Away
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        TeamJerseyCircle(
                            teamName = displayMatch.awayTeam,
                            colorHex = displayMatch.awayTeamColor,
                            sizeDp = 56
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayMatch.awayTeam,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (displayMatch.summary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = displayMatch.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                if (isAuthor) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onOpenController(displayMatch) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("detail_open_controller_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Publisher Live Console", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkBackground,
            contentColor = PitchGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PitchGreen
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DarkCardBorder)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedTabIndex == index) PitchGreen else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_$title")
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> TimelineTabContent(events = events, match = displayMatch)
            1 -> StatsTabContent(match = displayMatch)
            2 -> TacticalPitchView(
                match = displayMatch,
                onPlayerClick = { selectedLineupPlayer = it }
            )
            3 -> InfoTabContent(match = displayMatch)
        }
    }

    if (selectedLineupPlayer != null) {
        val player = selectedLineupPlayer!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedLineupPlayer = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PitchGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${player.jerseyNumber}",
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(player.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text(
                            text = "${player.position} • ${if (player.isCaptain) "★ Captain" else if (player.isGoalkeeper) "🧤 Goalkeeper" else "Starting XI"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchGreen
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (player.mobileNumber.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Default.Phone, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mobile: ${player.mobileNumber}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                    Text(
                        text = "Viewing tactical position for this match in ${displayMatch.tournamentName}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val profile = viewModel.allPlayers.value.find { it.name.equals(player.name, ignoreCase = true) }
                            ?: com.example.data.model.PlayerProfile(
                                name = player.name,
                                mobileNumber = player.mobileNumber,
                                jerseyNumber = player.jerseyNumber,
                                position = player.position,
                                location = displayMatch.venue
                            )
                        viewModel.selectPlayerProfile(profile)
                        selectedLineupPlayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                ) {
                    Text("View Full Player Card", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { selectedLineupPlayer = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TimelineTabContent(events: List<MatchEvent>, match: Match) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No match events recorded yet",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Live goals, cards and substitutions will appear here in real time.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(events, key = { it.id }) { event ->
                MatchEventCard(event = event, match = match)
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun MatchEventCard(event: MatchEvent, match: Match) {
    val isHome = event.team == "home"
    val isAway = event.team == "away"
    val teamName = if (isHome) match.homeTeam else if (isAway) match.awayTeam else "Neutral"

    val (iconText, iconColor) = when (event.type) {
        "goal", "penalty" -> "⚽" to PitchGreen
        "yellow_card" -> "🟨" to Color(0xFFFFD600)
        "red_card" -> "🟥" to LiveRed
        "substitution" -> "🔄" to Color(0xFF38BDF8)
        "kickoff" -> "⏱️" to PitchGreen
        "halftime" -> "⏸️" to Color(0xFFF59E0B)
        "fulltime" -> "🏁" to TextPrimary
        else -> "💬" to TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minute badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${event.minute}'",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PitchGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = iconText,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (event.playerName.isNotBlank()) event.playerName else event.type.replace('_', ' ').uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                if (event.detail.isNotBlank() || event.secondaryPlayer.isNotBlank()) {
                    Text(
                        text = listOfNotNull(
                            if (event.secondaryPlayer.isNotBlank()) "Assist: ${event.secondaryPlayer}" else null,
                            if (event.detail.isNotBlank()) event.detail else null
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = teamName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun StatsTabContent(match: Match) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Match Statistics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    StatBarRow(
                        label = "Possession",
                        homeValue = match.homePossession,
                        awayValue = match.awayPossession,
                        isPercentage = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatBarRow(
                        label = "Total Shots",
                        homeValue = match.homeShots,
                        awayValue = match.awayShots
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatBarRow(
                        label = "Shots on Target",
                        homeValue = match.homeShotsOnTarget,
                        awayValue = match.awayShotsOnTarget
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatBarRow(
                        label = "Corners",
                        homeValue = match.homeCorners,
                        awayValue = match.awayCorners
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatBarRow(
                        label = "Fouls",
                        homeValue = match.homeFouls,
                        awayValue = match.awayFouls
                    )
                }
            }
        }
    }
}

@Composable
fun InfoTabContent(match: Match) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoDetailRow(Icons.Default.LocationOn, "Pitch / Venue", match.venue)
                    InfoDetailRow(Icons.Default.CalendarToday, "Date", match.matchDate.ifBlank { "TBD" })
                    InfoDetailRow(Icons.Default.Schedule, "Kickoff Time", match.kickoffTime)
                    InfoDetailRow(Icons.Default.SportsSoccer, "Format", match.format)
                    InfoDetailRow(Icons.Default.Person, "Match Publisher", match.authorName.ifBlank { "Public User" })
                }
            }
        }
    }
}

@Composable
fun InfoDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        }
    }
}
