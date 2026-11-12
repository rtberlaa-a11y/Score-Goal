package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Match
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ScoreGoalRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createMatch_authenticatedUser_success() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ScoreGoalRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createMatch(
                title = "Local Derby",
                tournamentName = "City Cup",
                homeTeam = "Lions FC",
                awayTeam = "Tigers SC",
                homeTeamColor = "#D32F2F",
                awayTeamColor = "#1976D2",
                kickoffTime = "19:00",
                matchDate = "2026-11-12",
                venue = "West Park",
                format = "11v11"
            )
        }

        assertTrue("Expected createMatch to succeed", result.isSuccess)
        val matchId = result.getOrNull()
        assertNotNull(matchId)
    }

    @Test
    fun observeMatches_authenticatedUser_emitsMatches() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ScoreGoalRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createMatch(
                title = "Cup Semi-Final",
                tournamentName = "City Cup",
                homeTeam = "Hawks FC",
                awayTeam = "Falcons SC",
                homeTeamColor = "#388E3C",
                awayTeamColor = "#F57C00",
                kickoffTime = "20:00",
                matchDate = "2026-11-12",
                venue = "North Arena",
                format = "11v11"
            )
        }
        val matchId = createResult.getOrThrow()

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMatches().first { list ->
                list.any { it.id == matchId }
            }
        }
        assertTrue(emittedList.any { it.id == matchId })
    }

    @Test
    fun updateMatchScore_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = ScoreGoalRepository(firestore)
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createMatch(
                title = "League Match",
                tournamentName = "Premier League",
                homeTeam = "United",
                awayTeam = "City",
                homeTeamColor = "#D32F2F",
                awayTeamColor = "#1976D2",
                kickoffTime = "15:00",
                matchDate = "2026-11-12",
                venue = "Main Stadium",
                format = "11v11"
            )
        }
        val matchId = createResult.getOrThrow()

        // Sign in as Bob and attempt to update Alice's match score
        signInTestUser(BOB_EMAIL)
        val bobRepo = ScoreGoalRepository(firestore)

        val match = Match(id = matchId, authorId = "alice_uid", homeTeam = "United", awayTeam = "City")
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateMatchScore(match, newHomeScore = 5L, newAwayScore = 0L)
        }

        assertTrue("Expected update to fail for non-author", updateResult.isFailure)
        val ex = updateResult.exceptionOrNull()
        val firestoreEx = ex?.unwrapFirestoreException()
        if (firestoreEx != null) {
            assertTrue(firestoreEx.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
    }

    @Test
    fun createMatch_unauthenticated_fails() = runBlocking {
        auth.signOut()
        val repository = ScoreGoalRepository(firestore)

        try {
            repository.createMatch(
                title = "Unauth Match",
                tournamentName = "None",
                homeTeam = "A",
                awayTeam = "B",
                homeTeamColor = "#000",
                awayTeamColor = "#fff",
                kickoffTime = "12:00",
                matchDate = "2026-11-12",
                venue = "Pitch",
                format = "11v11"
            )
            fail("Expected exception when unauthenticated")
        } catch (e: IllegalStateException) {
            // Expected fail-fast
            assertTrue(e.message?.contains("User must be signed in") == true)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@scoregoal.test"
        const val BOB_EMAIL = "bob@scoregoal.test"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
