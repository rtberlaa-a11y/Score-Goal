package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Match
import com.example.ui.auth.signOutUser
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemePresets
import com.example.ui.viewmodel.ScoreGoalViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentUserId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val viewModel: ScoreGoalViewModel = viewModel(key = currentUserId)

    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeMatch by viewModel.activeMatch.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val selectedPlayerProfile by viewModel.selectedPlayerProfile.collectAsState()
    val followingPlayerIds by viewModel.followingPlayerIds.collectAsState()
    val backgroundTheme by viewModel.backgroundTheme.collectAsState()
    val isRegistrationDismissed by viewModel.isRegistrationDismissed.collectAsState()

    val currentTheme = ThemePresets.getTheme(backgroundTheme)

    var activeControllerMatch by remember { mutableStateOf<Match?>(null) }
    var isPublishingMatch by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // CricHeroes-style Compulsory Registration: mobile number, name, age, and location are compulsory for all players
    LaunchedEffect(currentUserProfile) {
        if (currentUserProfile != null && !currentUserProfile!!.isProfileComplete) {
            showRegistrationDialog = true
        }
    }

    val currentUser = Firebase.auth.currentUser

    // Determine current screen
    when {
        isSettingsOpen -> {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { isSettingsOpen = false }
            )
        }
        selectedPlayerProfile != null -> {
            val player = selectedPlayerProfile!!
            val isFollowing = followingPlayerIds.contains(player.userId)
            PlayerDetailView(
                player = player,
                isCurrentUser = player.userId == currentUser?.uid,
                isFollowing = isFollowing,
                onFollowClick = { viewModel.toggleFollowPlayer(player) },
                onEditClick = { showRegistrationDialog = true },
                onBack = { viewModel.selectPlayerProfile(null) }
            )
        }
        activeControllerMatch != null -> {
            MatchControllerScreen(
                match = activeControllerMatch!!,
                viewModel = viewModel,
                onBack = { activeControllerMatch = null }
            )
        }
        activeMatch != null -> {
            MatchDetailScreen(
                match = activeMatch!!,
                viewModel = viewModel,
                onBack = { viewModel.selectMatch(null) },
                onOpenController = { activeControllerMatch = it }
            )
        }
        isPublishingMatch -> {
            PublishMatchScreen(
                viewModel = viewModel,
                onBack = { isPublishingMatch = false },
                onMatchPublished = { _ ->
                    isPublishingMatch = false
                }
            )
        }
        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.setTab("matches") }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, PitchGreen, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_score_goal_logo),
                                        contentDescription = "Score Goal Logo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SCORE GOAL",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "LIVE FOOTBALL & STATS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PitchGreen
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentUser != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    // Settings & Support Button
                                    IconButton(
                                        onClick = { isSettingsOpen = true },
                                        modifier = Modifier.testTag("open_settings_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings & Support",
                                            tint = PitchGreen
                                        )
                                    }

                                    // Profile avatar thumbnail button
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceVariant)
                                            .border(1.5.dp, PitchGreen, CircleShape)
                                            .clickable { viewModel.setTab("profile") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentUserProfile?.avatarUri?.isNotBlank() == true) {
                                            AsyncImage(
                                                model = currentUserProfile!!.avatarUri,
                                                contentDescription = "My Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Text(
                                                text = currentUserProfile?.jerseyNumber?.let { "#$it" }
                                                    ?: currentUser.displayName?.firstOrNull()?.uppercase()
                                                    ?: "P",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PitchGreen
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { signOutUser(credentialManager, scope) },
                                        modifier = Modifier.testTag("sign_out_button")
                                    ) {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = "Sign Out",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkSurface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 4.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == "matches",
                            onClick = { viewModel.setTab("matches") },
                            icon = {
                                Icon(Icons.Default.SportsSoccer, contentDescription = "Matches")
                            },
                            label = { Text("Matches") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PitchGreen,
                                indicatorColor = PitchGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_matches")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "teams",
                            onClick = { viewModel.setTab("teams") },
                            icon = {
                                Icon(Icons.Default.Group, contentDescription = "Teams")
                            },
                            label = { Text("Teams") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PitchGreen,
                                indicatorColor = PitchGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_teams")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "leaderboards",
                            onClick = { viewModel.setTab("leaderboards") },
                            icon = {
                                Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard")
                            },
                            label = { Text("Standings") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PitchGreen,
                                indicatorColor = PitchGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_leaderboards")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "notifications",
                            onClick = { viewModel.setTab("notifications") },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (notifications.isNotEmpty()) {
                                            Badge(containerColor = PitchGreen, contentColor = Color.Black) {
                                                Text(notifications.size.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                                }
                            },
                            label = { Text("Alerts") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PitchGreen,
                                indicatorColor = PitchGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_alerts")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "profile",
                            onClick = { viewModel.setTab("profile") },
                            icon = {
                                Icon(Icons.Default.Person, contentDescription = "My Profile")
                            },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PitchGreen,
                                indicatorColor = PitchGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { isPublishingMatch = true },
                        containerColor = PitchGreen,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.testTag("fab_publish_match")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Publish Match", modifier = Modifier.size(28.dp))
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = DarkBackground
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(currentTheme.backgroundBrush)
                ) {
                    when (selectedTab) {
                        "matches" -> MatchesScreen(
                            viewModel = viewModel,
                            onSelectMatch = { viewModel.selectMatch(it) },
                            onOpenController = { activeControllerMatch = it },
                            onOpenPublish = { isPublishingMatch = true }
                        )
                        "teams" -> TeamsScreen(viewModel = viewModel)
                        "leaderboards" -> LeaderboardsScreen(viewModel = viewModel)
                        "notifications" -> NotificationsScreen(viewModel = viewModel)
                        "profile" -> MyProfileTab(
                            viewModel = viewModel,
                            onEditClick = { showRegistrationDialog = true },
                            onOpenSettings = { isSettingsOpen = true }
                        )
                        else -> MatchesScreen(
                            viewModel = viewModel,
                            onSelectMatch = { viewModel.selectMatch(it) },
                            onOpenController = { activeControllerMatch = it },
                            onOpenPublish = { isPublishingMatch = true }
                        )
                    }
                }
            }
        }
    }

    if (showRegistrationDialog) {
        CricHeroesRegistrationDialog(
            initialProfile = currentUserProfile,
            onDismiss = {
                if (currentUserProfile?.isProfileComplete == true) {
                    showRegistrationDialog = false
                    viewModel.dismissRegistration()
                }
            },
            onSave = { name, phone, jersey, loc, pos, foot, bio, theme, avatar, age ->
                viewModel.savePlayerProfile(name, phone, jersey, loc, pos, foot, bio, theme, avatar, age)
                showRegistrationDialog = false
            }
        )
    }
}
