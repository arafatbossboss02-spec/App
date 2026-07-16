package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.TournamentDao
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TournamentRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.tournamentDao()

    // Firebase state flags
    private var isFirebaseEnabled = false
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            // Attempt to initialize Firebase services. Will fail gracefully if google-services.json is missing
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            isFirebaseEnabled = true
            Log.d("TournamentRepository", "Firebase is successfully initialized!")
        } catch (e: Exception) {
            isFirebaseEnabled = false
            Log.w("TournamentRepository", "Firebase not initialized (no google-services.json/config). Falling back to Room database local mode.")
        }

        // Pre-populate some starter tournaments and admin notifications on first start
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateDatabase()
        }
    }

    // Expose all flows from Room
    val allUsers: Flow<List<User>> = dao.getAllUsers().flowOn(Dispatchers.IO)
    val allTournaments: Flow<List<Tournament>> = dao.getAllTournaments().flowOn(Dispatchers.IO)
    val allTransactions: Flow<List<WalletTransaction>> = dao.getAllTransactions().flowOn(Dispatchers.IO)
    val allNotifications: Flow<List<Notification>> = dao.getAllNotifications().flowOn(Dispatchers.IO)

    fun getTournamentFlow(id: String): Flow<Tournament?> = dao.getTournamentFlow(id).flowOn(Dispatchers.IO)
    fun getJoinedPlayersForTournament(tournamentId: String): Flow<List<JoinedPlayer>> = dao.getJoinedPlayersForTournament(tournamentId).flowOn(Dispatchers.IO)
    fun getJoinedTournamentsForUser(userId: String): Flow<List<JoinedPlayer>> = dao.getJoinedTournamentsForUser(userId).flowOn(Dispatchers.IO)
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransaction>> = dao.getTransactionsForUser(userId).flowOn(Dispatchers.IO)
    fun getUserFlow(userId: String): Flow<User?> = dao.getUserFlow(userId).flowOn(Dispatchers.IO)

    // Prepopulate database with starter tournaments
    private suspend fun prepopulateDatabase() {
        val tournaments = dao.getAllTournaments().first()
        if (tournaments.isEmpty()) {
            val starterTournaments = listOf(
                Tournament(
                    id = "t1",
                    name = "DLS Ultimate Championship (Weekend Special)",
                    entryFee = 50.0,
                    winningPrize = 500.0,
                    matchTime = "Today, 09:00 PM",
                    startTimeMillis = System.currentTimeMillis() + 4 * 3600000, // 4 hours from now
                    totalSlots = 20,
                    joinedSlots = 14,
                    rules = "1. No hacks or custom configs allowed.\n2. Winners must upload a screenshot of match results.\n3. Match starts strictly on time.\n4. Toxic behavior leads to immediate ban.",
                    roomId = "",
                    roomPassword = "",
                    status = "UPCOMING"
                ),
                Tournament(
                    id = "t2",
                    name = "DLS Daily Clash - Nagad Blitz",
                    entryFee = 20.0,
                    winningPrize = 180.0,
                    matchTime = "Today, 11:30 PM",
                    startTimeMillis = System.currentTimeMillis() + 6 * 3600000,
                    totalSlots = 11,
                    joinedSlots = 3,
                    rules = "1. Must join 5 mins before start time.\n2. Room ID/Password will display here when published.\n3. Prize distributed within 1 hour.",
                    roomId = "889922",
                    roomPassword = "dlsgo",
                    status = "UPCOMING"
                ),
                Tournament(
                    id = "t3",
                    name = "Bengali Pro League (Free Entry)",
                    entryFee = 0.0,
                    winningPrize = 100.0,
                    matchTime = "Tomorrow, 04:00 PM",
                    startTimeMillis = System.currentTimeMillis() + 20 * 3600000,
                    totalSlots = 50,
                    joinedSlots = 42,
                    rules = "1. Free for all registered Bengali players.\n2. Top 3 players win dynamic prize share.\n3. Room details shared 10 mins before.",
                    roomId = "",
                    roomPassword = "",
                    status = "UPCOMING"
                )
            )

            for (t in starterTournaments) {
                dao.insertTournament(t)
            }

            // Populate some starter notifications
            val adminNotification = Notification(
                title = "Welcome to DLS Tournament App!",
                titleBn = "ডিএলএস টুর্নামেন্ট অ্যাপে স্বাগতম!",
                message = "Earn money playing DLS matches! Fill your wallet using bKash, Nagad, or Rocket to join matches.",
                messageBn = "ডিএলএস খেলে টাকা আয় করুন! ম্যাচগুলোতে যোগ দিতে বিকাশ, নগদ বা রকেট দিয়ে ডিপোজিট করুন।",
                timestamp = System.currentTimeMillis()
            )
            dao.insertNotification(adminNotification)

            // Populate some mock leaderboard users
            val mockUsers = listOf(
                User("arafat@dls.com", "Arafat Hossain", "arafat@dls.com", "01712345678", 750.0, referralCode = "REF992", referredBy = ""),
                User("siam@dls.com", "Siam Ahmed", "siam@dls.com", "01812345678", 500.0, referralCode = "REF112", referredBy = "REF992"),
                User("imran@dls.com", "Imran Khan", "imran@dls.com", "01912345678", 320.0, referralCode = "REF552", referredBy = "")
            )
            for (u in mockUsers) {
                dao.insertUser(u)
            }
        }
    }

    // --- Authentication ---
    suspend fun register(name: String, email: String, phone: String, pin: String, referredBy: String = ""): Result<User> = withContext(Dispatchers.IO) {
        try {
            // If Firebase is enabled, attempt Firebase registration
            if (isFirebaseEnabled && firebaseAuth != null && firestore != null) {
                try {
                    firebaseAuth!!.createUserWithEmailAndPassword(email, pin)
                } catch (e: Exception) {
                    Log.e("TournamentRepository", "Firebase auth error during registration: ${e.message}")
                }
            }

            // Check if user already exists locally
            val existing = dao.getUserById(email)
            if (existing != null) {
                return@withContext Result.failure(Exception("Email already registered / ইমেইলটি ইতিপূর্বে নিবন্ধিত হয়েছে"))
            }

            // Create new user
            val referralCode = "DLS" + (100..999).random().toString()
            val newUser = User(
                id = email,
                name = name,
                email = email,
                phone = phone,
                walletBalance = 100.0, // Welcome gift of 100 Taka
                referralCode = referralCode,
                referredBy = referredBy
            )
            dao.insertUser(newUser)

            // If referred by someone, award them 20৳
            if (referredBy.isNotEmpty()) {
                val allUsersList = allUsers.first()
                val referrer = allUsersList.find { it.referralCode == referredBy }
                if (referrer != null) {
                    dao.insertUser(referrer.copy(walletBalance = referrer.walletBalance + 20.0))
                    dao.insertNotification(Notification(
                        title = "Referral Bonus Received!",
                        titleBn = "রেফারেল বোনাস পেয়েছেন!",
                        message = "You earned 20৳ because ${name} registered using your referral code.",
                        messageBn = "আপনার রেফারেল কোড ব্যবহার করে ${name} রেজিষ্ট্রেশন করায় আপনি ২০৳ বোনাস পেয়েছেন।"
                    ))
                }
            }

            // Sync with Firestore if enabled
            if (isFirebaseEnabled && firestore != null) {
                try {
                    firestore!!.collection("users").document(email).set(newUser)
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Failed to sync to Firestore: ${e.message}")
                }
            }

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pin: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (isFirebaseEnabled && firebaseAuth != null) {
                // Firebase logic here
            }

            val user = dao.getUserById(email)
            if (user == null) {
                return@withContext Result.failure(Exception("User not found. Please register / ব্যবহারকারী পাওয়া যায়নি। অনুগ্রহ করে রেজিস্ট্রেশন করুন।"))
            }

            if (user.isBanned) {
                return@withContext Result.failure(Exception("Your account is banned. Contact admin. / আপনার অ্যাকাউন্টটি ব্যান করা হয়েছে। এডমিনের সাথে যোগাযোগ করুন।"))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(email: String): User? = withContext(Dispatchers.IO) {
        dao.getUserById(email)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("users").document(user.id).set(user)
            } catch (e: Exception) {
                Log.w("TournamentRepository", "Firestore sync failed: ${e.message}")
            }
        }
    }

    // --- Tournament Actions ---
    suspend fun createTournament(tournament: Tournament) = withContext(Dispatchers.IO) {
        dao.insertTournament(tournament)
        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("tournaments").document(tournament.id).set(tournament)
            } catch (e: Exception) {
                Log.e("TournamentRepository", "Firestore set failed: ${e.message}")
            }
        }
    }

    suspend fun editTournament(tournament: Tournament) = withContext(Dispatchers.IO) {
        dao.insertTournament(tournament)
        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("tournaments").document(tournament.id).set(tournament)
            } catch (e: Exception) {
                Log.e("TournamentRepository", "Firestore edit failed: ${e.message}")
            }
        }
    }

    suspend fun deleteTournament(id: String) = withContext(Dispatchers.IO) {
        dao.deleteTournament(id)
        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("tournaments").document(id).delete()
            } catch (e: Exception) {
                Log.e("TournamentRepository", "Firestore delete failed: ${e.message}")
            }
        }
    }

    suspend fun joinTournament(userId: String, tournamentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch user & tournament
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found!"))
            val tournament = dao.getTournamentById(tournamentId) ?: return@withContext Result.failure(Exception("Tournament not found!"))

            // 2. Security validation
            if (tournament.joinedSlots >= tournament.totalSlots) {
                return@withContext Result.failure(Exception("Tournament is full! / টুর্নামেন্ট স্লট পূর্ণ!"))
            }

            val isJoinedAlready = dao.isUserJoined(userId, tournamentId) > 0
            if (isJoinedAlready) {
                return@withContext Result.failure(Exception("You have already joined this tournament! / আপনি ইতিমধ্যে এই টুর্নামেন্টে যোগ দিয়েছেন!"))
            }

            if (user.walletBalance < tournament.entryFee) {
                return@withContext Result.failure(Exception("Insufficient wallet balance. Please deposit! / ওয়ালেট ব্যালেন্স পর্যাপ্ত নয়। অনুগ্রহ করে ডিপোজিট করুন!"))
            }

            // 3. Atomic deduction and registration (Simulating transaction block)
            val updatedUser = user.copy(walletBalance = user.walletBalance - tournament.entryFee)
            val updatedTournament = tournament.copy(joinedSlots = tournament.joinedSlots + 1)

            dao.updateUser(updatedUser)
            dao.insertTournament(updatedTournament)

            val joinedPlayer = JoinedPlayer(
                userId = userId,
                userName = user.name,
                userPhone = user.phone,
                tournamentId = tournamentId
            )
            dao.insertJoinedPlayer(joinedPlayer)

            // Record transaction history
            val tx = WalletTransaction(
                id = "TX-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                userName = user.name,
                type = "JOIN_MATCH",
                amount = tournament.entryFee,
                method = "Wallet Deduct",
                senderNumber = user.phone,
                transactionId = "Match Join Fee: ${tournament.name}",
                status = "APPROVED",
                timestamp = System.currentTimeMillis()
            )
            dao.insertTransaction(tx)

            // Firebase syncing
            if (isFirebaseEnabled && firestore != null) {
                try {
                    val batch = firestore!!.batch()
                    batch.set(firestore!!.collection("users").document(userId), updatedUser)
                    batch.set(firestore!!.collection("tournaments").document(tournamentId), updatedTournament)
                    batch.set(firestore!!.collection("joined_players").document(), joinedPlayer)
                    batch.set(firestore!!.collection("transactions").document(tx.id), tx)
                    batch.commit()
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Firebase batch join sync error: ${e.message}")
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Wallet actions ---
    suspend fun requestDeposit(userId: String, amount: Double, method: String, senderNumber: String, txnId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found!"))

            val transaction = WalletTransaction(
                id = "DEP-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                userName = user.name,
                type = "DEPOSIT",
                amount = amount,
                method = method,
                senderNumber = senderNumber,
                transactionId = txnId,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )

            dao.insertTransaction(transaction)

            if (isFirebaseEnabled && firestore != null) {
                try {
                    firestore!!.collection("transactions").document(transaction.id).set(transaction)
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Firebase sync failed: ${e.message}")
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestWithdraw(userId: String, amount: Double, method: String, receiverNumber: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found!"))

            if (user.walletBalance < amount) {
                return@withContext Result.failure(Exception("Insufficient balance for withdrawal! / উত্তোলনের জন্য পর্যাপ্ত ব্যালেন্স নেই!"))
            }

            // Immediately lock/deduct withdraw amount from user balance
            val updatedUser = user.copy(walletBalance = user.walletBalance - amount)
            dao.updateUser(updatedUser)

            val transaction = WalletTransaction(
                id = "WTH-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                userName = user.name,
                type = "WITHDRAW",
                amount = amount,
                method = method,
                senderNumber = receiverNumber,
                transactionId = "PENDING_APPROVAL",
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )

            dao.insertTransaction(transaction)

            if (isFirebaseEnabled && firestore != null) {
                try {
                    firestore!!.collection("transactions").document(transaction.id).set(transaction)
                    firestore!!.collection("users").document(userId).set(updatedUser)
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Firebase sync failed: ${e.message}")
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Admin Dashboard Approvals ---
    suspend fun approveTransaction(transactionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val txn = dao.getTransactionById(transactionId) ?: return@withContext Result.failure(Exception("Transaction not found!"))
            if (txn.status != "PENDING") {
                return@withContext Result.failure(Exception("Transaction is already processed!"))
            }

            val user = dao.getUserById(txn.userId) ?: return@withContext Result.failure(Exception("User not found!"))

            val updatedTxn = txn.copy(status = "APPROVED")
            dao.updateTransaction(updatedTxn)

            var updatedUser = user
            if (txn.type == "DEPOSIT") {
                // For deposits, add money to wallet
                updatedUser = user.copy(walletBalance = user.walletBalance + txn.amount)
                dao.updateUser(updatedUser)

                // Push a custom notification
                dao.insertNotification(Notification(
                    title = "Deposit Approved! 🎉",
                    titleBn = "ডিপোজিট সফল হয়েছে! 🎉",
                    message = "${txn.amount}৳ has been added to your wallet.",
                    messageBn = "আপনার ওয়ালেটে ${txn.amount}৳ যোগ করা হয়েছে।"
                ))
            } else if (txn.type == "WITHDRAW") {
                // For withdrawals, money was already deducted when requested, so we just approve.
                dao.insertNotification(Notification(
                    title = "Withdrawal Success! 💸",
                    titleBn = "টাকা উত্তোলন সফল! 💸",
                    message = "Your withdraw request of ${txn.amount}৳ via ${txn.method} has been approved and sent.",
                    messageBn = "বিকাশ/নগদ/রকেটের মাধ্যমে আপনার ${txn.amount}৳ উত্তোলনের অনুরোধটি সফলভাবে সম্পন্ন হয়েছে।"
                ))
            }

            if (isFirebaseEnabled && firestore != null) {
                try {
                    firestore!!.collection("transactions").document(transactionId).set(updatedTxn)
                    firestore!!.collection("users").document(user.id).set(updatedUser)
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Firebase update failed: ${e.message}")
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectTransaction(transactionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val txn = dao.getTransactionById(transactionId) ?: return@withContext Result.failure(Exception("Transaction not found!"))
            if (txn.status != "PENDING") {
                return@withContext Result.failure(Exception("Transaction is already processed!"))
            }

            val user = dao.getUserById(txn.userId) ?: return@withContext Result.failure(Exception("User not found!"))

            val updatedTxn = txn.copy(status = "REJECTED")
            dao.updateTransaction(updatedTxn)

            var updatedUser = user
            if (txn.type == "WITHDRAW") {
                // If withdrawal is rejected, refund the money back to user balance!
                updatedUser = user.copy(walletBalance = user.walletBalance + txn.amount)
                dao.updateUser(updatedUser)

                dao.insertNotification(Notification(
                    title = "Withdraw Request Rejected ❌",
                    titleBn = "উত্তোলন অনুরোধ প্রত্যাখ্যাত ❌",
                    message = "${txn.amount}৳ has been refunded back to your wallet.",
                    messageBn = "আপনার ${txn.amount}৳ পুনরায় ওয়ালেটে ফেরত দেওয়া হয়েছে।"
                ))
            } else if (txn.type == "DEPOSIT") {
                dao.insertNotification(Notification(
                    title = "Deposit Request Rejected ❌",
                    titleBn = "ডিপোজিট অনুরোধ প্রত্যাখ্যাত ❌",
                    message = "Your deposit of ${txn.amount}৳ was rejected. Check your Trx ID and retry.",
                    messageBn = "আপনার ${txn.amount}৳ ডিপোজিট অনুরোধ বাতিল করা হয়েছে। ট্রানজেকশন আইডি যাচাই করে আবার চেষ্টা করুন।"
                ))
            }

            if (isFirebaseEnabled && firestore != null) {
                try {
                    firestore!!.collection("transactions").document(transactionId).set(updatedTxn)
                    firestore!!.collection("users").document(user.id).set(updatedUser)
                } catch (e: Exception) {
                    Log.w("TournamentRepository", "Firebase reject sync failed: ${e.message}")
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Admin operations ---
    suspend fun sendBroadcastNotification(title: String, titleBn: String, message: String, messageBn: String) = withContext(Dispatchers.IO) {
        val notification = Notification(
            title = title,
            titleBn = titleBn,
            message = message,
            messageBn = messageBn
        )
        dao.insertNotification(notification)

        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("notifications").document().set(notification)
            } catch (e: Exception) {
                Log.e("TournamentRepository", "Failed to send FCM broadcast simulation: ${e.message}")
            }
        }
    }

    suspend fun publishRoomDetails(tournamentId: String, roomId: String, roomPass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val tournament = dao.getTournamentById(tournamentId) ?: return@withContext Result.failure(Exception("Tournament not found"))
            val updated = tournament.copy(
                roomId = roomId,
                roomPassword = roomPass,
                isPublished = true
            )
            dao.insertTournament(updated)

            sendBroadcastNotification(
                title = "Room Details Published! 🎮",
                titleBn = "রুমের তথ্য প্রকাশিত হয়েছে! 🎮",
                message = "Room ID and Password are now visible for tournament: ${tournament.name}.",
                messageBn = "টুর্নামেন্ট: ${tournament.name} এর রুম আইডি ও পাসওয়ার্ড এখন দেখতে পাবেন।"
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun announceTournamentWinner(tournamentId: String, winnerName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val tournament = dao.getTournamentById(tournamentId) ?: return@withContext Result.failure(Exception("Tournament not found"))
            val updated = tournament.copy(
                winnerName = winnerName,
                status = "ENDED"
            )
            dao.insertTournament(updated)

            // If winner name is one of our users, award them the winning prize!
            val usersList = allUsers.first()
            val winningUser = usersList.find { it.name.lowercase() == winnerName.lowercase() || it.id.lowercase() == winnerName.lowercase() }
            if (winningUser != null) {
                val updatedWinner = winningUser.copy(walletBalance = winningUser.walletBalance + tournament.winningPrize)
                dao.updateUser(updatedWinner)

                // Add to transactions history
                val tx = WalletTransaction(
                    id = "PRZ-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                    userId = winningUser.id,
                    userName = winningUser.name,
                    type = "PRIZE",
                    amount = tournament.winningPrize,
                    method = "Tournament Prize",
                    senderNumber = "ADMIN",
                    transactionId = "Winner: ${tournament.name}",
                    status = "APPROVED",
                    timestamp = System.currentTimeMillis()
                )
                dao.insertTransaction(tx)
            }

            sendBroadcastNotification(
                title = "Tournament Winner Announced! 🏆",
                titleBn = "টুর্নামেন্ট বিজয়ীর নাম ঘোষণা! 🏆",
                message = "Congratulations to ${winnerName} for winning the ${tournament.name} and taking home ${tournament.winningPrize}৳!",
                messageBn = "অভিনন্দন ${winnerName}! টুর্নামেন্ট: ${tournament.name} জয় করে ${tournament.winningPrize}৳ নিয়ে যাওয়ার জন্য!"
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun banUserToggle(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            val updated = user.copy(isBanned = !user.isBanned)
            dao.updateUser(updated)
            Result.success(updated.isBanned)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dailyBonus(userId: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            val currentTime = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L

            if (currentTime - user.dailyBonusClaimedAt < oneDayMillis) {
                val timeLeftHours = ((oneDayMillis - (currentTime - user.dailyBonusClaimedAt)) / 3600000.0).toInt()
                return@withContext Result.failure(Exception("Daily bonus already claimed! Wait $timeLeftHours hours. / দৈনিক বোনাস দাবি করা হয়েছে! $timeLeftHours ঘণ্টা অপেক্ষা করুন।"))
            }

            val bonusAmount = 10.0 // 10৳ daily bonus
            val updatedUser = user.copy(
                walletBalance = user.walletBalance + bonusAmount,
                dailyBonusClaimedAt = currentTime
            )
            dao.updateUser(updatedUser)

            // Write Tx
            val tx = WalletTransaction(
                id = "BON-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                userName = user.name,
                type = "BONUS",
                amount = bonusAmount,
                method = "Daily Bonus",
                senderNumber = "SYSTEM",
                transactionId = "Claimed Daily Bonus",
                status = "APPROVED",
                timestamp = System.currentTimeMillis()
            )
            dao.insertTransaction(tx)

            Result.success(bonusAmount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
