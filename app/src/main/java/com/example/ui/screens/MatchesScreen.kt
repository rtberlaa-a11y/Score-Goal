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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MatchesScreen(
    viewModel: ScoreGoalViewModel,
    onSelectMatch: (Match) -> Unit,
    onOpenController: (Match) -> Unit,
    onOpenPublish: () -> Unit
) {
    val matchesState by viewModel.matchesState.collectAsState()
    val activeFilter by viewModel.matchFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val currentUid = Firebase.auth.currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search teams, tournaments, pitches...", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("match_search_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PitchGreen,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        // Filter chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            val filters = listOf(
                "all" to "All Matches",
                "live" to "⚡ Live Now",
                "upcoming" to "Upcoming",
                "finished" to "Finished",
                "my_matches" to "My Published"
            )
            items(filters) { (key, label) ->
                val selected = activeFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setMatchFilter(key) },
                    label = {
                        Text(
                            text = label,
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
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkCardBorder,
                        selectedBorderColor = PitchGreen,
                        enabled = true,
                        selected = selected
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_$key")
                )
            }
        }

        // Matches list content
        when (val state = matchesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PitchGreen)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is UiState.Success -> {
                val matches = state.data
                val filtered = matches.filter { match ->
                    val matchesQuery = searchQuery.isBlank() ||
                        match.homeTeam.contains(searchQuery, ignoreCase = true) ||
                        match.awayTeam.contains(searchQuery, ignoreCase = true) ||
                        match.tournamentName.contains(searchQuery, ignoreCase = true) ||
                        match.venue.contains(searchQuery, ignoreCase = true)

                    val matchesFilter = when (activeFilter) {
                        "live" -> match.isLive
                        "upcoming" -> match.status == "upcoming"
                        "finished" -> match.isFinished
                        "my_matches" -> match.authorId == currentUid
                        else -> true
                    }
                    matchesQuery && matchesFilter
                }

                if (filtered.isEmpty()) {
                    EmptyMatchesState(
                        filter = activeFilter,
                        onPublishClick = onOpenPublish,
                        onLoadDemoClick = { viewModel.seedDemoData() }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { match ->
                            val isFollowing = subscriptions.any { it.matchId == match.id }
                            MatchCard(
                                match = match,
                                isFollowing = isFollowing,
                                isAuthor = match.authorId == currentUid,
                                onClick = { onSelectMatch(match) },
                                onToggleFollow = { viewModel.toggleSubscription(match) },
                                onOpenController = { onOpenController(match) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    isFollowing: Boolean,
    isAuthor: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenController: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (match.isLive) LiveRed.copy(alpha = 0.5f) else DarkCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Tournament name + Venue + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = PitchGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.tournamentName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ${match.venue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LiveStatusBadge(
                    status = match.status,
                    currentMinute = match.currentMinute
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scoreboard Center
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamJerseyCircle(
                        teamName = match.homeTeam,
                        colorHex = match.homeTeamColor,
                        sizeDp = 40
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score Display
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == "upcoming") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = match.kickoffTime,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PitchGreen
                            )
                            if (match.matchDate.isNotBlank()) {
                                Text(
                                    text = match.matchDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "${match.homeScore}  -  ${match.awayScore}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = if (match.isLive) PitchGreen else TextPrimary
                        )
                    }
                }

                // Away Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TeamJerseyCircle(
                        teamName = match.awayTeam,
                        colorHex = match.awayTeamColor,
                        sizeDp = 40
                    )
                }
            }

            // Summary line
            if (match.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = match.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer action bar: Follow bell + Publisher Controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By ${match.authorName.ifBlank { "Host" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.6f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Follow Alert Button
                    IconButton(
                        onClick = onToggleFollow,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("follow_match_${match.id}")
                    ) {
                        Icon(
                            imageVector = if (isFollowing) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = if (isFollowing) "Unfollow alerts" else "Follow alerts",
                            tint = if (isFollowing) PitchGreen else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Publisher Controller button
                    if (isAuthor) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onOpenController,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("open_controller_${match.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchGreen.copy(alpha = 0.2f),
                                contentColor = PitchGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Live Console",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMatchesState(
    filter: String,
    onPublishClick: () -> Unit,
    onLoadDemoClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = PitchGreen,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                "live" -> "No live matches right now"
                "upcoming" -> "No upcoming matches scheduled"
                "finished" -> "No finished matches found"
                "my_matches" -> "You haven't published any matches yet"
                else -> "No matches found"
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Publish your local friendly, tournament match, or school derby to broadcast live scores!",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        Button(
            onClick = onPublishClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PitchGreen,
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(0.85f).testTag("empty_publish_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Publish Match Now", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        androidx.compose.material3.OutlinedButton(
            onClick = onLoadDemoClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PitchGreen),
            modifier = Modifier.fillMaxWidth(0.85f).testTag("empty_load_demo_button")
        ) {
            Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Load Demo Matches & League", fontWeight = FontWeight.Bold)
        }
    }
}

