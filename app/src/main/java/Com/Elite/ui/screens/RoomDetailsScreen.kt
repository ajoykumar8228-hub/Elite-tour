package Com.Elite.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.model.Tournament
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsScreen(
    tournament: Tournament,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val roomCredsState = viewModel.getRoomCredentials(tournament.id).collectAsState(initial = null)
    val roomCreds = roomCredsState.value
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Details", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // 1. Match Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.5.dp, NeonCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tournament.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan
                        ) {
                            Text(
                                text = tournament.gameMode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyBg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Map: ${tournament.mapName}", fontSize = 12.sp, color = MutedText)
                        Text("Device: ${tournament.deviceType}", fontSize = 12.sp, color = MutedText)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Match Time: ${tournament.matchDate} at ${tournament.matchTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldYellow
                    )
                }
            }

            // 2. Room ID & Password Section
            val isPublished = (roomCreds != null && roomCreds.isPublished && roomCreds.roomId.isNotBlank())

            if (isPublished) {
                // Room ID Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.5.dp, SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("CUSTOM ROOM ID", fontSize = 11.sp, color = MutedText, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = roomCreds.roomId,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(roomCreds.roomId))
                                Toast.makeText(context, "Room ID কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkNavyBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Password Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.5.dp, GoldYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("ROOM PASSWORD", fontSize = 11.sp, color = MutedText, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = roomCreds.roomPassword,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldYellow
                                )
                            }
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(roomCreds.roomPassword))
                                Toast.makeText(context, "Password কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkNavyBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.dp, GoldYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ROOM ID NOT PUBLISHED YET",
                            color = GoldYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ম্যাচ শুরু হওয়ার ১০-১৫ মিনিট আগে এখানে রুম আইডি ও পাসওয়ার্ড প্রকাশিত হবে। নোটিফিকেশনে লক্ষ্য রাখুন।",
                            color = PureWhite,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // 3. How to Join Guide
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("কীভাবে ম্যাচে জয়েন করবেন?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("১. Free Fire গেম ওপেন করুন।", fontSize = 12.sp, color = PureWhite)
                    Text("২. Mode Selection থেকে 'Custom' অপশনে যান।", fontSize = 12.sp, color = PureWhite)
                    Text("৩. Search বক্সে উপরের 'ROOM ID' পেস্ট করুন।", fontSize = 12.sp, color = PureWhite)
                    Text("৪. 'PASSWORD' দিন এবং নির্দিষ্ট স্লটে বসুন।", fontSize = 12.sp, color = PureWhite)
                    Text("৫. ম্যাচ শুরুর পর নির্দিষ্ট সময়ে খেলা আরম্ভ হবে।", fontSize = 12.sp, color = PureWhite)
                }
            }
        }
    }
}
