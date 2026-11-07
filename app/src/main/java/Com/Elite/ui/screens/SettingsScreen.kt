package Com.Elite.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var masterNotif by remember(user) { mutableStateOf(user?.notificationsEnabled ?: true) }
    var matchNotif by remember(user) { mutableStateOf(user?.matchStartNotify ?: true) }
    var resultNotif by remember(user) { mutableStateOf(user?.resultPublishedNotify ?: true) }
    var paymentNotif by remember(user) { mutableStateOf(user?.paymentApprovedNotify ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Push Notification Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PUSH NOTIFICATIONS (FCM)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Real-Time Notifications", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text("Receive match & payment updates", fontSize = 11.sp, color = MutedText)
                        }
                        Switch(checked = masterNotif, onCheckedChange = { masterNotif = it })
                    }

                    HorizontalDivider(color = CardNavyBorder, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Match Start Alerts", fontSize = 12.sp, color = PureWhite)
                            Text("Notify when Room ID is published", fontSize = 10.sp, color = MutedText)
                        }
                        Switch(checked = matchNotif && masterNotif, enabled = masterNotif, onCheckedChange = { matchNotif = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Result Published Alerts", fontSize = 12.sp, color = PureWhite)
                            Text("Notify when winners are declared", fontSize = 10.sp, color = MutedText)
                        }
                        Switch(checked = resultNotif && masterNotif, enabled = masterNotif, onCheckedChange = { resultNotif = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment Status Alerts", fontSize = 12.sp, color = PureWhite)
                            Text("Notify on Add Money / Withdraw status", fontSize = 10.sp, color = MutedText)
                        }
                        Switch(checked = paymentNotif && masterNotif, enabled = masterNotif, onCheckedChange = { paymentNotif = it })
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateNotificationSettings(
                                notificationsEnabled = masterNotif,
                                matchStartNotify = matchNotif,
                                resultPublishedNotify = resultNotif,
                                paymentApprovedNotify = paymentNotif
                            )
                            Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE NOTIFICATION SETTINGS", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Customer Support Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("24/7 CUSTOMER SUPPORT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801700000000"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WhatsApp", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/elitetwosupport"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Telegram", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Admin Panel Access (for admin users)
            if (user?.role == "admin") {
                Button(
                    onClick = onNavigateToAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENTER ADMIN CONTROL PANEL", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // App Info
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ELITE TWO • Bangladesh's No.1 Free Fire Arena", fontSize = 11.sp, color = MutedText)
                Text("App Version 2.5.0 (Build 2026)", fontSize = 10.sp, color = MutedText)
            }
        }
    }
}
