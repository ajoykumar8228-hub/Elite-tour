package Com.Elite.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current

    val diamondPacks = listOf(
        Pair("115 Diamonds", "৳80"),
        Pair("240 Diamonds", "৳160"),
        Pair("355 Diamonds", "৳240"),
        Pair("610 Diamonds", "৳400"),
        Pair("1240 Diamonds", "৳800"),
        Pair("2530 Diamonds", "৳1600")
    )

    val memberships = listOf(
        Pair("Weekly Membership (450💎)", "৳170"),
        Pair("Monthly Membership (2600💎)", "৳750")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khelo Bangladesh Shop", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Notice Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                border = BorderStroke(1.dp, NeonOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "কম দামে ডায়মন্ড টপ আপ ও মেম্বারশিপ অফার! যেকোনো প্যাকেজ সাথে সাথে ডেলিভারি।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange
                    )
                }
            }

            // Diamonds Section
            Text("💎 TOP UP DIAMOND PACKS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            diamondPacks.forEach { (pack, price) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.dp, CardNavyBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pack, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text(price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/8801700000000?text=I%20want%20to%20buy%20$pack%20for%20$price")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("BUY NOW", color = DarkNavyBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Membership Section
            Text("👑 VIP MEMBERSHIPS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            memberships.forEach { (pack, price) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                    border = BorderStroke(1.dp, CardNavyBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pack, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text(price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/8801700000000?text=I%20want%20to%20buy%20$pack%20for%20$price")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("BUY NOW", color = DarkNavyBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
