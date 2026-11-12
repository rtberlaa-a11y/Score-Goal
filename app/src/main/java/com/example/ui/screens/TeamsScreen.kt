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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Team
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
fun TeamsScreen(
    viewModel: ScoreGoalViewModel
) {
    val teams by viewModel.teams.collectAsState()
    val currentUid = Firebase.auth.currentUser?.uid

    var showCreateTeamDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Football Teams & Clubs",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = TextPrimary
            )
            Button(
                onClick = { showCreateTeamDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("create_team_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Team", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (teams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No football teams registered yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Create your club, register players with their mobile numbers, and publish matches!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = { showCreateTeamDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Your Team", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teams, key = { it.id }) { team ->
                    TeamCard(
                        team = team,
                        isAuthor = team.authorId == currentUid,
                        onDelete = { viewModel.deleteTeam(team.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(70.dp)) }
            }
        }
    }

    if (showCreateTeamDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateTeamDialog = false },
            onSave = { name, shortName, city, colorHex, captain, mobile ->
                viewModel.createTeam(name, shortName, city, colorHex, captain, mobile) {
                    showCreateTeamDialog = false
                }
            }
        )
    }
}

@Composable
fun TeamCard(
    team: Team,
    isAuthor: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamJerseyCircle(
                    teamName = team.name,
                    colorHex = team.colorHex,
                    sizeDp = 48
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = team.shortName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PitchGreen
                            )
                        }
                    }

                    Text(
                        text = "📍 ${team.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (isAuthor) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (team.captainName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Captain: ${team.captainName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                if (team.contactMobile.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = team.contactMobile,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, shortName: String, city: String, colorHex: String, captain: String, mobile: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var shortName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var captain by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#E53935") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val presetColors = listOf(
        "#E53935" to "Red",
        "#1E88E5" to "Blue",
        "#43A047" to "Green",
        "#FDD835" to "Yellow",
        "#8E24AA" to "Purple",
        "#FB8C00" to "Orange",
        "#00E676" to "Pitch Green"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Football Club / Team", color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (shortName.isBlank() && it.isNotBlank()) {
                            shortName = it.take(3).uppercase()
                        }
                    },
                    label = { Text("Team Name *") },
                    placeholder = { Text("e.g. Red Hawks FC") },
                    modifier = Modifier.fillMaxWidth().testTag("team_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PitchGreen,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shortName,
                        onValueChange = { shortName = it.take(4).uppercase() },
                        label = { Text("Code *") },
                        placeholder = { Text("RHK") },
                        modifier = Modifier.weight(0.4f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / Ground *") },
                        placeholder = { Text("San Francisco") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = captain,
                    onValueChange = { captain = it },
                    label = { Text("Captain Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Contact Mobile Number *") },
                    placeholder = { Text("+91 or +1 Mobile Number") },
                    modifier = Modifier.fillMaxWidth().testTag("team_mobile_input"),
                    singleLine = true
                )

                Text("Team Jersey Color", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetColors) { (hex, _) ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(2.dp, if (isSelected) PitchGreen else DarkCardBorder, CircleShape)
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || city.isBlank() || mobile.isBlank()) {
                        errorMsg = "Please fill in Team Name, City, and Mobile Number"
                        return@Button
                    }
                    onSave(name, shortName.ifBlank { name.take(3).uppercase() }, city, colorHex, captain, mobile)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_create_team_button")
            ) {
                Text("Create Team", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
