package Com.Elite.repository

import Com.Elite.model.AiMessage
import Com.Elite.model.Announcement
import Com.Elite.model.AppNotification
import Com.Elite.model.GameMode
import Com.Elite.model.HomeConfig
import Com.Elite.model.PaymentSettings

import Com.Elite.model.PlayerRankingItem

import Com.Elite.model.RoomCredentials
import Com.Elite.model.Tournament
import Com.Elite.model.TournamentRegistration
import Com.Elite.model.TournamentResult
import Com.Elite.model.UserProfile
import Com.Elite.model.WalletTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- AUTH & USER PROFILE ---
    fun observeCurrentUser(): Flow<UserProfile?> = callbackFlow {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val uid = firebaseUser.uid
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val isSuperOrAdmin = uid == UserProfile.SUPER_ADMIN_UID || (firebaseUser.email ?: "").trim().equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                    val fallback = UserProfile(
                        uid = uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Player",
                        role = if (isSuperOrAdmin) UserProfile.ROLE_SUPER_ADMIN else UserProfile.ROLE_PLAYER
                    )
                    trySend(fallback)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    val userEmail = firebaseUser.email ?: ""
                    val isSuperOrAdmin = uid == UserProfile.SUPER_ADMIN_UID || userEmail.trim().equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                    val defaultRefCode = "ELITE-" + uid.takeLast(6).uppercase()
                    val newProfile = UserProfile(
                        uid = uid,
                        email = userEmail,
                        name = firebaseUser.displayName ?: userEmail.substringBefore("@") ?: "Player",
                        role = if (isSuperOrAdmin) UserProfile.ROLE_SUPER_ADMIN else UserProfile.ROLE_PLAYER,
                        status = UserProfile.STATUS_ACTIVE,
                        walletBalance = 0.0,
                        referralCode = defaultRefCode,
                        createdAt = System.currentTimeMillis()
                    )
                    db.collection("users").document(uid).set(newProfile)
                    trySend(newProfile)
                } else {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    if (profile != null) {
                        val isSuperOrAdmin = uid == UserProfile.SUPER_ADMIN_UID || profile.email.trim().equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                        val needsRoleUpdate = isSuperOrAdmin && profile.role == UserProfile.ROLE_PLAYER
                        val needsRefCodeUpdate = profile.referralCode.isBlank()
                        
                        if (needsRoleUpdate || needsRefCodeUpdate) {
                            val genRefCode = if (profile.referralCode.isBlank()) "ELITE-" + uid.takeLast(6).uppercase() else profile.referralCode
                            val genRole = if (needsRoleUpdate) UserProfile.ROLE_SUPER_ADMIN else profile.role
                            val updatedProfile = profile.copy(role = genRole, referralCode = genRefCode)
                            db.collection("users").document(uid).update(
                                "role", genRole,
                                "referralCode", genRefCode
                            )
                            trySend(updatedProfile)
                            return@addSnapshotListener
                        }
                    }
                    trySend(profile)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)
            if (profile != null) {
                val isSuperOrAdmin = uid == UserProfile.SUPER_ADMIN_UID || profile.email.trim().equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
                if (isSuperOrAdmin && profile.role == UserProfile.ROLE_PLAYER) {
                    val updated = profile.copy(role = UserProfile.ROLE_SUPER_ADMIN)
                    db.collection("users").document(uid).update("role", UserProfile.ROLE_SUPER_ADMIN)
                    return updated
                }
            }
            profile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val isSuperAdmin = profile.uid == UserProfile.SUPER_ADMIN_UID || profile.email.trim().equals(UserProfile.ADMIN_GMAIL, ignoreCase = true)
            val finalRole = if (isSuperAdmin) UserProfile.ROLE_SUPER_ADMIN else profile.role
            val finalProfile = profile.copy(
                role = finalRole,
                updatedAt = System.currentTimeMillis()
            )
            db.collection("users").document(profile.uid).set(finalProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(UserProfile::class.java) } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    fun observeTopPlayers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = db.collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(UserProfile::class.java) } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserRole(targetUid: String, newRole: String): Result<Unit> {
        return try {
            if (targetUid == UserProfile.SUPER_ADMIN_UID) {
                return Result.failure(Exception("Cannot modify Super Admin role"))
            }
            db.collection("users").document(targetUid).update("role", newRole, "updatedAt", System.currentTimeMillis()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(targetUid: String, newStatus: String): Result<Unit> {
        return try {
            if (targetUid == UserProfile.SUPER_ADMIN_UID) {
                return Result.failure(Exception("Cannot change Super Admin status"))
            }
            db.collection("users").document(targetUid).update("status", newStatus, "updatedAt", System.currentTimeMillis()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserBalance(targetUid: String, deltaAmount: Double): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(targetUid)
            db.runTransaction { transaction ->
                val user = transaction.get(userRef).toObject(UserProfile::class.java)
                    ?: throw IllegalStateException("User not found")
                val newBal = (user.walletBalance + deltaAmount).coerceAtLeast(0.0)
                transaction.update(userRef, "walletBalance", newBal)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        return try {
            db.collection("users").document(uid).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GAME MODES ---
    fun observeGameModes(): Flow<List<GameMode>> = callbackFlow {
        val listener = db.collection("gameModes")
            .orderBy("slotIndex", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    // Provide default game modes if Firestore collection is empty
                    trySend(getDefaultGameModes())
                    return@addSnapshotListener
                }
                val modes = snapshot.documents.mapNotNull { it.toObject(GameMode::class.java) }
                if (modes.isEmpty()) {
                    trySend(getDefaultGameModes())
                } else {
                    trySend(modes)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun getDefaultGameModes(): List<GameMode> {
        return listOf(
            GameMode("gm_1", "BR MATCH", 0, maxPlayers = 48, defaultEntryFee = 100.0, defaultPrizePool = 1000.0, isDefault = true),
            GameMode("gm_2", "BR SURVIVAL", 1, maxPlayers = 48, defaultEntryFee = 50.0, defaultPrizePool = 500.0, isDefault = true),
            GameMode("gm_3", "Clash Squad", 2, maxPlayers = 12, defaultEntryFee = 40.0, defaultPrizePool = 400.0, isDefault = true),
            GameMode("gm_4", "CS 2 VS 2", 3, maxPlayers = 4, defaultEntryFee = 30.0, defaultPrizePool = 300.0, isDefault = true),
            GameMode("gm_5", "LONE WOLF 2VS2", 4, maxPlayers = 4, defaultEntryFee = 25.0, defaultPrizePool = 200.0, isDefault = true),
            GameMode("gm_6", "LONE WOLF 1VS1", 5, maxPlayers = 2, defaultEntryFee = 20.0, defaultPrizePool = 150.0, isDefault = true)
        )
    }

    suspend fun saveGameMode(gameMode: GameMode): Result<Unit> {
        return try {
            val id = if (gameMode.id.isEmpty()) db.collection("gameModes").document().id else gameMode.id
            val finalMode = gameMode.copy(id = id)
            db.collection("gameModes").document(id).set(finalMode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGameMode(id: String): Result<Unit> {
        return try {
            db.collection("gameModes").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- TOURNAMENTS ---
    fun observeTournaments(): Flow<List<Tournament>> = callbackFlow {
        val listener = db.collection("tournaments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(getDefaultTournaments())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { it.toObject(Tournament::class.java) }
                if (list.isEmpty()) {
                    trySend(getDefaultTournaments())
                } else {
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun getDefaultTournaments(): List<Tournament> {
        return listOf(
            Tournament(
                id = "t_1",
                title = "FULL MAP - SOLO",
                gameMode = "BR Ranked",
                entryFee = 50.0,
                prizePool = 1000.0,
                perKill = 10.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "১০ মিনিট পর চালু হবে",
                countdownSeconds = 623,
                maxPlayers = 50,
                registeredCount = 1,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_2",
                title = "FULL MAP - DUO",
                gameMode = "BR Ranked",
                entryFee = 100.0,
                prizePool = 1500.0,
                perKill = 15.0,
                entryType = "Duo",
                mapName = "Bermuda",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "আধা ঘন্টা পর চালু হবে",
                countdownSeconds = 1800,
                maxPlayers = 50,
                registeredCount = 12,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_3",
                title = "FULL MAP - SQUAD",
                gameMode = "BR Ranked",
                entryFee = 150.0,
                prizePool = 2000.0,
                perKill = 20.0,
                entryType = "Squad",
                mapName = "Purgatory",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "১ ঘন্টা পর চালু হবে",
                countdownSeconds = 3600,
                maxPlayers = 50,
                registeredCount = 24,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_4",
                title = "FULL MAP - SOLO SPECIAL",
                gameMode = "BR Ranked",
                entryFee = 20.0,
                prizePool = 500.0,
                perKill = 5.0,
                entryType = "Solo",
                mapName = "Kalahari",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "১৫ মিনিট পর চালু হবে",
                countdownSeconds = 900,
                maxPlayers = 50,
                registeredCount = 35,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_5",
                title = "BR RANKED SOLO LIMITED",
                gameMode = "BR Ranked",
                entryFee = 10.0,
                prizePool = 405.0,
                perKill = 5.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "৫ মিনিট পর চালু হবে",
                countdownSeconds = 300,
                maxPlayers = 50,
                registeredCount = 40,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_6",
                title = "CLASH SQUAD 6v6 BATTLE",
                gameMode = "CLASH SQUAD",
                entryFee = 50.0,
                prizePool = 480.0,
                perKill = 0.0,
                entryType = "Squad",
                mapName = "Bermuda",
                version = "5",
                deviceType = "MOBILE",
                bengaliTimeText = "২০ মিনিট পর চালু হবে",
                countdownSeconds = 1200,
                maxPlayers = 12,
                registeredCount = 0,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_7",
                title = "LONE WOLF 1VS1 SHOWDOWN",
                gameMode = "LONE WOLF 1VS1",
                entryFee = 50.0,
                prizePool = 75.0,
                perKill = 0.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "MOBILE",
                bengaliTimeText = "১০ মিনিট পর চালু হবে",
                countdownSeconds = 600,
                maxPlayers = 2,
                registeredCount = 1,
                status = Tournament.STATUS_OPEN
            ),
            Tournament(
                id = "t_8",
                title = "LONE WOLF 2VS2 DUEL",
                gameMode = "LONE WOLF 2VS2",
                entryFee = 50.0,
                prizePool = 150.0,
                perKill = 0.0,
                entryType = "Duo",
                mapName = "Bermuda",
                version = "MOBILE",
                bengaliTimeText = "FULL MATCH",
                countdownSeconds = 0,
                maxPlayers = 4,
                registeredCount = 4,
                status = Tournament.STATUS_FULL
            )
        )
    }

    fun observeTournamentResult(tournamentId: String): Flow<TournamentResult?> = callbackFlow {
        val listener = db.collection("results").document(tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val res = snapshot?.toObject(TournamentResult::class.java)
                trySend(res)
            }
        awaitClose { listener.remove() }
    }

    fun observeAllResults(): Flow<List<TournamentResult>> = callbackFlow {
        val listener = db.collection("results")
            .whereEqualTo("isPublished", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(getDefaultResults())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { it.toObject(TournamentResult::class.java) }
                if (list.isEmpty()) {
                    trySend(getDefaultResults())
                } else {
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun getDefaultResults(): List<TournamentResult> {
        val sampleRankings = listOf(
            PlayerRankingItem(rank = 1, playerName = "NEXUSSTRIKER", inGameName = "NEXUS_FF", freeFireUID = "82093841", kills = 8, placementPoints = 20, totalPoints = 28, prizeWon = 150.0),
            PlayerRankingItem(rank = 2, playerName = "GAMER_BOY99", inGameName = "BOY_OP", freeFireUID = "94102934", kills = 4, placementPoints = 15, totalPoints = 19, prizeWon = 50.0),
            PlayerRankingItem(rank = 3, playerName = "DARK_KING", inGameName = "KING_PRO", freeFireUID = "51920394", kills = 3, placementPoints = 10, totalPoints = 13, prizeWon = 20.0)
        )

        return listOf(
            TournamentResult(
                tournamentId = "res_1",
                tournamentTitle = "Lone Wolf 2 VS 2 | [ 1,3 Vs 2,4 ]",
                gameMode = "LoneWolf",
                matchDateText = "2026-11-06 at 10:00 AM",
                prizePool = 150.0,
                entryFee = 50.0,
                perKill = 0.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "MOBILE",
                winnerInfo = "NEXUSSTRIKER (8 Kills) - 1st Position",
                rankings = sampleRankings,
                isPublished = true
            ),
            TournamentResult(
                tournamentId = "res_2",
                tournamentTitle = "Lone Wolf 2 VS 2 | [ 1,2 Vs 3,4 ]",
                gameMode = "LoneWolf",
                matchDateText = "2026-11-06 at 09:45 AM",
                prizePool = 150.0,
                entryFee = 50.0,
                perKill = 0.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "MOBILE",
                winnerInfo = "GAMER_BOY99 (5 Kills)",
                rankings = sampleRankings,
                isPublished = true
            ),
            TournamentResult(
                tournamentId = "res_3",
                tournamentTitle = "Lone Wolf 2 VS 2 | [ 2,3 Vs 1,4 ]",
                gameMode = "LoneWolf",
                matchDateText = "2026-11-06 at 09:30 AM",
                prizePool = 150.0,
                entryFee = 50.0,
                perKill = 0.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "MOBILE",
                winnerInfo = "DARK_KING (6 Kills)",
                rankings = sampleRankings,
                isPublished = true
            ),
            TournamentResult(
                tournamentId = "res_4",
                tournamentTitle = "Lone Wolf 2 VS 2 | [ 1,4 Vs 2,3 ]",
                gameMode = "LoneWolf",
                matchDateText = "2026-11-06 at 09:15 AM",
                prizePool = 150.0,
                entryFee = 50.0,
                perKill = 0.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "MOBILE",
                winnerInfo = "NEXUSSTRIKER (7 Kills)",
                rankings = sampleRankings,
                isPublished = true
            ),
            TournamentResult(
                tournamentId = "res_5",
                tournamentTitle = "FULL MAP SOLO TIME SPECIAL",
                gameMode = "FF FULLMAP",
                matchDateText = "2026-11-06 at 08:30 AM",
                prizePool = 405.0,
                entryFee = 10.0,
                perKill = 5.0,
                entryType = "Solo",
                mapName = "Bermuda",
                version = "5",
                winnerInfo = "NEXUSSTRIKER - Winner 405 TK",
                rankings = sampleRankings,
                isPublished = true
            )
        )
    }

    suspend fun saveTournament(tournament: Tournament): Result<Unit> {
        return try {
            val id = if (tournament.id.isEmpty()) db.collection("tournaments").document().id else tournament.id
            val finalTournament = tournament.copy(id = id)
            db.collection("tournaments").document(id).set(finalTournament).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTournamentStatus(tournamentId: String, status: String): Result<Unit> {
        return try {
            db.collection("tournaments").document(tournamentId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTournament(tournamentId: String): Result<Unit> {
        return try {
            db.collection("tournaments").document(tournamentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- TOURNAMENT JOIN & REGISTRATION (ATOMIC TRANSACTION) ---
    suspend fun joinTournament(tournament: Tournament, player: UserProfile): Result<Unit> {
        return try {
            val regId = "${tournament.id}_${player.uid}"
            val regRef = db.collection("registrations").document(regId)
            val tourneyRef = db.collection("tournaments").document(tournament.id)
            val userRef = db.collection("users").document(player.uid)

            db.runTransaction { transaction ->
                val existingReg = transaction.get(regRef)
                if (existingReg.exists()) {
                    throw IllegalStateException("You have already joined this match!")
                }

                val freshTourney = transaction.get(tourneyRef).toObject(Tournament::class.java)
                    ?: throw IllegalStateException("Tournament does not exist")

                if (freshTourney.status != Tournament.STATUS_OPEN) {
                    throw IllegalStateException("Registration is not open for this tournament")
                }

                if (freshTourney.registeredCount >= freshTourney.maxPlayers) {
                    throw IllegalStateException("Tournament is full!")
                }

                val freshUser = transaction.get(userRef).toObject(UserProfile::class.java)
                    ?: throw IllegalStateException("User profile not found")

                if (freshUser.status == UserProfile.STATUS_BLOCKED) {
                    throw IllegalStateException("Your account is blocked.")
                }

                if (freshTourney.entryFee > 0) {
                    if (freshUser.walletBalance < freshTourney.entryFee) {
                        throw IllegalStateException("Insufficient wallet balance. Fee: ৳${freshTourney.entryFee}")
                    }
                    val newBalance = freshUser.walletBalance - freshTourney.entryFee
                    transaction.update(userRef, "walletBalance", newBalance)

                    // Record entry fee transaction
                    val txId = db.collection("transactions").document().id
                    val tx = WalletTransaction(
                        id = txId,
                        userId = player.uid,
                        userName = player.name,
                        type = WalletTransaction.TYPE_ENTRY_FEE,
                        amount = freshTourney.entryFee,
                        paymentMethod = "Wallet",
                        status = WalletTransaction.STATUS_COMPLETED,
                        note = "Entry fee for ${freshTourney.title}",
                        timestamp = System.currentTimeMillis()
                    )
                    transaction.set(db.collection("transactions").document(txId), tx)
                }

                val registration = TournamentRegistration(
                    id = regId,
                    tournamentId = freshTourney.id,
                    matchId = freshTourney.id,
                    playerId = player.uid,
                    playerName = player.name,
                    inGameName = player.inGameName,
                    freeFireUID = player.freeFireUID,
                    gameMode = freshTourney.gameMode,
                    registrationStatus = "joined",
                    teamId = TournamentRegistration.TEAM_UNASSIGNED,
                    joinedAt = System.currentTimeMillis()
                )

                transaction.set(regRef, registration)

                val newCount = freshTourney.registeredCount + 1
                val newStatus = if (newCount >= freshTourney.maxPlayers) Tournament.STATUS_FULL else freshTourney.status
                transaction.update(tourneyRef, "registeredCount", newCount, "status", newStatus)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRegistrationsForTournament(tournamentId: String): Flow<List<TournamentRegistration>> = callbackFlow {
        val listener = db.collection("registrations")
            .whereEqualTo("tournamentId", tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val regs = snapshot?.documents?.mapNotNull { it.toObject(TournamentRegistration::class.java) } ?: emptyList()
                trySend(regs)
            }
        awaitClose { listener.remove() }
    }

    fun observeUserRegistrations(playerId: String): Flow<List<TournamentRegistration>> = callbackFlow {
        val listener = db.collection("registrations")
            .whereEqualTo("playerId", playerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(TournamentRegistration::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun assignTeam(registrationId: String, teamId: String): Result<Unit> {
        return try {
            db.collection("registrations").document(registrationId).update("teamId", teamId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ROOM MANAGEMENT ---
    suspend fun publishRoomCredentials(creds: RoomCredentials): Result<Unit> {
        return try {
            val roomRef = db.collection("rooms").document(creds.tournamentId)
            val updated = creds.copy(updatedAt = System.currentTimeMillis())
            roomRef.set(updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoomCredentials(tournamentId: String): Flow<RoomCredentials?> = callbackFlow {
        val listener = db.collection("rooms").document(tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val creds = snapshot?.toObject(RoomCredentials::class.java)
                trySend(creds)
            }
        awaitClose { listener.remove() }
    }

    // --- RESULTS & LEADERBOARD ---
    suspend fun publishResult(result: TournamentResult): Result<Unit> {
        return try {
            val resRef = db.collection("results").document(result.tournamentId)
            val finalRes = result.copy(publishedAt = System.currentTimeMillis())
            resRef.set(finalRes).await()

            // Mark tournament as completed
            db.collection("tournaments").document(result.tournamentId).update("status", Tournament.STATUS_COMPLETED).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- WALLET & PAYMENTS ---
    suspend fun submitDepositRequest(userId: String, userName: String, amount: Double, method: String, senderPhone: String, trxId: String): Result<Unit> {
        return try {
            val id = db.collection("transactions").document().id
            val tx = WalletTransaction(
                id = id,
                userId = userId,
                userName = userName,
                type = WalletTransaction.TYPE_DEPOSIT,
                amount = amount,
                paymentMethod = method,
                accountOrTrxId = trxId,
                senderPhone = senderPhone,
                status = WalletTransaction.STATUS_PENDING,
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(id).set(tx).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitWithdrawRequest(userId: String, userName: String, amount: Double, method: String, accountNo: String): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val user = transaction.get(userRef).toObject(UserProfile::class.java)
                    ?: throw IllegalStateException("User not found")

                if (user.walletBalance < amount) {
                    throw IllegalStateException("Insufficient wallet balance for withdrawal.")
                }

                // Deduct balance upfront during pending withdrawal
                val newBal = user.walletBalance - amount
                transaction.update(userRef, "walletBalance", newBal)

                val id = db.collection("transactions").document().id
                val tx = WalletTransaction(
                    id = id,
                    userId = userId,
                    userName = userName,
                    type = WalletTransaction.TYPE_WITHDRAWAL,
                    amount = amount,
                    paymentMethod = method,
                    accountOrTrxId = accountNo,
                    senderPhone = accountNo,
                    status = WalletTransaction.STATUS_PENDING,
                    timestamp = System.currentTimeMillis()
                )
                transaction.set(db.collection("transactions").document(id), tx)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserTransactions(userId: String): Flow<List<WalletTransaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(WalletTransaction::class.java) } ?: emptyList()
                val sorted = list.sortedByDescending { it.timestamp }
                trySend(sorted)
            }
        awaitClose { listener.remove() }
    }

    fun observeAllTransactions(): Flow<List<WalletTransaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(WalletTransaction::class.java) } ?: emptyList()
                val sorted = list.sortedByDescending { it.timestamp }
                trySend(sorted)
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveTransaction(tx: WalletTransaction): Result<Unit> {
        return try {
            val txRef = db.collection("transactions").document(tx.id)
            val userRef = db.collection("users").document(tx.userId)

            db.runTransaction { transaction ->
                val currentTx = transaction.get(txRef).toObject(WalletTransaction::class.java)
                    ?: throw IllegalStateException("Transaction not found")

                if (currentTx.status != WalletTransaction.STATUS_PENDING) {
                    throw IllegalStateException("Transaction is already processed.")
                }

                if (currentTx.type == WalletTransaction.TYPE_DEPOSIT) {
                    val user = transaction.get(userRef).toObject(UserProfile::class.java)
                        ?: throw IllegalStateException("User not found")
                    val updatedBal = user.walletBalance + currentTx.amount
                    transaction.update(userRef, "walletBalance", updatedBal)
                }

                transaction.update(txRef, "status", WalletTransaction.STATUS_APPROVED)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectTransaction(tx: WalletTransaction, note: String = ""): Result<Unit> {
        return try {
            val txRef = db.collection("transactions").document(tx.id)
            val userRef = db.collection("users").document(tx.userId)

            db.runTransaction { transaction ->
                val currentTx = transaction.get(txRef).toObject(WalletTransaction::class.java)
                    ?: throw IllegalStateException("Transaction not found")

                if (currentTx.status != WalletTransaction.STATUS_PENDING) {
                    throw IllegalStateException("Transaction is already processed.")
                }

                // If withdrawal is rejected, refund the money back to the player's wallet
                if (currentTx.type == WalletTransaction.TYPE_WITHDRAWAL) {
                    val user = transaction.get(userRef).toObject(UserProfile::class.java)
                    if (user != null) {
                        val refundedBal = user.walletBalance + currentTx.amount
                        transaction.update(userRef, "walletBalance", refundedBal)
                    }
                }

                transaction.update(txRef, "status", WalletTransaction.STATUS_REJECTED, "note", note)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PAYMENT SETTINGS & HOME CONFIG ---
    fun observeHomeConfig(): Flow<HomeConfig> = callbackFlow {
        val listener = db.collection("app_settings").document("home_config")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(HomeConfig())
                    return@addSnapshotListener
                }
                val config = snapshot.toObject(HomeConfig::class.java) ?: HomeConfig()
                trySend(config)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveHomeConfig(config: HomeConfig): Result<Unit> {
        return try {
            val updated = config.copy(updatedAt = System.currentTimeMillis())
            db.collection("app_settings").document("home_config").set(updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePaymentSettings(): Flow<PaymentSettings> = callbackFlow {

        val listener = db.collection("app_settings").document("payment")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(PaymentSettings())
                    return@addSnapshotListener
                }
                val settings = snapshot.toObject(PaymentSettings::class.java) ?: PaymentSettings()
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun savePaymentSettings(settings: PaymentSettings): Result<Unit> {
        return try {
            val updated = settings.copy(updatedAt = System.currentTimeMillis())
            db.collection("app_settings").document("payment").set(updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NOTIFICATIONS & ANNOUNCEMENTS ---

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = db.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(AppNotification::class.java) }
                    ?.filter { it.userId == "all" || it.userId == userId }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendNotification(notification: AppNotification): Result<Unit> {
        return try {
            val id = db.collection("notifications").document().id
            val finalNotif = notification.copy(id = id)
            db.collection("notifications").document(id).set(finalNotif).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = db.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Announcement::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val id = db.collection("announcements").document().id
            val finalAnn = announcement.copy(id = id)
            db.collection("announcements").document(id).set(finalAnn).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSettings(
        uid: String,
        notificationsEnabled: Boolean,
        matchStartNotify: Boolean,
        resultPublishedNotify: Boolean,
        paymentApprovedNotify: Boolean
    ): Result<Unit> {
        return try {
            db.collection("users").document(uid).update(
                "notificationsEnabled", notificationsEnabled,
                "matchStartNotify", matchStartNotify,
                "resultPublishedNotify", resultPublishedNotify,
                "paymentApprovedNotify", paymentApprovedNotify,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyReferralCode(currentUserId: String, referralCodeInput: String): Result<Unit> {
        return try {
            val cleanCode = referralCodeInput.trim().uppercase()
            if (cleanCode.isEmpty()) {
                return Result.failure(Exception("Please enter a valid referral code"))
            }

            val userDocRef = db.collection("users").document(currentUserId)
            val userSnapshot = userDocRef.get().await()
            val currentUser = userSnapshot.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            if (currentUser.referredBy.isNotEmpty()) {
                return Result.failure(Exception("You have already used a referral code!"))
            }

            if (currentUser.referralCode.equals(cleanCode, ignoreCase = true)) {
                return Result.failure(Exception("You cannot use your own referral code!"))
            }

            val referrerQuery = db.collection("users")
                .whereEqualTo("referralCode", cleanCode)
                .get()
                .await()

            if (referrerQuery.isEmpty) {
                return Result.failure(Exception("Invalid referral code. No player found."))
            }

            val referrerDoc = referrerQuery.documents.first()
            val referrer = referrerDoc.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("Referrer profile invalid."))

            if (referrer.uid == currentUserId) {
                return Result.failure(Exception("You cannot use your own referral code!"))
            }

            val bonusAmount = 20.0

            // 1. Credit referee (current user)
            val refereeNewBalance = currentUser.walletBalance + bonusAmount
            userDocRef.update(
                "walletBalance", refereeNewBalance,
                "referredBy", cleanCode,
                "updatedAt", System.currentTimeMillis()
            ).await()

            // 2. Credit referrer
            val referrerDocRef = db.collection("users").document(referrer.uid)
            referrerDocRef.update(
                "walletBalance", referrer.walletBalance + bonusAmount,
                "referralCount", referrer.referralCount + 1,
                "referralEarnings", referrer.referralEarnings + bonusAmount,
                "updatedAt", System.currentTimeMillis()
            ).await()

            // 3. Add WalletTransaction for referee
            val refTxId = db.collection("transactions").document().id
            val refereeTx = WalletTransaction(
                id = refTxId,
                userId = currentUserId,
                userName = currentUser.name,
                type = WalletTransaction.TYPE_PRIZE_CREDIT,
                amount = bonusAmount,
                paymentMethod = "Referral Bonus",
                status = WalletTransaction.STATUS_COMPLETED,
                note = "Referral Signup Bonus (Code: $cleanCode)",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(refTxId).set(refereeTx).await()

            // 4. Add WalletTransaction for referrer
            val referrerTxId = db.collection("transactions").document().id
            val referrerTx = WalletTransaction(
                id = referrerTxId,
                userId = referrer.uid,
                userName = referrer.name,
                type = WalletTransaction.TYPE_PRIZE_CREDIT,
                amount = bonusAmount,
                paymentMethod = "Referral Bonus",
                status = WalletTransaction.STATUS_COMPLETED,
                note = "Referral Reward for inviting ${currentUser.name}",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(referrerTxId).set(referrerTx).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAiMessages(userId: String): Flow<List<AiMessage>> = callbackFlow {
        val listener = db.collection("users").document(userId).collection("ai_chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(AiMessage::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAiMessage(userId: String, message: AiMessage): Result<Unit> {
        return try {
            val id = message.id.ifBlank { db.collection("users").document(userId).collection("ai_chats").document().id }
            val finalMsg = message.copy(id = id)
            db.collection("users").document(userId).collection("ai_chats").document(id).set(finalMsg).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    // --- BANNERS ---
    fun observeBanners(): Flow<List<Com.Elite.model.Banner>> = callbackFlow {
        val listener = db.collection("banners")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Com.Elite.model.Banner::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBanner(banner: Com.Elite.model.Banner): Result<Unit> {
        return try {
            val id = if (banner.id.isEmpty()) db.collection("banners").document().id else banner.id
            val finalBanner = banner.copy(id = id)
            db.collection("banners").document(id).set(finalBanner).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBanner(id: String): Result<Unit> {
        return try {
            db.collection("banners").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeBannerSettings(): Flow<Com.Elite.model.BannerSettings> = callbackFlow {
        val listener = db.collection("app_settings").document("banner_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(Com.Elite.model.BannerSettings())
                    return@addSnapshotListener
                }
                val settings = snapshot.toObject(Com.Elite.model.BannerSettings::class.java) ?: Com.Elite.model.BannerSettings()
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBannerSettings(settings: Com.Elite.model.BannerSettings): Result<Unit> {
        return try {
            val updated = settings.copy(updatedAt = System.currentTimeMillis())
            db.collection("app_settings").document("banner_settings").set(updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
