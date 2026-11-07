package Com.Elite.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
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
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val user by viewModel.currentUser.collectAsState()
    val paymentSettings by viewModel.paymentSettings.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    var amount by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf(user?.phone?.takeIf { it != "N/A" } ?: "") }
    var trxId by remember { mutableStateOf("") }

    val availableMethods = remember(paymentSettings) {
        mutableListOf<String>().apply {
            if (paymentSettings.bkashEnabled) add("bKash")
            if (paymentSettings.nagadEnabled) add("Nagad")
            if (paymentSettings.rocketEnabled) add("Rocket")
            if (paymentSettings.upayEnabled) add("Upay")
            if (isEmpty()) add("bKash")
        }
    }
    var selectedMethod by remember(availableMethods) { mutableStateOf(availableMethods.firstOrNull() ?: "bKash") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Money", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // 1. Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available Balance", fontSize = 14.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Text(
                        text = "৳${user?.walletBalance?.toInt() ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldYellow
                    )
                }
            }

            // 2. Official Payment Numbers
            Text(
                text = "Send Money to these official numbers",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            val paymentMethodsList = listOf(
                Triple("bKash Personal", paymentSettings.bkashNumber, paymentSettings.bkashEnabled),
                Triple("Nagad Personal", paymentSettings.nagadNumber, paymentSettings.nagadEnabled),
                Triple("Rocket Personal", paymentSettings.rocketNumber, paymentSettings.rocketEnabled)
            )

            paymentMethodsList.filter { it.third }.forEach { (label, number, _) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.dp, CardNavyBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(number))
                            Toast.makeText(context, "Number Copied", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(label, fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Bold)
                            Text(number, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(number))
                                Toast.makeText(context, "Number Copied", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("COPY", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Warning Card
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldYellow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Send money first, then provide TrxID",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyBg,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 4. Method
            Text("Method", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("bKash", "Nagad", "Rocket").forEach { meth ->
                    val isSelected = selectedMethod == meth
                    Button(
                        onClick = { selectedMethod = meth },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NeonCyan else CardNavyBg
                        ),
                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardNavyBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = meth,
                            color = if (isSelected) DarkNavyBg else PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 5. Form
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (TK)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardNavyBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )

            OutlinedTextField(
                value = trxId,
                onValueChange = { trxId = it },
                label = { Text("Transaction ID (TrxID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardNavyBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Submit Button
            Button(
                onClick = {
                    if (amount.isBlank() || trxId.isBlank()) {
                        Toast.makeText(context, "Amount আর TrxID দাও", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val amt = amount.toDoubleOrNull()
                    if (amt == null) {
                        Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.depositMoney(amt, selectedMethod, "", trxId.trim())
                    Toast.makeText(context, "Request Sent! Admin will approve", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "SUBMIT",
                    color = DarkNavyBg,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
