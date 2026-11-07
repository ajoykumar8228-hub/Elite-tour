package Com.Elite.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val entryFee: Double,
    val matchDate: String,
    val matchTime: String,
    val maxPlayers: Int,
    val registeredCount: Int,
    val gameMode: String,
    val status: String,
    val prizePool: Double,
    val perKill: Double
)
