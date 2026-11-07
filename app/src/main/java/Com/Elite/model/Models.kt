package Com.Elite.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoURL: String = "",
    val freeFireUID: String = "",
    val inGameName: String = "",
    val role: String = ROLE_PLAYER, // "player", "admin", "super_admin"
    val status: String = STATUS_ACTIVE, // "active", "deactivated", "blocked"
    val walletBalance: Double = 0.0,
    val fcmToken: String = "",
    val notificationsEnabled: Boolean = true,
    val matchStartNotify: Boolean = true,
    val resultPublishedNotify: Boolean = true,
    val paymentApprovedNotify: Boolean = true,
    val referralCode: String = "",
    val referredBy: String = "",
    val referralCount: Int = 0,
    val referralEarnings: Double = 0.0,
    val totalWins: Int = 0,
    val totalPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SUPER_ADMIN_UID = "mNHNgULoyTbGuGKbSS9W5gL7l6c2"
        const val ADMIN_GMAIL = "ajoykumar8228@gmail.com"
        const val ROLE_PLAYER = "player"
        const val ROLE_ADMIN = "admin"
        const val ROLE_SUPER_ADMIN = "super_admin"

        const val STATUS_ACTIVE = "active"
        const val STATUS_DEACTIVATED = "deactivated"
        const val STATUS_BLOCKED = "blocked"
    }

    val isSuperAdmin: Boolean
        get() = uid == SUPER_ADMIN_UID || role == ROLE_SUPER_ADMIN || email.trim().equals(ADMIN_GMAIL, ignoreCase = true)

    val isAdmin: Boolean
        get() = isSuperAdmin || role == ROLE_ADMIN || email.trim().equals(ADMIN_GMAIL, ignoreCase = true)
}

data class GameMode(
    val id: String = "",
    val name: String = "",
    val slotIndex: Int = 0,
    val maxPlayers: Int = 12,
    val defaultEntryFee: Double = 0.0,
    val defaultPrizePool: Double = 0.0,
    val imageUrl: String = "",
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false
)

data class Tournament(
    val id: String = "",
    val title: String = "FULL MAP - SOLO",
    val gameMode: String = "BR MATCH",
    val gameModeId: String = "",
    val bannerUrl: String = "",
    val matchDate: String = "TODAY",
    val matchTime: String = "08:00 PM",
    val entryFee: Double = 100.0,
    val prizePool: Double = 1000.0,
    val perKill: Double = 10.0,
    val entryType: String = "Solo",
    val mapName: String = "BERMUDA",
    val version: String = "5",
    val deviceType: String = "MOBILE",
    val modeBadge: String = "BR MOD",
    val badges: String = "TIME | MOBILE | REGULAR",
    val bengaliTimeText: String = "১০ মিনিট পর চালু হবে",
    val countdownSeconds: Int = 623,
    val adminControlText: String = "সম্পূর্ণ এডমিনের হাতে কন্ট্রোল থাকবে",
    val maxPlayers: Int = 48,
    val registeredCount: Int = 0,
    val description: String = "",
    val rules: String = "1. No hacks/cheats allowed.\n2. Room ID & Pass shared 15 mins before match.\n3. Respect all players.",
    val status: String = STATUS_OPEN, // Upcoming, Registration Open, Full, Registration Closed, Live, Completed, Cancelled
    val roomId: String = "",
    val roomPassword: String = "",
    val startTimeMillis: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_UPCOMING = "Upcoming"
        const val STATUS_OPEN = "Registration Open"
        const val STATUS_FULL = "Full"
        const val STATUS_CLOSED = "Registration Closed"
        const val STATUS_LIVE = "LIVE"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_CANCELLED = "Cancelled"
    }

    val availableSlots: Int
        get() = (maxPlayers - registeredCount).coerceAtLeast(0)

    val isFull: Boolean
        get() = registeredCount >= maxPlayers
}

data class TournamentRegistration(
    val id: String = "",
    val tournamentId: String = "",
    val matchId: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val inGameName: String = "",
    val freeFireUID: String = "",
    val gameMode: String = "",
    val registrationStatus: String = "joined",
    val teamId: String = TEAM_UNASSIGNED, // "unassigned", "Team A", "Team B"
    val joinedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TEAM_UNASSIGNED = "Unassigned"
        const val TEAM_A = "Team A"
        const val TEAM_B = "Team B"
    }
}

data class RoomCredentials(
    val tournamentId: String = "",
    val roomId: String = "",
    val roomPassword: String = "",
    val matchName: String = "",
    val gameMode: String = "",
    val matchTime: String = "",
    val isPublished: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PlayerRankingItem(
    val rank: Int = 1,
    val playerId: String = "",
    val playerName: String = "",
    val inGameName: String = "",
    val freeFireUID: String = "",
    val teamName: String = "",
    val kills: Int = 0,
    val placementPoints: Int = 0,
    val totalPoints: Int = 0,
    val prizeWon: Double = 0.0
)

data class TournamentResult(
    val tournamentId: String = "",
    val tournamentTitle: String = "Lone Wolf 2 VS 2 | [ 1,3 Vs 2,4 ]",
    val gameMode: String = "LoneWolf", // "FF FULLMAP", "FF Clash Squad", "LoneWolf", "CS 1v1 2v2"
    val matchDateText: String = "2026-11-06 at 10:00 AM",
    val prizePool: Double = 150.0,
    val entryFee: Double = 50.0,
    val perKill: Double = 0.0,
    val entryType: String = "Solo",
    val mapName: String = "Bermuda",
    val version: String = "MOBILE",
    val teamAResult: String = "",
    val teamBResult: String = "",
    val winnerInfo: String = "",
    val rankings: List<PlayerRankingItem> = emptyList(),
    val isPublished: Boolean = true,
    val publishedAt: Long = System.currentTimeMillis()
)

data class WalletTransaction(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: String = TYPE_DEPOSIT, // "deposit", "withdrawal", "entry_fee", "prize_credit"
    val amount: Double = 0.0,
    val paymentMethod: String = "bKash", // bKash, Nagad, Rocket, UPay, Wallet
    val accountOrTrxId: String = "",
    val senderPhone: String = "",
    val status: String = STATUS_PENDING, // Pending, Approved, Rejected, Completed
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_DEPOSIT = "deposit"
        const val TYPE_WITHDRAWAL = "withdrawal"
        const val TYPE_ENTRY_FEE = "entry_fee"
        const val TYPE_PRIZE_CREDIT = "prize_credit"

        const val STATUS_PENDING = "Pending"
        const val STATUS_APPROVED = "Approved"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_COMPLETED = "Completed"
    }
}

data class AppNotification(
    val id: String = "",
    val userId: String = "all", // "all" or specific UID
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "announcement"
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentSettings(
    val bkashNumber: String = "01300000000",
    val bkashEnabled: Boolean = true,
    val nagadNumber: String = "01300000000",
    val nagadEnabled: Boolean = true,
    val rocketNumber: String = "01300000000",
    val rocketEnabled: Boolean = true,
    val upayNumber: String = "01300000000",
    val upayEnabled: Boolean = true,
    val whatsappNumber: String = "8801300000000",
    val minAddMoney: Double = 50.0,
    val minWithdraw: Double = 100.0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiMessage(
    val id: String = "",
    val sender: String = "ai", // "user" or "ai"
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


data class HomeConfig(
    val sliderBannerText: String = "KHELO BANGLADESH TOP UP - Free Diamond Topup",
    val orangeNoticeText: String = "কম দামে ডায়মন্ড টপ আপ করতে নিচে Shop بٹনে ক্লিক করুন।",
    val liveButtonText: String = "LIVE MATCHES - এখনই দেখুন",
    val updatedAt: Long = System.currentTimeMillis()
)

data class Banner(
    val id: String = "",
    val name: String = "Banner",
    val title: String = "",
    val subTitle: String = "",
    val buttonText: String = "JOIN NOW",
    val imageUrl: String = "",
    val link: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class BannerSettings(
    val autoScroll: Boolean = true,
    val showDots: Boolean = true,
    val speedSeconds: Int = 3,
    val updatedAt: Long = System.currentTimeMillis()
)

