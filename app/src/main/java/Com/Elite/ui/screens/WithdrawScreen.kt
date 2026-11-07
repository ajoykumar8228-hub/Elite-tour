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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
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
fun WithdrawScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val user by viewModel.currentUser.collectAsState()
    val paymentSettings by viewModel.paymentSettings.collectAsState()
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf(user?.phone?.takeIf { it != "N/A" } ?: "") }

    val availableMethods = listOf("bKash", "Nagad", "Rocket", "Upay")
    var selectedMethod by remember { mutableStateOf("bKash") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // 1. Current Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.5.dp, GoldYellow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CURRENT BALANCE", fontSize = 11.sp, color = MutedText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${user?.walletBalance?.toInt() ?: 497} TK",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow
                        )
                        Text(
                            text = "Bonus Earned: ৳${user?.referralEarnings?.toInt() ?: 0}",
                            fontSize = 11.sp,
                            color = SuccessGreen
                        )
                    }
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = GoldYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 2. Select Payment Method
            Text("Select Withdraw Method:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableMethods.forEach { meth ->
                    val isSelected = selectedMethod == meth
                    Button(
                        onClick = { selectedMethod = meth },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NeonCyan else CardNavyBg
                        ),
                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardNavyBorder),
                        shape = RoundedCornerShape(10.dp),
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

            // 3. Inputs
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Withdraw Amount (TK)") },
                placeholder = { Text("Min: ৳${paymentSettings.minWithdraw.toInt()}") },
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
                value = accountNo,
                onValueChange = { accountNo = it },
                label = { Text("Account Number ($selectedMethod Personal)") },
                placeholder = { Text("017XXXXXXXX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardNavyBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )

            // 4. Policy Notice
            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, CardNavyBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ℹ️ উইথড্র নিয়মাবলী:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• সর্বনিম্ন উইথড্র ৳${paymentSettings.minWithdraw.toInt()} টাকা।", fontSize = 11.sp, color = MutedText)
                    Text("• উইথড্র রিকোয়েস্ট ১০ থেকে ৩০ মিনিটের মধ্যে প্রসেস করা হয়।", fontSize = 11.sp, color = MutedText)
                    Text("• শুধুমাত্র Personal নাম্বারে পেমেন্ট পাঠানো হয়।", fontSize = 11.sp, color = MutedText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Submit Button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt < paymentSettings.minWithdraw) {
                        Toast.makeText(context, "নূন্যতম উইথড্র ৳${paymentSettings.minWithdraw.toInt()}", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val currentBal = user?.walletBalance ?: 0.0
                    if (amt > currentBal) {
                        Toast.makeText(context, "পর্যাপ্ত ব্যালেন্স নেই!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (accountNo.isBlank() || accountNo.length < 11) {
                        Toast.makeText(context, "সঠিক ১১ ডিজিটের ফোন নাম্বার দিন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.withdrawMoney(amt, selectedMethod, accountNo.trim())
                    Toast.makeText(context, "উইথড্র রিকোয়েস্ট সফলভাবে জমা হয়েছে!", Toast.LENGTH_LONG).show()
                    onBackClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "WITHDRAW NOW",
                    color = DarkNavyBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
