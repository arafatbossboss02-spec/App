package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    // Users
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // Tournaments
    @Query("SELECT * FROM tournaments ORDER BY startTimeMillis ASC")
    fun getAllTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: String): Tournament?

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun getTournamentFlow(tournamentId: String): Flow<Tournament?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: Tournament)

    @Query("DELETE FROM tournaments WHERE id = :tournamentId")
    suspend fun deleteTournament(tournamentId: String)

    // Joined Players
    @Query("SELECT * FROM joined_players WHERE tournamentId = :tournamentId")
    fun getJoinedPlayersForTournament(tournamentId: String): Flow<List<JoinedPlayer>>

    @Query("SELECT * FROM joined_players WHERE userId = :userId")
    fun getJoinedTournamentsForUser(userId: String): Flow<List<JoinedPlayer>>

    @Query("SELECT COUNT(*) FROM joined_players WHERE userId = :userId AND tournamentId = :tournamentId")
    suspend fun isUserJoined(userId: String, tournamentId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoinedPlayer(player: JoinedPlayer)

    // Transactions
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): WalletTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)

    @Update
    suspend fun updateTransaction(transaction: WalletTransaction)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)
}
