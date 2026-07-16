package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TournamentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    SPLASH, AUTH, HOME, DETAILS, WALLET, HISTORY, LEADERBOARD, ADMIN, PROFILE
}

class TournamentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TournamentRepository(application)

    // System Settings State
    val isBengali = mutableStateOf(false)
    val isDarkMode = mutableStateOf(true)

    // Navigation State
    val currentScreen = mutableStateOf(AppScreen.SPLASH)
    val selectedTournamentId = mutableStateOf<String?>(null)

    // Current Session State
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    val currentUser: StateFlow<User?> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getUserFlow(email) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Database flows
    val tournaments: StateFlow<List<Tournament>> = repository.allTournaments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<WalletTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered list states
    val searchTournamentQuery = mutableStateOf("")

    val filteredTournaments: StateFlow<List<Tournament>> = combine(tournaments, snapshotFlow { searchTournamentQuery.value }) { list, query ->
        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<WalletTransaction>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getTransactionsForUser(email) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val joinedTournaments: StateFlow<List<JoinedPlayer>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getJoinedTournamentsForUser(email) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTournamentJoinedPlayers: StateFlow<List<JoinedPlayer>> = snapshotFlow { selectedTournamentId.value }
        .flatMapLatest { id ->
            if (id != null) repository.getJoinedPlayersForTournament(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auth Input Fields State
    val authName = mutableStateOf("")
    val authEmail = mutableStateOf("")
    val authPhone = mutableStateOf("")
    val authPin = mutableStateOf("")
    val authReferralInput = mutableStateOf("")
    val isRegisterMode = mutableStateOf(false)
    val authErrorMessage = mutableStateOf<String?>(null)

    // Wallet Inputs State
    val depositAmount = mutableStateOf("")
    val depositMethod = mutableStateOf("bKash")
    val depositSenderNumber = mutableStateOf("")
    val depositTxnId = mutableStateOf("")
    val withdrawAmount = mutableStateOf("")
    val withdrawMethod = mutableStateOf("bKash")
    val withdrawReceiverNumber = mutableStateOf("")

    // Admin Inputs State
    val newTournamentName = mutableStateOf("")
    val newTournamentEntryFee = mutableStateOf("")
    val newTournamentPrize = mutableStateOf("")
    val newTournamentTime = mutableStateOf("")
    val newTournamentTotalSlots = mutableStateOf("20")
    val newTournamentRules = mutableStateOf("1. No cheats\n2. Share screenshot")
    
    val adminPublishRoomId = mutableStateOf("")
    val adminPublishRoomPassword = mutableStateOf("")
    val adminWinnerNameInput = mutableStateOf("")
    val adminNotificationTitleEn = mutableStateOf("")
    val adminNotificationTitleBn = mutableStateOf("")
    val adminNotificationMsgEn = mutableStateOf("")
    val adminNotificationMsgBn = mutableStateOf("")

    // Referral/Promo Code Inputs State
    val promoCodeInput = mutableStateOf("")
    val balanceTransferEmail = mutableStateOf("")
    val balanceTransferAmount = mutableStateOf("")
    val toastMessage = mutableStateOf<String?>(null)

    // Admin Access Simulation Trigger (Easy check)
    val isAdminLoggedIn = mutableStateOf(false)

    // Result upload image simulation
    val uploadedScreenshotPath = mutableStateOf<String?>(null)

    fun triggerToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // --- Authentication Actions ---
    fun performAuth() {
        authErrorMessage.value = null
        
        // Sanitize inputs
        val name = authName.value.trim()
        val email = authEmail.value.trim().lowercase()
        val phone = authPhone.value.trim()
        val pin = authPin.value.trim()
        val referral = authReferralInput.value.trim()

        if (isRegisterMode.value) {
            // Register
            if (name.isBlank() || email.isBlank() || phone.isBlank() || pin.isBlank()) {
                authErrorMessage.value = if (isBengali.value) "সবগুলো ঘর পূরণ করুন" else "Please fill all fields"
                return
            }
            if (!email.contains("@") || !email.contains(".")) {
                authErrorMessage.value = if (isBengali.value) "সঠিক জিমেইল এড্রেস লিখুন (যেমন: user@gmail.com)" else "Please enter a valid Gmail address"
                return
            }
            if (phone.length < 11) {
                authErrorMessage.value = if (isBengali.value) "সঠিক ১১ ডিজিটের মোবাইল নম্বর লিখুন" else "Please enter a valid 11-digit mobile number"
                return
            }
            if (pin.length < 4) {
                authErrorMessage.value = if (isBengali.value) "পাসওয়ার্ড পিন কমপক্ষে ৪ সংখ্যার হতে হবে" else "PIN must be at least 4 digits"
                return
            }
            
            viewModelScope.launch {
                val result = repository.register(
                    name = name,
                    email = email,
                    phone = phone,
                    pin = pin,
                    referredBy = referral
                )
                result.onSuccess { user ->
                    _currentUserEmail.value = user.id
                    isAdminLoggedIn.value = (user.id.lowercase() == "admin@dls.com")
                    currentScreen.value = AppScreen.HOME
                    triggerToast(if (isBengali.value) "রেজিস্ট্রেশন সফল হয়েছে!" else "Registration Successful!")
                }.onFailure { error ->
                    authErrorMessage.value = error.message
                }
            }
        } else {
            // Login
            if (email.isBlank() || pin.isBlank()) {
                authErrorMessage.value = if (isBengali.value) "ইমেইল এবং পিন লিখুন" else "Enter Email and PIN"
                return
            }

            // Quick bypass for admin testing
            if (email == "admin@dls.com" && pin == "1234") {
                viewModelScope.launch {
                    val adminUser = User("admin@dls.com", "App Admin", "admin@dls.com", "01700000000", 9999.0)
                    repository.updateUser(adminUser)
                    _currentUserEmail.value = adminUser.id
                    isAdminLoggedIn.value = true
                    currentScreen.value = AppScreen.HOME
                    triggerToast("Logged in as Admin!")
                }
                return
            }

            viewModelScope.launch {
                val result = repository.login(email, pin)
                result.onSuccess { user ->
                    _currentUserEmail.value = user.id
                    isAdminLoggedIn.value = (user.id.lowercase() == "admin@dls.com")
                    currentScreen.value = AppScreen.HOME
                    triggerToast(if (isBengali.value) "লগইন সফল হয়েছে!" else "Login Successful!")
                }.onFailure { error ->
                    authErrorMessage.value = error.message
                }
            }
        }
    }

    fun logout() {
        _currentUserEmail.value = null
        isAdminLoggedIn.value = false
        currentScreen.value = AppScreen.AUTH
        triggerToast(if (isBengali.value) "লগআউট করা হয়েছে" else "Logged out successfully")
    }

    fun handleForgotPassword() {
        if (authEmail.value.isBlank()) {
            authErrorMessage.value = if (isBengali.value) "অনুগ্রহ করে আপনার ইমেইল প্রবেশ করুন" else "Please enter your email"
            return
        }
        triggerToast(if (isBengali.value) "পাসওয়ার্ড রিসেট লিংক আপনার ইমেইলে পাঠানো হয়েছে!" else "Password reset simulation sent to your email!")
    }

    // --- Daily Bonus ---
    fun claimDailyBonus() {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            val result = repository.dailyBonus(email)
            result.onSuccess { amount ->
                triggerToast(if (isBengali.value) "দৈনিক বোনাস দাবি করা হয়েছে! আপনি ${amount}৳ পেয়েছেন।" else "Daily bonus claimed! You received ${amount}৳.")
            }.onFailure { error ->
                triggerToast(error.message ?: "Failed to claim")
            }
        }
    }

    // --- Balance Transfer ---
    fun performBalanceTransfer() {
        val senderEmail = _currentUserEmail.value ?: return
        val receiverEmail = balanceTransferEmail.value
        val amountStr = balanceTransferAmount.value
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        if (receiverEmail.isBlank() || amount <= 0) {
            triggerToast(if (isBengali.value) "সঠিক ইমেইল এবং পরিমাণ লিখুন" else "Enter valid email and amount")
            return
        }

        viewModelScope.launch {
            val sender = repository.getUser(senderEmail)
            val receiver = repository.getUser(receiverEmail)

            if (sender == null) return@launch
            if (receiver == null) {
                triggerToast(if (isBengali.value) "প্রাপক পাওয়া যায়নি!" else "Recipient user not found!")
                return@launch
            }

            if (sender.walletBalance < amount) {
                triggerToast(if (isBengali.value) "পর্যাপ্ত ব্যালেন্স নেই!" else "Insufficient balance!")
                return@launch
            }

            // Perform transfer
            repository.updateUser(sender.copy(walletBalance = sender.walletBalance - amount))
            repository.updateUser(receiver.copy(walletBalance = receiver.walletBalance + amount))

            // Add Transactions
            repository.createTournament(Tournament(
                id = "", // placeholder, we insert transactions directly using repo or via simulated txn
                name = "", entryFee = 0.0, winningPrize = 0.0, matchTime = "", startTimeMillis = 0, rules = ""
            )) // Dummy trigger

            // For clean local, we record transaction
            val t1 = WalletTransaction(
                id = "TRF-" + UUID.randomUUID().toString().substring(0,6).uppercase(),
                userId = senderEmail,
                userName = sender.name,
                type = "TRANSFER_SENT",
                amount = amount,
                method = "Wallet Transfer",
                senderNumber = receiverEmail,
                transactionId = "Sent to $receiverEmail",
                status = "APPROVED",
                timestamp = System.currentTimeMillis()
            )
            val t2 = WalletTransaction(
                id = "TRF-" + UUID.randomUUID().toString().substring(0,6).uppercase(),
                userId = receiverEmail,
                userName = receiver.name,
                type = "TRANSFER_RECEIVED",
                amount = amount,
                method = "Wallet Transfer",
                senderNumber = senderEmail,
                transactionId = "Received from $senderEmail",
                status = "APPROVED",
                timestamp = System.currentTimeMillis()
            )
            
            val db = AppDatabase.getDatabase(getApplication())
            db.tournamentDao().insertTransaction(t1)
            db.tournamentDao().insertTransaction(t2)

            triggerToast(if (isBengali.value) "সফলভাবে টাকা ট্রান্সফার করা হয়েছে!" else "Transfer successful!")
            balanceTransferEmail.value = ""
            balanceTransferAmount.value = ""
        }
    }

    // --- Promo Code ---
    fun applyPromoCode() {
        val email = _currentUserEmail.value ?: return
        val code = promoCodeInput.value.uppercase().trim()
        if (code.isBlank()) {
            triggerToast(if (isBengali.value) "প্রোমো কোড লিখুন" else "Enter a promo code")
            return
        }

        if (code == "DLS50" || code == "WELCOME100") {
            viewModelScope.launch {
                val user = repository.getUser(email) ?: return@launch
                val reward = if (code == "DLS50") 50.0 else 100.0

                repository.updateUser(user.copy(walletBalance = user.walletBalance + reward))

                val tx = WalletTransaction(
                    id = "PRO-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                    userId = email,
                    userName = user.name,
                    type = "PROMO_BONUS",
                    amount = reward,
                    method = "Promo Code",
                    senderNumber = "SYSTEM",
                    transactionId = "Code Applied: $code",
                    status = "APPROVED",
                    timestamp = System.currentTimeMillis()
                )
                val db = AppDatabase.getDatabase(getApplication())
                db.tournamentDao().insertTransaction(tx)

                triggerToast(if (isBengali.value) "অভিনন্দন! আপনি ${reward}৳ বোনাস পেয়েছেন।" else "Congrats! You received ${reward}৳ bonus.")
                promoCodeInput.value = ""
            }
        } else {
            triggerToast(if (isBengali.value) "ভুল প্রোমো কোড!" else "Invalid Promo Code!")
        }
    }

    // --- Join Match ---
    fun joinTournament(tournamentId: String) {
        val email = _currentUserEmail.value
        if (email == null) {
            currentScreen.value = AppScreen.AUTH
            return
        }

        viewModelScope.launch {
            val result = repository.joinTournament(email, tournamentId)
            result.onSuccess {
                triggerToast(if (isBengali.value) "সফলভাবে টুর্নামেন্টে যোগ দিয়েছেন! 🎉" else "Joined tournament successfully! 🎉")
            }.onFailure { error ->
                triggerToast(error.message ?: "Failed to join")
            }
        }
    }

    // --- Wallet Transactions ---
    fun performDeposit() {
        val email = _currentUserEmail.value ?: return
        val amount = depositAmount.value.toDoubleOrNull() ?: 0.0
        val method = depositMethod.value
        val number = depositSenderNumber.value
        val txnId = depositTxnId.value

        if (amount < 50) {
            triggerToast(if (isBengali.value) "সর্বনিম্ন ডিপোজিট ৫০৳" else "Minimum deposit is 50৳")
            return
        }
        if (number.isBlank() || txnId.isBlank()) {
            triggerToast(if (isBengali.value) "সবগুলো ঘর পূরণ করুন" else "Fill in all fields")
            return
        }

        viewModelScope.launch {
            val result = repository.requestDeposit(email, amount, method, number, txnId)
            result.onSuccess {
                triggerToast(if (isBengali.value) "ডিপোজিট অনুরোধ পাঠানো হয়েছে! এডমিন অনুমোদনের অপেক্ষা করুন।" else "Deposit request submitted! Please wait for admin approval.")
                depositAmount.value = ""
                depositSenderNumber.value = ""
                depositTxnId.value = ""
            }.onFailure { error ->
                triggerToast(error.message ?: "Failed")
            }
        }
    }

    fun performWithdraw() {
        val email = _currentUserEmail.value ?: return
        val amount = withdrawAmount.value.toDoubleOrNull() ?: 0.0
        val method = withdrawMethod.value
        val number = withdrawReceiverNumber.value

        if (amount < 100) {
            triggerToast(if (isBengali.value) "সর্বনিম্ন উত্তোলন ১০০৳" else "Minimum withdrawal is 100৳")
            return
        }
        if (number.isBlank()) {
            triggerToast(if (isBengali.value) "মোবাইল নম্বর লিখুন" else "Enter mobile number")
            return
        }

        viewModelScope.launch {
            val result = repository.requestWithdraw(email, amount, method, number)
            result.onSuccess {
                triggerToast(if (isBengali.value) "উত্তোলন অনুরোধ সফল! এডমিন অনুমোদনের পর টাকা পাঠানো হবে।" else "Withdrawal requested! Wallet balance deducted. Wait for admin transfer.")
                withdrawAmount.value = ""
                withdrawReceiverNumber.value = ""
            }.onFailure { error ->
                triggerToast(error.message ?: "Failed")
            }
        }
    }

    // --- Result Upload ---
    fun uploadResultSimulation() {
        uploadedScreenshotPath.value = "simulated_uri_screenshot_uploaded.png"
        triggerToast(if (isBengali.value) "স্ক্রিনশট সফলভাবে আপলোড করা হয়েছে! এডমিন এটি যাচাই করবে।" else "Screenshot uploaded successfully! Admin will verify the result.")
    }

    // --- Admin Control Panel Actions ---
    fun adminCreateTournament() {
        val name = newTournamentName.value
        val fee = newTournamentEntryFee.value.toDoubleOrNull() ?: 0.0
        val prize = newTournamentPrize.value.toDoubleOrNull() ?: 0.0
        val time = newTournamentTime.value
        val slots = newTournamentTotalSlots.value.toIntOrNull() ?: 20
        val rules = newTournamentRules.value

        if (name.isBlank() || time.isBlank()) {
            triggerToast("Fill all tournament fields!")
            return
        }

        viewModelScope.launch {
            val newTournament = Tournament(
                id = "t-" + UUID.randomUUID().toString().substring(0, 8),
                name = name,
                entryFee = fee,
                winningPrize = prize,
                matchTime = time,
                startTimeMillis = System.currentTimeMillis() + 8 * 3600000,
                totalSlots = slots,
                joinedSlots = 0,
                rules = rules,
                status = "UPCOMING"
            )
            repository.createTournament(newTournament)
            triggerToast("Match Created Successfully!")
            // Clear inputs
            newTournamentName.value = ""
            newTournamentEntryFee.value = ""
            newTournamentPrize.value = ""
            newTournamentTime.value = ""
        }
    }

    fun adminPublishRoom(tournamentId: String) {
        val roomId = adminPublishRoomId.value
        val roomPass = adminPublishRoomPassword.value
        if (roomId.isBlank() || roomPass.isBlank()) {
            triggerToast("Room ID & Password cannot be empty!")
            return
        }
        viewModelScope.launch {
            repository.publishRoomDetails(tournamentId, roomId, roomPass)
            triggerToast("Room details published & Notifications sent!")
            adminPublishRoomId.value = ""
            adminPublishRoomPassword.value = ""
        }
    }

    fun adminAnnounceWinner(tournamentId: String) {
        val winner = adminWinnerNameInput.value
        if (winner.isBlank()) {
            triggerToast("Please enter a winner name!")
            return
        }
        viewModelScope.launch {
            repository.announceTournamentWinner(tournamentId, winner)
            triggerToast("Winner declared & Prize transferred!")
            adminWinnerNameInput.value = ""
        }
    }

    fun adminApproveTransaction(id: String) {
        viewModelScope.launch {
            repository.approveTransaction(id)
            triggerToast("Transaction Approved!")
        }
    }

    fun adminRejectTransaction(id: String) {
        viewModelScope.launch {
            repository.rejectTransaction(id)
            triggerToast("Transaction Rejected & Refunded if withdraw!")
        }
    }

    fun adminBanUserToggle(userId: String) {
        viewModelScope.launch {
            val isBanned = repository.banUserToggle(userId)
            isBanned.onSuccess { banned ->
                triggerToast(if (banned) "User is Banned!" else "User is Unbanned!")
            }
        }
    }

    fun adminSendNotification() {
        val titleEn = adminNotificationTitleEn.value
        val titleBn = adminNotificationTitleBn.value
        val msgEn = adminNotificationMsgEn.value
        val msgBn = adminNotificationMsgBn.value

        if (titleEn.isBlank() || msgEn.isBlank()) {
            triggerToast("Title and message are required!")
            return
        }

        viewModelScope.launch {
            repository.sendBroadcastNotification(titleEn, titleBn, msgEn, msgBn)
            triggerToast("Broadcast notification sent successfully!")
            adminNotificationTitleEn.value = ""
            adminNotificationTitleBn.value = ""
            adminNotificationMsgEn.value = ""
            adminNotificationMsgBn.value = ""
        }
    }
}
