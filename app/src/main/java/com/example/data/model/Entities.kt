package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // email or generated ID
    val name: String,
    val email: String,
    val phone: String,
    val walletBalance: Double = 0.0,
    val isBanned: Boolean = false,
    val referralCode: String = "",
    val referredBy: String = "",
    val dailyBonusClaimedAt: Long = 0L,
    val profilePicUrl: String = ""
)

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey val id: String,
    val name: String,
    val entryFee: Double,
    val winningPrize: Double,
    val matchTime: String, // formatted time string
    val startTimeMillis: Long,
    val totalSlots: Int = 100,
    val joinedSlots: Int = 0,
    val rules: String,
    val roomId: String = "",
    val roomPassword: String = "",
    val isPublished: Boolean = false,
    val winnerName: String = "",
    val status: String = "UPCOMING" // UPCOMING, LIVE, ENDED
)

@Entity(tableName = "joined_players")
data class JoinedPlayer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val tournamentId: String,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class WalletTransaction(
    @PrimaryKey val id: String, // unique request ID or TxnID
    val userId: String,
    val userName: String,
    val type: String, // DEPOSIT or WITHDRAW
    val amount: Double,
    val method: String, // bKash, Nagad, Rocket
    val senderNumber: String, // or receiver number for withdraw
    val transactionId: String, // transaction hash/ID entered by user
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleBn: String,
    val message: String,
    val messageBn: String,
    val timestamp: Long = System.currentTimeMillis()
)
