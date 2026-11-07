package com.jennyai.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
        setContent { JennyApp(this) }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("fa", "IR"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    fun speakPersian(text: String): Boolean {
        if (!ttsReady) return false
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jenny_reply")
        return true
    }

    fun isPersianVoiceReady(): Boolean = ttsReady

    fun stopPersianSpeaking() { tts?.stop() }

    fun startPersianListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            onError("سرویس تشخیص گفتار روی این گوشی در دسترس نیست.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "با جنی فارسی صحبت کن")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "صدایی تشخیص داده نشد؛ دوباره امتحان کن."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "مجوز میکروفون داده نشده است."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "اتصال اینترنت یا سرویس تشخیص گفتار را بررسی کن."
                    else -> "تشخیص صدا انجام نشد. دوباره تلاش کن."
                }
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull().orEmpty()
                if (spokenText.isNotBlank()) onResult(spokenText)
                else onError("متنی از صدایت دریافت نشد.")
            }
        })

        recognizer.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}

@Composable
private fun JennyApp(activity: MainActivity) {
    val chatScrollState = rememberScrollState()
    var message by remember { mutableStateOf("") }
    val chatPreferences = remember {
        activity.getSharedPreferences("jenny_chat", Context.MODE_PRIVATE)
    }
    var conversation by remember {
        mutableStateOf(
            chatPreferences.getString(
                "conversation",
                "سلام! من جنی هستم. می‌توانی پیام بنویسی یا با دکمه میکروفون فارسی صحبت کنی."
            ) ?: "سلام! من جنی هستم. می‌توانی پیام بنویسی یا با دکمه میکروفون فارسی صحبت کنی."
        )
    }
    var status by remember { mutableStateOf("آماده") }
    var currentAnimation by remember { mutableStateOf("Idle") }

    val microphonePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            status = "در حال شنیدن..."
            activity.startPersianListening(
                onResult = { spoken ->
                    message = spoken
                    status = "صدایت به متن تبدیل شد؛ برای دریافت پاسخ ارسال را بزن."
                },
                onError = { error -> status = error }
            )
        } else {
            status = "برای صحبت با جنی باید مجوز میکروفون را فعال کنی."
        }
    }

    fun sendMessage() {
        val userMessage = message.trim()
        if (userMessage.isBlank()) return
        val reply = offlineDemoReply(userMessage)
        conversation = if (conversation.isBlank()) {
            "تو: $userMessage\n\nجنی: $reply"
        } else {
            "$conversation\n\n────────────\n\nتو: $userMessage\n\nجنی: $reply"
        }
        chatPreferences.edit().putString("conversation", conversation).apply()
        message = ""
        status = if (activity.speakPersian(reply)) {
            "پاسخ آزمایشی با صدای فارسی پخش می‌شود."
        } else {
            "صدای فارسی در موتور گفتار گوشی موجود نیست؛ می‌توانی آن را در تنظیمات تبدیل متن به گفتار نصب کنی."
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF10121B)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF191D30), Color(0xFF10121B))))
                    .padding(20.dp)
                    .verticalScroll(chatScrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Jenny AI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("دستیار فارسی • نسخه ۰٫۳", color = Color(0xFFB8BBD0), fontSize = 14.sp)
                Text("گفت‌وگوی محلی و گفتار فارسی", color = Color(0xFF858BA2), fontSize = 11.sp)

                Spacer(modifier = Modifier.size(24.dp))

                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                val modelInstance = rememberModelInstance(modelLoader, "models/rigged-model.glb")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF30204F), Color(0xFF171326), Color(0xFF10121B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SceneView(
                        modifier = Modifier.fillMaxSize(),
                        engine = engine,
                        modelLoader = modelLoader,
                        cameraManipulator = rememberCameraManipulator()
                    ) {
                        modelInstance?.let { instance ->
                            ModelNode(
                                modelInstance = instance,
                                scaleToUnits = 1.75f,
                                autoAnimate = false,
                                animationName = currentAnimation,
                                animationLoop = true
                            )
                        }
                    }
                    if (modelInstance == null) {
                        Text(
                            "در حال بارگذاری جنی سه‌بعدی...",
                            color = Color(0xFFE9D5FF),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Text("جنی", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(status, color = Color(0xFFB8BBD0), fontSize = 12.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.size(12.dp))
                Text("حالت و حرکت جنی", color = Color(0xFFE9D5FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { currentAnimation = "Idle"; status = "جنی در حالت عادی است." },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentAnimation == "Idle") Color(0xFF7657E8) else Color(0xFF343A50))
                    ) { Text("عادی") }
                    Button(
                        onClick = { currentAnimation = "Wave"; status = "جنی برایت دست تکان می‌دهد!" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentAnimation == "Wave") Color(0xFF7657E8) else Color(0xFF343A50))
                    ) { Text("سلام 👋") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { currentAnimation = "Walk"; status = "جنی در حال راه رفتن است." },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentAnimation == "Walk") Color(0xFF7657E8) else Color(0xFF343A50))
                    ) { Text("راه رفتن") }
                    Button(
                        onClick = { currentAnimation = "Run"; status = "جنی در حال دویدن است." },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentAnimation == "Run") Color(0xFF7657E8) else Color(0xFF343A50))
                    ) { Text("دویدن") }
                }

                Spacer(modifier = Modifier.size(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = conversation,
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        color = Color.White,
                        textAlign = TextAlign.Right,
                        fontSize = 16.sp,
                        lineHeight = 27.sp
                    )
                }

                Spacer(modifier = Modifier.size(18.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("پیامت را بنویس یا صحبت کن...", color = Color(0xFF9DA3B8)) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { sendMessage() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7657E8))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "ارسال")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ارسال")
                    }

                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                activity, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                status = "در حال شنیدن..."
                                activity.startPersianListening(
                                    onResult = { spoken ->
                                        message = spoken
                                        status = "صدایت به متن تبدیل شد؛ برای دریافت پاسخ ارسال را بزن."
                                    },
                                    onError = { error -> status = error }
                                )
                            } else {
                                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "میکروفون")
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        activity.stopPersianSpeaking()
                        status = "پخش صدا متوقف شد."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50))
                ) {
                    Text("توقف صدای جنی")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "نسخه ۰٫۳ • پاسخ‌گویی فعلاً آفلاین و آزمایشی است",
                    color = Color(0xFF858BA2),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun offlineDemoReply(input: String): String {
    val text = input.lowercase(Locale("fa", "IR"))
    return when {
        text.contains("سلام") || text.contains("درود") ->
            "سلام! خوشحالم با من صحبت می‌کنی. هنوز در نسخه‌ی آزمایشی هستم."
        text.contains("اسمت") || text.contains("کی هستی") ->
            "من جنی هستم، دستیار فارسی تو. مغز هوشمند آنلاینم در مرحله‌ی بعد متصل می‌شود."
        text.contains("خوبی") || text.contains("حالت") ->
            "ممنون که پرسیدی! من آماده‌ام به تو کمک کنم."
        text.contains("خداحافظ") || text.contains("فعلا") ->
            "خداحافظ! هر وقت برگشتی من اینجا هستم."
        else ->
            "پیامت را دریافت کردم. فعلاً پاسخ‌های من آزمایشی و محدود هستند؛ اتصال به هوش مصنوعی اصلی در مرحله‌ی بعد انجام می‌شود."
    }
}
