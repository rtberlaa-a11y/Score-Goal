package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PlayerProfile
import com.example.data.model.SCORE_GOAL_RANK_TIERS
import com.example.data.model.GoalRankTier
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemePresets
import com.example.ui.viewmodel.ScoreGoalViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File
import java.io.FileOutputStream

@Composable
fun CricHeroesRegistrationDialog(
    initialProfile: PlayerProfile?,
    allPlayers: List<PlayerProfile> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, jersey: Long, location: String, pos: String, foot: String, bio: String, theme: String, avatarUri: String, age: Long) -> Unit
) {
    val context = LocalContext.current
    val currentAuthUser = Firebase.auth.currentUser
    val defaultName = initialProfile?.name?.ifBlank { currentAuthUser?.displayName ?: "" } ?: (currentAuthUser?.displayName ?: "")
    val isProfileAlreadyComplete = initialProfile?.isProfileComplete == true

    var name by remember { mutableStateOf(defaultName) }
    var mobileNumber by remember { mutableStateOf(initialProfile?.mobileNumber ?: "") }
    var ageText by remember { mutableStateOf(initialProfile?.age?.toString() ?: "21") }
    var jerseyNumberText by remember { mutableStateOf(initialProfile?.jerseyNumber?.toString() ?: "10") }
    var location by remember { mutableStateOf(initialProfile?.location ?: "") }
    var position by remember { mutableStateOf(initialProfile?.position ?: "Forward") }
    var strongFoot by remember { mutableStateOf(initialProfile?.strongFoot ?: "Right") }
    var bio by remember { mutableStateOf(initialProfile?.bio ?: "Ready to score goals!") }
    var backgroundTheme by remember { mutableStateOf(initialProfile?.backgroundTheme ?: "midnight_stadium") }
    var avatarUri by remember { mutableStateOf(initialProfile?.avatarUri ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    avatarUri = Uri.fromFile(file).toString()
                } else {
                    avatarUri = uri.toString()
                }
            } catch (e: Exception) {
                avatarUri = uri.toString()
            }
        }
    }

    val positions = listOf("Forward", "Midfielder", "Defender", "Goalkeeper", "Winger")
    val feet = listOf("Right", "Left", "Both")

    AlertDialog(
        onDismissRequest = {
            if (isProfileAlreadyComplete) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isProfileAlreadyComplete,
            dismissOnClickOutside = isProfileAlreadyComplete
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PitchGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isProfileAlreadyComplete) "Edit Player Profile" else "Compulsory Registration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = if (isProfileAlreadyComplete) "Score Goal Football Roster & Stats" else "Mobile #, Name, Age, & Location are compulsory",
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
                // Profile Avatar Photo Customization from Gallery
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(2.dp, PitchGreen, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Custom Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Pick Photo from Gallery",
                                    tint = PitchGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.testTag("pick_gallery_photo_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (avatarUri.isNotBlank()) "Change Photo from Gallery" else "Upload Picture from Gallery",
                                color = PitchGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PitchGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Mobile Number (Compulsory for all)
                item {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Registered Mobile Number * (Compulsory)") },
                        placeholder = { Text("e.g. +91 9876543210 or +1 555-0199") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PitchGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Age & Jersey Number
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Age *") },
                            placeholder = { Text("21") },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = PitchGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("reg_age_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = jerseyNumberText,
                            onValueChange = { jerseyNumberText = it },
                            label = { Text("Jersey # *") },
                            placeholder = { Text("10") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PitchGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("reg_jersey_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreen,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Location (Compulsory)
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("City / Playing Location * (Compulsory)") },
                        placeholder = { Text("e.g. Metro Park or London") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PitchGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_location_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Position Selector
                item {
                    Text("Playing Position", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(positions) { pos ->
                            val selected = position == pos
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) PitchGreen else DarkSurfaceVariant)
                                    .clickable { position = pos }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pos,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Strong Foot
                item {
                    Text("Preferred Foot", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        feet.forEach { f ->
                            val selected = strongFoot == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) PitchGreen else DarkSurfaceVariant)
                                    .clickable { strongFoot = f }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = f,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Background Customization Theme
                item {
                    Text("Stadium Background Theme", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ThemePresets.themes) { themeItem ->
                            val isSelected = backgroundTheme == themeItem.key
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { backgroundTheme = themeItem.key }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.previewColor)
                                        .border(2.dp, if (isSelected) PitchGreen else DarkCardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = themeItem.name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isSelected) PitchGreen else TextSecondary
                                )
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageNum = ageText.toLongOrNull() ?: 0L
                    if (name.isBlank() || mobileNumber.isBlank() || location.isBlank() || ageNum <= 0L) {
                        errorMessage = "Mobile number, Full Name, Age (>0), and Location are compulsory."
                        return@Button
                    }
                    val jerseyNum = jerseyNumberText.toLongOrNull() ?: 10L
                    onSave(name, mobileNumber, jerseyNum, location, position, strongFoot, bio, backgroundTheme, avatarUri, ageNum)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_registration_button")
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (isProfileAlreadyComplete) {
            {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_registration_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        } else null,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PlayerDetailView(
    player: PlayerProfile,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onEditClick: () -> Unit,
    onBack: () -> Unit
) {
    val theme = ThemePresets.getTheme(player.backgroundTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Player Card",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            if (isCurrentUser) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PitchGreen)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar or Jersey Badge
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor)
                                .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (player.avatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = player.avatarUri,
                                    contentDescription = "Player Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = "#${player.jerseyNumber}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(20.dp))
                        }

                        Text(
                            text = "${player.position} • Age ${player.age} • #${player.jerseyNumber} • ${player.strongFoot}-footed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.accentColor
                        )

                        Text(
                            text = "📍 ${player.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        if (player.mobileNumber.isNotBlank()) {
                            Text(
                                text = "📱 ${player.mobileNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Follow button / Followers count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${player.followersCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text("Followers", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${player.overallRating}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldAccent
                                )
                                Text("Overall Rating", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }

                            if (!isCurrentUser) {
                                Button(
                                    onClick = onFollowClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) DarkSurfaceVariant else theme.accentColor,
                                        contentColor = if (isFollowing) TextPrimary else Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isFollowing) "Following" else "Follow", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Stats Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Career Ranking Stats",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatBox("⚽ Goals", player.goals.toString(), theme.accentColor)
                            StatBox("🎯 Assists", player.assists.toString(), Color(0xFF38BDF8))
                            StatBox("👟 Passes", player.passes.toString(), GoldAccent)
                            StatBox("⭐ MVPs", player.mvpCount.toString(), Color(0xFFFF5722))
                        }
                    }
                }
            }

            // Bio
            if (player.bio.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Player Bio",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = theme.accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = player.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyProfileTab(
    viewModel: ScoreGoalViewModel,
    onEditClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val backgroundTheme by viewModel.backgroundTheme.collectAsState()
    val theme = ThemePresets.getTheme(backgroundTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Player Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PitchGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("profile_settings_nav_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp), tint = PitchGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("edit_my_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (currentUserProfile == null || !currentUserProfile!!.isProfileComplete) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchGreen)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Complete Your Score Card Ready",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Mobile number registration, name, age, and location are compulsory for all players. Register your details to get your player score card ready!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("get_score_card_ready_button")
                    ) {
                        Text("Get Score Card Ready", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val p = currentUserProfile!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Player Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentColor)
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (p.avatarUri.isNotBlank()) {
                                    AsyncImage(
                                        model = p.avatarUri,
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = "#${p.jerseyNumber}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(20.dp))
                            }

                            Text(
                                text = "${p.position} • Age ${p.age} • #${p.jerseyNumber} • ${p.strongFoot}-footed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.accentColor
                            )

                            Text(
                                text = "📍 ${p.location}  •  📱 ${p.mobileNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatBox("⚽ Goals", p.goals.toString(), theme.accentColor)
                                StatBox("🎯 Assists", p.assists.toString(), Color(0xFF38BDF8))
                                StatBox("👟 Passes", p.passes.toString(), GoldAccent)
                                StatBox("⭐ Rating", p.overallRating.toString(), Color(0xFFFF5722))
                            }
                        }
                    }
                }

                // Settings & Support Option Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSettings() }
                            .testTag("profile_settings_support_card"),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PitchGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PitchGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Settings & Support",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Customize Location, Age, Jersey # or contact Help Center",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Background Customization Theme Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stadium Background Theme",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Personalize your app background aesthetic",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(ThemePresets.themes) { themeItem ->
                                    val isSelected = backgroundTheme == themeItem.key
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { viewModel.setBackgroundTheme(themeItem.key) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(themeItem.previewColor)
                                                .border(2.5.dp, if (isSelected) PitchGreen else DarkCardBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = themeItem.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PitchGreen else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, accent: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = accent)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
