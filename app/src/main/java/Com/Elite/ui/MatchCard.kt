package Com.Elite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.model.Tournament
import Com.Elite.ui.theme.CardNavyBg
import Com.Elite.ui.theme.CardNavyBorder
import Com.Elite.ui.theme.CrimsonRed
import Com.Elite.ui.theme.DarkNavyBg
import Com.Elite.ui.theme.GoldYellow
import Com.Elite.ui.theme.MutedText
import Com.Elite.ui.theme.NeonCyan
import Com.Elite.ui.theme.NeonOrange
import Com.Elite.ui.theme.PureWhite
import Com.Elite.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun MatchCard(
    tournament: Tournament,
    hasJoined: Boolean,
    onRegisterClick: () -> Unit,
    onViewJoinedClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var countdownSeconds by remember(tournament.id) { mutableIntStateOf(tournament.countdownSeconds.coerceAtLeast(10)) }

    // Live countdown timer decreasing every second
    LaunchedEffect(tournament.id) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    val minutes = countdownSeconds / 60
    val seconds = countdownSeconds % 60
    val countdownFormatted = String.format("%02d:%02d", minutes, seconds)

    val isFull = tournament.isFull
    val isOpen = tournament.status == Tournament.STATUS_OPEN || tournament.status == Tournament.STATUS_LIVE
    val emptySlots = (tournament.maxPlayers - tournament.registeredCount).coerceAtLeast(0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_card_${tournament.id}"),
        colors = CardDefaults.cardColors(containerColor = CardNavyBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Bar: LIVE badge, Blue outline mode badge, Countdown & Bengali relative time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Red LIVE badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CrimsonRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (tournament.status == Tournament.STATUS_LIVE) "LIVE" else "UPCOMING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Blue outline mode badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                            .background(DarkNavyBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tournament.modeBadge.ifBlank { "BR MOD" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                // Countdown timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "STARTING IN = $countdownFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bengali relative time text
            Text(
                text = tournament.bengaliTimeText.ifBlank { "১০ মিনিট পর চালু হবে" },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonOrange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Big Title
            Text(
                text = tournament.title.ifBlank { "FULL MAP - SOLO" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Badges: TIME | MOBILE | REGULAR
            Text(
                text = tournament.badges.ifBlank { "TIME | MOBILE | REGULAR" },
                fontSize = 11.sp,
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x4 Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: WIN PRIZE | ENTRY FEE
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridTile(label = "WIN PRIZE", value = "৳${tournament.prizePool.toInt()}", valueColor = GoldYellow, modifier = Modifier.weight(1f))
                    GridTile(label = "ENTRY FEE", value = if (tournament.entryFee == 0.0) "FREE" else "৳${tournament.entryFee.toInt()}", valueColor = NeonCyan, modifier = Modifier.weight(1f))
                }

                // Row 2: ENTRY TYPE | PER KILL
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridTile(label = "ENTRY TYPE", value = tournament.entryType, valueColor = PureWhite, modifier = Modifier.weight(1f))
                    GridTile(label = "PER KILL", value = "৳${tournament.perKill.toInt()}", valueColor = SuccessGreen, modifier = Modifier.weight(1f))
                }

                // Row 3: MAP | VERSION
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridTile(label = "MAP", value = tournament.mapName, valueColor = PureWhite, modifier = Modifier.weight(1f))
                    GridTile(label = "VERSION", value = tournament.version, valueColor = PureWhite, modifier = Modifier.weight(1f))
                }

                // Row 4: DEVICE | CAPACITY
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridTile(label = "DEVICE", value = tournament.deviceType, valueColor = PureWhite, modifier = Modifier.weight(1f))
                    GridTile(label = "SLOTS FILLED", value = "${tournament.registeredCount}/${tournament.maxPlayers}", valueColor = NeonOrange, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bengali Admin Controlled Disclaimer Note
            Text(
                text = tournament.adminControlText.ifBlank { "সম্পূর্ণ এডমিনের হাতে কন্ট্রোল থাকবে এবং সম্পূর্ণ এডমিন পরিচালিত করবে" },
                fontSize = 11.sp,
                color = MutedText,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button (REGISTER / JOINED / FULL)
            val buttonText = when {
                hasJoined -> "JOINED"
                isFull -> "MATCH FULL"
                !isOpen -> tournament.status.uppercase()
                else -> "REGISTER"
            }

            val buttonColor = when {
                hasJoined -> SuccessGreen
                isFull -> CrimsonRed
                !isOpen -> CardNavyBorder
                else -> NeonCyan
            }

            Button(
                onClick = onRegisterClick,
                enabled = isOpen && !hasJoined && !isFull,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("join_button_${tournament.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    disabledContainerColor = if (hasJoined) SuccessGreen else CardNavyBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasJoined) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkNavyBg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = buttonText,
                        color = if (hasJoined || (isOpen && !isFull)) DarkNavyBg else MutedText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. JOIN DETAILS BAR (Below JOIN Button): [ JOINED: 1 ] [ EMPTY: 49 ] [ TOTAL: 50 ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkNavyBg)
                        .border(1.dp, SuccessGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("JOINED: ${tournament.registeredCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkNavyBg)
                        .border(1.dp, CardNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("EMPTY: $emptySlots", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkNavyBg)
                        .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("TOTAL: ${tournament.maxPlayers}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Neon Cyan Capacity / Progress Bar (e.g. 1/50)
            val progress = if (tournament.maxPlayers > 0) tournament.registeredCount.toFloat() / tournament.maxPlayers else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isFull) CrimsonRed else NeonCyan,
                trackColor = DarkNavyBg
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. VIEW WHO JOINED BUTTON (Opens Full Screen Page)
            androidx.compose.material3.OutlinedButton(
                onClick = onViewJoinedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardNavyBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "👁️ VIEW WHO JOINED (${tournament.registeredCount}/${tournament.maxPlayers}) >",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GridTile(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkNavyBg)
            .border(1.dp, CardNavyBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, fontSize = 9.sp, color = MutedText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
