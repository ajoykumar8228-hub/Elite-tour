package Com.Elite.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import Com.Elite.model.PlayerRankingItem
import Com.Elite.model.TournamentResult
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailsScreen(
    result: TournamentResult,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val myRanking = result.rankings.find { it.playerId == currentUid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Details", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardNavyBg)
            )
        },
        containerColor = DarkNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tournament Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.5.dp, GoldYellow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.tournamentTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldYellow
                        ) {
                            Text(
                                text = "OFFICIAL RESULT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyBg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Date: ${result.matchDateText}", fontSize = 12.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Prize Distributed: ৳${result.rankings.sumOf { it.prizeWon }.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            // 2. Winner Leaderboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WINNER LEADERBOARD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkNavyBg)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("POS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.weight(0.8f))
                        Text("IGN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.weight(2f))
                        Text("UID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.weight(1.5f))
                        Text("KILL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.weight(1f))
                        Text("PRIZE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val rankingsToDisplay = if (result.rankings.isNotEmpty()) result.rankings else listOf(
                        PlayerRankingItem(rank = 1, playerId = "", playerName = "NEXUSSTRIKER", inGameName = "NEXUS_FF", freeFireUID = "82093841", teamName = "Team A", kills = 8, placementPoints = 20, totalPoints = 28, prizeWon = 150.0),
                        PlayerRankingItem(rank = 2, playerId = "", playerName = "GAMER_BOY99", inGameName = "BOY_OP", freeFireUID = "94102934", teamName = "Team B", kills = 4, placementPoints = 15, totalPoints = 19, prizeWon = 50.0),
                        PlayerRankingItem(rank = 3, playerId = "", playerName = "DARK_KING", inGameName = "KING_PRO", freeFireUID = "51920394", teamName = "Team A", kills = 3, placementPoints = 10, totalPoints = 13, prizeWon = 20.0)
                    )

                    rankingsToDisplay.forEach { rank ->
                        val posColor = when (rank.rank) {
                            1 -> GoldYellow
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> PureWhite
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
                                if (rank.rank in 1..3) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = posColor, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text("#${rank.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = posColor)
                            }
                            Text(rank.inGameName.ifBlank { rank.playerName }, fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                            Text(rank.freeFireUID, fontSize = 11.sp, color = MutedText, modifier = Modifier.weight(1.5f))
                            Text("${rank.kills}", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("৳${rank.prizeWon.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = CardNavyBorder, thickness = 0.5.dp)
                    }
                }
            }

            // 3. My Performance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.5.dp, NeonCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MY PERFORMANCE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Your Kills", fontSize = 11.sp, color = MutedText)
                            Text("${myRanking?.kills ?: 8}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                        Column {
                            Text("Position", fontSize = 11.sp, color = MutedText)
                            Text("#${myRanking?.rank ?: 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                        }
                        Column {
                            Text("Prize Won", fontSize = 11.sp, color = MutedText)
                            Text("৳${myRanking?.prizeWon?.toInt() ?: 150}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}
