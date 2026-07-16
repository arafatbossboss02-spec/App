package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay

// Localization helper
object Txt {
    fun get(en: String, bn: String, isBn: Boolean): String {
        return if (isBn) bn else en
    }
}

@Composable
fun AppNavigator(viewModel: TournamentViewModel) {
    val screen by viewModel.currentScreen
    val isBn = viewModel.isBengali.value
    val toastMsg by viewModel.toastMessage

    val context = LocalContext.current
    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            androidx.compose.material3.SnackbarHostState() // or toast
            android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
    ) {
        when (screen) {
            AppScreen.SPLASH -> SplashScreen(viewModel)
            AppScreen.AUTH -> AuthScreen(viewModel)
            else -> {
                Scaffold(
                    bottomBar = { AppBottomBar(viewModel) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (screen) {
                            AppScreen.HOME -> HomeScreen(viewModel)
                            AppScreen.DETAILS -> TournamentDetailsScreen(viewModel)
                            AppScreen.WALLET -> WalletScreen(viewModel)
                            AppScreen.HISTORY -> HistoryScreen(viewModel)
                            AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel)
                            AppScreen.ADMIN -> AdminDashboardScreen(viewModel)
                            AppScreen.PROFILE -> ProfileScreen(viewModel)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SPLASH SCREEN -----------------
@Composable
fun SplashScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "Alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // 2.5 seconds delay
        // Navigate to Auth screen
        viewModel.currentScreen.value = AppScreen.AUTH
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightDark, CardSurfaceDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Neon Glow Circle Logo with Custom Image
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim)
                    .clip(CircleShape)
                    .background(CardSurfaceDark)
                    .border(2.dp, NeonBlue, CircleShape)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = NeonBlue),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "DLS Esports Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with typing gradient
            Text(
                text = "DLS TOURNAMENT",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = NeonBlue,
                modifier = Modifier.alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Txt.get(
                    "Play. Win. Earn Money.",
                    "খেলুন। জিতুন। টাকা ইনকাম করুন।",
                    isBn
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = TextGrayLight,
                modifier = Modifier.alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ----------------- AUTHENTICATION SCREEN -----------------
@Composable
fun AuthScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val isRegister by viewModel.isRegisterMode
    val errorMsg by viewModel.authErrorMessage

    var showForgotPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MidnightDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Language selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.isBengali.value = !viewModel.isBengali.value }) {
                        Text(
                            text = if (isBn) "English" else "বাংলা",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Header custom logo image
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "DLS Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonBlue, CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (showForgotPassword) {
                        Txt.get("Reset Password", "পাসওয়ার্ড রিসেট", isBn)
                    } else if (isRegister) {
                        Txt.get("Create Account", "অ্যাকাউন্ট তৈরি করুন", isBn)
                    } else {
                        Txt.get("Login Session", "লগইন করুন", isBn)
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = NeonRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (showForgotPassword) {
                    // Forgot Password View
                    OutlinedTextField(
                        value = viewModel.authEmail.value,
                        onValueChange = { viewModel.authEmail.value = it },
                        label = { Text(Txt.get("Gmail Address", "জিমেইল এড্রেস", isBn)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = TextGray,
                            focusedLabelColor = NeonBlue,
                            unfocusedLabelColor = TextGray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.handleForgotPassword() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            Txt.get("Send Reset Link", "রিসেট লিংক পাঠান", isBn),
                            color = MidnightDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { showForgotPassword = false }) {
                        Text(Txt.get("Back to Login", "লগইনে ফিরে যান", isBn), color = NeonCyan)
                    }
                } else {
                    // Login / Register Form
                    if (isRegister) {
                        OutlinedTextField(
                            value = viewModel.authName.value,
                            onValueChange = { viewModel.authName.value = it },
                            label = { Text(Txt.get("Full Name", "সম্পূর্ণ নাম", isBn)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = viewModel.authEmail.value,
                        onValueChange = { viewModel.authEmail.value = it },
                        label = { Text(Txt.get("Gmail Address", "জিমেইল এড্রেস", isBn)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = TextGray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRegister) {
                        OutlinedTextField(
                            value = viewModel.authPhone.value,
                            onValueChange = { viewModel.authPhone.value = it },
                            label = { Text(Txt.get("Mobile Number", "মোবাইল নম্বর", isBn)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = viewModel.authPin.value,
                        onValueChange = { viewModel.authPin.value = it },
                        label = { Text(Txt.get("Password PIN (4+ digits)", "পাসওয়ার্ড পিন (৪+ সংখ্যা)", isBn)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = TextGray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isRegister) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.authReferralInput.value,
                            onValueChange = { viewModel.authReferralInput.value = it },
                            label = { Text(Txt.get("Referral Code (Optional)", "রেফারেল কোড (ঐচ্ছিক)", isBn)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.performAuth() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRegister) Txt.get("Register Now", "রেজিস্টার করুন", isBn)
                            else Txt.get("Login to Wallet", "লগইন করুন", isBn),
                            color = MidnightDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.isRegisterMode.value = !isRegister }) {
                            Text(
                                text = if (isRegister) Txt.get("Already have account?", "ইতিমধ্যে অ্যাকাউন্ট আছে?", isBn)
                                else Txt.get("Create Account", "নতুন অ্যাকাউন্ট খুলুন", isBn),
                                color = NeonCyan,
                                fontSize = 13.sp
                            )
                        }

                        if (!isRegister) {
                            TextButton(onClick = { showForgotPassword = true }) {
                                Text(
                                    text = Txt.get("Forgot PIN?", "পিন ভুলে গেছেন?", isBn),
                                    color = TextGrayLight,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- HOME SCREEN -----------------
@Composable
fun HomeScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val user by viewModel.currentUser.collectAsState()
    val tournamentsList by viewModel.filteredTournaments.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(16.dp)
    ) {
        // App Header Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, NeonBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = Txt.get("Welcome Back,", "স্বাগতম,", isBn),
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = user?.name ?: "DLS Player",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language Toggle
                IconButton(onClick = { viewModel.isBengali.value = !viewModel.isBengali.value }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = NeonCyan
                    )
                }

                // Notifications Center Icon
                Box {
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = NeonBlue
                        )
                    }
                    if (notificationsList.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NeonRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Wallet Balance Overview Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonBlue, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Txt.get("Wallet Balance", "ওয়ালেট ব্যালেন্স", isBn),
                            color = TextGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${user?.walletBalance ?: 0.0} ৳",
                            color = NeonGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row {
                        Button(
                            onClick = { viewModel.currentScreen.value = AppScreen.WALLET },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MidnightDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Txt.get("Deposit", "ডিপোজিট", isBn), color = MidnightDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.currentScreen.value = AppScreen.WALLET },
                            border = BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(Txt.get("Withdraw", "উত্তোলন", isBn), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bonus Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.claimDailyBonus() },
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = NeonGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Txt.get("Daily Bonus 🎁", "দৈনিক বোনাস 🎁", isBn), color = TextWhite, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardSurfaceElevated)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(Txt.get("Referral Code", "রেফার কোড", isBn), color = TextGray, fontSize = 9.sp)
                            Text(user?.referralCode ?: "", color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Filter Box
        OutlinedTextField(
            value = viewModel.searchTournamentQuery.value,
            onValueChange = { viewModel.searchTournamentQuery.value = it },
            placeholder = { Text(Txt.get("Search Tournament...", "টুর্নামেন্ট খুঁজুন...", isBn)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = CardSurfaceElevated,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedPlaceholderColor = TextGray,
                unfocusedPlaceholderColor = TextGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Promo Code Mini-bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.promoCodeInput.value,
                onValueChange = { viewModel.promoCodeInput.value = it },
                placeholder = { Text(Txt.get("Apply Promo (DLS50)", "প্রোমো কোড (DLS50)", isBn), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardSurfaceElevated,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedPlaceholderColor = TextGray,
                    unfocusedPlaceholderColor = TextGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.applyPromoCode() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(Txt.get("Apply", "প্রয়োগ", isBn), color = MidnightDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tournament List Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Txt.get("Active Tournaments", "চলতি টুর্নামেন্ট সমূহ", isBn),
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${tournamentsList.size} ${Txt.get("Matches", "ম্যাচ", isBn)}",
                color = NeonCyan,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tournaments Feed
        if (tournamentsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Upcoming, contentDescription = null, tint = TextGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(Txt.get("No Tournaments Available", "কোনো টুর্নামেন্ট পাওয়া যায়নি", isBn), color = TextGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tournamentsList) { tournament ->
                    TournamentCard(tournament = tournament, viewModel = viewModel)
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text(Txt.get("Notifications Inbox", "নোটিফিকেশন বক্স", isBn), color = NeonBlue, fontWeight = FontWeight.Bold) },
            text = {
                if (notificationsList.isEmpty()) {
                    Text(Txt.get("No notifications yet", "কোনো নোটিফিকেশন নেই", isBn), color = TextGray)
                } else {
                    LazyColumn(modifier = Modifier.height(250.dp)) {
                        items(notificationsList) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = Txt.get(notif.title, notif.titleBn, isBn),
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Txt.get(notif.message, notif.messageBn, isBn),
                                        color = TextWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("OK", color = NeonCyan)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ----------------- TOURNAMENT CARD COMPONENT -----------------
@Composable
fun TournamentCard(tournament: Tournament, viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val isFull = tournament.joinedSlots >= tournament.totalSlots
    val progress = if (tournament.totalSlots > 0) tournament.joinedSlots.toFloat() / tournament.totalSlots else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.selectedTournamentId.value = tournament.id
                viewModel.currentScreen.value = AppScreen.DETAILS
            }
            .border(1.dp, NeonBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (tournament.status) {
                                "LIVE" -> NeonRed.copy(alpha = 0.2f)
                                "ENDED" -> TextGray.copy(alpha = 0.2f)
                                else -> NeonCyan.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (tournament.status) {
                            "LIVE" -> Txt.get("LIVE 🎮", "লাইভ 🎮", isBn)
                            "ENDED" -> Txt.get("ENDED 🏆", "সমাপ্ত 🏆", isBn)
                            else -> Txt.get("UPCOMING ⏳", "আসন্ন ⏳", isBn)
                        },
                        color = when (tournament.status) {
                            "LIVE" -> NeonRed
                            "ENDED" -> TextGray
                            else -> NeonCyan
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Match details timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = NeonGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tournament.matchTime,
                        color = NeonGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tournament Name
            Text(
                text = tournament.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Entry Fee vs Prizes details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(Txt.get("Winning Prize 🏆", "জয়ের পুরস্কার 🏆", isBn), color = TextGray, fontSize = 11.sp)
                    Text("${tournament.winningPrize} ৳", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(Txt.get("Entry Fee 🪙", "প্রবেশ ফি 🪙", isBn), color = TextGray, fontSize = 11.sp)
                    Text(
                        text = if (tournament.entryFee == 0.0) Txt.get("FREE", "ফ্রি", isBn) else "${tournament.entryFee} ৳",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Slots bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Txt.get("Joined Slots:", "পূরণকৃত স্লট:", isBn)} ${tournament.joinedSlots}/${tournament.totalSlots}",
                    color = TextGrayLight,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isFull) Txt.get("SLOT FULL!", "স্লট পূর্ণ!", isBn) else "${tournament.totalSlots - tournament.joinedSlots} ${Txt.get("slots left", "টি খালি", isBn)}",
                    color = if (isFull) NeonRed else NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = if (isFull) NeonRed else NeonBlue,
                trackColor = CardSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Join Trigger
            if (tournament.status == "UPCOMING") {
                Button(
                    onClick = { viewModel.joinTournament(tournament.id) },
                    enabled = !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlue,
                        disabledContainerColor = CardSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isFull) Txt.get("SLOT FULL", "স্লট পূর্ণ", isBn) else Txt.get("JOIN TOURNAMENT", "টুর্নামেন্টে জয়েন করুন", isBn),
                        color = if (isFull) TextGray else MidnightDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (tournament.status == "ENDED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceElevated)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${Txt.get("Winner: ", "বিজয়ী: ", isBn)} ${tournament.winnerName.ifEmpty { "TBD" }}",
                        color = NeonGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Button(
                    onClick = { 
                        viewModel.selectedTournamentId.value = tournament.id
                        viewModel.currentScreen.value = AppScreen.DETAILS
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(Txt.get("VIEW LIVE MATCH", "লাইভ ম্যাচ দেখুন", isBn), color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------- TOURNAMENT DETAILS SCREEN -----------------
@Composable
fun TournamentDetailsScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val tournamentId = viewModel.selectedTournamentId.value ?: return
    val tournamentsList by viewModel.tournaments.collectAsState()
    val tournament = tournamentsList.find { it.id == tournamentId } ?: return

    val playersList by viewModel.selectedTournamentJoinedPlayers.collectAsState()

    var showRoomDetails by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen.value = AppScreen.HOME }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(Txt.get("Tournament Details", "টুর্নামেন্টের বিবরণ", isBn), color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Tournament Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonBlue, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(tournament.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(Txt.get("Winning Prize", "জয়ের পুরস্কার", isBn), color = TextGray, fontSize = 11.sp)
                        Text("${tournament.winningPrize} ৳", color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Txt.get("Entry Fee", "প্রবেশ ফি", isBn), color = TextGray, fontSize = 11.sp)
                        Text("${tournament.entryFee} ৳", color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ROOM ID & Password Box (SENSITIVE)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Txt.get("🎮 Room ID & Password 🎮", "🎮 রুম আইডি এবং পাসওয়ার্ড 🎮", isBn), color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (tournament.isPublished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(Txt.get("ROOM ID", "রুম আইডি", isBn), color = TextGray, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tournament.roomId, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { 
                                    clipboardManager.setText(AnnotatedString(tournament.roomId))
                                    viewModel.triggerToast("Room ID copied!")
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(Txt.get("PASSWORD", "পাসওয়ার্ড", isBn), color = TextGray, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tournament.roomPassword, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { 
                                    clipboardManager.setText(AnnotatedString(tournament.roomPassword))
                                    viewModel.triggerToast("Password copied!")
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Txt.get(
                            "Room ID/Pass will be visible 15 mins before match starts.",
                            "ম্যাচ শুরুর ১৫ মিনিট আগে রুমের বিবরণ প্রকাশ করা হবে।",
                            isBn
                        ),
                        color = TextGrayLight,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Match Rules Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Txt.get("Tournament Rules", "খেলার নিয়মাবলী", isBn), color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(tournament.rules, color = TextWhite, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result Screenshot Upload
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Txt.get("🏆 Winner Result Submission", "🏆 খেলার ফলাফল জমা দিন", isBn), color = NeonGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(Txt.get("Upload screenshot of final match lobby results to claim prize.", "পুরস্কার দাবি করতে ম্যাচের শেষ স্ক্রিনশট আপলোড করুন।", isBn), color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.uploadResultSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Txt.get("Select Screenshot Image", "স্ক্রিনশট ছবি নির্বাচন করুন", isBn), color = TextWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Joined Players Section
        Text(
            text = "${Txt.get("Registered Players", "নিবন্ধিত খেলোয়াড় সমূহ", isBn)} (${playersList.size})",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (playersList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(Txt.get("No players registered yet.", "এখনো কোনো খেলোয়াড় নিবন্ধিত হয়নি।", isBn), color = TextGray)
            }
        } else {
            playersList.forEachIndexed { index, player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(NeonBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((index + 1).toString(), color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(player.userName, color = TextWhite, fontWeight = FontWeight.Bold)
                    }

                    Text(player.userPhone.take(6) + "*****", color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------- WALLET SCREEN (DEPOSIT & WITHDRAW) -----------------
@Composable
fun WalletScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val user by viewModel.currentUser.collectAsState()
    val txList by viewModel.userTransactions.collectAsState()

    var isDepositTab by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(Txt.get("Financial Wallet", "আর্থিক ওয়ালেট", isBn), color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(12.dp))

        // Wallet Balance Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(Txt.get("Available Balance", "মোট জমা ব্যালেন্স", isBn), color = TextGray, fontSize = 13.sp)
                    Text("${user?.walletBalance ?: 0.0} ৳", color = NeonGreen, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }

                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab selection Deposit vs Withdraw
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isDepositTab = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDepositTab) NeonBlue else CardSurfaceDark
                ),
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(Txt.get("Deposit Cash", "টাকা জমা করুন", isBn), color = if (isDepositTab) MidnightDark else TextWhite, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { isDepositTab = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isDepositTab) NeonBlue else CardSurfaceDark
                ),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(Txt.get("Withdraw Cash", "টাকা উত্তোলন", isBn), color = if (!isDepositTab) MidnightDark else TextWhite, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction form panel
        Box(modifier = Modifier.weight(1.3f)) {
            if (isDepositTab) {
                // DEPOSIT LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Manual numbers overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(Txt.get("Manual Payment Instructions:", "ম্যানুয়াল পেমেন্ট নিয়মাবলী:", isBn), color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(Txt.get("1. Send Money to our numbers below via bKash/Nagad/Rocket Cash Out.", "১. বিকাশ/নগদ/রকেটে নিচে দেওয়া নম্বরে ক্যাশ আউট করুন।", isBn), color = TextWhite, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• bKash Personal: 01700000000", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("• Nagad Personal: 01800000000", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("• Rocket Personal: 01900000000", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(Txt.get("2. Enter details and Transaction ID (TxnID) below to submit request.", "২. অনুরোধ সাবমিট করতে নিচে পেমেন্ট বিবরণ ও TxnID লিখুন।", isBn), color = TextWhite, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.depositAmount.value,
                        onValueChange = { viewModel.depositAmount.value = it },
                        label = { Text(Txt.get("Deposit Amount (Minimum 50৳)", "টাকার পরিমাণ (সর্বনিম্ন ৫০৳)", isBn)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method selector radio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("bKash", "Nagad", "Rocket").forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = viewModel.depositMethod.value == m,
                                    onClick = { viewModel.depositMethod.value = m },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                                Text(m, color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.depositSenderNumber.value,
                        onValueChange = { viewModel.depositSenderNumber.value = it },
                        label = { Text(Txt.get("Your Payment Mobile Number", "আপনার পেমেন্ট মোবাইল নম্বর", isBn)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.depositTxnId.value,
                        onValueChange = { viewModel.depositTxnId.value = it },
                        label = { Text(Txt.get("Payment Transaction ID (TxnID)", "পেমেন্ট ট্রানজেকশন আইডি (TxnID)", isBn)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.performDeposit() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Txt.get("SUBMIT DEPOSIT REQUEST", "ডিপোজিট সাবমিট করুন", isBn), color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // WITHDRAW LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(Txt.get("Withdrawal Policy:", "টাকা উত্তোলন নীতি:", isBn), color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(Txt.get("• Minimum withdrawal amount is 100৳.", "• সর্বনিম্ন উত্তোলনের পরিমাণ ১০০৳।", isBn), color = TextWhite, fontSize = 12.sp)
                            Text(Txt.get("• Processed within 1 to 24 hours strictly.", "• ১ থেকে ২৪ ঘণ্টার মধ্যে টাকা পাঠানো হবে।", isBn), color = TextWhite, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.withdrawAmount.value,
                        onValueChange = { viewModel.withdrawAmount.value = it },
                        label = { Text(Txt.get("Withdrawal Amount (Min 100৳)", "উত্তোলন পরিমাণ (সর্বনিম্ন ১০০৳)", isBn)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("bKash", "Nagad", "Rocket").forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = viewModel.withdrawMethod.value == m,
                                    onClick = { viewModel.withdrawMethod.value = m },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                                Text(m, color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.withdrawReceiverNumber.value,
                        onValueChange = { viewModel.withdrawReceiverNumber.value = it },
                        label = { Text(Txt.get("Receiver Mobile Number", "প্রাপক মোবাইল নম্বর", isBn)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.performWithdraw() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Txt.get("SUBMIT WITHDRAW REQUEST", "উত্তোলন সাবমিট করুন", isBn), color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History logs
        Text(Txt.get("Recent Transaction Logs", "সাম্প্রতিক লেনদেন সমূহ", isBn), color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        if (txList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(Txt.get("No transactions recorded.", "কোনো লেনদেনের রেকর্ড নেই।", isBn), color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(txList) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardSurfaceDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${tx.type} (${tx.method})",
                                color = if (tx.type == "DEPOSIT" || tx.type == "PRIZE" || tx.type == "BONUS") NeonGreen else NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(tx.transactionId, color = TextWhite, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (tx.type == "DEPOSIT" || tx.type == "PRIZE" || tx.type == "BONUS" || tx.type == "TRANSFER_RECEIVED") "+" else "-"}${tx.amount} ৳",
                                color = if (tx.type == "DEPOSIT" || tx.type == "PRIZE" || tx.type == "BONUS") NeonGreen else NeonRed,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (tx.status) {
                                            "APPROVED" -> NeonGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> NeonRed.copy(alpha = 0.2f)
                                            else -> NeonGold.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tx.status,
                                    color = when (tx.status) {
                                        "APPROVED" -> NeonGreen
                                        "REJECTED" -> NeonRed
                                        else -> NeonGold
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TOURNAMENT HISTORY SCREEN -----------------
@Composable
fun HistoryScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val joinedHistory by viewModel.joinedTournaments.collectAsState()
    val tournamentsList by viewModel.tournaments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(16.dp)
    ) {
        Text(Txt.get("Matches History", "খেলার ইতিহাস", isBn), color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (joinedHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(Txt.get("You have not joined any matches yet.", "আপনি এখনো কোনো খেলায় অংশ নেননি।", isBn), color = TextGray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(joinedHistory) { joinRecord ->
                    val match = tournamentsList.find { it.id == joinRecord.tournamentId }
                    if (match != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NeonBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(match.name, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        text = match.status,
                                        color = if (match.status == "ENDED") TextGray else NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${Txt.get("Prize Pooled: ", "মোট পুরস্কার: ", isBn)} ${match.winningPrize}৳", color = NeonGreen, fontSize = 12.sp)
                                    Text("${Txt.get("Registered: ", "নিবন্ধন তারিখ: ", isBn)} Today", color = TextGray, fontSize = 11.sp)
                                }

                                if (match.status == "ENDED") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CardSurfaceElevated)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${Txt.get("Match Winner: ", "খেলার বিজয়ী: ", isBn)} ${match.winnerName}",
                                            color = NeonGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- LEADERBOARD SCREEN -----------------
@Composable
fun LeaderboardScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val usersList by viewModel.users.collectAsState()

    // Sorted leaderboard players
    val sortedPlayers = usersList.sortedByDescending { it.walletBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(16.dp)
    ) {
        Text(Txt.get("Top Elite Leaderboard", "সেরা খেলোয়াড়দের লিডারবোর্ড", isBn), color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Top 3 Podium layout
        if (sortedPlayers.size >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumColumn(user = sortedPlayers[1], rank = "2", height = 120, color = NeonCyan)
                // 1st Place
                PodiumColumn(user = sortedPlayers[0], rank = "1", height = 160, color = NeonGold)
                // 3rd Place
                PodiumColumn(user = sortedPlayers[2], rank = "3", height = 100, color = NeonAqua)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complete Standings list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedPlayers.take(15)) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurfaceDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${sortedPlayers.indexOf(user) + 1}",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(user.name, color = TextWhite, fontWeight = FontWeight.Bold)
                            Text(user.id, color = TextGray, fontSize = 11.sp)
                        }
                    }

                    Text("${user.walletBalance} ৳", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(user: User, rank: String, height: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Icon(
            imageVector = if (rank == "1") Icons.Default.Stars else Icons.Default.AccountCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(user.name.take(10) + "..", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("${user.walletBalance} ৳", color = NeonGreen, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(rank, color = MidnightDark, fontWeight = FontWeight.Black, fontSize = 32.sp)
        }
    }
}

// ----------------- PROFILE & CONTROLS SCREEN -----------------
@Composable
fun ProfileScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val user by viewModel.currentUser.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Banner
        Text(Txt.get("My Profile", "আমার প্রোফাইল", isBn), color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonBlue, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(user?.name ?: "DLS Player", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "player@dls.com", color = TextGray, fontSize = 13.sp)
                Text(user?.phone ?: "017*********", color = TextGray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${Txt.get("Wallet:", "ওয়ালেট ব্যালেন্স:", isBn)} ${user?.walletBalance ?: 0.0} ৳",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BALANCE TRANSFER DRAWER FORM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Txt.get("💸 Wallet Balance Transfer", "💸 ওয়ালেট ব্যালেন্স ট্রান্সফার", isBn), color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(Txt.get("Transfer money instantly to any registered user email.", "যেকোনো নিবন্ধিত ইউজারের ইমেইলে সাথে সাথে টাকা ট্রান্সফার করুন।", isBn), color = TextGray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.balanceTransferEmail.value,
                    onValueChange = { viewModel.balanceTransferEmail.value = it },
                    label = { Text(Txt.get("Recipient User Email", "প্রাপক ইউজারের ইমেইল", isBn)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.balanceTransferAmount.value,
                    onValueChange = { viewModel.balanceTransferAmount.value = it },
                    label = { Text(Txt.get("Transfer Amount (৳)", "টাকার পরিমাণ (৳)", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.performBalanceTransfer() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(Txt.get("CONFIRM BALANCE TRANSFER", "ব্যালেন্স ট্রান্সফার নিশ্চিত করুন", isBn), color = MidnightDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social and Contact Developer Channel Launcher Buttons
        Text(Txt.get("Developer Contact & Helpdesk", "ডেভেলপার যোগাযোগ ও সাপোর্ট", isBn), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Telegram Action
            Button(
                onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/dlstournament"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = TextWhite)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Telegram", color = TextWhite)
            }

            // WhatsApp Support Action
            Button(
                onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801700000000"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextWhite)
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", color = TextWhite)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Info Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(Txt.get("Developer Info:", "ডেভেলপার পরিচিতি:", isBn), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(Txt.get("Application engineered by Google AI Studio Build.", "এই অ্যাপ্লিকেশনটি গুগল এআই স্টুডিও বিল্ড দ্বারা তৈরি করা হয়েছে।", isBn), color = TextWhite, fontSize = 12.sp)
                Text(Txt.get("Version: 1.0.0 (Native Android Compose Release)", "সংস্করণ: ১.০.০ (নেটিভ অ্যান্ড্রয়েড কমপোজ)", isBn), color = TextGray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Txt.get("LOG OUT MY SESSION", "আমার সেশন লগআউট করুন", isBn), color = TextWhite, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ----------------- ADMIN DASHBOARD CONTROL SCREEN -----------------
@Composable
fun AdminDashboardScreen(viewModel: TournamentViewModel) {
    val isBn = viewModel.isBengali.value
    val usersList by viewModel.users.collectAsState()
    val pendingTransactions by viewModel.transactions.collectAsState()
    val tournamentsList by viewModel.tournaments.collectAsState()

    var isAddingTournament by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Panel Title
        Text("⚙️ Admin Control Board ⚙️", color = NeonBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // BROADCAST NOTIFICATION FORM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📢 Send Push Notification", color = NeonBlue, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.adminNotificationTitleEn.value,
                    onValueChange = { viewModel.adminNotificationTitleEn.value = it },
                    label = { Text("Title (English)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = viewModel.adminNotificationTitleBn.value,
                    onValueChange = { viewModel.adminNotificationTitleBn.value = it },
                    label = { Text("Title (Bengali)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = viewModel.adminNotificationMsgEn.value,
                    onValueChange = { viewModel.adminNotificationMsgEn.value = it },
                    label = { Text("Message Body (English)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = viewModel.adminNotificationMsgBn.value,
                    onValueChange = { viewModel.adminNotificationMsgBn.value = it },
                    label = { Text("Message Body (Bengali)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.adminSendNotification() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SEND BROADCAST NOW", color = MidnightDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CREATE NEW TOURNAMENT COMPONENT
        Button(
            onClick = { isAddingTournament = !isAddingTournament },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MidnightDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isAddingTournament) "CLOSE NEW MATCH PANEL" else "CREATE NEW MATCH TOURNAMENT", color = MidnightDark, fontWeight = FontWeight.Bold)
        }

        if (isAddingTournament) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, NeonBlue, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎮 Match Properties", color = NeonCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = viewModel.newTournamentName.value,
                        onValueChange = { viewModel.newTournamentName.value = it },
                        label = { Text("Tournament Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = viewModel.newTournamentEntryFee.value,
                        onValueChange = { viewModel.newTournamentEntryFee.value = it },
                        label = { Text("Entry Fee (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = viewModel.newTournamentPrize.value,
                        onValueChange = { viewModel.newTournamentPrize.value = it },
                        label = { Text("Winning Prize (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = viewModel.newTournamentTime.value,
                        onValueChange = { viewModel.newTournamentTime.value = it },
                        label = { Text("Match Time (e.g. Tomorrow, 09:00 PM)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = viewModel.newTournamentRules.value,
                        onValueChange = { viewModel.newTournamentRules.value = it },
                        label = { Text("Match Rules") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { 
                            viewModel.adminCreateTournament()
                            isAddingTournament = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE TOURNAMENT", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MANAGE ACTIVE TOURNAMENTS (ROOM ID & WINNER DECLARATION)
        Text("🎮 Manage Rooms & Winners", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        tournamentsList.forEach { tournament ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(tournament.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Publish Room details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.adminPublishRoomId.value,
                            onValueChange = { viewModel.adminPublishRoomId.value = it },
                            placeholder = { Text("Room ID") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = viewModel.adminPublishRoomPassword.value,
                            onValueChange = { viewModel.adminPublishRoomPassword.value = it },
                            placeholder = { Text("Pass") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = { viewModel.adminPublishRoom(tournament.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Publish")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Declare Winner details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.adminWinnerNameInput.value,
                            onValueChange = { viewModel.adminWinnerNameInput.value = it },
                            placeholder = { Text("Winner Email / Name") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGold, unfocusedBorderColor = CardSurfaceElevated, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = { viewModel.adminAnnounceWinner(tournament.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Winner")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PENDING DEPOSIT/WITHDRAW REQUESTS APPROVALS
        Text("💸 Process Transactions Requests", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val pendingTx = pendingTransactions.filter { it.status == "PENDING" }
        if (pendingTx.isEmpty()) {
            Text("No pending transaction requests found.", color = TextGray, fontSize = 12.sp)
        } else {
            pendingTx.forEach { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${tx.type} (${tx.method})", color = if (tx.type == "DEPOSIT") NeonGreen else NeonRed, fontWeight = FontWeight.Bold)
                            Text("${tx.amount} ৳", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("User: ${tx.userName} (${tx.userId})", color = TextGrayLight, fontSize = 11.sp)
                        Text("Phone: ${tx.senderNumber}", color = TextGrayLight, fontSize = 11.sp)
                        Text("TXID: ${tx.transactionId}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.adminApproveTransaction(tx.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("APPROVE", color = MidnightDark)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.adminRejectTransaction(tx.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REJECT", color = TextWhite)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIEW & BAN USERS
        Text("👥 Register Users Management", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        usersList.forEach { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurfaceDark)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(user.name, color = TextWhite, fontWeight = FontWeight.Bold)
                    Text("Bal: ${user.walletBalance}৳ | ${user.email}", color = TextGray, fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.adminBanUserToggle(user.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) NeonGreen else NeonRed
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (user.isBanned) "UNBAN" else "BAN", color = TextWhite, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ----------------- NAVIGATION BAR -----------------
@Composable
fun AppBottomBar(viewModel: TournamentViewModel) {
    val screen by viewModel.currentScreen
    val isBn = viewModel.isBengali.value
    val isAdmin = viewModel.isAdminLoggedIn.value

    NavigationBar(
        containerColor = CardSurfaceDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = screen == AppScreen.HOME,
            onClick = { viewModel.currentScreen.value = AppScreen.HOME },
            icon = { Icon(Icons.Default.SportsEsports, contentDescription = null) },
            label = { Text(Txt.get("Matches", "খেলা", isBn), fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightDark,
                selectedTextColor = NeonBlue,
                indicatorColor = NeonBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )

        NavigationBarItem(
            selected = screen == AppScreen.WALLET,
            onClick = { viewModel.currentScreen.value = AppScreen.WALLET },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
            label = { Text(Txt.get("Wallet", "ওয়ালেট", isBn), fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightDark,
                selectedTextColor = NeonBlue,
                indicatorColor = NeonBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )

        NavigationBarItem(
            selected = screen == AppScreen.HISTORY,
            onClick = { viewModel.currentScreen.value = AppScreen.HISTORY },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text(Txt.get("History", "ইতিহাস", isBn), fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightDark,
                selectedTextColor = NeonBlue,
                indicatorColor = NeonBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )

        NavigationBarItem(
            selected = screen == AppScreen.LEADERBOARD,
            onClick = { viewModel.currentScreen.value = AppScreen.LEADERBOARD },
            icon = { Icon(Icons.Default.Stars, contentDescription = null) },
            label = { Text(Txt.get("Rankings", "র‍্যাংকিং", isBn), fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightDark,
                selectedTextColor = NeonBlue,
                indicatorColor = NeonBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )

        if (isAdmin) {
            NavigationBarItem(
                selected = screen == AppScreen.ADMIN,
                onClick = { viewModel.currentScreen.value = AppScreen.ADMIN },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Admin", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MidnightDark,
                    selectedTextColor = NeonBlue,
                    indicatorColor = NeonBlue,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                )
            )
        }

        NavigationBarItem(
            selected = screen == AppScreen.PROFILE,
            onClick = { viewModel.currentScreen.value = AppScreen.PROFILE },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(Txt.get("Profile", "প্রোফাইল", isBn), fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightDark,
                selectedTextColor = NeonBlue,
                indicatorColor = NeonBlue,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
    }
}
