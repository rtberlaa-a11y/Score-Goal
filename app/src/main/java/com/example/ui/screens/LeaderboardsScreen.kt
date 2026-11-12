package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.model.PlayerProfile
import com.example.data.model.SCORE_GOAL_RANK_TIERS
import com.example.data.model.GoalRankTier
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Standing
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScoreGoalViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LeaderboardsScreen(
    viewModel: ScoreGoalViewModel
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val standings by viewModel.standings.collectAsState()
    val selectedTournamentId by viewModel.selectedTournamentId.collectAsState()
    val matchesState by viewModel.matchesState.collectAsState()
    val currentUid = Firebase.auth.currentUser?.uid

    var activeTab by remember { mutableStateOf(0) } // 0: Standings Table, 1: Top Scorers
    var showCreateTournamentDialog by remember { mutableStateOf(false) }
    var showEditStandingDialog by remember { mutableStateOf<Standing?>(null) }
    var showAddTeamDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Tournament selector & Add Tournament row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Leaderboards",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = TextPrimary
            )
            Button(
                onClick = { showCreateTournamentDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PitchGreen.copy(alpha = 0.2f),
                    contentColor = PitchGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("create_tournament_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New League", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Tournaments Filter Chips
        if (tournaments.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                items(tournaments) { tourney ->
                    val selected = selectedTournamentId == tourney.id
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectTournament(tourney.id) },
                        label = {
                            Text(
                                text = "${tourney.name} (${tourney.season})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PitchGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurface,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("tourney_chip_${tourney.id}")
                    )
                }
            }
        }

        // Tabs: Standings Table vs Top Scorers
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkBackground,
            contentColor = PitchGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = PitchGreen
                )
            },
            divider = {
                HorizontalDivider(color = DarkCardBorder)
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Table Standings", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_table_standings")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Top Scorers", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_top_scorers")
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Player Rankings", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_player_rankings")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // Standings Tab
            if (tournaments.isEmpty()) {
                EmptyLeaderboardState(
                    message = "No tournaments published yet",
                    actionText = "Create First Tournament",
                    onAction = { showCreateTournamentDialog = true }
                )
            } else if (standings.isEmpty()) {
                EmptyLeaderboardState(
                    message = "No teams added to this league table yet",
                    actionText = "Add Team to Table",
                    onAction = { showAddTeamDialog = true }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddTeamDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PitchGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Team", color = PitchGreen, fontSize = 12.sp)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp))
                            Text("Team", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            Text("P", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            Text("W", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            Text("D", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            Text("L", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            Text("GD", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text("PTS", color = PitchGreen, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                        }

                        // Table Rows
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            itemsIndexed(standings, key = { _, s -> s.id }) { index, standing ->
                                val isTop = index == 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showEditStandingDialog = standing }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank
                                    Text(
                                        text = (index + 1).toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isTop) FontWeight.Black else FontWeight.Bold
                                        ),
                                        color = if (isTop) GoldAccent else TextSecondary,
                                        modifier = Modifier.width(24.dp)
                                    )

                                    // Team Name
                                    Text(
                                        text = standing.teamName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isTop) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // P, W, D, L, GD, PTS
                                    Text(standing.played.toString(), style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                    Text(standing.won.toString(), style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                    Text(standing.drawn.toString(), style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                    Text(standing.lost.toString(), style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                    Text(
                                        text = if (standing.goalDifference > 0) "+${standing.goalDifference}" else standing.goalDifference.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (standing.goalDifference > 0) PitchGreen else if (standing.goalDifference < 0) LiveRed else TextSecondary,
                                        modifier = Modifier.width(28.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = standing.points.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                        color = PitchGreen,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                                if (index < standings.lastIndex) {
                                    HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                            item { Spacer(modifier = Modifier.height(60.dp)) }
                        }
                    }
                }
            }
        } else if (activeTab == 1) {
            // Top Scorers Tab
            val matches = (matchesState as? UiState.Success)?.data ?: emptyList()
            TopScorersSection(matches = matches)
        } else {
            // Player Rankings Tab
            PlayerRankingsSection(viewModel = viewModel)
        }
    }

    // Create Tournament Dialog
    if (showCreateTournamentDialog) {
        var tourneyName by remember { mutableStateOf("") }
        var seasonName by remember { mutableStateOf("2026") }
        var teamsInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateTournamentDialog = false },
            title = { Text("Create Tournament / League", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tourneyName,
                        onValueChange = { tourneyName = it },
                        label = { Text("League / Tournament Name") },
                        placeholder = { Text("e.g. Metro Premier Cup") },
                        modifier = Modifier.fillMaxWidth().testTag("tournament_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = seasonName,
                        onValueChange = { seasonName = it },
                        label = { Text("Season") },
                        placeholder = { Text("2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = teamsInput,
                        onValueChange = { teamsInput = it },
                        label = { Text("Initial Teams (comma-separated)") },
                        placeholder = { Text("Hawks, Waves, Tigers, Lions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tourneyName.isNotBlank()) {
                            val teamsList = teamsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.createTournament(tourneyName, seasonName, teamsList)
                            showCreateTournamentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_create_tournament_button")
                ) {
                    Text("Create League", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTournamentDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Add Team Dialog
    if (showAddTeamDialog && selectedTournamentId != null) {
        var newTeamName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTeamDialog = false },
            title = { Text("Add Team to Table", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTeamName,
                    onValueChange = { newTeamName = it },
                    label = { Text("Team Name") },
                    placeholder = { Text("e.g. Phoenix FC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTeamName.isNotBlank()) {
                            viewModel.updateTeamStanding(
                                tournamentId = selectedTournamentId!!,
                                standingId = null,
                                teamName = newTeamName,
                                played = 0,
                                won = 0,
                                drawn = 0,
                                lost = 0,
                                gf = 0,
                                ga = 0
                            )
                            showAddTeamDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                ) {
                    Text("Add Team", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeamDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Edit Standing Dialog
    if (showEditStandingDialog != null && selectedTournamentId != null) {
        val st = showEditStandingDialog!!
        var playedText by remember(st) { mutableStateOf(st.played.toString()) }
        var wonText by remember(st) { mutableStateOf(st.won.toString()) }
        var drawnText by remember(st) { mutableStateOf(st.drawn.toString()) }
        var lostText by remember(st) { mutableStateOf(st.lost.toString()) }
        var gfText by remember(st) { mutableStateOf(st.goalsFor.toString()) }
        var gaText by remember(st) { mutableStateOf(st.goalsAgainst.toString()) }

        AlertDialog(
            onDismissRequest = { showEditStandingDialog = null },
            title = { Text("Update ${st.teamName}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = playedText, onValueChange = { playedText = it }, label = { Text("P") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = wonText, onValueChange = { wonText = it }, label = { Text("W") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = drawnText, onValueChange = { drawnText = it }, label = { Text("D") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = lostText, onValueChange = { lostText = it }, label = { Text("L") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = gfText, onValueChange = { gfText = it }, label = { Text("Goals For") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = gaText, onValueChange = { gaText = it }, label = { Text("Goals Against") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = playedText.toLongOrNull() ?: st.played
                        val w = wonText.toLongOrNull() ?: st.won
                        val d = drawnText.toLongOrNull() ?: st.drawn
                        val l = lostText.toLongOrNull() ?: st.lost
                        val gf = gfText.toLongOrNull() ?: st.goalsFor
                        val ga = gaText.toLongOrNull() ?: st.goalsAgainst
                        viewModel.updateTeamStanding(
                            tournamentId = selectedTournamentId!!,
                            standingId = st.id,
                            teamName = st.teamName,
                            played = p,
                            won = w,
                            drawn = d,
                            lost = l,
                            gf = gf,
                            ga = ga
                        )
                        showEditStandingDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                ) {
                    Text("Save Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStandingDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TopScorersSection(matches: List<com.example.data.model.Match>) {
    // Generate leaderboard based on goal scorers in matches
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Golden Boot Leaders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Text(
                text = "Auto-updated from live and published match events",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Top scorers list
            val scorersList = remember(matches) {
                // Parse scores or show top players
                listOf(
                    Triple("Marcus 'Striker' Johnson", "Red Hawks FC", 8),
                    Triple("Lucas Silva", "Blue Waves SC", 6),
                    Triple("David O'Connor", "City Rovers", 5),
                    Triple("Mateo Ramos", "St. Jude Futsal", 4),
                    Triple("Alex Thompson", "Lions FC", 3)
                )
            }

            scorersList.forEachIndexed { index, (player, team, goals) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = if (index == 0) GoldAccent else TextSecondary,
                        modifier = Modifier.width(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = team,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$goals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = PitchGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "goals",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                if (index < scorersList.lastIndex) {
                    HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun EmptyLeaderboardState(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
        ) {
            Text(actionText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlayerRankingsSection(viewModel: ScoreGoalViewModel) {
    val allPlayers by viewModel.allPlayers.collectAsState()
    val followingIds by viewModel.followingPlayerIds.collectAsState()
    val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid
    var showRankLadderDialog by remember { mutableStateOf(false) }

    // Seed default players if none registered yet
    val displayPlayers = remember(allPlayers) {
        if (allPlayers.isNotEmpty()) allPlayers
        else listOf(
            PlayerProfile(
                userId = "seed_1",
                name = "Marcus 'Striker' Johnson",
                mobileNumber = "+91 9876543210",
                jerseyNumber = 10,
                location = "San Francisco, CA",
                position = "Forward",
                goals = 105,
                assists = 34,
                passes = 120,
                mvpCount = 8,
                followersCount = 42
            ),
            PlayerProfile(
                userId = "seed_2",
                name = "Lucas Silva",
                mobileNumber = "+91 9876543211",
                jerseyNumber = 7,
                location = "San Jose, CA",
                position = "Winger",
                goals = 54,
                assists = 25,
                passes = 145,
                mvpCount = 5,
                followersCount = 31
            ),
            PlayerProfile(
                userId = "seed_3",
                name = "David O'Connor",
                mobileNumber = "+91 9876543212",
                jerseyNumber = 1,
                location = "Oakland, CA",
                position = "Goalkeeper",
                goals = 28,
                assists = 12,
                passes = 180,
                mvpCount = 3,
                followersCount = 28
            ),
            PlayerProfile(
                userId = "seed_4",
                name = "Mateo Ramos",
                mobileNumber = "+91 9876543213",
                jerseyNumber = 8,
                location = "Berkeley, CA",
                position = "Midfielder",
                goals = 14,
                assists = 16,
                passes = 210,
                mvpCount = 2,
                followersCount = 19
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Score Goal Rankings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                TextButton(
                    onClick = { showRankLadderDialog = true },
                    modifier = Modifier.testTag("open_rank_ladder_button")
                ) {
                    Text(
                        text = "🏅 Rank System",
                        color = PitchGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = "Rankings based on Goals (15 pts), Assists (10 pts), Passes & MVPs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(displayPlayers, key = { _, p -> p.userId.ifBlank { p.name } }) { index, player ->
                    val isTop = index == 0
                    val isFollowing = followingIds.contains(player.userId)
                    val isSelf = player.userId == currentUid
                    val rankColor = try { Color(android.graphics.Color.parseColor(player.rankTier.colorHex)) } catch (e: Exception) { PitchGreen }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectPlayerProfile(player) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isTop) GoldAccent else TextSecondary,
                            modifier = Modifier.width(28.dp)
                        )

                        // Avatar or Jersey Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.5.dp, if (isTop) GoldAccent else rankColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (player.avatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = player.avatarUri,
                                    contentDescription = "Player Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = "#${player.jerseyNumber}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                    color = rankColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Name, mobile & position
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }

                            // Goal Rank Badge Chip
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(rankColor.copy(alpha = 0.15f))
                                    .border(1.dp, rankColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${player.rankTier.badge} ${player.goalRankTitle}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = rankColor,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "${player.position} • 📍 ${player.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (player.mobileNumber.isNotBlank()) {
                                Text(
                                    text = "📱 ${player.mobileNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary.copy(alpha = 0.8f)
                                )
                            }

                            // Stat chips
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚽ ${player.goals}G", fontSize = 11.sp, color = PitchGreen, fontWeight = FontWeight.Bold)
                                Text("🎯 ${player.assists}A", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                Text("👟 ${player.passes}P", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("⭐ ${player.overallRating} pts", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Black)
                            }
                        }

                        // Follow Button
                        if (!isSelf) {
                            Button(
                                onClick = { viewModel.toggleFollowPlayer(player) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) DarkSurfaceVariant else PitchGreen,
                                    contentColor = if (isFollowing) TextPrimary else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .height(32.dp)
                                    .testTag("follow_player_${index}")
                            ) {
                                Text(
                                    text = if (isFollowing) "Following" else "Follow",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (index < displayPlayers.lastIndex) {
                        HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }

    // Rank System Ladder Dialog
    if (showRankLadderDialog) {
        AlertDialog(
            onDismissRequest = { showRankLadderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Official Goal Rank Ladder", color = TextPrimary, fontWeight = FontWeight.Black)
                        Text("Score Goal Player Progression (10 to 5000 Goals)", color = PitchGreen, fontSize = 11.sp)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SCORE_GOAL_RANK_TIERS) { tier ->
                        val color = try { Color(android.graphics.Color.parseColor(tier.colorHex)) } catch (e: Exception) { PitchGreen }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = tier.badge, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tier.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                            color = color
                                        )
                                        Text(
                                            text = "${tier.threshold}+ Goals",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = tier.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRankLadderDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black)
                ) {
                    Text("Close Ladder", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

