package Com.Elite.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.model.Tournament
import Com.Elite.model.TournamentRegistration
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinedPlayersScreen(
    tournament: Tournament,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val registrationsState = viewModel.observeTournamentRegistrations(tournament.id).collectAsState(initial = emptyList())
    val registrations = registrationsState.value
    val totalSlots = tournament.maxPlayers.coerceAtLeast(50)
    val joinedCount = registrations.size
    val emptyCount = (totalSlots - joinedCount).coerceAtLeast(0)
    val progress = (joinedCount.toFloat() / totalSlots.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Joined Players - ${tournament.title}",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${tournament.gameMode} • ${tournament.mapName}",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                },
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // 1. Stats Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SuccessGreen)
                        ) {
                            Text(
                                text = "JOINED: $joinedCount",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldYellow.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, GoldYellow)
                        ) {
                            Text(
                                text = "EMPTY: $emptyCount",
                                color = GoldYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Text(
                                text = "TOTAL: $totalSlots",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = DarkNavyBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (registrations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.dp, CardNavyBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🔥 Be the first to Join! Slots are filling up fast.",
                        color = GoldYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 2. 50 Slots Grid (5 columns per row, 5x10)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items((1..totalSlots).toList()) { slotNumber ->
                    val regIndex = slotNumber - 1
                    val reg = registrations.getOrNull(regIndex)
                    val slotLabel = String.format("%02d", slotNumber)

                    SlotCard(slotLabel = slotLabel, registration = reg)
                }
            }
        }
    }
}

@Composable
private fun SlotCard(slotLabel: String, registration: TournamentRegistration?) {
    val isJoined = registration != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isJoined) CardNavyBg else DarkNavyBg
        ),
        border = BorderStroke(
            1.dp,
            if (isJoined) SuccessGreen else CardNavyBorder
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Slot Label Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SLOT $slotLabel",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isJoined) NeonCyan else MutedText
                )
                if (isJoined) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Joined",
                        tint = SuccessGreen,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            if (isJoined) {
                // Player Avatar Circle
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(DarkNavyBg)
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = registration?.inGameName?.ifBlank { registration.playerName } ?: "Player",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "UID: ${registration?.freeFireUID?.ifBlank { "N/A" }}",
                        fontSize = 7.sp,
                        color = GoldYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CardNavyBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "⏳ EMPTY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
