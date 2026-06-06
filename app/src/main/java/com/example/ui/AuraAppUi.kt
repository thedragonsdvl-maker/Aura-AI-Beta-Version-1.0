package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.HistoryItem
import com.example.ui.theme.*
import com.example.viewmodel.ApiState
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

@Composable
fun AuraAppContainer(viewModel: MainViewModel) {
    MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = viewModel.currentScreen, label = "screen_tr") { screen ->
                when (screen) {
                    AppScreen.Login -> LoginScreen(viewModel)
                    AppScreen.Signup -> SignupScreen(viewModel)
                    AppScreen.ForgotPassword -> ForgotPasswordScreen(viewModel)
                    AppScreen.Home -> HomeScreen(viewModel)
                    AppScreen.Settings -> SettingsScreen(viewModel)
                    AppScreen.MasterMode -> MasterModeScreen(viewModel)
                }
            }
        }
    }
}

// --- Common UI Components ---

@Composable
fun GradientBackground(isDark: Boolean, content: @Composable BoxScope.() -> Unit) {
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030206),
                Color(0xFF0C091C),
                Color(0xFF08060F)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF3F0FF),
                Color(0xFFF9F7FF),
                Color(0xFFEBE6FF)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .drawBehind {
                // Add stylized subtle decorative circles of ambient neon glow
                if (isDark) {
                    drawCircle(
                        color = AuraNeonPrimary.copy(alpha = 0.08f),
                        radius = 400.dp.toPx(),
                        center = Offset(0f, 0f)
                    )
                    drawCircle(
                        color = AuraNeonBlue.copy(alpha = 0.06f),
                        radius = 450.dp.toPx(),
                        center = Offset(size.width, size.height * 0.7f)
                    )
                } else {
                    drawCircle(
                        color = AuraLightSecondary.copy(alpha = 0.04f),
                        radius = 350.dp.toPx(),
                        center = Offset(size.width * 0.2f, size.height * 0.1f)
                    )
                }
            },
        content = content
    )
}

// Custom Rotating & Breathing Aura Orb Animation
@Composable
fun AuraGlowingOrb(isThinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")
    
    // Scale animation
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // Inner rotation animation
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 1500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotate"
    )

    // Cyan glow amplitude
    val cyanGlowRadius by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.5f else 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "cyan_glow"
    )

    Box(
        modifier = Modifier
            .size(240.dp)
            .scale(scaleFactor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background soft gradient halo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AuraNeonPrimary.copy(alpha = 0.35f),
                                AuraNeonBlue.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width / 2f
                    )
                }
        )

        // Outer rotating ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(angleRotation)
        ) {
            val strokeWidth = 3.dp.toPx()
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        AuraGlowCyan,
                        AuraNeonPrimary,
                        AuraGlowMagenta,
                        AuraGlowCyan
                    )
                ),
                style = Stroke(width = strokeWidth),
                radius = size.width / 2.2f * cyanGlowRadius
            )
        }

        // Mid rings
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .rotate(-angleRotation * 1.5f)
        ) {
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AuraNeonBlue.copy(alpha = if (isThinking) 0.8f else 0.4f),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 2.dp.toPx()),
                radius = size.width / 2.5f
            )
        }

        // Core Logo/Orb Dot
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF22114F),
                            Color(0xFF0F0829)
                        )
                    )
                )
                .border(2.dp, AuraNeonPrimary.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_aura_logo),
                contentDescription = "Aura Orb Core",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.95f))
            )
        }
    }
}

// --- AUTHENTICATION SCREENS ---

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme
    val uriHandler = LocalUriHandler.current

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_aura_logo),
                contentDescription = "Aura Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .shadow(12.dp, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AURA AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else AuraLightPrimary,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "The Premium Intelligent Aura Companion",
                fontSize = 13.sp,
                color = if (isDark) SlateGray else Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Login glassmorphic card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) AuraDarkSurface.copy(alpha = 0.85f) else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) BorderColorDark else BorderColorLight
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACCOUNT LOGIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else AuraLightOnSurface,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error notifications
                    viewModel.authError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.loginEmail,
                        onValueChange = { viewModel.loginEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    OutlinedTextField(
                        value = viewModel.loginPassword,
                        onValueChange = { viewModel.loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.navigateTo(AppScreen.ForgotPassword) }) {
                            Text(
                                "Forgot Password?",
                                fontSize = 13.sp,
                                color = if (isDark) AuraNeonBlue else AuraLightSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) AuraNeonPrimary else AuraLightPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("LOGIN SECURELY", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button with actual redirect continue rules as specified
            OutlinedButton(
                onClick = {
                    val googleUrl = "https://accounts.google.com/v3/signin/identifier?continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26app%3Ddesktop%26hl%3Dro%26next%3D%252F&hl=ro&passive=false&service=youtube&uilel=0&flowName=GlifWebSignIn&flowEntry=AddSession&dsh=S1546089167%3A1780767463638038"
                    uriHandler.openUri(googleUrl)
                    viewModel.addDevLog("Invoked Google Sign-In external sequence channel")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF302B4B) else Color(0xFFC9C4E9)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDark) AuraDarkSurface.copy(alpha = 0.5f) else Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Icon",
                        tint = if (isDark) AuraNeonBlue else AuraLightSecondary,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        "AUTHENTICATE WITH GOOGLE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else AuraLightOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New occupant? ",
                    fontSize = 14.sp,
                    color = if (isDark) SlateGray else Color.Gray
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.Signup) }) {
                    Text(
                        text = "Create Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) AuraNeonPrimary else AuraLightPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SignupScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JOIN AURA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else AuraLightPrimary,
                letterSpacing = 3.sp
            )

            Text(
                text = "Begin your secure intelligence companionship",
                fontSize = 13.sp,
                color = if (isDark) SlateGray else Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) AuraDarkSurface.copy(alpha = 0.85f) else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) BorderColorDark else BorderColorLight
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else AuraLightOnSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    viewModel.authError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.signupUsername,
                        onValueChange = { viewModel.signupUsername = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    OutlinedTextField(
                        value = viewModel.signupEmail,
                        onValueChange = { viewModel.signupEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    OutlinedTextField(
                        value = viewModel.signupPassword,
                        onValueChange = { viewModel.signupPassword = it },
                        label = { Text("Account Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    Button(
                        onClick = { viewModel.handleSignUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) AuraNeonPrimary else AuraLightPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("SIGN UP NOW", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { viewModel.navigateTo(AppScreen.Login) }) {
                Text(
                    text = "← Back to Login Space",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AuraNeonBlue else AuraLightSecondary
                )
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PASSWORD RECOVERY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else AuraLightPrimary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Secure local retrieval via database validation",
                fontSize = 13.sp,
                color = if (isDark) SlateGray else Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) AuraDarkSurface.copy(alpha = 0.85f) else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) BorderColorDark else BorderColorLight
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VERIFY INDentity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else AuraLightOnSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    viewModel.authError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    viewModel.authSuccess?.let {
                        Text(
                            text = it,
                            color = if (isDark) AuraGlowCyan else AuraLightSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.forgotEmail,
                        onValueChange = { viewModel.forgotEmail = it },
                        label = { Text("Your Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraNeonPrimary,
                            unfocusedBorderColor = if (isDark) Color(0xFF322E42) else Color(0xFFD4D0E6)
                        )
                    )

                    Button(
                        onClick = { viewModel.handleForgotPassword() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) AuraNeonPrimary else AuraLightPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("VALIDATE & RETRIEVE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { viewModel.navigateTo(AppScreen.Login) }) {
                Text(
                    text = "← Back to Login Space",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AuraNeonBlue else AuraLightSecondary
                )
            }
        }
    }
}


// --- MAIN WORKSPACE WORKSTATION (HOME) ---

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme
    val history by viewModel.history.collectAsState()
    val apiState by viewModel.apiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User Details Greeting with Master Mode Badge
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AURA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else AuraLightPrimary,
                            letterSpacing = 2.sp
                        )
                        // Active VIP Master mode Badge trigger
                        if (viewModel.isMasterModeActive) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .background(
                                        Brush.linearGradient(listOf(AuraGlowCyan, AuraNeonPrimary)),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.navigateTo(AppScreen.MasterMode) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "MASTER DEV",
                                    fontSize = 9.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "User: ${viewModel.loggedInUser?.username ?: "Guest"}",
                        fontSize = 11.sp,
                        color = if (isDark) SlateGray else Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Settings Icon
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Settings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDark) Color.White else AuraLightOnSurface
                        )
                    }

                    // Logout Action
                    IconButton(onClick = { viewModel.handleLogout() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = if (isDark) SlateGray else Color.Gray
                        )
                    }
                }
            }

            // Central Space layout divided creatively:
            // Tabs Row
            TabRow(
                selectedTabIndex = getTabIndex(viewModel.activeTab),
                containerColor = Color.Transparent,
                contentColor = if (isDark) AuraNeonPrimary else AuraLightPrimary,
                divider = { Divider(color = if (isDark) Color(0xFF1E1935) else Color(0xFFEAEAFF)) }
            ) {
                listOf("Chat", "AI Detector", "AI Humanizer", "Photo Creator").forEach { tabName ->
                    Tab(
                        selected = viewModel.activeTab == tabName,
                        onClick = {
                            viewModel.activeTab = tabName
                            viewModel.setIdle()
                        },
                        text = {
                            Text(
                                text = tabName,
                                fontSize = 12.sp,
                                fontWeight = if (viewModel.activeTab == tabName) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Row(modifier = Modifier.weight(1f)) {
                // Main Workspace
                Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                    when (viewModel.activeTab) {
                        "Chat" -> ChatTabWorkspace(viewModel, apiState)
                        "AI Detector" -> AiDetectorTabWorkspace(viewModel, apiState)
                        "AI Humanizer" -> AiHumanizerTabWorkspace(viewModel, apiState)
                        "Photo Creator" -> PhotoCreatorTabWorkspace(viewModel, apiState)
                    }
                }

                // Collapsible Search & History Sidebar directly populated from DB history
                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .fillMaxHeight()
                        .background(
                            if (isDark) AuraDarkSurface.copy(alpha = 0.4f) else Color(0xFFF0ECFF).copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF15122E) else Color(0xFFEAEAFF),
                            RoundedCornerShape(0.dp)
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "HISTORY ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) SlateGray else Color.Gray,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { viewModel.clearSearchHistory() },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = if (isDark) SlateGray else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Logs empty.\nAwaiting query.",
                                fontSize = 11.sp,
                                color = if (isDark) SlateGray.copy(alpha = 0.5f) else Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(history.take(15)) { item ->
                                HistoryRowItem(item, isDark) {
                                    // Auto loading query Text
                                    when (item.category) {
                                        "Chat" -> {
                                            viewModel.activeTab = "Chat"
                                            viewModel.sendChatPrompt(item.queryText)
                                        }
                                        "Text Detection" -> {
                                            viewModel.activeTab = "AI Detector"
                                            viewModel.detectTextAI(item.queryText)
                                        }
                                        "Humanizer" -> {
                                            viewModel.activeTab = "AI Humanizer"
                                            viewModel.humanizeText(item.queryText)
                                        }
                                        "Image Creator" -> {
                                            viewModel.activeTab = "Photo Creator"
                                            viewModel.generateCreativePhoto(item.queryText)
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

fun getTabIndex(tab: String): Int {
    return when (tab) {
        "Chat" -> 0
        "AI Detector" -> 1
        "AI Humanizer" -> 2
        "Photo Creator" -> 3
        else -> 0
    }
}

@Composable
fun HistoryRowItem(item: HistoryItem, isDark: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) Color(0xFF1B1733).copy(alpha = 0.7f) else Color.White
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = item.category.uppercase(),
                fontSize = 8.sp,
                color = when (item.category) {
                    "Chat" -> AuraNeonBlue
                    "Text Detection", "Image Detection" -> AuraNeonSecondary
                    "Humanizer" -> AuraGlowMagenta
                    else -> AuraGlowCyan
                },
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.queryText,
                fontSize = 11.sp,
                color = if (isDark) Color.White else AuraLightOnSurface,
                maxLines = 2,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

// --- WORKSPACE TABS ---

@Composable
fun ChatTabWorkspace(viewModel: MainViewModel, apiState: ApiState) {
    val isDark = viewModel.isDarkTheme
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper text / status indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                "AURA MAIN COMM CENTER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) AuraNeonBlue else AuraLightSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = when (apiState) {
                    ApiState.Loading -> "Transmitting uplink..."
                    is ApiState.Success -> "Link secure. Synaptic response established."
                    is ApiState.Error -> "Atmosphere interference detected."
                    else -> "Orb Idle. Awaiting voice/text inputs."
                },
                fontSize = 12.sp,
                color = if (isDark) SlateGray else Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Orb Animation Area (Scrollable detail responses if success)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (apiState) {
                ApiState.Loading -> AuraGlowingOrb(isThinking = true)
                is ApiState.Success -> {
                    // Show beautiful output scrolling box
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) AuraDarkSurfaceVariant.copy(alpha = 0.5f) else Color(0xFFF1EEFF)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isDark) BorderColorDark else BorderColorLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Aura AI Response:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraNeonPrimary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = apiState.response,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else AuraLightOnSurface,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                is ApiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = apiState.message,
                            fontSize = 13.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.setIdle() },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraNeonPrimary)
                        ) {
                            Text("Reheat Core", color = Color.White)
                        }
                    }
                }
                else -> {
                    // Show standard orb in centered layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AuraGlowingOrb(isThinking = false)
                    }
                }
            }
        }

        // Action Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask Aura anything...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraNeonPrimary,
                    unfocusedBorderColor = if (isDark) Color(0xFF262144) else Color(0xFFE4E1FE),
                    focusedContainerColor = if (isDark) Color(0xFF100C22) else Color.Transparent,
                    unfocusedContainerColor = if (isDark) Color(0xFF0C091C) else Color.Transparent
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputQuery.trim().isNotEmpty()) {
                        viewModel.sendChatPrompt(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDark) AuraNeonPrimary else AuraLightPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AiDetectorTabWorkspace(viewModel: MainViewModel, apiState: ApiState) {
    val isDark = viewModel.isDarkTheme
    var detectionText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Image Picker Setup for camera/gallery checking
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.detectImageAI(bitmap, "Analyze this imported image coordinate")
                    Toast.makeText(context, "Scanning visual elements...", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Scan fail: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "AI SCANNERS & DETECTORS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraNeonSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scanner panel switches: Choose check text or select visual metadata
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = detectionText,
                onValueChange = { detectionText = it },
                label = { Text("Paste Text Sample (Articles, emails, essays)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraNeonSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.detectTextAI(detectionText) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraNeonSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SCAN TEXT SIGNATURE", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { photoLauncher.launch("image/*") },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraNeonBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SCAN PHOTO FOR AI", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display results
        when (apiState) {
            ApiState.Loading -> {
                Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AuraNeonSecondary)
                }
            }
            is ApiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF101C17) else Color(0xFFEAF9ED)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1A462B) else Color(0xFFB1EAA3))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AuraNeonSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SCANNER DIAGNOSTICS:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraNeonSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = apiState.response,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White else AuraLightOnSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            is ApiState.Error -> {
                Text(apiState.message, color = Color.Red, fontSize = 13.sp)
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(1.dp, if (isDark) Color(0xFF211D36) else Color(0xFFECEBFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Uplink connected.\nPaste text or upload an image above.",
                        fontSize = 12.sp,
                        color = if (isDark) SlateGray else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AiHumanizerTabWorkspace(viewModel: MainViewModel, apiState: ApiState) {
    val isDark = viewModel.isDarkTheme
    var inputToHumanize by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "SYNAPTIC AI TEXT HUMANIZER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraGlowMagenta,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = inputToHumanize,
            onValueChange = { inputToHumanize = it },
            label = { Text("Robotic/AI Text Input") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraGlowMagenta
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.humanizeText(inputToHumanize) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AuraGlowMagenta),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("HUMANIZE SYNTAX", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (apiState) {
            ApiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuraGlowMagenta)
                }
            }
            is ApiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1F101A) else Color(0xFFFFF0FA)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF4C1030) else Color(0xFFFFCEF1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AuraGlowMagenta)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "HUMANIZED OPTIMIZER REPORT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraGlowMagenta
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = apiState.response,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White else AuraLightOnSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(1.dp, if (isDark) Color(0xFF2C274A) else Color(0xFFECE9FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Paste robotic content above to optimize style parameters.",
                        fontSize = 11.sp,
                        color = if (isDark) SlateGray else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoCreatorTabWorkspace(viewModel: MainViewModel, apiState: ApiState) {
    val isDark = viewModel.isDarkTheme
    var photoPrompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "ARTISTIC PHOTO CREATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraGlowCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = photoPrompt,
            onValueChange = { photoPrompt = it },
            label = { Text("What visual mastershot would you like to create?") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraGlowCyan
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.generateCreativePhoto(photoPrompt) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AuraGlowCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("GENERATE CAMERA BLUEPRINT", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (apiState) {
            ApiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuraGlowCyan)
                }
            }
            is ApiState.Success -> {
                // Renders beautifully detailed photo configurations and mock beautiful gradient art matching the theme colors
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Colorful generated artistic abstract texture representing camera shot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AuraGlowCyan,
                                        AuraNeonPrimary,
                                        AuraDarkBg
                                    )
                                )
                            )
                            .border(1.dp, AuraGlowCyan, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Camera, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
                            Text(
                                "RENDERING SIMULATED PHOTO SNAPSHOT",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = (apiState.extraData as? String) ?: "Neon abstract",
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0F1A24) else Color(0xFFEDF8FF)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF104A5E) else Color(0xFFBCE6FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AuraGlowCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "AURA CAMERA SETTINGS REPORT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraGlowCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = apiState.response,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else AuraLightOnSurface,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(1.dp, if (isDark) Color(0xFF1B1B33) else Color(0xFFECE7FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ready to build camera directives.",
                        fontSize = 11.sp,
                        color = if (isDark) SlateGray else Color.Gray
                    )
                }
            }
        }
    }
}


// --- SETTINGS SCREEN ---

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else AuraLightOnSurface
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AURA CONFIG SETTINGS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else AuraLightPrimary
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) AuraDarkSurface else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDark) BorderColorDark else BorderColorLight)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "SYSTEM PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraNeonPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Light/Dark mode toggle (easily configured per user command)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDark) AuraGlowCyan else AuraLightPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Dark Theme Active",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else AuraLightOnSurface
                                )
                                Text(
                                    "Toggles glowing night matrix",
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuraNeonPrimary,
                                checkedTrackColor = AuraNeonPrimary.copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }

                    Divider(color = if (isDark) Color(0xFF26214B) else Color(0xFFECE9FF), modifier = Modifier.padding(vertical = 8.dp))

                    // Account Details Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AuraNeonBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Companionship Core",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else AuraLightOnSurface
                            )
                            Text(
                                "Database credentials validated securely.",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                            Text(
                                "Logged in as: ${viewModel.loggedInUser?.email}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraNeonBlue,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuraNeonPrimary)
            ) {
                Text("DONE SETTINGS", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}


// --- SECRET MASTER DEVELOPER MODE view (`thedragonsdvl@gmail.com`) ---

@Composable
fun MasterModeScreen(viewModel: MainViewModel) {
    val isDark = viewModel.isDarkTheme
    val devLogs by viewModel.devLogs.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    GradientBackground(isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else AuraLightOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AURA MASTER CONSOLE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraGlowCyan,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color.Red, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("VIP ROOT", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                "Welcome Master thedragonsdvl@gmail.com. Showing live model coordinates & local telemetry.",
                fontSize = 12.sp,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Live telemetry log monitor (Beautiful matrix styling)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, AuraGlowCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "LIVE LOG TERMINAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraGlowCyan
                        )
                        IconButton(
                            onClick = { viewModel.devLogs.value = listOf("Telemetry console rebooted") },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = AuraGlowCyan, modifier = Modifier.size(12.dp))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(devLogs.asReversed()) { log ->
                            Text(
                                text = log,
                                fontSize = 11.sp,
                                color = AuraGlowCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Tuning simulation knobs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (isDark) AuraDarkSurface else Color.White),
                border = BorderStroke(1.dp, if (isDark) BorderColorDark else BorderColorLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "EXPERIMENTAL TUNING CALIBRATORS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraNeonPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    var draftTemp by remember { mutableStateOf(0.7f) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aura Model Density Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Dynamic synaptic calibration", fontSize = 10.sp, color = SlateGray)
                        }
                        Slider(
                            value = draftTemp,
                            onValueChange = { draftTemp = it },
                            modifier = Modifier.width(130.dp),
                            valueRange = 0.1f..1.1f,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraNeonPrimary,
                                activeTrackColor = AuraNeonPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Force purging index states...", Toast.LENGTH_SHORT).show()
                                viewModel.addDevLog("Invoked force purge sequence override.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("FORCE CORE PURGE", fontSize = 10.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Core optimization safe.", Toast.LENGTH_SHORT).show()
                                viewModel.addDevLog("Optimized neural vectors successfully.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraNeonBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("REOPTIMIZE VECTORS", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuraNeonPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("EXIT CONSOLE INTEGRITY", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
