package Com.Elite.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.R
import Com.Elite.model.Tournament
import Com.Elite.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailsScreen(
    tournament: Tournament,
    hasJoined: Boolean,
    onJoinClick: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Countdown logic
    var timeLeft by remember { 
        mutableLongStateOf(
            if (tournament.startTimeMillis > 0) {
                ((tournament.startTimeMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            } else {
                tournament.countdownSeconds.toLong()
            }
        )
    }
    LaunchedEffect(tournament.startTimeMillis) {
        while (timeLeft > 0) {
            delay(1000L)
            if (tournament.startTimeMillis > 0) {
                timeLeft = ((tournament.startTimeMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            } else {
                timeLeft--
            }
        }
    }
    
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeStr = String.format("%dm %ds", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tournament.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (timeLeft > 0) "LIVE SOON" else "LIVE NOW", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBg)
            )
        },
        containerColor = DarkNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 0. Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                        Text(tournament.gameMode, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PureWhite)
                        Text(tournament.mapName, fontSize = 14.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.elite_logo), // Placeholder for Map Image
                        contentDescription = "Map Image",
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(140.dp)
                            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 1. Countdown Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardNavyBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (timeLeft > 0) "$timeStr left • Starts in $timeStr" else "Match is LIVE NOW",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column - Details
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MATCH DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SilverAccent)
                    
                    DetailRowSmall(label = "Time", value = tournament.matchTime)
                    DetailRowSmall(label = "Slots", value = tournament.maxPlayers.toString())
                    DetailRowSmall(label = "Mode", value = tournament.entryType)
                    DetailRowSmall(label = "Map", value = tournament.mapName)
                    DetailRowSmall(label = "Entry Fee", value = "৳${tournament.entryFee.toInt()}", valueColor = NeonCyan)
                    DetailRowSmall(label = "Prize Pool", value = "৳${tournament.prizePool.toInt()}", valueColor = GoldYellow)
                    DetailRowSmall(label = "Per Kill", value = "৳${tournament.perKill.toInt()}", valueColor = SuccessGreen)
                }

                // Right Column - Room
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ROOM DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SilverAccent)
                    
                    RoomDetailBox(
                        label = "Room ID",
                        value = if (hasJoined) tournament.roomId.ifBlank { "TBA" } else "12345678",
                        isLocked = !hasJoined,
                        onCopy = {
                            if (hasJoined) {
                                clipboardManager.setText(AnnotatedString(tournament.roomId))
                                Toast.makeText(context, "Room ID Copied!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    RoomDetailBox(
                        label = "Password",
                        value = if (hasJoined) tournament.roomPassword.ifBlank { "TBA" } else "FF2026",
                        isLocked = !hasJoined,
                        onCopy = {
                            if (hasJoined) {
                                clipboardManager.setText(AnnotatedString(tournament.roomPassword))
                                Toast.makeText(context, "Password Copied!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("..........", color = MutedText, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Join the match to unlock details", fontSize = 10.sp, color = MutedText, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* More details */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VIEW DETAILS", color = NeonCyan, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onJoinClick,
                    enabled = !hasJoined && !tournament.isFull,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (hasJoined) "JOINED" else "JOIN NOW", fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DetailRowSmall(label: String, value: String, valueColor: Color = PureWhite) {
    Column {
        Text(label, fontSize = 10.sp, color = MutedText)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun RoomDetailBox(label: String, value: String, isLocked: Boolean, onCopy: () -> Unit) {
    var isCopied by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardNavyBg)
            .border(1.dp, CardNavyBorder, RoundedCornerShape(12.dp))
            .clickable { 
                if (!isLocked) {
                    onCopy()
                    isCopied = true
                }
            }
            .padding(12.dp)
    ) {
        Text(label, fontSize = 10.sp, color = MutedText)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLocked) MutedText else PureWhite
            )
            if (!isLocked) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = if (isCopied) SuccessGreen else NeonCyan, modifier = Modifier.size(16.dp))
            }
        }
        if (!isLocked) {
            Text(
                text = if (isCopied) "Copied!" else "Tap to copy",
                fontSize = 9.sp,
                color = if (isCopied) SuccessGreen else NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
