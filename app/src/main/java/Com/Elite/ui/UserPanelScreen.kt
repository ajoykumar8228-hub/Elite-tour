package Com.Elite.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import Com.Elite.R
import Com.Elite.model.Announcement
import Com.Elite.model.AppNotification
import Com.Elite.model.GameMode
import Com.Elite.model.HomeConfig
import Com.Elite.model.RoomCredentials
import Com.Elite.model.Tournament
import Com.Elite.model.TournamentRegistration
import Com.Elite.model.TournamentResult
import Com.Elite.model.UserProfile

import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel

import Com.Elite.ui.screens.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ViewList

sealed class UserDestination {
    object Main : UserDestination()
    object AiSupport : UserDestination()
    object AddMoney : UserDestination()
    object Withdraw : UserDestination()
    data class JoinedPlayers(val tournament: Tournament) : UserDestination()
    data class RoomDetails(val tournament: Tournament) : UserDestination()
    data class ResultDetails(val result: TournamentResult) : UserDestination()
    object Shop : UserDestination()
    object Notifications : UserDestination()
    object ReferAndEarn : UserDestination()
    object Settings : UserDestination()
    object EditProfile : UserDestination()
    object Wallet : UserDestination()
    data class RegistrationConfirmation(val tournament: Tournament) : UserDestination()
    data class TournamentDetails(val tournament: Tournament) : UserDestination()
    object Leaderboard : UserDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPanelScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGameModeFilter by remember { mutableStateOf("ALL") }
    var currentDestination by remember { mutableStateOf<UserDestination>(UserDestination.Main) }

    val user by viewModel.currentUser.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val userRegistrations by viewModel.userRegistrations.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    when (val dest = currentDestination) {
        is UserDestination.AiSupport -> {
            AiSupportScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.AddMoney -> {
            AddMoneyScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Withdraw -> {
            WithdrawScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.JoinedPlayers -> {
            JoinedPlayersScreen(tournament = dest.tournament, viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.RoomDetails -> {
            RoomDetailsScreen(tournament = dest.tournament, viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.ResultDetails -> {
            ResultDetailsScreen(result = dest.result, viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Shop -> {
            ShopScreen(onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Notifications -> {
            NotificationsScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.ReferAndEarn -> {
            ReferAndEarnScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Settings -> {
            SettingsScreen(viewModel = viewModel, onNavigateToAdmin = onNavigateToAdmin, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.EditProfile -> {
            EditProfileScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Wallet -> {
            WalletScreen(
                viewModel = viewModel,
                onAddMoneyClick = { currentDestination = UserDestination.AddMoney },
                onWithdrawClick = { currentDestination = UserDestination.Withdraw },
                onBackClick = { currentDestination = UserDestination.Main }
            )
        }
        is UserDestination.RegistrationConfirmation -> {
            RegistrationConfirmationScreen(
                tournament = dest.tournament,
                onConfirm = {
                    viewModel.joinTournament(dest.tournament)
                    currentDestination = UserDestination.Main
                },
                onBackClick = { currentDestination = UserDestination.Main }
            )
        }
        is UserDestination.TournamentDetails -> {
            val hasJoined = userRegistrations.any { it.tournamentId == dest.tournament.id }
            TournamentDetailsScreen(
                tournament = dest.tournament,
                hasJoined = hasJoined,
                onJoinClick = { currentDestination = UserDestination.RegistrationConfirmation(dest.tournament) },
                onBackClick = { currentDestination = UserDestination.Main }
            )
        }
        is UserDestination.Leaderboard -> {
            LeaderboardScreen(viewModel = viewModel, onBackClick = { currentDestination = UserDestination.Main })
        }
        is UserDestination.Main -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.elite_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ELITE TWO",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { currentDestination = UserDestination.Leaderboard },
                                modifier = Modifier.testTag("top_bar_leaderboard_btn")
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = "Leaderboard",
                                    tint = GoldYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = { currentDestination = UserDestination.AiSupport },
                                modifier = Modifier.testTag("top_bar_support_btn")
                            ) {
                                Icon(
                                    Icons.Default.HeadsetMic,
                                    contentDescription = "AI Support",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = { currentDestination = UserDestination.Notifications },
                                modifier = Modifier.testTag("top_bar_notif_btn")
                            ) {
                                BadgedBox(badge = {
                                    Badge(containerColor = CrimsonRed) {
                                        Text("6", color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PureWhite)
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CardNavyBg)
                                    .border(1.5.dp, CrimsonRedBorder, RoundedCornerShape(20.dp))
                                    .clickable { currentDestination = UserDestination.Wallet }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("top_bar_wallet_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "৳${user?.walletBalance?.toInt() ?: 350}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBg)
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { currentDestination = UserDestination.AiSupport },
                        containerColor = SuccessGreen,
                        contentColor = DarkNavyBg,
                        shape = CircleShape,
                        modifier = Modifier.testTag("fab_support")
                    ) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = "AI Support")
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkNavyBg,
                        contentColor = MutedText
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, selectedTextColor = NeonCyan, indicatorColor = CardNavyBg)
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Play") },
                            label = { Text("Play", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, selectedTextColor = NeonCyan, indicatorColor = CardNavyBg)
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "My Matches") },
                            label = { Text("My Matches", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, selectedTextColor = NeonCyan, indicatorColor = CardNavyBg)
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Result") },
                            label = { Text("Result", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldYellow, selectedTextColor = GoldYellow, indicatorColor = CardNavyBg)
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, selectedTextColor = NeonCyan, indicatorColor = CardNavyBg)
                        )
                    }
                },
                containerColor = DarkNavyBg
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> UserHomeScreen(
                            viewModel = viewModel,
                            onSelectGameMode = { mode ->
                                selectedGameModeFilter = mode
                                selectedTab = 1
                            },
                            onOpenShop = { currentDestination = UserDestination.Shop },
                            onOpenAiSupport = { currentDestination = UserDestination.AiSupport }
                        )
                        1 -> UserTournamentsScreen(
                            viewModel = viewModel,
                            selectedGameMode = selectedGameModeFilter,
                            onFilterChange = { selectedGameModeFilter = it },
                            onViewJoined = { tourney -> currentDestination = UserDestination.JoinedPlayers(tourney) },
                            onRegister = { tourney -> currentDestination = UserDestination.TournamentDetails(tourney) }
                        )
                        2 -> UserMyMatchesScreen(
                            viewModel = viewModel,
                            onOpenRoomDetails = { tourney -> currentDestination = UserDestination.RoomDetails(tourney) }
                        )
                        3 -> ResultScreen(
                            viewModel = viewModel,
                            onOpenResultDetails = { res -> currentDestination = UserDestination.ResultDetails(res) }
                        )
                        4 -> UserProfileScreen(
                            viewModel = viewModel,
                            onNavigateToAdmin = onNavigateToAdmin,
                            onOpenAiSupport = { currentDestination = UserDestination.AiSupport },
                            onEditProfile = { currentDestination = UserDestination.EditProfile },
                            onReferAndEarn = { currentDestination = UserDestination.ReferAndEarn },
                            onSettings = { currentDestination = UserDestination.Settings },
                            onWalletHistory = { currentDestination = UserDestination.Wallet },
                            onLeaderboard = { currentDestination = UserDestination.Leaderboard }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserHomeScreen(
    viewModel: MainViewModel,
    onSelectGameMode: (String) -> Unit,
    onOpenShop: () -> Unit,
    onOpenAiSupport: () -> Unit
) {
    val homeConfig by viewModel.homeConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Slider
        val banners by viewModel.banners.collectAsState()
        val bannerSettings by viewModel.bannerSettings.collectAsState()
        
        if (banners.any { it.isActive }) {
            BannerSlider(
                banners = banners.filter { it.isActive },
                settings = bannerSettings
            )
        }

        // Top Header
        Text(
            text = "CHOOSE CATEGORY",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )

        val finalCategories = listOf(
            "BR MATCH",
            "BR SURVIVAL",
            "Clash Squad",
            "Lone Wolf"
        )

        // Render 4 Column Grid items
        finalCategories.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPair.forEach { modeName ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { onSelectGameMode(modeName) },
                        colors = CardDefaults.cardColors(containerColor = CardNavyBg),
                        border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = modeName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Text(
                                text = "Click to View List",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Re-adding essential functional buttons but keeping the "4 cards" focus
        Button(
            onClick = onOpenShop,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = DarkNavyBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ELITE SHOP", color = DarkNavyBg, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onOpenAiSupport,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = DarkNavyBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI SUPPORT 24/7", color = DarkNavyBg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UserTournamentsScreen(
    viewModel: MainViewModel,
    selectedGameMode: String,
    onFilterChange: (String) -> Unit,
    onViewJoined: (Tournament) -> Unit,
    onRegister: (Tournament) -> Unit
) {
    val gameModes by viewModel.gameModes.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val userRegistrations by viewModel.userRegistrations.collectAsState()

    val filteredTournaments = if (selectedGameMode == "ALL") {
        tournaments
    } else {
        tournaments.filter { it.gameMode.equals(selectedGameMode, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ELITE TWO MATCH LIST", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
        Spacer(modifier = Modifier.height(10.dp))

        // Mode Filter Tabs
        val modeNames = listOf("ALL") + listOf("BR MATCH", "BR SURVIVAL", "Clash Squad", "CS 2 VS 2", "LONE WOLF 2VS2", "LONE WOLF 1VS1") + gameModes.filter { it.isEnabled }.map { it.name }
        val distinctModes = modeNames.distinct()

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(distinctModes) { mode ->
                val isSelected = selectedGameMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NeonCyan else CardNavyBg)
                        .border(1.dp, if (isSelected) NeonCyan else CardNavyBorder, RoundedCornerShape(20.dp))
                        .clickable { onFilterChange(mode) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) DarkNavyBg else MutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTournaments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("কোনো টুর্নামেন্ট পাওয়া যায়নি", color = MutedText, fontSize = 15.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filteredTournaments) { tournament ->
                    val hasJoined = userRegistrations.any { it.tournamentId == tournament.id }
                    MatchCard(
                        tournament = tournament,
                        hasJoined = hasJoined,
                        onRegisterClick = { onRegister(tournament) },
                        onViewJoinedClick = { onViewJoined(tournament) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserMyMatchesScreen(
    viewModel: MainViewModel,
    onOpenRoomDetails: (Tournament) -> Unit
) {
    val userRegistrations by viewModel.userRegistrations.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()

    val myMatches = userRegistrations.mapNotNull { reg ->
        tournaments.find { it.id == reg.tournamentId }?.let { tourney -> reg to tourney }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("MY JOINED MATCHES", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
        Spacer(modifier = Modifier.height(12.dp))

        if (myMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("এখনো কোনো ম্যাচে Join করা হয়নি", color = MutedText, fontSize = 15.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(myMatches) { (reg, tournament) ->
                    MyMatchCard(
                        tournament = tournament,
                        registration = reg,
                        viewModel = viewModel,
                        onOpenRoomDetails = { onOpenRoomDetails(tournament) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyMatchCard(
    tournament: Tournament,
    registration: TournamentRegistration,
    viewModel: MainViewModel,
    onOpenRoomDetails: () -> Unit = {}
) {
    val roomCredsState = viewModel.getRoomCredentials(tournament.id).collectAsState(initial = null)
    val roomCreds = roomCredsState.value
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenRoomDetails() },
        colors = CardDefaults.cardColors(containerColor = CardNavyBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tournament.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(tournament.gameMode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkNavyBg)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Match Time: ${tournament.matchDate} at ${tournament.matchTime}", fontSize = 12.sp, color = MutedText)

            Spacer(modifier = Modifier.height(8.dp))

            if (roomCreds != null && roomCreds.isPublished) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ROOM CREDENTIALS PUBLISHED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM ID: ${roomCreds.roomId}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                Text("PASSWORD: ${roomCreds.roomPassword}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString("Room ID: ${roomCreds.roomId}, Pass: ${roomCreds.roomPassword}"))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavyBg)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Room ID & Password will be published 15 mins before match.",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenRoomDetails,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("VIEW ROOM DETAILS", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}


@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onOpenResultDetails: (TournamentResult) -> Unit
) {
    val allResults by viewModel.allResults.collectAsState()
    var selectedResultTab by remember { mutableIntStateOf(2) }

    val resultTabs = listOf("FF FULLMAP", "FF Clash Squad", "LoneWolf", "CS 1v1 2v2")

    val filteredResults = remember(allResults, selectedResultTab) {
        val targetCategory = resultTabs[selectedResultTab]
        allResults.filter { res ->
            when (selectedResultTab) {
                0 -> res.gameMode.equals("FF FULLMAP", ignoreCase = true) || res.gameMode.equals("BR Ranked", ignoreCase = true) || res.gameMode.equals("BR MATCH", ignoreCase = true)
                1 -> res.gameMode.equals("FF Clash Squad", ignoreCase = true) || res.gameMode.equals("Clash Squad", ignoreCase = true) || res.gameMode.equals("CLASH SQUAD", ignoreCase = true)
                2 -> res.gameMode.equals("LoneWolf", ignoreCase = true) || res.gameMode.contains("LONE WOLF", ignoreCase = true)
                3 -> res.gameMode.equals("CS 1v1 2v2", ignoreCase = true) || res.gameMode.contains("CS 2 VS 2", ignoreCase = true) || res.gameMode.contains("1VS1", ignoreCase = true)
                else -> true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "Result",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(resultTabs.indices.toList()) { index ->
                val isSelected = selectedResultTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GoldYellow else CardNavyBg)
                        .border(1.dp, if (isSelected) GoldYellow else CardNavyBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedResultTab = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = resultTabs[index],
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) DarkNavyBg else PureWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("কোনো রেজাল্ট পাওয়া যায়নি", color = MutedText, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredResults) { res ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenResultDetails(res) },
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.elite_logo),
                                contentDescription = "Result Banner",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.tournamentTitle.ifBlank { "Lone Wolf 2 VS 2 | [ 1,3 Vs 2,4 ]" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CardWhiteText
                                )
                                Text(
                                    text = res.matchDateText.ifBlank { "2026-11-06 at 10:00 AM" },
                                    fontSize = 11.sp,
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("WIN PRIZE ${res.prizePool.toInt()} TK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CardWhiteText)
                                    Text("ENTRY TYPE ${res.entryType}", fontSize = 10.sp, color = CardWhiteText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ENTRY FEE ${res.entryFee.toInt()} / PER KILL ${res.perKill.toInt()}", fontSize = 10.sp, color = CardWhiteText)
                                    Text("MAP ${res.mapName} | ${res.version}", fontSize = 10.sp, color = CardWhiteText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onOpenAiSupport: () -> Unit,
    onEditProfile: () -> Unit,
    onReferAndEarn: () -> Unit,
    onSettings: () -> Unit,
    onWalletHistory: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ELITE TWO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardNavyBg)
                    .border(1.dp, CrimsonRedBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("BALANCE: ৳${user?.walletBalance?.toInt() ?: 0}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(3.dp, NeonCyan, CircleShape)
            )
            Image(
                painter = painterResource(id = R.drawable.elite_logo),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(user?.name?.ifBlank { "Player" } ?: "Player", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PureWhite)
        Text("Player ID: #EL${user?.uid?.takeLast(6)?.uppercase() ?: "208734"}", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
        Text("Phone: ${user?.phone?.ifBlank { "N/A" } ?: "N/A"}", fontSize = 12.sp, color = MutedText)

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onEditProfile,
            colors = ButtonDefaults.buttonColors(containerColor = CardNavyBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Profile", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(label = "Total Matches", value = "0", icon = Icons.Default.SportsEsports, modifier = Modifier.weight(1f))
            ProfileStatCard(label = "Total Wins", value = "${user?.totalWins ?: 0}", icon = Icons.Default.EmojiEvents, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(label = "Total Points", value = "${user?.totalPoints ?: 0}", icon = Icons.Default.Star, modifier = Modifier.weight(1f))
            ProfileStatCard(label = "Wallet Balance", value = "৳${user?.walletBalance?.toInt() ?: 0}", icon = Icons.Default.AccountBalanceWallet, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavyBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column {
                ProfileMenuItem(title = "Transaction History", icon = Icons.Default.Payments, onClick = onWalletHistory)
                ProfileMenuItem(title = "🏆 Leaderboard", icon = Icons.Default.EmojiEvents, onClick = onLeaderboard)
                ProfileMenuItem(title = "🤖 ELITE TWO AI Support", icon = Icons.Default.HeadsetMic, onClick = onOpenAiSupport)
                ProfileMenuItem(title = "Refer & Earn", icon = Icons.Default.Share, onClick = onReferAndEarn)
                ProfileMenuItem(title = "Settings", icon = Icons.Default.Settings, onClick = onSettings)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (user?.isAdmin == true) {
            Button(
                onClick = onNavigateToAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_panel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("OPEN ADMIN COMMAND CENTER", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed)
        ) {
            Text("LOG OUT", color = CrimsonRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardNavyBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = MutedText)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
        }
        Text(">", fontSize = 14.sp, color = MutedText)
    }
}

@Composable
fun BannerSlider(
    banners: List<Com.Elite.model.Banner>,
    settings: Com.Elite.model.BannerSettings
) {
    var currentIndex by remember { mutableStateOf(0) }
    
    if (settings.autoScroll && banners.size > 1) {
        LaunchedEffect(currentIndex) {
            kotlinx.coroutines.delay(settings.speedSeconds * 1000L)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    val currentBanner = banners.getOrNull(currentIndex) ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { /* Handle link */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavyBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = currentBanner.imageUrl.ifBlank { "https://picsum.photos/400/200" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                // Content Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (currentBanner.title.isNotBlank()) {
                        Text(currentBanner.title, color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    if (currentBanner.subTitle.isNotBlank()) {
                        Text(currentBanner.subTitle, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (currentBanner.buttonText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(currentBanner.buttonText, color = DarkNavyBg, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        if (settings.showDots && banners.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (index == currentIndex) NeonCyan else MutedText.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
