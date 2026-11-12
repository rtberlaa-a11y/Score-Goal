package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PlayerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val mobileNumber: String = "",
    val jerseyNumber: Long = 10L,
    val location: String = "",
    val position: String = "Forward", // Forward, Midfielder, Defender, Goalkeeper, Winger
    val strongFoot: String = "Right", // Right, Left, Both
    val bio: String = "",
    val backgroundTheme: String = "midnight_stadium", // midnight_stadium, emerald_turf, cyber_neon, sunset_derby, classic_pitch
    val avatarUri: String = "",
    val age: Long = 21L,
    val goals: Long = 0L,
    val assists: Long = 0L,
    val passes: Long = 0L,
    val matchesPlayed: Long = 0L,
    val mvpCount: Long = 0L,
    val followersCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val overallRating: Long
        get() = (goals * 15L) + (assists * 10L) + (passes / 2L) + (mvpCount * 25L)

    val isProfileComplete: Boolean
        get() = mobileNumber.isNotBlank() && name.isNotBlank() && location.isNotBlank() && age > 0L

    val rankTier: GoalRankTier
        get() = getGoalRankForGoals(goals)

    val goalRankTitle: String
        get() = rankTier.title

    val nextRankTier: GoalRankTier?
        get() = getNextGoalRankForGoals(goals)

    val rankProgress: Float
        get() = getGoalRankProgress(goals)

    val goalsToNextRank: Long
        get() = nextRankTier?.let { (it.threshold - goals).coerceAtLeast(0L) } ?: 0L
}

data class GoalRankTier(
    val threshold: Long,
    val title: String,
    val badge: String,
    val colorHex: String,
    val description: String
)

val SCORE_GOAL_RANK_TIERS = listOf(
    GoalRankTier(5000L, "Universe Legend", "👑🌌", "#FFD700", "5,000+ goals! Universe Legend of all time"),
    GoalRankTier(4000L, "No Words to Say", "🤯⭐", "#E040FB", "4,000 goals! Beyond words, absolute historic god"),
    GoalRankTier(3000L, "Imposible", "⚡🔥", "#FF1744", "3,000 goals! Defying physical reality"),
    GoalRankTier(2000L, "Unbittable", "🛡️💎", "#00E676", "2,000 goals! Untouchable on every ground"),
    GoalRankTier(1000L, "Boss of Legends", "🏆✨", "#FFD700", "1,000 goals! Boss of all football legends"),
    GoalRankTier(750L, "Legend", "🌟🎖️", "#FFAB00", "750 career goals! Certified living football legend"),
    GoalRankTier(500L, "Universal Boss", "🪐👑", "#7C4DFF", "500 goals! Universal boss across every stadium"),
    GoalRankTier(400L, "Goadest", "🐐🔥", "#FF6D00", "400 goals! The goadest goal hunter in the game"),
    GoalRankTier(300L, "Positive Master", "🎯💫", "#00B0FF", "300 goals! Positive master with clinical accuracy"),
    GoalRankTier(250L, "Life Facer", "⚔️💪", "#00E5FF", "250 goals! Life facer standing tall against every defense"),
    GoalRankTier(200L, "God Play", "⚡👑", "#FF5252", "200 goals! Unstoppable god play in front of the net"),
    GoalRankTier(150L, "Master Level", "🏅⚡", "#651FFF", "150 goals! Reached true master level finishing"),
    GoalRankTier(100L, "Master Class", "🎓⚽", "#1DE9B6", "100 goals! Delivering master class displays every match"),
    GoalRankTier(75L, "Going Ahead", "🚀🏃", "#76FF03", "75 goals! Surging ahead leaving opponents behind"),
    GoalRankTier(50L, "Class Shower", "🎩✨", "#FFC400", "50 goals! Shower of pure football class"),
    GoalRankTier(25L, "Struggling Face", "😤🔥", "#FF9100", "25 goals! Conquered the struggling face through grit"),
    GoalRankTier(10L, "Active Player", "⚽🟢", "#00E676", "10 goals! Certified active striker on score sheets"),
    GoalRankTier(0L, "Rising Talent", "🌱⚽", "#9E9E9E", "Promising striker aiming for 10 goals to become Active Player")
)

fun getGoalRankForGoals(goals: Long): GoalRankTier {
    return SCORE_GOAL_RANK_TIERS.firstOrNull { goals >= it.threshold } ?: SCORE_GOAL_RANK_TIERS.last()
}

fun getNextGoalRankForGoals(goals: Long): GoalRankTier? {
    val current = getGoalRankForGoals(goals)
    val index = SCORE_GOAL_RANK_TIERS.indexOf(current)
    return if (index > 0) SCORE_GOAL_RANK_TIERS[index - 1] else null
}

fun getGoalRankProgress(goals: Long): Float {
    val current = getGoalRankForGoals(goals)
    val next = getNextGoalRankForGoals(goals) ?: return 1.0f
    val needed = (next.threshold - current.threshold).toFloat()
    if (needed <= 0f) return 1.0f
    val progress = (goals - current.threshold).toFloat() / needed
    return progress.coerceIn(0f, 1f)
}

@IgnoreExtraProperties
data class TeamPlayer(
    val name: String = "",
    val mobileNumber: String = "",
    val jerseyNumber: Long = 10L,
    val position: String = "FWD"
)

@IgnoreExtraProperties
data class Team(
    @DocumentId val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val city: String = "",
    val colorHex: String = "#E53935",
    val authorId: String = "",
    val captainName: String = "",
    val contactMobile: String = "",
    val formation: String = "4-3-3",
    val playersCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PitchPlayer(
    val name: String = "",
    val jerseyNumber: Long = 10L,
    val position: String = "FWD",
    val role: String = "Striker",
    val xRatio: Float = 0.5f, // 0.0 (left) to 1.0 (right)
    val yRatio: Float = 0.8f  // 0.0 (goal line) to 1.0 (forward)
)

@IgnoreExtraProperties
data class Match(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val tournamentName: String = "Local Friendly",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val homeTeamColor: String = "#E53935",
    val awayTeamColor: String = "#1E88E5",
    val homeScore: Long = 0L,
    val awayScore: Long = 0L,
    val status: String = "upcoming", // upcoming, live_1h, halftime, live_2h, extra_time, penalties, finished, cancelled
    val currentMinute: Long = 0L,
    val kickoffTime: String = "18:00",
    val matchDate: String = "",
    val venue: String = "City Stadium",
    val format: String = "11v11 (90m)",
    val summary: String = "",
    val homeFormation: String = "4-3-3",
    val awayFormation: String = "4-4-2",
    val homeTactics: String = "High Press & Quick Transitions",
    val awayTactics: String = "Counter-Attack & Solid Midfield",
    val homeShots: Long = 0L,
    val awayShots: Long = 0L,
    val homeShotsOnTarget: Long = 0L,
    val awayShotsOnTarget: Long = 0L,
    val homeFouls: Long = 0L,
    val awayFouls: Long = 0L,
    val homeCorners: Long = 0L,
    val awayCorners: Long = 0L,
    val homePossession: Long = 50L,
    val awayPossession: Long = 50L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isLive: Boolean
        get() = status in listOf("live_1h", "halftime", "live_2h", "extra_time", "penalties")

    val isFinished: Boolean
        get() = status == "finished"

    val statusDisplayName: String
        get() = when (status) {
            "upcoming" -> "Upcoming"
            "live_1h" -> "1st Half"
            "halftime" -> "Half Time"
            "live_2h" -> "2nd Half"
            "extra_time" -> "Extra Time"
            "penalties" -> "Penalties"
            "finished" -> "Full Time"
            "cancelled" -> "Cancelled"
            else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
}

@IgnoreExtraProperties
data class MatchEvent(
    @DocumentId val id: String = "",
    val matchId: String = "",
    val authorId: String = "",
    val minute: Long = 0L,
    val team: String = "home", // home, away, neutral
    val type: String = "goal", // goal, yellow_card, red_card, substitution, penalty, kickoff, halftime, fulltime, commentary
    val playerName: String = "",
    val secondaryPlayer: String = "",
    val detail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Tournament(
    @DocumentId val id: String = "",
    val name: String = "",
    val season: String = "2026",
    val authorId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Standing(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val authorId: String = "",
    val teamName: String = "",
    val played: Long = 0L,
    val won: Long = 0L,
    val drawn: Long = 0L,
    val lost: Long = 0L,
    val goalsFor: Long = 0L,
    val goalsAgainst: Long = 0L,
    val goalDifference: Long = 0L,
    val points: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Subscription(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val matchTitle: String = "",
    @ServerTimestamp val subscribedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class NotificationItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "goal",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
