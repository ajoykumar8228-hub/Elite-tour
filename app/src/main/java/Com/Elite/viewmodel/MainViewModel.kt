package Com.Elite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Com.Elite.model.AiMessage
import Com.Elite.model.Announcement
import Com.Elite.model.AppNotification
import Com.Elite.model.GameMode
import Com.Elite.model.HomeConfig
import Com.Elite.model.PaymentSettings
import Com.Elite.model.RoomCredentials
import Com.Elite.model.Tournament
import Com.Elite.model.TournamentRegistration
import Com.Elite.model.TournamentResult
import Com.Elite.model.UserProfile
import Com.Elite.model.WalletTransaction
import Com.Elite.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _homeConfig = MutableStateFlow(HomeConfig())
    val homeConfig: StateFlow<HomeConfig> = _homeConfig.asStateFlow()

    private val _paymentSettings = MutableStateFlow(PaymentSettings())
    val paymentSettings: StateFlow<PaymentSettings> = _paymentSettings.asStateFlow()

    private val _gameModes = MutableStateFlow<List<GameMode>>(emptyList())
    val gameModes: StateFlow<List<GameMode>> = _gameModes.asStateFlow()

    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    private val _allResults = MutableStateFlow<List<TournamentResult>>(emptyList())
    val allResults: StateFlow<List<TournamentResult>> = _allResults.asStateFlow()


    private val _userRegistrations = MutableStateFlow<List<TournamentRegistration>>(emptyList())
    val userRegistrations: StateFlow<List<TournamentRegistration>> = _userRegistrations.asStateFlow()

    private val _userTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val userTransactions: StateFlow<List<WalletTransaction>> = _userTransactions.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(emptyList())
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    private val _banners = MutableStateFlow<List<Com.Elite.model.Banner>>(emptyList())
    val banners: StateFlow<List<Com.Elite.model.Banner>> = _banners.asStateFlow()

    private val _bannerSettings = MutableStateFlow(Com.Elite.model.BannerSettings())
    val bannerSettings: StateFlow<Com.Elite.model.BannerSettings> = _bannerSettings.asStateFlow()

    // ADMIN DATA
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val allTransactions: StateFlow<List<WalletTransaction>> = _allTransactions.asStateFlow()

    private val _topPlayers = MutableStateFlow<List<UserProfile>>(emptyList())
    val topPlayers: StateFlow<List<UserProfile>> = _topPlayers.asStateFlow()

    init {
        observeUserSession()
        observePublicData()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            repository.observeCurrentUser().collectLatest { profile ->
                _currentUser.value = profile
                if (profile != null) {
                    observeUserData(profile.uid)
                    if (profile.isAdmin) {
                        observeAdminData()
                    }
                }
            }
        }
    }

    private fun observePublicData() {
        viewModelScope.launch {
            repository.observeHomeConfig().collectLatest { config ->
                _homeConfig.value = config
            }
        }

        viewModelScope.launch {
            repository.observeTopPlayers().collectLatest { list ->
                _topPlayers.value = list
            }
        }

        viewModelScope.launch {
            repository.observePaymentSettings().collectLatest { settings ->
                _paymentSettings.value = settings
            }
        }

        viewModelScope.launch {
            repository.observeGameModes().collectLatest { modes ->
                _gameModes.value = modes
            }
        }

        viewModelScope.launch {
            repository.observeTournaments().collectLatest { tourneys ->
                _tournaments.value = tourneys
            }
        }

        viewModelScope.launch {
            repository.observeAllResults().collectLatest { results ->
                _allResults.value = results
            }
        }

        viewModelScope.launch {
            repository.observeAnnouncements().collectLatest { list ->
                _announcements.value = list
            }
        }

        viewModelScope.launch {
            repository.observeBanners().collectLatest { list ->
                _banners.value = list
            }
        }

        viewModelScope.launch {
            repository.observeBannerSettings().collectLatest { settings ->
                _bannerSettings.value = settings
            }
        }
    }

    fun saveBanner(banner: Com.Elite.model.Banner) {
        viewModelScope.launch {
            repository.saveBanner(banner)
        }
    }

    fun deleteBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBanner(id)
        }
    }

    fun saveBannerSettings(settings: Com.Elite.model.BannerSettings) {
        viewModelScope.launch {
            repository.saveBannerSettings(settings)
        }
    }

    private fun observeUserData(uid: String) {
        viewModelScope.launch {
            repository.observeUserRegistrations(uid).collectLatest { regs ->
                _userRegistrations.value = regs
            }
        }

        viewModelScope.launch {
            repository.observeUserTransactions(uid).collectLatest { txs ->
                _userTransactions.value = txs
            }
        }

        viewModelScope.launch {
            repository.observeNotifications(uid).collectLatest { notifs ->
                _notifications.value = notifs
            }
        }

        viewModelScope.launch {
            repository.observeAiMessages(uid).collectLatest { msgs ->
                if (msgs.isEmpty()) {
                    val greeting = AiMessage(
                        id = "greeting",
                        sender = "ai",
                        text = "আসসালামু আলাইকুম স্যার ❤️\nআমি আপনার AI সাপোর্ট স্যার।\nআপনার কি সমস্যা স্যার? বলেন স্যার, আমি সমাধান করে দিবো স্যার।\n১. টাকা অ্যাড স্যার\n২. রুম আইডি স্যার\n৩. জয়েন স্যার\n৪. রেজাল্ট স্যার",
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveAiMessage(uid, greeting)
                } else {
                    _aiMessages.value = msgs
                }
            }
        }
    }

    private fun observeAdminData() {
        viewModelScope.launch {
            repository.getAllUsers().collectLatest { users ->
                _allUsers.value = users
            }
        }

        viewModelScope.launch {
            repository.observeAllTransactions().collectLatest { txs ->
                _allTransactions.value = txs
            }
        }
    }

    fun observeTournamentRegistrations(tournamentId: String): kotlinx.coroutines.flow.Flow<List<TournamentRegistration>> {
        return repository.observeRegistrationsForTournament(tournamentId)
    }

    fun clearMessages() {
        _authError.value = null
        _userMessage.value = null
    }

    // --- AUTHENTICATION ---
    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _authError.value = "Please enter your Gmail/Email and Password."
            return
        }
        _isLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                val uid = authResult.user?.uid ?: throw Exception("User identification failed.")

                val existingProfile = repository.getUserProfile(uid)
                val isSuperAdmin = uid == UserProfile.SUPER_ADMIN_UID || cleanEmail.equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                val profileToSet = existingProfile ?: UserProfile(
                    uid = uid,
                    email = cleanEmail,
                    name = authResult.user?.displayName ?: cleanEmail.substringBefore("@"),
                    role = if (isSuperAdmin) UserProfile.ROLE_SUPER_ADMIN else UserProfile.ROLE_PLAYER,
                    status = UserProfile.STATUS_ACTIVE,
                    walletBalance = 0.0,
                    createdAt = System.currentTimeMillis()
                )
                if (existingProfile == null) {
                    repository.saveUserProfile(profileToSet)
                }
                _currentUser.value = profileToSet
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true ||
                            e.message?.contains("no user record", ignoreCase = true) == true ->
                        "Account not found for this Gmail. Please register first."
                    e.message?.contains("wrong-password", ignoreCase = true) == true ||
                            e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                        "Incorrect password. Please check your password."
                    e.message?.contains("invalid-email", ignoreCase = true) == true ->
                        "Invalid email format."
                    else -> e.localizedMessage ?: "Login failed. Please check credentials."
                }
                _authError.value = errorMsg
            }
        }
    }

    fun registerPlayer(
        name: String,
        email: String,
        pass: String,
        phone: String,
        ffUid: String,
        inGameName: String,
        photoUrl: String,
        onSuccess: () -> Unit = {}
    ) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()
        val cleanPhone = phone.trim()
        val cleanFfUid = ffUid.trim()
        val cleanIgn = inGameName.trim()

        val finalFfUid = cleanFfUid.ifBlank { "N/A" }
        val finalIgn = cleanIgn.ifBlank { cleanName }
        val finalPhone = cleanPhone.ifBlank { "N/A" }

        if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank()) {
            _authError.value = "Please enter Name, Email, and Password."
            return
        }

        if (cleanPass.length < 6) {
            _authError.value = "Password must be at least 6 characters long."
            return
        }

        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            try {
                val res = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                val uid = res.user?.uid ?: throw Exception("User creation failed.")

                val isSuperAdmin = uid == UserProfile.SUPER_ADMIN_UID || cleanEmail.equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                val initialRole = if (isSuperAdmin) UserProfile.ROLE_SUPER_ADMIN else UserProfile.ROLE_PLAYER

                val profile = UserProfile(
                    uid = uid,
                    name = cleanName,
                    email = cleanEmail,
                    phone = finalPhone,
                    photoURL = photoUrl.ifBlank { "https://picsum.photos/200" },
                    freeFireUID = finalFfUid,
                    inGameName = finalIgn,
                    role = initialRole,
                    status = UserProfile.STATUS_ACTIVE,
                    walletBalance = 0.0,
                    createdAt = System.currentTimeMillis()
                )

                repository.saveUserProfile(profile).getOrThrow()
                _currentUser.value = profile
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                val isUserCollision = e is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                        e.message?.contains("already in use", ignoreCase = true) == true ||
                        e.message?.contains("email-already-in-use", ignoreCase = true) == true

                if (isUserCollision) {
                    try {
                        val loginRes = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                        val uid = loginRes.user?.uid ?: throw Exception("Login failed")

                        val existingProfile = repository.getUserProfile(uid)
                        val isSuperAdmin = uid == UserProfile.SUPER_ADMIN_UID || cleanEmail.equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)

                        val profileToSave = existingProfile?.copy(
                            name = if (existingProfile.name.isNotBlank()) existingProfile.name else cleanName,
                            freeFireUID = if (existingProfile.freeFireUID.isNotBlank() && existingProfile.freeFireUID != "N/A") existingProfile.freeFireUID else finalFfUid,
                            inGameName = if (existingProfile.inGameName.isNotBlank()) existingProfile.inGameName else finalIgn,
                            phone = if (existingProfile.phone.isNotBlank() && existingProfile.phone != "N/A") existingProfile.phone else finalPhone,
                            role = if (isSuperAdmin) UserProfile.ROLE_SUPER_ADMIN else existingProfile.role
                        ) ?: UserProfile(
                            uid = uid,
                            name = cleanName,
                            email = cleanEmail,
                            phone = finalPhone,
                            photoURL = photoUrl.ifBlank { "https://picsum.photos/200" },
                            freeFireUID = finalFfUid,
                            inGameName = finalIgn,
                            role = if (isSuperAdmin) UserProfile.ROLE_SUPER_ADMIN else UserProfile.ROLE_PLAYER,
                            status = UserProfile.STATUS_ACTIVE,
                            walletBalance = 0.0,
                            createdAt = System.currentTimeMillis()
                        )

                        repository.saveUserProfile(profileToSave)
                        _currentUser.value = profileToSave
                        _isLoading.value = false
                        onSuccess()
                    } catch (loginErr: Exception) {
                        _isLoading.value = false
                        _authError.value = "An account with this Gmail already exists. Please log in with your password."
                    }
                } else {
                    _isLoading.value = false
                    _authError.value = e.localizedMessage ?: "Registration failed."
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }

    // --- TOURNAMENT JOIN ---
    fun joinTournament(tournament: Tournament) {
        val user = _currentUser.value
        if (user == null) {
            _userMessage.value = "Please log in first."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.joinTournament(tournament, user)
            _isLoading.value = false
            result.onSuccess {
                _userMessage.value = "Successfully joined ${tournament.title}!"
                repository.sendNotification(
                    AppNotification(
                        userId = user.uid,
                        title = "Joined Tournament",
                        message = "You have registered for ${tournament.title} (${tournament.gameMode}).",
                        type = "tournament"
                    )
                )
            }.onFailure { err ->
                _userMessage.value = err.localizedMessage ?: "Failed to join tournament."
            }
        }
    }

    // --- ADMIN TOURNAMENT MANAGEMENT ---
    fun saveTournament(tournament: Tournament, onSuccess: () -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.saveTournament(tournament)
            _isLoading.value = false
            res.onSuccess {
                _userMessage.value = "Tournament saved successfully."
                onSuccess()
            }.onFailure {
                _userMessage.value = "Failed to save tournament: ${it.message}"
            }
        }
    }

    fun updateTournamentStatus(tournamentId: String, status: String) {
        viewModelScope.launch {
            repository.updateTournamentStatus(tournamentId, status)
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            repository.deleteTournament(tournamentId)
        }
    }

    fun autoCreateTournaments(
        baseTournament: Tournament,
        totalMatches: Int,
        timeGapMinutes: Int,
        startTimeStr: String // e.g. "10:00 AM"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            val calendar = java.util.Calendar.getInstance()
            
            try {
                val baseTime = sdf.parse(startTimeStr) ?: java.util.Date()
                calendar.time = baseTime
                
                for (i in 1..totalMatches) {
                    val currentMatchTime = sdf.format(calendar.time)
                    val newTournament = baseTournament.copy(
                        id = "", // Firebase will generate ID
                        title = "#$i ${baseTournament.title}",
                        matchTime = currentMatchTime,
                        startTimeMillis = calendar.timeInMillis,
                        createdAt = System.currentTimeMillis() + i // Slight offset for sorting
                    )
                    repository.saveTournament(newTournament)
                    
                    // Increment time for next match
                    calendar.add(java.util.Calendar.MINUTE, timeGapMinutes)
                }
                _userMessage.value = "Successfully created $totalMatches matches!"
            } catch (e: Exception) {
                _userMessage.value = "Error creating matches: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- ADMIN GAME MODE SLOTS ---
    fun saveGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            repository.saveGameMode(gameMode)
        }
    }

    fun deleteGameMode(id: String) {
        viewModelScope.launch {
            repository.deleteGameMode(id)
        }
    }

    // --- CS RANKED TEAM ASSIGNMENT ---
    fun getRegistrationsForTournament(tournamentId: String) = repository.observeRegistrationsForTournament(tournamentId)

    fun assignTeam(registrationId: String, teamId: String) {
        viewModelScope.launch {
            repository.assignTeam(registrationId, teamId)
        }
    }

    // --- ROOM MANAGEMENT ---
    fun publishRoomCredentials(creds: RoomCredentials) {
        viewModelScope.launch {
            val res = repository.publishRoomCredentials(creds)
            res.onSuccess {
                _userMessage.value = "Room credentials published!"
            }.onFailure {
                _userMessage.value = "Error publishing room details: ${it.message}"
            }
        }
    }

    fun getRoomCredentials(tournamentId: String) = repository.observeRoomCredentials(tournamentId)

    // --- RESULTS & LEADERBOARD ---
    fun publishResult(result: TournamentResult) {
        viewModelScope.launch {
            val res = repository.publishResult(result)
            res.onSuccess {
                _userMessage.value = "Match results published!"
            }.onFailure {
                _userMessage.value = "Failed to publish result: ${it.message}"
            }
        }
    }

    fun getTournamentResult(tournamentId: String) = repository.observeTournamentResult(tournamentId)

    // --- HOME CONFIG & PAYMENT SETTINGS ---
    fun saveHomeConfig(config: HomeConfig) {
        viewModelScope.launch {
            val res = repository.saveHomeConfig(config)
            res.onSuccess {
                _userMessage.value = "Banner and Notice Updated Successfully!"
            }.onFailure {
                _userMessage.value = "Failed to save config: ${it.message}"
            }
        }
    }

    fun savePaymentSettings(settings: PaymentSettings) {
        viewModelScope.launch {
            val res = repository.savePaymentSettings(settings)
            res.onSuccess {
                _userMessage.value = "Saved Successfully!"
            }.onFailure {
                _userMessage.value = "Failed to save settings: ${it.message}"
            }
        }
    }

    fun depositMoney(amount: Double, method: String, senderPhone: String, trxId: String) {
        val user = _currentUser.value
        if (user == null) {
            _userMessage.value = "Please log in first."
            return
        }
        val settings = _paymentSettings.value
        if (amount < settings.minAddMoney) {
            _userMessage.value = "সর্বনিম্ন অ্যাড মানি ৳${settings.minAddMoney.toInt()} হতে হবে।"
            return
        }
        if (trxId.isBlank() || senderPhone.isBlank()) {
            _userMessage.value = "অনুগ্ৰহ করে আপনার সঠিক নম্বর ও TrxID দিন।"
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.submitDepositRequest(user.uid, user.name, amount, method, senderPhone, trxId)
            _isLoading.value = false
            res.onSuccess {
                _userMessage.value = "Request Sent - Pending Approval"
            }.onFailure {
                _userMessage.value = "Deposit request failed: ${it.message}"
            }
        }
    }

    fun withdrawMoney(amount: Double, method: String, accountNo: String) {
        val user = _currentUser.value ?: return
        val minWithdraw = _paymentSettings.value.minWithdraw
        if (amount < minWithdraw) {
            _userMessage.value = "সর্বনিম্ন উইথড্র ৳${minWithdraw.toInt()} হতে হবে।"
            return
        }
        if (accountNo.isBlank()) {
            _userMessage.value = "অনুগ্ৰহ করে আপনার অ্যাকাউন্ট নম্বর দিন।"
            return
        }
        viewModelScope.launch {
            val res = repository.submitWithdrawRequest(user.uid, user.name, amount, method, accountNo)
            res.onSuccess {
                _userMessage.value = "Withdrawal request submitted!"
            }.onFailure {
                _userMessage.value = it.localizedMessage ?: "Withdrawal request failed."
            }
        }
    }

    fun approveTransaction(tx: WalletTransaction) {
        viewModelScope.launch {
            val res = repository.approveTransaction(tx)
            res.onSuccess {
                _userMessage.value = "Transaction approved!"
            }.onFailure {
                _userMessage.value = "Failed to approve transaction: ${it.message}"
            }
        }
    }

    fun rejectTransaction(tx: WalletTransaction, note: String = "") {
        viewModelScope.launch {
            val res = repository.rejectTransaction(tx, note)
            res.onSuccess {
                _userMessage.value = "Transaction rejected."
            }.onFailure {
                _userMessage.value = "Failed to reject transaction: ${it.message}"
            }
        }
    }

    // --- ADMIN USER & ROLE MANAGEMENT ---
    fun updateUserRole(uid: String, role: String) {
        viewModelScope.launch {
            val res = repository.updateUserRole(uid, role)
            res.onSuccess {
                _userMessage.value = "Role updated successfully."
            }.onFailure {
                _userMessage.value = it.message ?: "Failed to update role."
            }
        }
    }

    fun updateUserStatus(uid: String, status: String) {
        viewModelScope.launch {
            val res = repository.updateUserStatus(uid, status)
            res.onSuccess {
                _userMessage.value = "User status updated to $status."
            }.onFailure {
                _userMessage.value = it.message ?: "Failed to update user status."
            }
        }
    }

    fun updateUserBalance(uid: String, deltaAmount: Double) {
        viewModelScope.launch {
            val res = repository.updateUserBalance(uid, deltaAmount)
            res.onSuccess {
                _userMessage.value = "User wallet balance updated by ৳${deltaAmount.toInt()}"
            }.onFailure {
                _userMessage.value = it.message ?: "Failed to update balance."
            }
        }
    }

    fun updateNotificationSettings(
        notificationsEnabled: Boolean,
        matchStartNotify: Boolean,
        resultPublishedNotify: Boolean,
        paymentApprovedNotify: Boolean
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.updateNotificationSettings(
                user.uid,
                notificationsEnabled,
                matchStartNotify,
                resultPublishedNotify,
                paymentApprovedNotify
            )
            res.onSuccess {
                _userMessage.value = "Notification settings saved!"
            }.onFailure {
                _userMessage.value = it.message ?: "Failed to save settings."
            }
        }
    }

    fun updateFcmToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.updateFcmToken(uid, token)
        }
    }

    fun applyReferralCode(code: String, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "Please log in first.")
            return
        }
        viewModelScope.launch {
            val res = repository.applyReferralCode(user.uid, code)
            res.onSuccess {
                _userMessage.value = "🎉 ৳20 Referral Bonus added to your wallet!"
                onComplete(true, "Bonus ৳20 added to wallet successfully!")
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to apply referral code."
                onComplete(false, err.message ?: "Failed to apply referral code.")
            }
        }
    }

    fun createAnnouncement(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.createAnnouncement(Announcement(title = title, content = content))
            _userMessage.value = "Announcement posted!"
        }
    }

    fun sendAiMessage(text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMsg = AiMessage(
                id = "",
                sender = "user",
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.saveAiMessage(user.uid, userMsg)

            _isAiTyping.value = true
            kotlinx.coroutines.delay(800L)

            val aiResponseText = generateAiResponse(text)
            val aiMsg = AiMessage(
                id = "",
                sender = "ai",
                text = aiResponseText,
                timestamp = System.currentTimeMillis()
            )
            repository.saveAiMessage(user.uid, aiMsg)
            _isAiTyping.value = false
        }
    }

    private fun generateAiResponse(input: String): String {
        val text = input.trim().lowercase()

        return when {
            text.contains("টাকা") || text.contains("add") || text.contains("ডিপোজিট") || text.contains("পেমেন্ট") || text.contains("money") || text.contains("taka") || text.contains("trx") || text.contains("balance") ->
                "জি স্যার, চিন্তা করবেন না স্যার। TrxID টা দেন স্যার, ১০ মিনিটের মধ্যে অ্যাড করে দিবো স্যার।"

            text.contains("রুম") || text.contains("আইডি") || text.contains("room") || text.contains("pass") || text.contains("পাসওয়ার্ড") ->
                "জি স্যার, রুম আইডি ম্যাচ শুরুর ১০ মিনিট আগে দেওয়া হবে স্যার। আপনি 'My Matches' অপশনে গিয়ে চেক করুন স্যার, পেয়ে যাবেন স্যার।"

            text.contains("জয়েন") || text.contains("join") || text.contains("খেলবো") ->
                "জি স্যার, খুব সহজেই জয়েন করতে পারবেন স্যার। প্লে (Play) ট্যাবে যান স্যার, ম্যাচ সিলেক্ট করে 'JOIN NOW' এ ক্লিক করুন স্যার। ওয়ালেটে পর্যাপ্ত ব্যালেন্স থাকতে হবে স্যার।"

            text.contains("রেজাল্ট") || text.contains("result") || text.contains("উইনার") || text.contains("winner") ->
                "জি স্যার, ম্যাচ শেষ হওয়ার ১০ মিনিট পর রেজাল্ট পেজে উইনার লিস্ট চলে আসবে স্যার। প্রাইজ মানি অটো ওয়ালেটে এড হয়ে যাবে স্যার।"

            text.contains("সালাম") || text.contains("হাই") || text.contains("hello") || text.contains("hi") || text.contains("hey") ->
                "আসসালামু আলাইকুম স্যার, আমি আপনার AI সাপোর্ট স্যার। আপনার কি সমস্যা স্যার?"

            text.contains("ধন্যবাদ") || text.contains("থ্যাঙ্কস") || text.contains("thanks") || text.contains("welcome") ->
                "অনেক ধন্যবাদ স্যার ❤️। আপনার সেবা করতে পেরে আমি অত্যন্ত আনন্দিত স্যার। কোনো সমস্যা হলে সাথে সাথে জানাবেন স্যার।"

            text.contains("খারাপ") || text.contains("ফালতু") || text.contains("সমস্যা") || text.contains("problem") || text.contains("cheat") ->
                "ক্ষমা করবেন স্যার, অত্যন্ত দুঃখ প্রকাশ করছি স্যার। আপনার সমস্যাটি বলুন স্যার, আমি এখনই অ্যাডমিনকে জানিয়ে সমাধান করে দিচ্ছি স্যার।"

            else ->
                "জি স্যার, আমি আপনার কথা বুঝতে পেরেছি স্যার। আমি আপনার AI সাপোর্ট স্যার, বলুন স্যার আর কীভাবে আপনাকে সাহায্য করতে পারি স্যার?"
        }
    }
}
