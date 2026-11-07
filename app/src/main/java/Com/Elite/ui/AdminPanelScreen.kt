package Com.Elite.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Com.Elite.model.Tournament
import Com.Elite.model.UserProfile
import Com.Elite.model.WalletTransaction
import Com.Elite.ui.theme.*
import Com.Elite.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val sections = listOf("Dashboard", "Game Modes", "Players", "Banners")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ADMIN COMMAND CENTER", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Navigation Tabs
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    val isSelected = selectedSection == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ErrorRed else CardBg)
                            .border(1.dp, if (isSelected) ErrorRed else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedSection = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PureWhite else SilverAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                when (selectedSection) {
                    0 -> AdminDashboardSection(viewModel)
                    1 -> AdminGameModesSection(viewModel)
                    2 -> AdminPlayersSection(viewModel)
                    3 -> AdminBannerSection(viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardSection(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    val pendingPayments = allTransactions.filter { it.status == WalletTransaction.STATUS_PENDING }
    val activeTournaments = tournaments.filter { it.status == Tournament.STATUS_OPEN || it.status == Tournament.STATUS_LIVE }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("DASHBOARD STATS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(title = "TOTAL PLAYERS", count = "${allUsers.size}", color = ElectricBlue, modifier = Modifier.weight(1f))
            StatCard(title = "ACTIVE MATCHES", count = "${activeTournaments.size}", color = SuccessGreen, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(title = "PENDING PAYMENTS", count = "${pendingPayments.size}", color = GoldYellow, modifier = Modifier.weight(1f))
            StatCard(title = "TOTAL MATCHES", count = "${tournaments.size}", color = SilverAccent, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("RECENT PENDING PAYMENTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Spacer(modifier = Modifier.height(8.dp))

        if (pendingPayments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardBg).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending payment requests", color = MutedText, fontSize = 13.sp)
            }
        } else {
            pendingPayments.take(5).forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val typeDisplay = if (tx.type == WalletTransaction.TYPE_DEPOSIT) "ADD MONEY" else tx.type.uppercase()
                            Text("${tx.userName} ($typeDisplay)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text("${tx.paymentMethod} • ${tx.accountOrTrxId}", fontSize = 11.sp, color = SilverAccent)
                        }
                        Text("৳${tx.amount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldYellow)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminGameModesSection(viewModel: MainViewModel) {
    val modes = listOf(
        "BR MATCH", "BR SURVIVAL", "CS - Clash Squad", "Lone Wolf",
        "CS Official Match", "BR Custom", "Full Map", "Daily Special"
    )
    
    var selectedMode by remember { mutableStateOf<String?>(null) }
    var showMatchListFor by remember { mutableStateOf<String?>(null) }

    if (showMatchListFor != null) {
        AdminInsideModeSection(modeName = showMatchListFor!!, viewModel = viewModel, onBack = { showMatchListFor = null })
    } else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("GAME MODES MANAGEMENT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
            Spacer(modifier = Modifier.height(16.dp))

            modes.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { modeName ->
                        val isSelected = selectedMode == modeName
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .clickable { selectedMode = modeName }
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) ErrorRed else CardBorder,
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(modeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = SilverAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Slots: 48 | Mode: Squad", fontSize = 10.sp, color = MutedText)
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp)).background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("MAP IMAGE", fontSize = 10.sp, color = SilverAccent)
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showMatchListFor = modeName },
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("EDIT / MANAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AdminInsideModeSection(modeName: String, viewModel: MainViewModel, onBack: () -> Unit) {
    val tournaments by viewModel.tournaments.collectAsState()
    val modeMatches = tournaments.filter { it.gameMode == modeName }
    
    var showCreateForm by remember { mutableStateOf(false) }
    var matchToEdit by remember { mutableStateOf<Tournament?>(null) }

    if (showCreateForm || matchToEdit != null) {
        AdminCreateMatchForm(
            modeName = modeName,
            viewModel = viewModel,
            matchToEdit = matchToEdit,
            onDismiss = { 
                showCreateForm = false
                matchToEdit = null
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = PureWhite) }
                Text(modeName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { showCreateForm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ CREATE NEW MATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(modeMatches) { match ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(match.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                Text("Time: ${match.matchTime} | Fee: ৳${match.entryFee.toInt()}", fontSize = 11.sp, color = SilverAccent)
                            }
                            
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SuccessGreen.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PUBLISHED", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(onClick = { matchToEdit = match }) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.deleteTournament(match.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCreateMatchForm(
    modeName: String,
    viewModel: MainViewModel,
    matchToEdit: Tournament? = null,
    onDismiss: () -> Unit
) {
    var matchTitle by remember { mutableStateOf(matchToEdit?.title ?: "$modeName #${System.currentTimeMillis().toString().takeLast(3)}") }
    var date by remember { mutableStateOf(matchToEdit?.matchDate ?: "TODAY") }
    var startTime by remember { mutableStateOf(matchToEdit?.matchTime ?: "10:00 AM") }
    var slots by remember { mutableStateOf(matchToEdit?.maxPlayers?.toString() ?: "48") }
    var mode by remember { mutableStateOf(matchToEdit?.entryType ?: "Squad") }
    var entryFee by remember { mutableStateOf(matchToEdit?.entryFee?.toInt()?.toString() ?: "100") }
    var prizePool by remember { mutableStateOf(matchToEdit?.prizePool?.toInt()?.toString() ?: "1000") }
    var perKill by remember { mutableStateOf(matchToEdit?.perKill?.toInt()?.toString() ?: "20") }
    var roomId by remember { mutableStateOf(matchToEdit?.roomId ?: "") }
    var password by remember { mutableStateOf(matchToEdit?.roomPassword ?: "") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = PureWhite) }
            Text(if (matchToEdit != null) "EDIT $modeName" else "CREATE $modeName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminTextField(value = matchTitle, onValueChange = { matchTitle = it }, label = "Match Title")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminTextField(value = date, onValueChange = { date = it }, label = "Date", modifier = Modifier.weight(1f))
                    AdminTextField(value = startTime, onValueChange = { startTime = it }, label = "Start Time", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminTextField(value = slots, onValueChange = { slots = it }, label = "Slots", modifier = Modifier.weight(1f))
                    AdminTextField(value = mode, onValueChange = { mode = it }, label = "Mode", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminTextField(value = entryFee, onValueChange = { entryFee = it }, label = "Entry Fee", modifier = Modifier.weight(1f))
                    AdminTextField(value = prizePool, onValueChange = { prizePool = it }, label = "Prize Pool", modifier = Modifier.weight(1f))
                    AdminTextField(value = perKill, onValueChange = { perKill = it }, label = "Per Kill", modifier = Modifier.weight(1f))
                }
                AdminTextField(value = roomId, onValueChange = { roomId = it }, label = "Room ID")
                AdminTextField(value = password, onValueChange = { password = it }, label = "Password")

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                        val stMillis = try {
                            val timeDate = sdf.parse(startTime)
                            val cal = java.util.Calendar.getInstance()
                            val timeCal = java.util.Calendar.getInstance().apply { time = timeDate }
                            cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                            cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                            cal.timeInMillis
                        } catch (e: Exception) {
                            System.currentTimeMillis() + 3600000
                        }

                        val t = (matchToEdit ?: Tournament()).copy(
                            title = matchTitle,
                            gameMode = modeName,
                            mapName = "Bermuda",
                            matchDate = date,
                            matchTime = startTime,
                            maxPlayers = slots.toIntOrNull() ?: 48,
                            entryType = mode,
                            entryFee = entryFee.toDoubleOrNull() ?: 100.0,
                            prizePool = prizePool.toDoubleOrNull() ?: 1000.0,
                            perKill = perKill.toDoubleOrNull() ?: 20.0,
                            roomId = roomId,
                            roomPassword = password,
                            status = if (matchToEdit == null) Tournament.STATUS_OPEN else matchToEdit.status,
                            startTimeMillis = stMillis,
                            countdownSeconds = 3600 // Legacy support
                        )
                        viewModel.saveTournament(t)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (matchToEdit != null) "UPDATE MATCH" else "Create 1 Card - 1 ta banabe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (matchToEdit == null) {
                    Button(
                        onClick = {
                            val baseT = Tournament(
                                title = matchTitle,
                                gameMode = modeName,
                                mapName = "Bermuda",
                                matchDate = date,
                                matchTime = startTime,
                                maxPlayers = slots.toIntOrNull() ?: 48,
                                entryType = mode,
                                entryFee = entryFee.toDoubleOrNull() ?: 100.0,
                                prizePool = prizePool.toDoubleOrNull() ?: 1000.0,
                                perKill = perKill.toDoubleOrNull() ?: 20.0,
                                roomId = roomId,
                                roomPassword = password,
                                status = Tournament.STATUS_OPEN,
                                countdownSeconds = 3600
                            )
                            viewModel.autoCreateTournaments(baseT, 20, 30, startTime)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Auto Create 20 Cards - 20 ta auto banabe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPlayersSection(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    var search by remember { mutableStateOf("") }
    
    val filtered = allUsers.filter {
        it.name.contains(search, ignoreCase = true) || it.freeFireUID.contains(search, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search Players") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text("UID: ${user.freeFireUID} | Bal: ৳${user.walletBalance.toInt()}", fontSize = 11.sp, color = SilverAccent)
                        }
                        IconButton(onClick = { viewModel.updateUserBalance(user.uid, 100.0) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SuccessGreen)
                        }
                        IconButton(onClick = { viewModel.updateUserBalance(user.uid, -100.0) }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricBlue,
            unfocusedBorderColor = CardBorder,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
@Composable
fun AdminBannerSection(viewModel: MainViewModel) {
    val banners by viewModel.banners.collectAsState()
    val settings by viewModel.bannerSettings.collectAsState()
    
    var editingBannerId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. ADD NEW BANNER BUTTON
        Button(
            onClick = {
                val newBanner = Com.Elite.model.Banner(name = "New Banner", createdAt = System.currentTimeMillis())
                viewModel.saveBanner(newBanner)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Text("+  ADD NEW BANNER", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BANNER LIST
        banners.forEachIndexed { index, banner ->
            AdminBannerItemCard(
                banner = banner,
                index = index + 1,
                isEditing = editingBannerId == banner.id,
                onEditClick = { editingBannerId = banner.id },
                onCancelClick = { editingBannerId = null },
                onSave = { updatedBanner ->
                    viewModel.saveBanner(updatedBanner)
                    editingBannerId = null
                },
                onDelete = { viewModel.deleteBanner(banner.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. GLOBAL SETTINGS
        AdminGlobalBannerSettingsCard(
            settings = settings,
            onSave = { viewModel.saveBannerSettings(it) }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AdminBannerItemCard(
    banner: Com.Elite.model.Banner,
    index: Int,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSave: (Com.Elite.model.Banner) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(banner) { mutableStateOf(banner.name) }
    var title by remember(banner) { mutableStateOf(banner.title) }
    var subTitle by remember(banner) { mutableStateOf(banner.subTitle) }
    var buttonText by remember(banner) { mutableStateOf(banner.buttonText) }
    var imageUrl by remember(banner) { mutableStateOf(banner.imageUrl) }
    var link by remember(banner) { mutableStateOf(banner.link) }
    var isActive by remember(banner) { mutableStateOf(banner.isActive) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Banner $index", color = MutedText, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                coil.compose.AsyncImage(
                    model = imageUrl.ifBlank { "https://picsum.photos/200/150" },
                    modifier = Modifier
                        .size(width = 80.dp, height = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Banner Name", color = MutedText, fontSize = 11.sp)
                    if (isEditing) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = PureWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    } else {
                        Text(name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (!isEditing) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SilverAccent)
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                
                AdminBannerFormField(label = "Title", value = title, onValueChange = { title = it })
                AdminBannerFormField(label = "Sub Title", value = subTitle, onValueChange = { subTitle = it })
                AdminBannerFormField(label = "Button Text", value = buttonText, onValueChange = { buttonText = it })
                AdminBannerFormField(label = "Image URL", value = imageUrl, onValueChange = { imageUrl = it })
                AdminBannerFormField(label = "Link (Match ID/URL)", value = link, onValueChange = { link = it })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active", color = PureWhite, fontSize = 14.sp)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricBlue)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSave(banner.copy(name = name, title = title, subTitle = subTitle, buttonText = buttonText, imageUrl = imageUrl, link = link, isActive = isActive)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SilverAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCEL", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBannerFormField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, color = MutedText, fontSize = 11.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = PureWhite),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = CardBorder
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun AdminGlobalBannerSettingsCard(
    settings: Com.Elite.model.BannerSettings,
    onSave: (Com.Elite.model.BannerSettings) -> Unit
) {
    var autoScroll by remember(settings) { mutableStateOf(settings.autoScroll) }
    var showDots by remember(settings) { mutableStateOf(settings.showDots) }
    var speed by remember(settings) { mutableStateOf(settings.speedSeconds.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GLOBAL SETTINGS", color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            AdminSettingSwitchRow(label = "Auto Scroll", checked = autoScroll, onCheckedChange = { 
                autoScroll = it
                onSave(settings.copy(autoScroll = it))
            })
            AdminSettingSwitchRow(label = "Show Dots", checked = showDots, onCheckedChange = { 
                showDots = it
                onSave(settings.copy(showDots = it))
            })

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Scroll Speed (sec)", color = PureWhite, fontSize = 14.sp)
                OutlinedTextField(
                    value = speed,
                    onValueChange = { 
                        speed = it
                        it.toIntOrNull()?.let { s -> onSave(settings.copy(speedSeconds = s)) }
                    },
                    modifier = Modifier.width(80.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = PureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun AdminSettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PureWhite, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = ElectricBlue)
        )
    }
}
