package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.PitchPlayer
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class LineupPlayerInfo(
    val name: String,
    val mobileNumber: String,
    val jerseyNumber: Long,
    val position: String,
    val isCaptain: Boolean = false,
    val isGoalkeeper: Boolean = false,
    val xRatio: Float, // 0.0 (left) to 1.0 (right)
    val yRatio: Float  // 0.0 (goal line bottom) to 1.0 (forward top)
)

object FormationPresets {
    fun getLineupForFormation(
        formation: String,
        captainName: String,
        goalkeeperName: String,
        teamName: String
    ): List<LineupPlayerInfo> {
        val gk = goalkeeperName.ifBlank { "David (GK)" }
        val cap = captainName.ifBlank { "Marcus (C)" }

        return when (formation) {
            "4-3-3" -> listOf(
                LineupPlayerInfo(gk, "+91 9876543210", 1, "GK", isCaptain = false, isGoalkeeper = true, xRatio = 0.50f, yRatio = 0.08f),
                LineupPlayerInfo("Kyle W.", "+91 9876543211", 2, "RB", xRatio = 0.85f, yRatio = 0.28f),
                LineupPlayerInfo("John S.", "+91 9876543212", 5, "CB", xRatio = 0.62f, yRatio = 0.25f),
                LineupPlayerInfo("Ruben D.", "+91 9876543213", 4, "CB", xRatio = 0.38f, yRatio = 0.25f),
                LineupPlayerInfo("Nathan A.", "+91 9876543214", 3, "LB", xRatio = 0.15f, yRatio = 0.28f),
                LineupPlayerInfo("Rodri C.", "+91 9876543215", 16, "DM", xRatio = 0.50f, yRatio = 0.45f),
                LineupPlayerInfo("Kevin B.", "+91 9876543216", 17, "CM", xRatio = 0.72f, yRatio = 0.58f),
                LineupPlayerInfo("Bernardo S.", "+91 9876543217", 8, "CM", xRatio = 0.28f, yRatio = 0.58f),
                LineupPlayerInfo("Phil F.", "+91 9876543218", 47, "RW", xRatio = 0.82f, yRatio = 0.82f),
                LineupPlayerInfo(cap, "+91 9876543219", 9, "ST", isCaptain = true, isGoalkeeper = false, xRatio = 0.50f, yRatio = 0.86f),
                LineupPlayerInfo("Jack G.", "+91 9876543220", 10, "LW", xRatio = 0.18f, yRatio = 0.82f)
            )
            "4-4-2" -> listOf(
                LineupPlayerInfo(gk, "+91 9876543210", 1, "GK", isCaptain = false, isGoalkeeper = true, xRatio = 0.50f, yRatio = 0.08f),
                LineupPlayerInfo("Ben W.", "+91 9876543211", 2, "RB", xRatio = 0.85f, yRatio = 0.26f),
                LineupPlayerInfo("Gabriel M.", "+91 9876543212", 6, "CB", xRatio = 0.62f, yRatio = 0.24f),
                LineupPlayerInfo("William S.", "+91 9876543213", 4, "CB", xRatio = 0.38f, yRatio = 0.24f),
                LineupPlayerInfo("Jurrien T.", "+91 9876543214", 3, "LB", xRatio = 0.15f, yRatio = 0.26f),
                LineupPlayerInfo("Bukayo S.", "+91 9876543215", 7, "RM", xRatio = 0.85f, yRatio = 0.54f),
                LineupPlayerInfo("Declan R.", "+91 9876543216", 41, "CM", xRatio = 0.60f, yRatio = 0.50f),
                LineupPlayerInfo("Thomas P.", "+91 9876543217", 5, "CM", xRatio = 0.40f, yRatio = 0.50f),
                LineupPlayerInfo("Gabriel M.", "+91 9876543218", 11, "LM", xRatio = 0.15f, yRatio = 0.54f),
                LineupPlayerInfo(cap, "+91 9876543219", 10, "ST", isCaptain = true, isGoalkeeper = false, xRatio = 0.62f, yRatio = 0.82f),
                LineupPlayerInfo("Kai H.", "+91 9876543220", 29, "ST", xRatio = 0.38f, yRatio = 0.82f)
            )
            "3-5-2" -> listOf(
                LineupPlayerInfo(gk, "+91 9876543210", 1, "GK", isCaptain = false, isGoalkeeper = true, xRatio = 0.50f, yRatio = 0.08f),
                LineupPlayerInfo("Cristian R.", "+91 9876543211", 17, "CB", xRatio = 0.75f, yRatio = 0.25f),
                LineupPlayerInfo("Micky V.", "+91 9876543212", 37, "CB", xRatio = 0.50f, yRatio = 0.23f),
                LineupPlayerInfo("Radu D.", "+91 9876543213", 6, "CB", xRatio = 0.25f, yRatio = 0.25f),
                LineupPlayerInfo("Pedro P.", "+91 9876543214", 23, "RWB", xRatio = 0.88f, yRatio = 0.50f),
                LineupPlayerInfo("Pape S.", "+91 9876543215", 29, "CM", xRatio = 0.68f, yRatio = 0.48f),
                LineupPlayerInfo("Yves B.", "+91 9876543216", 8, "DM", xRatio = 0.50f, yRatio = 0.42f),
                LineupPlayerInfo("James M.", "+91 9876543217", 10, "AM", xRatio = 0.50f, yRatio = 0.62f),
                LineupPlayerInfo("Destiny U.", "+91 9876543218", 13, "LWB", xRatio = 0.12f, yRatio = 0.50f),
                LineupPlayerInfo(cap, "+91 9876543219", 7, "ST", isCaptain = true, isGoalkeeper = false, xRatio = 0.62f, yRatio = 0.82f),
                LineupPlayerInfo("Richarlison", "+91 9876543220", 9, "ST", xRatio = 0.38f, yRatio = 0.82f)
            )
            else -> listOf(
                LineupPlayerInfo(gk, "+91 9876543210", 1, "GK", isCaptain = false, isGoalkeeper = true, xRatio = 0.50f, yRatio = 0.08f),
                LineupPlayerInfo("Player 2", "+91 9876543211", 2, "RB", xRatio = 0.85f, yRatio = 0.26f),
                LineupPlayerInfo("Player 5", "+91 9876543212", 5, "CB", xRatio = 0.62f, yRatio = 0.24f),
                LineupPlayerInfo("Player 4", "+91 9876543213", 4, "CB", xRatio = 0.38f, yRatio = 0.24f),
                LineupPlayerInfo("Player 3", "+91 9876543214", 3, "LB", xRatio = 0.15f, yRatio = 0.26f),
                LineupPlayerInfo("Player 8", "+91 9876543215", 8, "CM", xRatio = 0.65f, yRatio = 0.50f),
                LineupPlayerInfo("Player 6", "+91 9876543216", 6, "CM", xRatio = 0.35f, yRatio = 0.50f),
                LineupPlayerInfo("Player 7", "+91 9876543217", 7, "RW", xRatio = 0.82f, yRatio = 0.78f),
                LineupPlayerInfo(cap, "+91 9876543218", 10, "AM", isCaptain = true, isGoalkeeper = false, xRatio = 0.50f, yRatio = 0.70f),
                LineupPlayerInfo("Player 11", "+91 9876543219", 11, "LW", xRatio = 0.18f, yRatio = 0.78f),
                LineupPlayerInfo("Player 9", "+91 9876543220", 9, "ST", xRatio = 0.50f, yRatio = 0.86f)
            )
        }
    }
}

@Composable
fun TacticalPitchView(
    match: Match,
    onPlayerClick: (LineupPlayerInfo) -> Unit
) {
    var selectedTeamTab by remember { mutableStateOf(0) } // 0: Home, 1: Away
    var currentFormation by remember(selectedTeamTab, match) {
        mutableStateOf(if (selectedTeamTab == 0) match.homeFormation else match.awayFormation)
    }

    val teamName = if (selectedTeamTab == 0) match.homeTeam else match.awayTeam
    val teamColorHex = if (selectedTeamTab == 0) match.homeTeamColor else match.awayTeamColor
    val tactics = if (selectedTeamTab == 0) match.homeTactics else match.awayTactics
    val captain = if (selectedTeamTab == 0) "Marcus Johnson" else "Lucas Silva"
    val goalkeeper = if (selectedTeamTab == 0) "David O'Connor" else "Mark Davis"

    val players = remember(currentFormation, teamName, captain, goalkeeper) {
        FormationPresets.getLineupForFormation(currentFormation, captain, goalkeeper, teamName)
    }

    val formationsList = listOf("4-3-3", "4-4-2", "3-5-2", "4-2-3-1")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Home vs Away Team Tab
        TabRow(
            selectedTabIndex = selectedTeamTab,
            containerColor = DarkSurface,
            contentColor = PitchGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTeamTab]),
                    color = PitchGreen
                )
            }
        ) {
            Tab(
                selected = selectedTeamTab == 0,
                onClick = { selectedTeamTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamJerseyCircle(teamName = match.homeTeam, colorHex = match.homeTeamColor, sizeDp = 20)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(match.homeTeam, maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_tactics_home")
            )
            Tab(
                selected = selectedTeamTab == 1,
                onClick = { selectedTeamTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamJerseyCircle(teamName = match.awayTeam, colorHex = match.awayTeamColor, sizeDp = 20)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(match.awayTeam, maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_tactics_away")
            )
        }

        // Formation selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Formation: $currentFormation",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PitchGreen
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                formationsList.forEach { f ->
                    val isSelected = currentFormation == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PitchGreen else DarkSurfaceVariant)
                            .clickable { currentFormation = f }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = f,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }

        // Strategy info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PitchGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tactical Playstyle: $tactics",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Captain: $captain • Keeper: $goalkeeper",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Interactive Tactical Football Pitch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3B20)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E6B3B))
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val pitchWidth = maxWidth
                val pitchHeight = maxHeight

                // Football pitch lines canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val lineColor = Color.White.copy(alpha = 0.55f)
                    val stroke = Stroke(width = 2.dp.toPx())

                    // Outer border
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                        size = Size(w - 32.dp.toPx(), h - 32.dp.toPx()),
                        style = stroke
                    )

                    // Halfway line
                    drawLine(
                        color = lineColor,
                        start = Offset(16.dp.toPx(), h / 2f),
                        end = Offset(w - 16.dp.toPx(), h / 2f),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Center Circle
                    drawCircle(
                        color = lineColor,
                        radius = 45.dp.toPx(),
                        center = Offset(w / 2f, h / 2f),
                        style = stroke
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = Offset(w / 2f, h / 2f)
                    )

                    // Goal Box (Bottom / Defending)
                    val boxW = w * 0.55f
                    val boxH = h * 0.16f
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - boxW) / 2f, 16.dp.toPx()),
                        size = Size(boxW, boxH),
                        style = stroke
                    )

                    // Goal Box (Top / Attacking)
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - boxW) / 2f, h - 16.dp.toPx() - boxH),
                        size = Size(boxW, boxH),
                        style = stroke
                    )
                }

                // Render players on the pitch
                players.forEach { p ->
                    val xOffset = pitchWidth * p.xRatio - 32.dp
                    val yOffset = pitchHeight * p.yRatio - 24.dp

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .clickable { onPlayerClick(p) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Player circle node
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(teamColorHex)))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.jerseyNumber.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )

                                // Captain / GK badge
                                if (p.isCaptain) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(GoldAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("C", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                } else if (p.isGoalkeeper) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("GK", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                }
                            }

                            // Player Name Tag
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = p.name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "💡 Tap any player on the pitch to inspect their career goals, assists, and registered mobile number.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
