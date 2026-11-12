package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Match
import com.example.data.model.MatchEvent
import com.example.data.model.NotificationItem
import com.example.data.model.PlayerProfile
import com.example.data.model.Standing
import com.example.data.model.Subscription
import com.example.data.model.Team
import com.example.data.model.Tournament
import com.example.data.repository.ScoreGoalRepository
import com.example.util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ScoreGoalViewModel(
    application: Application,
    private val repository: ScoreGoalRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        ScoreGoalRepository(application)
    )

    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    // Navigation and tab states
    private val _selectedTab = MutableStateFlow("matches") // "matches", "leaderboard", "teams", "notifications", "profile"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _matchFilter = MutableStateFlow("all") // "all", "live", "upcoming", "finished", "my_matches"
    val matchFilter: StateFlow<String> = _matchFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTournamentId = MutableStateFlow<String?>(null)
    val selectedTournamentId: StateFlow<String?> = _selectedTournamentId.asStateFlow()

    // Matches
    val matchesState: StateFlow<UiState<List<Match>>> = repository.observeMatches()
        .onEach { matches ->
            checkAndTriggerAlerts(matches)
        }
        .map<List<Match>, UiState<List<Match>>> { UiState.Success(it) }
        .catch { error ->
            emit(UiState.Error(error.message ?: "Failed to load matches"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Player Profile State (CricHeroes Style)
    private val _currentUserProfile = MutableStateFlow<PlayerProfile?>(null)
    val currentUserProfile: StateFlow<PlayerProfile?> = _currentUserProfile.asStateFlow()

    private val _allPlayers = MutableStateFlow<List<PlayerProfile>>(emptyList())
    val allPlayers: StateFlow<List<PlayerProfile>> = _allPlayers.asStateFlow()

    private val _followingPlayerIds = MutableStateFlow<List<String>>(emptyList())
    val followingPlayerIds: StateFlow<List<String>> = _followingPlayerIds.asStateFlow()

    private val _selectedPlayerProfile = MutableStateFlow<PlayerProfile?>(null)
    val selectedPlayerProfile: StateFlow<PlayerProfile?> = _selectedPlayerProfile.asStateFlow()

    private val _backgroundTheme = MutableStateFlow("midnight_stadium")
    val backgroundTheme: StateFlow<String> = _backgroundTheme.asStateFlow()

    private val _isRegistrationDismissed = MutableStateFlow(false)
    val isRegistrationDismissed: StateFlow<Boolean> = _isRegistrationDismissed.asStateFlow()

    // Teams
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    // Tournaments & Standings
    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    private val _standings = MutableStateFlow<List<Standing>>(emptyList())
    val standings: StateFlow<List<Standing>> = _standings.asStateFlow()

    // Subscriptions
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Selected Match & Events
    private val _activeMatch = MutableStateFlow<Match?>(null)
    val activeMatch: StateFlow<Match?> = _activeMatch.asStateFlow()

    private val _matchEvents = MutableStateFlow<List<MatchEvent>>(emptyList())
    val matchEvents: StateFlow<List<MatchEvent>> = _matchEvents.asStateFlow()

    // Status / Operation messages
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Cache of match scores to detect new goals and trigger notifications
    private val previousMatchScores = mutableMapOf<String, Pair<Long, Long>>()
    private val previousMatchStatuses = mutableMapOf<String, String>()

    init {
        NotificationHelper.createNotificationChannel(application)
        loadTournaments()
        loadSubscriptions()
        loadNotifications()
        loadCurrentUserProfile()
        loadAllPlayers()
        loadFollowing()
        loadTeams()
    }

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setMatchFilter(filter: String) {
        _matchFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun dismissRegistration() {
        _isRegistrationDismissed.value = true
    }

    fun selectPlayerProfile(player: PlayerProfile?) {
        _selectedPlayerProfile.value = player
    }

    fun setBackgroundTheme(themeKey: String) {
        _backgroundTheme.value = themeKey
        _currentUserProfile.value?.let { profile ->
            savePlayerProfile(
                name = profile.name,
                mobileNumber = profile.mobileNumber,
                jerseyNumber = profile.jerseyNumber,
                location = profile.location,
                position = profile.position,
                strongFoot = profile.strongFoot,
                bio = profile.bio,
                backgroundTheme = themeKey
            )
        }
    }

    private fun loadCurrentUserProfile() {
        val uid = currentUserId ?: return
        repository.observePlayerProfile(uid)
            .catch { emit(null) }
            .onEach { profile ->
                _currentUserProfile.value = profile
                if (profile != null && profile.backgroundTheme.isNotBlank()) {
                    _backgroundTheme.value = profile.backgroundTheme
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadAllPlayers() {
        repository.observeAllPlayers()
            .catch { emit(emptyList()) }
            .onEach { _allPlayers.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadFollowing() {
        repository.observeFollowing()
            .catch { emit(emptyList()) }
            .onEach { _followingPlayerIds.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadTeams() {
        repository.observeTeams()
            .catch { emit(emptyList()) }
            .onEach { _teams.value = it }
            .launchIn(viewModelScope)
    }

    fun copyImageToInternalStorage(uri: android.net.Uri): String {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
            val file = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            android.net.Uri.fromFile(file).toString()
        } catch (e: Exception) {
            uri.toString()
        }
    }

    fun savePlayerProfile(
        name: String,
        mobileNumber: String,
        jerseyNumber: Long,
        location: String,
        position: String,
        strongFoot: String,
        bio: String,
        backgroundTheme: String,
        avatarUri: String = "",
        age: Long = 21L
    ) {
        viewModelScope.launch {
            val result = repository.savePlayerProfile(
                name = name,
                mobileNumber = mobileNumber,
                jerseyNumber = jerseyNumber,
                location = location,
                position = position,
                strongFoot = strongFoot,
                bio = bio,
                backgroundTheme = backgroundTheme,
                avatarUri = avatarUri,
                age = age
            )
            if (result.isSuccess) {
                _userMessage.value = "Player profile updated successfully!"
                _isRegistrationDismissed.value = true
                loadCurrentUserProfile()
                loadAllPlayers()
            } else {
                _userMessage.value = "Failed to update profile: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updatePlayerSettings(
        location: String,
        age: Long,
        jerseyNumber: Long,
        name: String? = null,
        bio: String? = null
    ) {
        val current = _currentUserProfile.value
        savePlayerProfile(
            name = name?.ifBlank { null } ?: current?.name ?: "",
            mobileNumber = current?.mobileNumber ?: "",
            jerseyNumber = jerseyNumber,
            location = location,
            position = current?.position ?: "Forward",
            strongFoot = current?.strongFoot ?: "Right",
            bio = bio ?: current?.bio ?: "",
            backgroundTheme = current?.backgroundTheme ?: "midnight_stadium",
            avatarUri = current?.avatarUri ?: "",
            age = age
        )
    }

    fun submitSupportTicket(subject: String, description: String, callback: () -> Unit) {
        viewModelScope.launch {
            _userMessage.value = "Support ticket received for '$subject'! Our team will assist you."
            callback()
        }
    }

    fun updateMatchTactics(
        matchId: String,
        homeFormation: String,
        awayFormation: String,
        homeTactics: String,
        awayTactics: String,
        venue: String? = null
    ) {
        viewModelScope.launch {
            repository.updateMatchTactics(matchId, homeFormation, awayFormation, homeTactics, awayTactics, venue)
        }
    }

    fun updateMatchVenue(matchId: String, venue: String) {
        viewModelScope.launch {
            val result = repository.updateMatchVenue(matchId, venue)
            if (result.isSuccess) {
                _userMessage.value = "Ground updated to $venue"
            }
        }
    }

    fun findPlayerByMobile(mobile: String): PlayerProfile? {
        val clean = mobile.trim()
        if (clean.isBlank()) return null
        return _allPlayers.value.find { it.mobileNumber.trim() == clean }
    }

    fun lookupPlayerByMobile(mobile: String, onResult: (PlayerProfile?) -> Unit) {
        val local = findPlayerByMobile(mobile)
        if (local != null) {
            onResult(local)
            return
        }
        viewModelScope.launch {
            val result = repository.findPlayerByMobile(mobile)
            onResult(result)
        }
    }

    fun toggleFollowPlayer(targetPlayer: PlayerProfile) {
        val isFollowing = _followingPlayerIds.value.contains(targetPlayer.userId)
        viewModelScope.launch {
            val result = repository.toggleFollowPlayer(targetPlayer.userId, isFollowing)
            if (result.isSuccess) {
                _userMessage.value = if (isFollowing) "Unfollowed ${targetPlayer.name}" else "Following ${targetPlayer.name}!"
            }
        }
    }

    fun createTeam(
        name: String,
        shortName: String,
        city: String,
        colorHex: String,
        captainName: String,
        contactMobile: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createTeam(name, shortName, city, colorHex, captainName, contactMobile)
            if (result.isSuccess) {
                _userMessage.value = "Team '$name' created!"
                onSuccess(result.getOrThrow())
            } else {
                _userMessage.value = "Failed to create team: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            val result = repository.deleteTeam(teamId)
            if (result.isSuccess) {
                _userMessage.value = "Team removed"
            }
        }
    }

    private fun loadTournaments() {
        repository.observeTournaments()
            .catch { emit(emptyList()) }
            .onEach { list ->
                _tournaments.value = list
                if (_selectedTournamentId.value == null && list.isNotEmpty()) {
                    selectTournament(list.first().id)
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectTournament(tournamentId: String) {
        _selectedTournamentId.value = tournamentId
        repository.observeStandings(tournamentId)
            .catch { emit(emptyList()) }
            .onEach { _standings.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadSubscriptions() {
        repository.observeSubscriptions()
            .catch { emit(emptyList()) }
            .onEach { _subscriptions.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadNotifications() {
        repository.observeNotifications()
            .catch { emit(emptyList()) }
            .onEach { _notifications.value = it }
            .launchIn(viewModelScope)
    }

    fun selectMatch(match: Match?) {
        _activeMatch.value = match
        if (match != null) {
            repository.observeMatch(match.id)
                .catch { /* keep existing */ }
                .onEach { updated ->
                    if (updated != null) _activeMatch.value = updated
                }
                .launchIn(viewModelScope)

            repository.observeMatchEvents(match.id)
                .catch { emit(emptyList()) }
                .onEach { events ->
                    _matchEvents.value = events
                }
                .launchIn(viewModelScope)
        } else {
            _matchEvents.value = emptyList()
        }
    }

    private fun checkAndTriggerAlerts(matches: List<Match>) {
        val followedIds = _subscriptions.value.map { it.matchId }.toSet()

        for (match in matches) {
            val prevScore = previousMatchScores[match.id]
            val prevStatus = previousMatchStatuses[match.id]

            if (prevScore != null && followedIds.contains(match.id)) {
                if (match.homeScore > prevScore.first) {
                    val alertTitle = "GOAL! ${match.homeTeam}"
                    val alertMsg = "${match.homeTeam} scored! ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                    NotificationHelper.showMatchAlert(
                        getApplication(),
                        match.id.hashCode() + 1,
                        alertTitle,
                        alertMsg,
                        match.tournamentName
                    )
                    viewModelScope.launch {
                        repository.sendNotification(match.id, alertTitle, alertMsg, "goal")
                    }
                } else if (match.awayScore > prevScore.second) {
                    val alertTitle = "GOAL! ${match.awayTeam}"
                    val alertMsg = "${match.awayTeam} scored! ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                    NotificationHelper.showMatchAlert(
                        getApplication(),
                        match.id.hashCode() + 2,
                        alertTitle,
                        alertMsg,
                        match.tournamentName
                    )
                    viewModelScope.launch {
                        repository.sendNotification(match.id, alertTitle, alertMsg, "goal")
                    }
                }
            }

            if (prevStatus != null && prevStatus != match.status && followedIds.contains(match.id)) {
                val statusMsg = when (match.status) {
                    "live_1h" -> "KICK OFF: ${match.homeTeam} vs ${match.awayTeam} has started!"
                    "halftime" -> "HALF TIME: ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                    "live_2h" -> "SECOND HALF: ${match.homeTeam} vs ${match.awayTeam} resumed!"
                    "finished" -> "FULL TIME: ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                    else -> null
                }
                if (statusMsg != null) {
                    NotificationHelper.showMatchAlert(
                        getApplication(),
                        match.id.hashCode() + 3,
                        "Match Status: ${match.statusDisplayName}",
                        statusMsg,
                        match.tournamentName
                    )
                    viewModelScope.launch {
                        repository.sendNotification(match.id, "Status Update", statusMsg, "status")
                    }
                }
            }

            previousMatchScores[match.id] = Pair(match.homeScore, match.awayScore)
            previousMatchStatuses[match.id] = match.status
        }
    }

    fun toggleSubscription(match: Match) {
        val isSubscribed = _subscriptions.value.any { it.matchId == match.id }
        viewModelScope.launch {
            val result = repository.toggleSubscription(
                matchId = match.id,
                matchTitle = "${match.homeTeam} vs ${match.awayTeam}",
                isSubscribed = isSubscribed
            )
            if (result.isSuccess) {
                _userMessage.value = if (isSubscribed) "Unfollowed ${match.homeTeam} vs ${match.awayTeam}"
                else "Following ${match.homeTeam} vs ${match.awayTeam} for live alerts!"
            }
        }
    }

    fun publishMatch(
        title: String,
        tournamentName: String,
        homeTeam: String,
        awayTeam: String,
        homeTeamColor: String,
        awayTeamColor: String,
        kickoffTime: String,
        matchDate: String,
        venue: String,
        format: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createMatch(
                title = title,
                tournamentName = tournamentName,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                homeTeamColor = homeTeamColor,
                awayTeamColor = awayTeamColor,
                kickoffTime = kickoffTime,
                matchDate = matchDate,
                venue = venue,
                format = format
            )
            if (result.isSuccess) {
                _userMessage.value = "Match published successfully!"
                onSuccess(result.getOrThrow())
            } else {
                _userMessage.value = "Failed to publish: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateScore(match: Match, newHome: Long, newAway: Long) {
        viewModelScope.launch {
            val result = repository.updateMatchScore(match, newHome, newAway)
            if (result.isFailure) {
                _userMessage.value = "Error updating score: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateMatchStatus(matchId: String, status: String, minute: Long, summary: String) {
        viewModelScope.launch {
            val result = repository.updateMatchStatus(matchId, status, minute, summary)
            if (result.isFailure) {
                _userMessage.value = "Error: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateMatchStats(
        matchId: String,
        homeShots: Long,
        awayShots: Long,
        homeShotsOnTarget: Long,
        awayShotsOnTarget: Long,
        homeFouls: Long,
        awayFouls: Long,
        homeCorners: Long,
        awayCorners: Long,
        homePossession: Long,
        awayPossession: Long
    ) {
        viewModelScope.launch {
            repository.updateMatchStats(
                matchId,
                homeShots,
                awayShots,
                homeShotsOnTarget,
                awayShotsOnTarget,
                homeFouls,
                awayFouls,
                homeCorners,
                awayCorners,
                homePossession,
                awayPossession
            )
        }
    }

    fun addMatchEvent(
        match: Match,
        minute: Long,
        team: String,
        type: String,
        player: String,
        secondary: String = "",
        detail: String = ""
    ) {
        viewModelScope.launch {
            val result = repository.addMatchEvent(
                match, minute, team, type, player, secondary, detail
            )
            if (result.isSuccess) {
                _userMessage.value = "Event added: $player ($type)"
            } else {
                _userMessage.value = "Failed to add event: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteMatch(matchId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteMatch(matchId)
            if (result.isSuccess) {
                _userMessage.value = "Match removed"
                onDeleted()
            } else {
                _userMessage.value = "Failed to delete: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun createTournament(name: String, season: String, teams: List<String>) {
        viewModelScope.launch {
            val result = repository.createTournament(name, season)
            if (result.isSuccess) {
                val tournamentId = result.getOrThrow()
                for (team in teams) {
                    if (team.isNotBlank()) {
                        repository.updateStanding(
                            tournamentId = tournamentId,
                            teamName = team,
                            played = 0,
                            won = 0,
                            drawn = 0,
                            lost = 0,
                            goalsFor = 0,
                            goalsAgainst = 0
                        )
                    }
                }
                _userMessage.value = "Tournament '$name' created!"
                selectTournament(tournamentId)
            } else {
                _userMessage.value = "Error: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateTeamStanding(
        tournamentId: String,
        standingId: String?,
        teamName: String,
        played: Long,
        won: Long,
        drawn: Long,
        lost: Long,
        gf: Long,
        ga: Long
    ) {
        viewModelScope.launch {
            val result = repository.updateStanding(
                tournamentId, standingId, teamName, played, won, drawn, lost, gf, ga
            )
            if (result.isSuccess) {
                _userMessage.value = "Standing updated for $teamName"
            }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            val res1 = repository.createMatch(
                title = "Metro Premier Derby",
                tournamentName = "City Premier League 2026",
                homeTeam = "Red Hawks FC",
                awayTeam = "Blue Waves SC",
                homeTeamColor = "#E53935",
                awayTeamColor = "#1E88E5",
                kickoffTime = "18:00",
                matchDate = "2026-11-12",
                venue = "St. Jude Stadium - Pitch 1",
                format = "11v11 (90m)"
            )
            if (res1.isSuccess) {
                val mId = res1.getOrThrow()
                repository.updateMatchStatus(mId, "live_2h", 68, "GOAL! Marcus Johnson 68' puts Red Hawks in the lead!")
                repository.updateMatchScore(
                    Match(id = mId, homeTeam = "Red Hawks FC", awayTeam = "Blue Waves SC"),
                    2L, 1L,
                    "Red Hawks FC 2 - 1 Blue Waves SC"
                )
                repository.updateMatchStats(mId, 12, 9, 6, 4, 8, 11, 5, 4, 54, 46)
                repository.addMatchEvent(
                    Match(id = mId, homeTeam = "Red Hawks FC", awayTeam = "Blue Waves SC", homeScore = 1, awayScore = 1),
                    68, "home", "goal", "Marcus Johnson", "Alex Reed", "Right-footed curling shot from outside the box"
                )
                repository.addMatchEvent(
                    Match(id = mId, homeTeam = "Red Hawks FC", awayTeam = "Blue Waves SC"),
                    54, "away", "goal", "Lucas Silva", "", "Equalizer penalty tucked low into left corner"
                )
                repository.addMatchEvent(
                    Match(id = mId, homeTeam = "Red Hawks FC", awayTeam = "Blue Waves SC"),
                    24, "home", "goal", "Marcus Johnson", "", "Header from corner kick"
                )
            }

            repository.createMatch(
                title = "Cup Quarter-Final",
                tournamentName = "City Summer Cup",
                homeTeam = "City Rovers",
                awayTeam = "Lions FC",
                homeTeamColor = "#43A047",
                awayTeamColor = "#FDD835",
                kickoffTime = "20:30",
                matchDate = "2026-11-12",
                venue = "Arena Central",
                format = "11v11 (90m)"
            )

            createTournament(
                name = "City Premier League",
                season = "2026",
                teams = listOf("Red Hawks FC", "Blue Waves SC", "City Rovers", "Lions FC")
            )

            // Seed sample teams
            repository.createTeam("Red Hawks FC", "RHK", "San Francisco", "#E53935", "Marcus Johnson", "+19876543210")
            repository.createTeam("Blue Waves SC", "BWS", "San Jose", "#1E88E5", "Lucas Silva", "+19876543211")

            _userMessage.value = "Demo matches, teams and league table created!"
        }
    }
}
