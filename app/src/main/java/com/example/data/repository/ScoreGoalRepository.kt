package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Match
import com.example.data.model.MatchEvent
import com.example.data.model.NotificationItem
import com.example.data.model.PlayerProfile
import com.example.data.model.Standing
import com.example.data.model.Subscription
import com.example.data.model.Team
import com.example.data.model.Tournament
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ScoreGoalRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun currentUserName(): String {
        return auth.currentUser?.displayName ?: "User"
    }

    // ----------------------------------------------------
    // MATCHES
    // ----------------------------------------------------

    fun observeMatches(): Flow<List<Match>> = flow {
        val path = "matches"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Match::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMatch(matchId: String): Flow<Match?> = flow {
        val path = "matches/$matchId"
        emitAll(
            db.collection("matches").document(matchId)
                .snapshots()
                .map { snapshot -> snapshot.toObject(Match::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createMatch(
        title: String,
        tournamentName: String,
        homeTeam: String,
        awayTeam: String,
        homeTeamColor: String,
        awayTeamColor: String,
        kickoffTime: String,
        matchDate: String,
        venue: String,
        format: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("matches").document()
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to currentUserName(),
            "title" to title.ifBlank { "$homeTeam vs $awayTeam" },
            "tournamentName" to tournamentName.ifBlank { "Local League" },
            "homeTeam" to homeTeam.trim(),
            "awayTeam" to awayTeam.trim(),
            "homeTeamColor" to homeTeamColor,
            "awayTeamColor" to awayTeamColor,
            "homeScore" to 0L,
            "awayScore" to 0L,
            "status" to "upcoming",
            "currentMinute" to 0L,
            "kickoffTime" to kickoffTime,
            "matchDate" to matchDate,
            "venue" to venue.ifBlank { "Local Pitch" },
            "format" to format,
            "summary" to "Match scheduled to kick off soon.",
            "homeShots" to 0L,
            "awayShots" to 0L,
            "homeShotsOnTarget" to 0L,
            "awayShotsOnTarget" to 0L,
            "homeFouls" to 0L,
            "awayFouls" to 0L,
            "homeCorners" to 0L,
            "awayCorners" to 0L,
            "homePossession" to 50L,
            "awayPossession" to 50L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMatchScore(
        match: Match,
        newHomeScore: Long,
        newAwayScore: Long,
        summary: String? = null
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(match.id)
        val updatedSummary = summary ?: "${match.homeTeam} $newHomeScore - $newAwayScore ${match.awayTeam}"
        val updatedMap = mapOf(
            "homeScore" to newHomeScore,
            "awayScore" to newAwayScore,
            "summary" to updatedSummary,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updatedMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMatchStatus(
        matchId: String,
        newStatus: String,
        currentMinute: Long,
        summary: String
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(matchId)
        val updateMap = mapOf(
            "status" to newStatus,
            "currentMinute" to currentMinute,
            "summary" to summary,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMatchTactics(
        matchId: String,
        homeFormation: String,
        awayFormation: String,
        homeTactics: String,
        awayTactics: String,
        venue: String? = null
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(matchId)
        val updateMap = mutableMapOf<String, Any>(
            "homeFormation" to homeFormation,
            "awayFormation" to awayFormation,
            "homeTactics" to homeTactics,
            "awayTactics" to awayTactics,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!venue.isNullOrBlank()) {
            updateMap["venue"] = venue.trim()
        }
        return try {
            docRef.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMatchVenue(matchId: String, venue: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(matchId)
        return try {
            docRef.update(
                mapOf(
                    "venue" to venue.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMatchStats(
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
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(matchId)
        val updateMap = mapOf(
            "homeShots" to homeShots,
            "awayShots" to awayShots,
            "homeShotsOnTarget" to homeShotsOnTarget,
            "awayShotsOnTarget" to awayShotsOnTarget,
            "homeFouls" to homeFouls,
            "awayFouls" to awayFouls,
            "homeCorners" to homeCorners,
            "awayCorners" to awayCorners,
            "homePossession" to homePossession,
            "awayPossession" to awayPossession,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMatch(matchId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("matches").document(matchId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // MATCH EVENTS
    // ----------------------------------------------------

    fun observeMatchEvents(matchId: String): Flow<List<MatchEvent>> = flow {
        val path = "matches/$matchId/events"
        emitAll(
            db.collection("matches").document(matchId).collection("events")
                .orderBy("minute", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(MatchEvent::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addMatchEvent(
        match: Match,
        minute: Long,
        team: String,
        type: String,
        playerName: String,
        secondaryPlayer: String = "",
        detail: String = ""
    ): Result<String> {
        val uid = requireUserId()
        val eventsColl = db.collection("matches").document(match.id).collection("events")
        val docRef = eventsColl.document()
        val payload = mapOf(
            "matchId" to match.id,
            "authorId" to uid,
            "minute" to minute,
            "team" to team,
            "type" to type,
            "playerName" to playerName.trim(),
            "secondaryPlayer" to secondaryPlayer.trim(),
            "detail" to detail.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()

            // If event is a goal, increment the corresponding team's score in the parent match!
            if (type == "goal" || type == "penalty") {
                val newHome = if (team == "home") match.homeScore + 1 else match.homeScore
                val newAway = if (team == "away") match.awayScore + 1 else match.awayScore
                val goalSummary = "GOAL! $playerName ($minute') for ${if (team == "home") match.homeTeam else match.awayTeam}!"
                db.collection("matches").document(match.id).update(
                    mapOf(
                        "homeScore" to newHome,
                        "awayScore" to newAway,
                        "currentMinute" to minute,
                        "summary" to goalSummary,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // TOURNAMENTS & STANDINGS (LEADERBOARDS)
    // ----------------------------------------------------

    fun observeTournaments(): Flow<List<Tournament>> = flow {
        val path = "tournaments"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Tournament::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeStandings(tournamentId: String): Flow<List<Standing>> = flow {
        val path = "tournaments/$tournamentId/standings"
        emitAll(
            db.collection("tournaments").document(tournamentId).collection("standings")
                .orderBy("points", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Standing::class.java).sortedWith(
                        compareByDescending<Standing> { it.points }
                            .thenByDescending { it.goalDifference }
                            .thenByDescending { it.goalsFor }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createTournament(name: String, season: String = "2026"): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("tournaments").document()
        val payload = mapOf(
            "name" to name.trim(),
            "season" to season.trim(),
            "authorId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateStanding(
        tournamentId: String,
        standingId: String? = null,
        teamName: String,
        played: Long,
        won: Long,
        drawn: Long,
        lost: Long,
        goalsFor: Long,
        goalsAgainst: Long
    ): Result<Unit> {
        val uid = requireUserId()
        val standingsColl = db.collection("tournaments").document(tournamentId).collection("standings")
        val docRef = if (standingId != null) standingsColl.document(standingId) else standingsColl.document()
        val goalDiff = goalsFor - goalsAgainst
        val points = (won * 3) + drawn
        val payload = mapOf(
            "tournamentId" to tournamentId,
            "authorId" to uid,
            "teamName" to teamName.trim(),
            "played" to played,
            "won" to won,
            "drawn" to drawn,
            "lost" to lost,
            "goalsFor" to goalsFor,
            "goalsAgainst" to goalsAgainst,
            "goalDifference" to goalDiff,
            "points" to points,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // USER SUBSCRIPTIONS (FOLLOW MATCHES)
    // ----------------------------------------------------

    fun observeSubscriptions(): Flow<List<Subscription>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/subscriptions"
        emitAll(
            db.collection("users").document(uid).collection("subscriptions")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Subscription::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun toggleSubscription(matchId: String, matchTitle: String, isSubscribed: Boolean): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("subscriptions").document(matchId)
        return try {
            if (isSubscribed) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "matchId" to matchId,
                    "matchTitle" to matchTitle,
                    "subscribedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // USER NOTIFICATIONS
    // ----------------------------------------------------

    fun observeNotifications(): Flow<List<NotificationItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(NotificationItem::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun sendNotification(
        matchId: String,
        title: String,
        message: String,
        type: String = "goal"
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notifications").document()
        val payload = mapOf(
            "userId" to uid,
            "matchId" to matchId,
            "title" to title,
            "message" to message,
            "type" to type,
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // PLAYER PROFILES (CRICHEROES STYLE RANKINGS & STATS)
    // ----------------------------------------------------

    fun observePlayerProfile(userId: String): Flow<PlayerProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot -> snapshot.toObject(PlayerProfile::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeAllPlayers(): Flow<List<PlayerProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PlayerProfile::class.java)
                        .filter { it.isProfileComplete }
                        .sortedByDescending { it.overallRating }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    suspend fun findPlayerByMobile(mobileNumber: String): PlayerProfile? {
        val clean = mobileNumber.trim()
        if (clean.isBlank()) return null
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("mobileNumber", clean)
                .limit(1)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.toObject(PlayerProfile::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun savePlayerProfile(
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
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existingDoc = try { docRef.get().await() } catch (e: Exception) { null }
        val isNew = existingDoc == null || !existingDoc.exists()

        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "name" to name.trim(),
            "mobileNumber" to mobileNumber.trim(),
            "jerseyNumber" to jerseyNumber,
            "location" to location.trim(),
            "position" to position,
            "strongFoot" to strongFoot,
            "bio" to bio.trim(),
            "backgroundTheme" to backgroundTheme,
            "avatarUri" to avatarUri,
            "age" to age,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (isNew) {
            payload["goals"] = 0L
            payload["assists"] = 0L
            payload["passes"] = 0L
            payload["matchesPlayed"] = 0L
            payload["mvpCount"] = 0L
            payload["followersCount"] = 0L
            payload["createdAt"] = FieldValue.serverTimestamp()
        }

        return try {
            if (isNew) {
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // PLAYER FOLLOWS
    // ----------------------------------------------------

    fun observeFollowing(): Flow<List<String>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/following"
        emitAll(
            db.collection("users").document(uid).collection("following")
                .snapshots()
                .map { snapshot -> snapshot.documents.map { it.id } }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun toggleFollowPlayer(targetUserId: String, isFollowing: Boolean): Result<Unit> {
        val uid = requireUserId()
        val followDoc = db.collection("users").document(uid).collection("following").document(targetUserId)
        val targetProfileDoc = db.collection("users").document(targetUserId)

        return try {
            if (isFollowing) {
                followDoc.delete().await()
                try {
                    targetProfileDoc.update("followersCount", FieldValue.increment(-1L)).await()
                } catch (e: Exception) { /* ignore */ }
            } else {
                followDoc.set(mapOf("followedAt" to FieldValue.serverTimestamp())).await()
                try {
                    targetProfileDoc.update("followersCount", FieldValue.increment(1L)).await()
                } catch (e: Exception) { /* ignore */ }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, followDoc.path)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // TEAMS
    // ----------------------------------------------------

    fun observeTeams(): Flow<List<Team>> = flow {
        val path = "teams"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Team::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createTeam(
        name: String,
        shortName: String,
        city: String,
        colorHex: String,
        captainName: String,
        contactMobile: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("teams").document()
        val payload = mapOf(
            "name" to name.trim(),
            "shortName" to shortName.trim().uppercase(),
            "city" to city.trim(),
            "colorHex" to colorHex,
            "authorId" to uid,
            "captainName" to captainName.trim(),
            "contactMobile" to contactMobile.trim(),
            "playersCount" to 1L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTeam(teamId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("teams").document(teamId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}

