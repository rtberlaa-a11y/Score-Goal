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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemePresets
import com.example.ui.viewmodel.ScoreGoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ScoreGoalViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val backgroundTheme by viewModel.backgroundTheme.collectAsState()
    val theme = ThemePresets.getTheme(backgroundTheme)

    // Player Customization Fields
    var name by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.name ?: "") }
    var location by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.location ?: "") }
    var ageText by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.age?.toString() ?: "21") }
    var jerseyNumberText by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.jerseyNumber?.toString() ?: "10") }
    var bio by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.bio ?: "") }
    var position by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.position ?: "Forward") }
    var strongFoot by remember(currentUserProfile) { mutableStateOf(currentUserProfile?.strongFoot ?: "Right") }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Support Ticket state
    var showSupportDialog by remember { mutableStateOf(false) }
    var supportSubject by remember { mutableStateOf("") }
    var supportDescription by remember { mutableStateOf("") }

    val positions = listOf("Forward", "Midfielder", "Defender", "Goalkeeper", "Winger")
    val feet = listOf("Right", "Left", "Both")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings & Support",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Profile Customization Section (Location, Age, Jersey Number, Name, Bio)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PitchGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PitchGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Customize Player Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Update Location, Age, Jersey Number & Playing Role",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PitchGreen
                                )
                            }
                        }

                        // Compulsory Registered Mobile Number (Read-Only Status)
                        OutlinedTextField(
                            value = currentUserProfile?.mobileNumber ?: "Compulsory Registration Required",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Registered Mobile Number (Compulsory)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PitchGreen) },
                            trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Locked to Account", tint = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PitchGreen) },
                            modifier = Modifier.fillMaxWidth().testTag("settings_name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Location & Age Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location / Ground *") },
                                placeholder = { Text("San Francisco, CA") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PitchGreen) },
                                modifier = Modifier.weight(0.6f).testTag("settings_location_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Age *") },
                                placeholder = { Text("22") },
                                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = PitchGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.4f).testTag("settings_age_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchGreen,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        // Jersey Number
                        OutlinedTextField(
                            value = jerseyNumberText,
                            onValueChange = { jerseyNumberText = it },
                            label = { Text("Jersey Number (1-99) *") },
                            placeholder = { Text("10") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PitchGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("settings_jersey_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Position Selector
                        Text("Playing Position", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(positions) { pos ->
                                val isSelected = position == pos
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PitchGreen else DarkSurfaceVariant)
                                        .clickable { position = pos }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = pos,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }

                        // Preferred Foot
                        Text("Preferred Foot", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            feet.forEach { f ->
                                val isSelected = strongFoot == f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PitchGreen else DarkSurfaceVariant)
                                        .clickable { strongFoot = f }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = f,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }

                        // Player Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Player Bio & Motto") },
                            placeholder = { Text("Ready to lead my squad to the championship!") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (saveSuccessMessage != null) {
                            Text(
                                text = saveSuccessMessage ?: "",
                                color = PitchGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isBlank() || location.isBlank()) {
                                    errorMessage = "Name and Location are required"
                                    return@Button
                                }
                                val age = ageText.toLongOrNull() ?: 21L
                                if (age <= 0L || age > 99L) {
                                    errorMessage = "Please enter a valid age between 5 and 99"
                                    return@Button
                                }
                                val jersey = jerseyNumberText.toLongOrNull() ?: 10L
                                errorMessage = null
                                viewModel.savePlayerProfile(
                                    name = name,
                                    mobileNumber = currentUserProfile?.mobileNumber ?: "",
                                    jerseyNumber = jersey,
                                    location = location,
                                    position = position,
                                    strongFoot = strongFoot,
                                    bio = bio,
                                    backgroundTheme = currentUserProfile?.backgroundTheme ?: "midnight_stadium",
                                    avatarUri = currentUserProfile?.avatarUri ?: "",
                                    age = age
                                )
                                saveSuccessMessage = "Settings updated successfully!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Customization Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Customer Support & Help Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Support & Help Center",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Helpline, issue reporting & FAQs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Text(
                            text = "Have questions about scoring matches, tournament leaderboards, or mobile login? Our dedicated community support team is available 24/7.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Contact items
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Official Support Email", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("support@scoregoal.app", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                        }

                        // Help Topics / FAQs
                        Text("Frequently Asked Questions", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PitchGreen)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FaqItem("How are player ratings calculated?", "Goals provide 15 points, assists give 10 points, passes give 0.5 points, and MVP selections grant 25 bonus points.")
                            FaqItem("Why is mobile registration compulsory?", "To verify legitimate player identity, avoid duplicate tournament rosters, and ensure reliable team lineups.")
                            FaqItem("How do I publish a local tournament?", "Tap the '+' floating button on the Matches screen, fill in team names, venue, and start scoring in real-time.")
                        }

                        Button(
                            onClick = { showSupportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_support_ticket_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = PitchGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Support Request / Feedback", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // About & Version Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE GOAL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = TextPrimary)
                        Text("Local Football Scoring, Roster Management & Public Broadcast", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Version 1.2.0 • Fair Play Community Standards Active", style = MaterialTheme.typography.labelSmall, color = PitchGreen)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }

    // Support Request Ticket Dialog
    if (showSupportDialog) {
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Customer Support", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Submit an issue, dispute a match score, or request feature assistance. Our team will review your ticket.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = supportSubject,
                        onValueChange = { supportSubject = it },
                        label = { Text("Subject *") },
                        placeholder = { Text("e.g. Score correction, Tournament query") },
                        modifier = Modifier.fillMaxWidth().testTag("support_subject_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = supportDescription,
                        onValueChange = { supportDescription = it },
                        label = { Text("Description & Details *") },
                        placeholder = { Text("Provide match details or issue description...") },
                        modifier = Modifier.fillMaxWidth().height(110.dp).testTag("support_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (error != null) {
                        Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (supportSubject.isBlank() || supportDescription.isBlank()) {
                            error = "Please fill in Subject and Description"
                            return@Button
                        }
                        viewModel.submitSupportTicket(supportSubject, supportDescription) {
                            showSupportDialog = false
                            supportSubject = ""
                            supportDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("submit_support_button")
                ) {
                    Text("Submit Ticket", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .padding(10.dp)
    ) {
        Text("Q: $question", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(answer, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
