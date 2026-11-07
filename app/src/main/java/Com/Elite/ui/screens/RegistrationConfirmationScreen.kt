package Com.Elite.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.model.Tournament
import Com.Elite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationConfirmationScreen(
    tournament: Tournament,
    onConfirm: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Registration", color = PureWhite, fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Confirm Match Entry",
                fontSize = 18.sp,
                color = MutedText
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tournament.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
            ) {
                Text(
                    text = "Entry Fee: ৳${tournament.entryFee.toInt()}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldYellow,
                    modifier = Modifier.padding(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CONFIRM REGISTRATION", color = DarkNavyBg, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
