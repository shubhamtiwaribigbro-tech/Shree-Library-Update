package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PaymentEntity
import com.example.data.SeatEntity
import com.example.data.UserEntity
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    LANDING,
    LOGIN,
    STUDENT_DASHBOARD,
    ADMIN_DASHBOARD
}

@Composable
fun LibraryApp(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.LANDING) }
    val currentUser by viewModel.currentUser.collectAsState()
    val seats by viewModel.seats.collectAsState()
    val users by viewModel.users.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val context = LocalContext.current

    // Error / Notification Handler
    LaunchedEffect(authState) {
        authState?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearAuthState()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Elegant simple persistent brand label
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shree Library • Mau Branch Terminal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "24x7 Active Hub",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LANDING -> {
                    LandingScreen(
                        seats = seats,
                        currentUser = currentUser,
                        viewModel = viewModel,
                        onNavigateToAuth = { currentScreen = AppScreen.LOGIN },
                        onNavigateToDashboard = {
                            currentScreen = if (currentUser?.role == "admin") {
                                AppScreen.ADMIN_DASHBOARD
                            } else {
                                AppScreen.STUDENT_DASHBOARD
                            }
                        },
                        onLogout = { viewModel.logout() }
                    )
                }
                AppScreen.LOGIN -> {
                    LoginScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.LANDING },
                        onAuthSuccess = { user ->
                            currentScreen = if (user.role == "admin") {
                                AppScreen.ADMIN_DASHBOARD
                            } else {
                                AppScreen.STUDENT_DASHBOARD
                            }
                        }
                    )
                }
                AppScreen.STUDENT_DASHBOARD -> {
                    StudentDashboardScreen(
                        user = currentUser,
                        seats = seats,
                        payments = payments,
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.LANDING },
                        onLogout = {
                            viewModel.logout()
                            currentScreen = AppScreen.LANDING
                        }
                    )
                }
                AppScreen.ADMIN_DASHBOARD -> {
                    AdminDashboardScreen(
                        seats = seats,
                        users = users,
                        payments = payments,
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.LANDING },
                        onLogout = {
                            viewModel.logout()
                            currentScreen = AppScreen.LANDING
                        }
                    )
                }
            }
        }
    }
}

// ---------------------- LANDING SCREEN ----------------------
@Composable
fun LandingScreen(
    seats: List<SeatEntity>,
    currentUser: UserEntity?,
    viewModel: LibraryViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Cosmic Slate Background for premium look
    ) {
        // Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF312E81), Color(0xFF0F172A))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // SL Brand Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHREE LIBRARY",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your Premium 24x7\nStudy Sanctuary",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mau's leading high-speed automated workspace. 125 premium ergonomic desks, fully air-conditioned, with dynamic AI integrations.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (currentUser != null) {
                            Button(
                                onClick = onNavigateToDashboard,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                modifier = Modifier
                                    .testTag("landing_dashboard_button")
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = onLogout,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Log Out")
                            }
                        } else {
                            Button(
                                onClick = onNavigateToAuth,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                modifier = Modifier
                                    .testTag("landing_join_button")
                                    .height(48.dp)
                            ) {
                                Text("Book Desk Slot / Join Now", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }
            }
        }

        // Live Occupancy Quick Widget
        item {
            val total = seats.size
            val occupied = seats.count { it.status == "Occupied" }
            val available = seats.count { it.status == "Available" }
            val reserved = seats.count { it.status == "Reserved" }

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Live Seating Metrics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBadge(label = "Total Desks", value = "$total", color = Color(0xFF38BDF8))
                        StatBadge(label = "Occupied", value = "$occupied", color = Color(0xFFF43F5E))
                        StatBadge(label = "Available", value = "$available", color = Color(0xFF34D399))
                        StatBadge(label = "Reserved", value = "$reserved", color = Color(0xFFFBBF24))
                    }
                }
            }
        }

        // Library Guru AI Assistant Chat Widget
        item {
            val chatMessages by viewModel.chatMessages.collectAsState()
            val chatLoading by viewModel.chatLoading.collectAsState()
            var userInput by remember { mutableStateOf("") }

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "AI Support",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Library Guru AI Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Instant timing, fees & desk booking details",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chat History Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Reverse order to show latest first when scrolling from bottom
                            items(chatMessages.reversed()) { msg ->
                                val isGuru = msg.sender == "guru"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = if (isGuru) Arrangement.Start else Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isGuru) 0.dp else 12.dp,
                                                    bottomEnd = if (isGuru) 12.dp else 0.dp
                                                )
                                            )
                                            .background(if (isGuru) Color(0xFF334155) else Color(0xFF4F46E5))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        if (chatLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x88000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = { Text("Ask Guru about timings, plans, or lockers...", fontSize = 12.sp, color = Color.Gray) },
                            modifier = Modifier
                                .weight(1.0f)
                                .testTag("chat_input_field")
                                .height(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                disabledContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    viewModel.askLibraryGuru(userInput)
                                    userInput = ""
                                }
                            },
                            modifier = Modifier
                                .testTag("chat_send_button")
                                .size(48.dp)
                                .background(Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }

        // Pricing Cards Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Automated Membership Plans",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Select any desk allocation track. Secure UPI Verification.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                PricingCard(
                    title = "Basic Desk Shift",
                    price = "₹300",
                    period = "month",
                    features = listOf("8-Hour Dedicated Shift", "High-Speed Unlimited Wi-Fi", "Standard Study Seat"),
                    colorAccent = Color(0xFF38BDF8),
                    onSelect = onNavigateToAuth
                )

                Spacer(modifier = Modifier.height(16.dp))

                PricingCard(
                    title = "Premium Workspace",
                    price = "₹750",
                    period = "month",
                    features = listOf("12-Hour Continuous Access", "Personal Secured safe Locker", "Comfort Ergonomic Desk set"),
                    colorAccent = Color(0xFFFBBF24),
                    onSelect = onNavigateToAuth
                )

                Spacer(modifier = Modifier.height(16.dp))

                PricingCard(
                    title = "VIP 24x7 Facility",
                    price = "₹1000",
                    period = "month",
                    features = listOf("24x7 Dedicated Unrestricted Access", "Fixed Assigned Seat Permanently", "Complimentary Refreshment & Tea", "Priority Support Desk"),
                    colorAccent = Color(0xFFEC4899),
                    onSelect = onNavigateToAuth
                )
            }
        }

        // Footer Core Perks
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(24.dp)
            ) {
                Text(
                    text = "Premium Sanctuary Standard",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureListItem(icon = Icons.Default.Check, text = "100% Silent Air Conditioned Chambers")
                FeatureListItem(icon = Icons.Default.Check, text = "Power Outages Proof (Heavy Generators Backed)")
                FeatureListItem(icon = Icons.Default.Check, text = "CCTV Secured Desk Environment")
                FeatureListItem(icon = Icons.Default.Check, text = "Mau Branch location easily accessible")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "© 2026 Shree Library Group. Built beautifully.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
fun FeatureListItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 13.sp, color = Color(0xFFCBD5E1))
    }
}

@Composable
fun PricingCard(
    title: String,
    price: String,
    period: String,
    features: List<String>,
    colorAccent: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.5.dp, colorAccent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = price, fontWeight = FontWeight.Black, fontSize = 28.sp, color = colorAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "/$period", fontSize = 13.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.height(12.dp))
            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Included",
                        tint = colorAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = feat, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = colorAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select & Subscribe", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }
    }
}

// ---------------------- LOGIN / AUTH SCREEN ----------------------
@Composable
fun LoginScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onAuthSuccess: (UserEntity) -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") } // "student" or "admin"
    var password by remember { mutableStateOf("") } // Mock verification

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Match the elegant slate dark canvas
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Return to home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("back_to_home_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Return to Home", color = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF4F46E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Shree Library Security Node",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    text = "Authorized Access Session Pipeline",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRegister) Color(0xFF1E293B) else Color.Transparent)
                            .clickable { isRegister = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold, color = if (!isRegister) Color.White else Color.Gray, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegister) Color(0xFF1E293B) else Color.Transparent)
                            .clickable { isRegister = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Register", fontWeight = FontWeight.Bold, color = if (isRegister) Color.White else Color.Gray, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Registration Extra Fields
                if (isRegister) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email Address
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isRegister) "Email Address" else "Email Address (or type 'admin' or 'student')", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().testTag("username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    singleLine = true
                )

                if (isRegister) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Picker Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Register Account Role:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = role == "student",
                                onClick = { role = "student" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF59E0B))
                            )
                            Text("Student", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = role == "admin",
                                onClick = { role = "admin" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF59E0B))
                            )
                            Text("Admin", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Secret Password (any for trial)", color = Color.LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isRegister) {
                            viewModel.register(name, email, mobile, role, onAuthSuccess)
                        } else {
                            viewModel.login(email, onAuthSuccess)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_button")
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(
                        text = if (isRegister) "Create Account" else "Authenticate Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Help test chips
                Text(
                    text = "💡 Quick Trial Keys:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionChip(
                        onClick = {
                            email = "admin"
                            password = "trial"
                            viewModel.login("admin", onAuthSuccess)
                        },
                        label = { Text("Admin Console", fontSize = 11.sp, color = Color.White) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF334155))
                    )
                    SuggestionChip(
                        onClick = {
                            email = "student"
                            password = "trial"
                            viewModel.login("student", onAuthSuccess)
                        },
                        label = { Text("Student Console", fontSize = 11.sp, color = Color.White) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF334155))
                    )
                }
            }
        }
    }
}

// ---------------------- SEAT MAP CORE COMPONENT ----------------------
@Composable
fun SeatMapLayout(
    seats: List<SeatEntity>,
    userAssignedSeat: Int?,
    interactive: Boolean,
    onSeatSelect: (SeatEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "125 Desk Layout Seating Matrix",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF34D399), CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Free", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF4F46E5), CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Full", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFBBF24), CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Hold", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid scroll view
            Box(modifier = Modifier.height(280.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(seats) { seat ->
                        val isUserDesk = seat.seatNumber == userAssignedSeat
                        val colorSchemeBg = when {
                            isUserDesk -> Color(0xFFEC4899) // Hot pink highlight for own desk!
                            seat.status == "Available" -> Color(0x1A10B981)
                            seat.status == "Occupied" -> Color(0x1A4F46E5)
                            else -> Color(0x1AFBBF24)
                        }
                        val colorSchemeBorder = when {
                            isUserDesk -> Color(0xFFEC4899)
                            seat.status == "Available" -> Color(0xFF34D399)
                            seat.status == "Occupied" -> Color(0xFF4F46E5)
                            else -> Color(0xFFFBBF24)
                        }
                        val colorSchemeText = when {
                            isUserDesk -> Color.White
                            seat.status == "Available" -> Color(0xFF34D399)
                            seat.status == "Occupied" -> Color(0xFF818CF8)
                            else -> Color(0xFFFCD34D)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorSchemeBg)
                                .border(1.dp, colorSchemeBorder, RoundedCornerShape(8.dp))
                                .clickable(enabled = interactive || seat.status == "Available") {
                                    onSeatSelect(seat)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "#${seat.seatNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = colorSchemeText
                                )
                                if (isUserDesk) {
                                    Text("MINE", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- STUDENT DASHBOARD SCREEN ----------------------
@Composable
fun StudentDashboardScreen(
    user: UserEntity?,
    seats: List<SeatEntity>,
    payments: List<PaymentEntity>,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("profile") } // "profile", "seats", "payment"

    // Subscribing payment dialog values
    var selectedPlanForPayment by remember { mutableStateOf("VIP") }
    var transactionIdInput by remember { mutableStateOf("") }
    var paymentAmount by remember { mutableStateOf(1000) }

    val userPayments = payments.filter { it.userId == user?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Navigation bar
        Surface(
            tonalElevation = 4.dp,
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Home", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Workspace Hub Console",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Logout", tint = Color.Red)
                    }
                }

                // Dynamic local tabs
                TabRow(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
                    selectedTabIndex = when (activeTab) {
                        "profile" -> 0
                        "seats" -> 1
                        else -> 2
                    }
                ) {
                    Tab(
                        selected = activeTab == "profile",
                        onClick = { activeTab = "profile" },
                        text = { Text("My Profile", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                    Tab(
                        selected = activeTab == "seats",
                        onClick = { activeTab = "seats" },
                        text = { Text("Desk Matrix", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                    Tab(
                        selected = activeTab == "payment",
                        onClick = { activeTab = "payment" },
                        text = { Text("Fees Status", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (activeTab) {
                "profile" -> {
                    item {
                        // User Profile Welcome Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(Color(0xFFF59E0B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (user?.name?.firstOrNull() ?: 'S').toString(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = user?.name ?: "Student User",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = user?.email ?: "student@domain.com",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "Mob: ${user?.mobile ?: "9555529599"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Divider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("CURRENT STATION", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        Text(
                                            text = if (user?.assignedSeat != null) "Desk Station #${user.assignedSeat}" else "Not Allocated Yet",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = Color(0xFFFBBF24)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("MEMBERSHIP LEVEL", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        Text(
                                            text = if (user?.plan != "None") "${user?.plan} Plan" else "No Plan Selected",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("RENEWAL VALIDITY", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        val validText = if (user?.validUntil != null) {
                                            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                                            sdf.format(Date(user.validUntil))
                                        } else {
                                            "None"
                                        }
                                        Text(
                                            text = validText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when (user?.planStatus) {
                                                    "Active" -> Color(0xFF10B981)
                                                    "Pending" -> Color(0xFFFBBF24)
                                                    else -> Color(0xFFEF4444)
                                                }
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = user?.planStatus ?: "Expired",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("My Assigned Location", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        SeatMapLayout(
                            seats = seats,
                            userAssignedSeat = user?.assignedSeat,
                            interactive = false,
                            onSeatSelect = {}
                        )
                    }
                }

                "seats" -> {
                    item {
                        Text(
                            text = "Browse Seating Layout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Green slots are open. Apply in the UPI panel or contact Admin to occupy.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        SeatMapLayout(
                            seats = seats,
                            userAssignedSeat = user?.assignedSeat,
                            interactive = false,
                            onSeatSelect = {}
                        )
                    }
                }

                "payment" -> {
                    item {
                        // UPI direct payment details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Direct UPI Payment Terminal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pay via GPay, PhonePe, or Paytm, then enter the Transaction Ref ID below.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("OFFICIAL UPI VPA ADDRESS:", fontSize = 10.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "shubhamtiwari3731@okaxis",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text("Merchant: Shree Library Mau Operations", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Select plan
                                Text("Select Subscription Plan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("Basic (₹300)", "Premium (₹750)", "VIP (₹1000)").forEach { option ->
                                        val planValue = option.substringBefore(" ")
                                        val amountValue = option.substringAfter("₹").substringBefore(")").toInt()
                                        val isSelected = selectedPlanForPayment == planValue

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFF334155))
                                                .clickable {
                                                    selectedPlanForPayment = planValue
                                                    paymentAmount = amountValue
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = option,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Transaction ID input
                                OutlinedTextField(
                                    value = transactionIdInput,
                                    onValueChange = { transactionIdInput = it },
                                    label = { Text("Paste UPI Ref No. (e.g. TXN123456)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val context = LocalContext.current
                                Button(
                                    onClick = {
                                        if (transactionIdInput.isNotBlank()) {
                                            viewModel.submitPayment(
                                                planName = selectedPlanForPayment,
                                                amount = paymentAmount,
                                                transactionId = transactionIdInput,
                                                onSubmitted = {
                                                    transactionIdInput = ""
                                                    android.widget.Toast.makeText(context, "Payment Submitted! Awaiting Admin Approval.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            android.widget.Toast.makeText(context, "Please enter a valid Transaction ID", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_payment_button")
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Text("Submit Payment Receipt Log", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("My Submitted Payments History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (userPayments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No payment history logs submitted.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        } else {
                            userPayments.forEach { p ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "${p.planName} Plan - ₹${p.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text(text = "Ref: ${p.transactionId}", fontSize = 11.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when (p.status) {
                                                        "Approved" -> Color(0xFFD1FAE5)
                                                        "Pending" -> Color(0xFFFEF3C7)
                                                        else -> Color(0xFFFEE2E2)
                                                    }
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = p.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (p.status) {
                                                    "Approved" -> Color(0xFF065F46)
                                                    "Pending" -> Color(0xFF92400E)
                                                    else -> Color(0xFF991B1B)
                                                }
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
}

// ---------------------- ADMIN DASHBOARD SCREEN ----------------------
@Composable
fun AdminDashboardScreen(
    seats: List<SeatEntity>,
    users: List<UserEntity>,
    payments: List<PaymentEntity>,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("dashboard") } // "dashboard", "seats", "payments"

    // Reservation properties modal state
    var selectedSeatForConfig by remember { mutableStateOf<SeatEntity?>(null) }
    var inputSeatStatus by remember { mutableStateOf("Available") }
    var inputOccupantUserId by remember { mutableStateOf<Int?>(null) }
    var inputSeatShift by remember { mutableStateOf("24x7") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Navigation Bar
        Surface(
            tonalElevation = 4.dp,
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Home", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "System Management Console",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Mau Branch Central Control Node",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Logout", tint = Color.Red)
                    }
                }

                // Admin dynamic Tabs
                TabRow(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
                    selectedTabIndex = when (activeTab) {
                        "dashboard" -> 0
                        "seats" -> 1
                        else -> 2
                    }
                ) {
                    Tab(
                        selected = activeTab == "dashboard",
                        onClick = { activeTab = "dashboard" },
                        text = { Text("Analytics Hub", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                    Tab(
                        selected = activeTab == "seats",
                        onClick = { activeTab = "seats" },
                        text = { Text("Seat Manager", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                    Tab(
                        selected = activeTab == "payments",
                        onClick = { activeTab = "payments" },
                        text = { Text("Fee Requests", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF6366F1),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (activeTab) {
                "dashboard" -> {
                    item {
                        // Quick Metric Cards Grid
                        val totalStudentsCount = users.count { it.role == "student" }
                        val occupancyCount = seats.count { it.status == "Occupied" }
                        val activePaidCount = users.count { it.planStatus == "Active" }
                        val pendingRequestsCount = payments.count { it.status == "Pending" }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MetricCard(title = "Enrolled Students", value = "$totalStudentsCount Active", color = Color(0xFF4F46E5), modifier = Modifier.weight(1f))
                                MetricCard(title = "Live Occupancy", value = "$occupancyCount / 125 Desks", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MetricCard(title = "Active Subscriptions", value = "$activePaidCount Active", color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
                                MetricCard(title = "Fee Log Review Queue", value = "$pendingRequestsCount Pending", color = Color(0xFFEC4899), modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Active Seat Matrix Allocator", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("Tap any seat to allocate, free, or configure details.", fontSize = 11.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))

                        SeatMapLayout(
                            seats = seats,
                            userAssignedSeat = null,
                            interactive = true,
                            onSeatSelect = { seat ->
                                selectedSeatForConfig = seat
                                inputSeatStatus = seat.status
                                inputOccupantUserId = seat.userId
                                inputSeatShift = seat.shift
                            }
                        )
                    }
                }

                "seats" -> {
                    item {
                        Text("Seating Management Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        SeatMapLayout(
                            seats = seats,
                            userAssignedSeat = null,
                            interactive = true,
                            onSeatSelect = { seat ->
                                selectedSeatForConfig = seat
                                inputSeatStatus = seat.status
                                inputOccupantUserId = seat.userId
                                inputSeatShift = seat.shift
                            }
                        )
                    }
                }

                "payments" -> {
                    val pendingPayments = payments.filter { it.status == "Pending" }
                    val historyPayments = payments.filter { it.status != "Pending" }

                    item {
                        Text("Pending UPI Fee Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (pendingPayments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("All fee requests audited. Queue is empty!", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    } else {
                        items(pendingPayments) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = p.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                            Text(text = p.userEmail, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "${p.planName} Plan", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF6366F1))
                                            Text(text = "Amount: ₹${p.amount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "UPI Ref ID: ${p.transactionId}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { viewModel.rejectPayment(p) }) {
                                            Text("Reject Request", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Button(
                                            onClick = { viewModel.approvePayment(p) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                        ) {
                                            Text("Approve & Activate", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Processed Audited History", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (historyPayments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No historical logs.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    } else {
                        items(historyPayments) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = p.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text(text = "${p.planName} (₹${p.amount}) - Ref ${p.transactionId}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (p.status == "Approved") Color(0xFFD1FAE5) else Color(0xFFFEE2E2))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = p.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (p.status == "Approved") Color(0xFF065F46) else Color(0xFF991B1B)
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

    // Modal Seat properties editor
    selectedSeatForConfig?.let { seat ->
        AlertDialog(
            onDismissRequest = { selectedSeatForConfig = null },
            title = { Text("Desk Config - Seat #${seat.seatNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Allocation Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Available", "Occupied", "Reserved").forEach { status ->
                            val isSelected = inputSeatStatus == status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9))
                                    .clickable { inputSeatStatus = status }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(status, color = if (isSelected) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (inputSeatStatus == "Occupied") {
                        Text("Assign to Enrolled Student:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val studentsList = users.filter { it.role == "student" }
                        
                        if (studentsList.isEmpty()) {
                            Text("No students enrolled. Tap custom allocation.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            val currentOccupantName = studentsList.find { it.id == inputOccupantUserId }?.name ?: "Tap to choose student"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { dropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                Text(currentOccupantName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    studentsList.forEach { student ->
                                        DropdownMenuItem(
                                            text = { Text("${student.name} (${student.email})") },
                                            onClick = {
                                                inputOccupantUserId = student.id
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Active Operational Shift:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("24x7", "Morning", "Evening").forEach { shift ->
                            val isSelected = inputSeatShift == shift
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9))
                                    .clickable { inputSeatShift = shift }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(shift, color = if (isSelected) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.allocateSeat(
                            seatNumber = seat.seatNumber,
                            status = inputSeatStatus,
                            studentId = if (inputSeatStatus == "Occupied") inputOccupantUserId else null,
                            shift = inputSeatShift
                        )
                        selectedSeatForConfig = null
                    }
                ) {
                    Text("Apply Layout Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSeatForConfig = null }) {
                    Text("Discard")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
