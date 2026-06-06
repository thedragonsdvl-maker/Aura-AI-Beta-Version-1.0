package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.InlineData
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.AppSetting
import com.example.data.HistoryItem
import com.example.data.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import org.json.JSONObject

sealed interface AppScreen {
    object Login : AppScreen
    object Signup : AppScreen
    object ForgotPassword : AppScreen
    object Home : AppScreen
    object Settings : AppScreen
    object MasterMode : AppScreen
}

// UI State representer for various modes
sealed interface ApiState {
    object Idle : ApiState
    object Loading : ApiState
    data class Success(val response: String, val extraData: Any? = null) : ApiState
    data class Error(val message: String) : ApiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val historyDao = db.historyDao()
    private val settingsDao = db.settingsDao()

    // Screen State
    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Login)
        private set

    // Theme state
    var isDarkTheme by mutableStateOf(true)
        private set

    // Connected user
    var loggedInUser by mutableStateOf<UserEntity?>(null)
        private set

    // Is Developer Mode active for thedragonsdvl@gmail.com
    var isMasterModeActive by mutableStateOf(false)
        private set

    // Main API operational state
    private val _apiState = MutableStateFlow<ApiState>(ApiState.Idle)
    val apiState: StateFlow<ApiState> = _apiState.asStateFlow()

    // History flows
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    // UI state states
    var activeTab by mutableStateOf("Chat") // "Chat", "AI Detector", "AI Humanizer", "Photo Creator"
    
    // Auth inputs
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var signupEmail by mutableStateOf("")
    var signupUsername by mutableStateOf("")
    var signupPassword by mutableStateOf("")
    var forgotEmail by mutableStateOf("")

    // Status notifications/Errors
    var authError by mutableStateOf<String?>(null)
    var authSuccess by mutableStateOf<String?>(null)
    
    // Advanced Dev Mode Tools States
    var devLogs = MutableStateFlow<List<String>>(listOf("System Initialized", "Database Ready"))
    
    init {
        // Load initial theme and history
        viewModelScope.launch {
            val setting = settingsDao.getSetting("dark_theme")
            isDarkTheme = setting?.settingValue?.toBoolean() ?: true
            
            historyDao.getAllHistory().collect {
                _history.value = it
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        authError = null
        authSuccess = null
    }

    fun addDevLog(message: String) {
        viewModelScope.launch {
            val current = devLogs.value.toMutableList()
            current.add("[${System.currentTimeMillis() % 100000}] $message")
            devLogs.value = current
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        viewModelScope.launch {
            settingsDao.saveSetting(AppSetting("dark_theme", isDarkTheme.toString()))
            addDevLog("Theme toggled to ${if (isDarkTheme) "Dark" else "Light"}")
        }
    }

    // --- Authentication Actions ---

    fun handleLogin() {
        authError = null
        val email = loginEmail.trim().lowercase()
        val password = loginPassword

        if (email.isEmpty() || password.isEmpty()) {
            authError = "Please enter email and password."
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.passwordHash == password) {
                loggedInUser = user
                
                // Activate Secret Master mode if email matches master requirements
                isMasterModeActive = (email == "thedragonsdvl@gmail.com")
                addDevLog("User $email logged in. Master Mode = $isMasterModeActive")
                
                navigateTo(AppScreen.Home)
                loginPassword = ""
            } else {
                authError = "Invalid email or password. Please try again."
            }
        }
    }

    fun handleSignUp() {
        authError = null
        val email = signupEmail.trim().lowercase()
        val username = signupUsername.trim()
        val password = signupPassword

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            authError = "Please fill out all fields."
            return
        }

        if (password.length < 4) {
            authError = "Password must be at least 4 characters."
            return
        }

        viewModelScope.launch {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                authError = "User with this email already exists."
                return@launch
            }

            try {
                userDao.insertUser(UserEntity(email = email, username = username, passwordHash = password))
                authSuccess = "Account created successfully! You can now log in."
                loginEmail = email
                loginPassword = password
                signupPassword = ""
                signupUsername = ""
                signupEmail = ""
                
                addDevLog("New registration: $email ($username)")
                navigateTo(AppScreen.Login)
            } catch (e: Exception) {
                authError = "Failed to create account: ${e.message}"
            }
        }
    }

    fun handleForgotPassword() {
        authError = null
        authSuccess = null
        val email = forgotEmail.trim().lowercase()

        if (email.isEmpty()) {
            authError = "Please enter your email."
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                // Return password directly in success (per User requests to securely see passwords that work)
                authSuccess = "Your password is: '${user.passwordHash}'"
                addDevLog("Password recovery requested for $email")
            } else {
                authError = "No account found with that email address."
            }
        }
    }

    fun handleLogout() {
        loggedInUser = null
        isMasterModeActive = false
        navigateTo(AppScreen.Login)
        addDevLog("User logged out")
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
            addDevLog("History cleared")
        }
    }

    // --- Gemini Actions ---

    // Dynamic Persona System Instruction
    private fun getSystemInstruction(): Content {
        return Content(
            parts = listOf(
                Part(
                    text = "You are Aura AI, a warm, modern, lightning-fast, and highly intelligent AI companion. " +
                           "Do NOT call yourself Gemini, Google, or any other assistant under any circumstances. " +
                           "Speak naturally, casually, and authentically, like a brilliant but approachable friend. " +
                           "Feel free to express warm personality traits, use standard intuitive formatting, and keep responses snappy."
                )
            )
        )
    }

    fun sendChatPrompt(prompt: String) {
        if (prompt.trim().isEmpty()) return
        _apiState.value = ApiState.Loading
        addDevLog("Sending chat prompt: $prompt")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Record in Room Database history
                historyDao.insertHistory(HistoryItem(queryText = prompt, category = "Chat"))

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    systemInstruction = getSystemInstruction(),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )

                val response = RetrofitClient.service.generate(
                    model = "gemini-3.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I apologize, but I received empty telemetry coordinates. Could you try pulsing that query again?"

                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Success(reply)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Error("Aura Link Offline: ${e.localizedMessage}")
                }
            }
        }
    }

    fun detectTextAI(text: String) {
        if (text.trim().isEmpty()) return
        _apiState.value = ApiState.Loading
        addDevLog("Analyzing text for AI signature...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.insertHistory(
                    HistoryItem(
                        queryText = if (text.length > 30) text.take(30) + "..." else text,
                        category = "Text Detection"
                    )
                )

                val detectorPrompt = "Analyze the following text sample. Determine with high confidence if it was generated by an AI model (like LLMs) or a Human. " +
                        "Provide your assessment structure. Return an approximate 'AI Probability Percentage' (0 to 100), a label (e.g., 'Likely AI', 'Uncertain', 'Likely Human'), " +
                        "and 2-3 brief logical reasons analyzing style patterns, vocabulary repetitiveness, or structure. " +
                        "Begin your response directly with a neat, human-scannable summary:\n" +
                        "Sample text: \"$text\""

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = detectorPrompt)))),
                    systemInstruction = getSystemInstruction()
                )

                val response = RetrofitClient.service.generate(
                    model = "gemini-3.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "AI detection engine returned static. Please recheck content credentials."

                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Success(reply)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Error("Detector Link Failure: ${e.localizedMessage}")
                }
            }
        }
    }

    fun detectImageAI(bitmap: Bitmap, promptAddition: String = "") {
        _apiState.value = ApiState.Loading
        addDevLog("Processing image metadata in neural scanner...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.insertHistory(HistoryItem(queryText = "Scanned visual asset", category = "Image Detection"))

                // Convert bitmap to Base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val requestPrompt = "Analyze this image. Your objective is to detect artificial signatures (artifacts, lighting anomalies, repetitive structural noise, hyper-smooth blending) typical of generative AI architectures (e.g. Midjourney, DALL-E, Stable Diffusion). " +
                        "Return your final verdict: AI Probability % (0-100), AI vs Human label, and 2 key points of visual observation. $promptAddition"

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(
                            Part(text = requestPrompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data))
                        ))
                    ),
                    systemInstruction = getSystemInstruction()
                )

                val response = RetrofitClient.service.generate(
                    model = "gemini-3.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Atmospheric scan returned empty. Visual AI analyzer could not inspect parameters."

                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Success(reply)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Error("Visual Analyzer Offline: ${e.localizedMessage}")
                }
            }
        }
    }

    fun humanizeText(text: String) {
        if (text.trim().isEmpty()) return
        _apiState.value = ApiState.Loading
        addDevLog("Initiating AI text humanizer flow...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.insertHistory(
                    HistoryItem(
                        queryText = if (text.length > 30) text.take(30) + "..." else text,
                        category = "Humanizer"
                    )
                )

                val humanizePrompt = "Take the following AI-sounding text and rewrite it into a highly compelling, natural, authentic, and completely human-like text style. " +
                        "Remove mechanical transitions (e.g., 'demystifying', 'in summary', 'furthermore'), minimize lexical density, and insert minor conversational imperfections or varying sentence lengths typical of real, articulate humans. " +
                        "Present the output beautifully with clear titles: " +
                        "1. **Humanized version** \n" +
                        "2. **Aura's Optimization Report (briefly explaining which robotic patterns were eliminated)**:\n\n" +
                        "Original content: \"$text\""

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = humanizePrompt)))),
                    systemInstruction = getSystemInstruction()
                )

                val response = RetrofitClient.service.generate(
                    model = "gemini-3.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Humanizer relay interrupted. Please submit prompt credentials again."

                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Success(reply)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Error("Humanizer Service Failure: ${e.localizedMessage}")
                }
            }
        }
    }

    fun generateCreativePhoto(prompt: String) {
        if (prompt.trim().isEmpty()) return
        _apiState.value = ApiState.Loading
        addDevLog("Generating AI visual photo prompt: $prompt")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.insertHistory(HistoryItem(queryText = prompt, category = "Image Creator"))

                // Prompting text-to-image or generating beautiful textured prompt ideas along with full visual analysis
                val imageArtPrompt = "Act as an elite digital photographer. Describe a surreal, photorealistic, premium HD scene with absolute artistic detail in matching representation for this prompt: \"$prompt\". " +
                        "I will generate and display a vibrant simulated camera snapshot. Provide the Master camera settings (e.g. f/1.8, ISO 100, 1/250s, 85mm lens), artistic composition details, lighting schema (e.g., volumetric god-rays, rim purple highlights), " +
                        "and a rich text description of what makes this shot beautiful. This acts as our camera visualization blueprint."

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = imageArtPrompt)))),
                    systemInstruction = getSystemInstruction()
                )

                val response = RetrofitClient.service.generate(
                    model = "gemini-3.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Canvas generation could not pull metadata layers."

                withContext(Dispatchers.Main) {
                    // We will return success, and in UI we can also generate a beautiful glowing fluid vector placeholder
                    // matching the theme colors, rendering a gorgeous generated image concept.
                    _apiState.value = ApiState.Success(reply, extraData = prompt)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _apiState.value = ApiState.Error("Visual blueprint generator offline: ${e.localizedMessage}")
                }
            }
        }
    }

    fun setIdle() {
        _apiState.value = ApiState.Idle
    }
}
