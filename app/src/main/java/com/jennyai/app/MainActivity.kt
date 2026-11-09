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
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

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
            ttsReady = result != null &&
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            if (ttsReady) {
                tts?.setSpeechRate(0.92f)
                tts?.setPitch(1.0f)
            }
        }
    }

    fun speakPersian(text: String): Boolean {
        if (!ttsReady) return false
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jenny_reply")
        return true
    }

    fun isPersianVoiceReady(): Boolean = ttsReady

    fun installPersianVoice() {
        try {
            startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
        } catch (_: Exception) {
            statusMessageForVoiceInstall()
        }
    }

    private fun statusMessageForVoiceInstall() {
        try {
            startActivity(Intent("com.android.settings.TTS_SETTINGS"))
        } catch (_: Exception) {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }
    }

    fun stopPersianSpeaking() { tts?.stop() }

    fun generateFalVideo(prompt: String, imageUrl: String, onResult: (String?, String?) -> Unit) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("https://jenny-ai.javademad83.workers.dev/video/generate").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 20000
                    readTimeout = 45000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                val payload = JSONObject()
                    .put("prompt", prompt)
                    .put("duration", 5)
                    .put("resolution", "480P")
                    .put("aspect_ratio", "16:9")
                if (imageUrl.isNotBlank()) payload.put("image_url", imageUrl.trim())
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val json = JSONObject(stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty().ifBlank { "{}" })
                if (code !in 200..299) throw IllegalStateException(json.optString("error", "خطای سرور ($code)"))
                val requestId = json.optString("request_id").trim()
                if (requestId.isBlank()) throw IllegalStateException("شناسه درخواست از سرور دریافت نشد.")
                activityRunOnUiThread(onResult, requestId, null)
            } catch (error: Exception) {
                activityRunOnUiThread(onResult, null, error.localizedMessage ?: "ارسال درخواست ویدیو ناموفق بود.")
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun checkFalVideo(requestId: String, onResult: (String, String?, String?) -> Unit) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("https://jenny-ai.javademad83.workers.dev/video/status?request_id=$requestId").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20000
                    readTimeout = 45000
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val json = JSONObject(stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty().ifBlank { "{}" })
                if (code !in 200..299) throw IllegalStateException(json.optString("error", "خطای سرور ($code)"))
                val state = json.optString("status", "IN_PROGRESS")
                val videoUrl = json.optString("video_url").takeIf { it.startsWith("https://") }
                runOnUiThread { onResult(state, videoUrl, null) }
            } catch (error: Exception) {
                activity.runOnUiThread { onResult("ERROR", null, error.localizedMessage ?: "بررسی وضعیت ناموفق بود.") }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun activityRunOnUiThread(callback: (String?, String?) -> Unit, value: String?, error: String?) {
        runOnUiThread { callback(value, error) }
    }

    fun openGeneratedVideo(videoUrl: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(videoUrl)))
        } catch (_: Exception) { }
    }

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
            // Prefer the installed speech service's normal mode; Persian offline
            // recognition is not available on every Android device.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
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
                        "صدایی تشخیص داده نشد؛ نزدیک میکروفون صحبت کن و دوباره تلاش کن."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "مجوز میکروفون داده نشده است؛ دسترسی میکروفون را بررسی کن."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "اتصال اینترنت یا سرویس تشخیص گفتار Google را بررسی کن."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        "سرویس تشخیص گفتار مشغول است؛ چند لحظه صبر کن و دوباره بزن."
                    SpeechRecognizer.ERROR_SERVER, SpeechRecognizer.ERROR_SERVER_DISCONNECTED ->
                        "سرویس تشخیص گفتار پاسخ نمی‌دهد؛ سرویس Google را بررسی کن."
                    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS ->
                        "درخواست‌های زیادی فرستاده شده؛ کمی صبر کن و دوباره تلاش کن."
                    else -> "تشخیص صدا انجام نشد (کد خطا: $error). اتصال اینترنت و سرویس گفتار را بررسی کن."
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

        try {
            recognizer.startListening(intent)
        } catch (exception: Exception) {
            onError("شروع تشخیص گفتار ممکن نشد: ${exception.localizedMessage ?: "خطای نامشخص"}")
        }
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
    var showVideoStudio by remember { mutableStateOf(false) }
    var videoPrompt by remember { mutableStateOf("") }
    var videoImageUrl by remember { mutableStateOf("") }
    var videoJobId by remember { mutableStateOf("") }
    var videoJobStatus by remember { mutableStateOf("") }
    var generatedVideoUrl by remember { mutableStateOf("") }
    var videoBusy by remember { mutableStateOf(false) }
    val avatarBitmap = remember {
        activity.assets.open("jenny_avatar.jpg").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
    }

    fun sendMessage(voiceOrTypedMessage: String = message.trim()) {
        val userMessage = voiceOrTypedMessage.trim()
        if (userMessage.isBlank()) return
        val previousConversation = conversation.takeLast(5000)
        message = ""
        status = "در حال دریافت پاسخ از هوش مصنوعی جنی..."
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("https://jenny-ai.javademad83.workers.dev").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 20000
                    readTimeout = 45000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                val payload = JSONObject().put("message", userMessage)
                    .put("conversation", previousConversation).toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json = JSONObject(responseText.ifBlank { "{}" })
                if (code !in 200..299) {
                    throw IllegalStateException(json.optString("error", "خطای سرور ($code)"))
                }
                val reply = json.optString("reply").trim()
                if (reply.isBlank()) throw IllegalStateException("پاسخ خالی از سرور دریافت شد.")
                activity.runOnUiThread {
                    conversation = if (conversation.isBlank()) {
                        "تو: $userMessage\\n\\nجنی: $reply"
                    } else {
                        "$conversation\\n\\n────────────\\n\\nتو: $userMessage\\n\\nجنی: $reply"
                    }
                    chatPreferences.edit().putString("conversation", conversation).apply()
                    status = if (activity.speakPersian(reply)) {
                        "جنی پاسخ داد و صدا در حال پخش است."
                    } else {
                        "پاسخ جنی دریافت شد؛ برای شنیدن آن صدای فارسی گوشی را نصب کن."
                    }
                }
            } catch (error: Exception) {
                // Keep chat usable when the network or AI service is unavailable.
                val offlineReply = offlineDemoReply(userMessage)
                activity.runOnUiThread {
                    conversation = if (conversation.isBlank()) {
                        "تو: $userMessage\\n\\nجنی (آفلاین): $offlineReply"
                    } else {
                        "$conversation\\n\\n────────────\\n\\nتو: $userMessage\\n\\nجنی (آفلاین): $offlineReply"
                    }
                    chatPreferences.edit().putString("conversation", conversation).apply()
                    status = if (activity.speakPersian(offlineReply)) {
                        "اتصال برقرار نشد؛ جنی با پاسخ ساده آفلاین جواب داد."
                    } else {
                        "اتصال برقرار نشد؛ پاسخ ساده آفلاین نمایش داده شد."
                    }
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    val microphonePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            status = "در حال شنیدن..."
            activity.startPersianListening(
                onResult = { spoken ->
                    message = spoken
                    sendMessage(spoken)
                },
                onError = { error -> status = error }
            )
        } else {
            status = "برای صحبت با جنی باید مجوز میکروفون را فعال کنی."
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF10111B)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF20243B), Color(0xFF11131F), Color(0xFF0D0F18))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Compact app bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("☰", color = Color(0xFFCFD2E7), fontSize = 25.sp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jenny AI", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("دستیار هوشمند فارسی", color = Color(0xFF9FA5C2), fontSize = 11.sp)
                    }
                    Text("⋮", color = Color(0xFFCFD2E7), fontSize = 25.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar stage: smaller, rounded, and visually separated from chat
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF29243D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Jenny3DViewer(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            color = Color(0xCC20243B),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6EE7A8))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جنی", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("جنی", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    status,
                    color = Color(0xFFB6BAD0),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                if (!activity.isPersianVoiceReady()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { activity.installPersianVoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7657E8))
                    ) { Text("نصب صدای فارسی") }
                }


                Button(
                    onClick = { showVideoStudio = !showVideoStudio },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50)),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(if (showVideoStudio) "بستن استودیو ویدیو" else "🎬 استودیو ساخت ویدیو با Fal") }

                if (showVideoStudio) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF202437)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            OutlinedTextField(
                                value = videoPrompt,
                                onValueChange = { videoPrompt = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("توضیح ویدیو") },
                                placeholder = { Text("مثلاً حرکت آرام دوربین در یک شهر آینده‌نگر") },
                                maxLines = 3,
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, textAlign = TextAlign.Right)
                            )
                            OutlinedTextField(
                                value = videoImageUrl,
                                onValueChange = { videoImageUrl = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("لینک تصویر مرجع (اختیاری)") },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, textAlign = TextAlign.Left)
                            )
                            Text(
                                videoJobStatus.ifBlank { "۵ ثانیه • کیفیت آزمایشی 480P • تولید ابری با هزینه احتمالی" },
                                color = Color(0xFFB6BAD0),
                                fontSize = 11.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = !videoBusy && videoPrompt.isNotBlank(),
                                    onClick = {
                                        videoBusy = true
                                        videoJobStatus = "در حال ارسال درخواست به Fal..."
                                        activity.generateFalVideo(videoPrompt, videoImageUrl) { requestId, error ->
                                            videoBusy = false
                                            if (requestId != null) {
                                                videoJobId = requestId
                                                generatedVideoUrl = ""
                                                videoJobStatus = "در صف تولید قرار گرفت. شناسه: $requestId"
                                            } else {
                                                videoJobStatus = error ?: "خطای نامشخص"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7657E8))
                                ) { Text(if (videoBusy) "در حال ارسال..." else "ساخت ویدیو") }
                                Button(
                                    enabled = !videoBusy && videoJobId.isNotBlank(),
                                    onClick = {
                                        videoBusy = true
                                        videoJobStatus = "در حال بررسی وضعیت..."
                                        activity.checkFalVideo(videoJobId) { state, url, error ->
                                            videoBusy = false
                                            if (error != null) videoJobStatus = error
                                            else if (url != null) {
                                                generatedVideoUrl = url
                                                videoJobStatus = "ویدیو آماده است."
                                            } else videoJobStatus = "وضعیت تولید: $state"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50))
                                ) { Text("بررسی") }
                            }
                            if (generatedVideoUrl.isNotBlank()) {
                                Button(
                                    onClick = { activity.openGeneratedVideo(generatedVideoUrl) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF287A62))
                                ) { Text("پخش ویدیو") }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Conversation panel uses the available screen space instead of a giant message card.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF202437)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(chatScrollState)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "گفت‌وگو",
                            color = Color(0xFFB8A5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = conversation.replace("\\\\n", "\n"),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            fontSize = 14.sp,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("پیامت را برای جنی بنویس…", color = Color(0xFF9DA3B8)) },
                    singleLine = false,
                    maxLines = 2,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, textAlign = TextAlign.Right),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { sendMessage() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7657E8)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "ارسال")
                        Spacer(modifier = Modifier.width(7.dp))
                        Text("ارسال پیام")
                    }
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                activity, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                status = "در حال شنیدن…"
                                activity.startPersianListening(
                                    onResult = { spoken ->
                                        message = spoken
                                        sendMessage(spoken)
                                    },
                                    onError = { error -> status = error }
                                )
                            } else {
                                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "میکروفون")
                    }
                    Button(
                        onClick = {
                            activity.stopPersianSpeaking()
                            status = "پخش صدا متوقف شد."
                        },
                        modifier = Modifier.size(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343A50)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("■", color = Color.White, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "نسخه آزمایشی • گفت‌وگوی فارسی",
                    color = Color(0xFF777E9A),
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun offlineDemoReply(input: String): String {
    val text = input.lowercase(Locale("fa", "IR"))
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .trim()

    return when {
        text.contains("سلام") || text.contains("درود") || text.contains("صبح بخیر") ->
            "سلام! خوش اومدی. من جنی هستم؛ آماده‌ام باهات گپ بزنم."
        text.contains("اسمت") || text.contains("کی هستی") ->
            "اسم من جنیه. من یک دستیار فارسی هستم که فعلاً در حالت آزمایشی و آفلاین کار می‌کنم."
        text.contains("خوبی") || text.contains("حالت چطوره") || text.contains("چه خبر") ->
            "ممنون که پرسیدی! من آماده‌ام. تو چطوری؟"
        text.contains("ممنون") || text.contains("مرسی") || text.contains("متشکرم") ->
            "خواهش می‌کنم! هر وقت کمکی خواستی بگو."
        text.contains("خداحافظ") || text.contains("فعلا") || text.contains("فعلاً") ->
            "فعلاً! امیدوارم دوباره با هم صحبت کنیم."
        text.contains("دوست") && (text.contains("دارم") || text.contains("داری")) ->
            "من اینجام تا باهات صحبت کنم و کمکت کنم. دوست داری درباره چی حرف بزنیم؟"
        text.contains("ساعت") ->
            "برای دیدن ساعت دقیق، ساعت گوشی‌ات رو بررسی کن. در نسخه‌های بعدی می‌تونم قابلیت‌های بیشتری داشته باشم."
        text.contains("کمک") || text.contains("راهنمایی") ->
            "حتماً. می‌تونم در حد قابلیت‌های فعلی‌ام راهنمایی‌ات کنم. دقیقاً چه کاری می‌خوای انجام بدی؟"
        text.contains("جوک") || text.contains("لطیفه") ->
            "چرا کامپیوتر سردش شد؟ چون پنجره‌هاش باز بود!"
        text.contains("فارسی") || text.contains("زبان") ->
            "من برای گفت‌وگوی فارسی طراحی شدم. تشخیص صدا و پخش صدا به پشتیبانی زبان فارسی در سرویس‌های گوشی بستگی داره."
        text.contains("هوش مصنوعی") || text.contains("آنلاین") ->
            "در حال حاضر مغز هوش مصنوعی آنلاین به من متصل نیست و پاسخ‌هایم از چند قانون ساده آفلاین ساخته می‌شوند."
        else ->
            "پیامت رو دریافت کردم: «$input». هنوز مدل زبانی کامل به من متصل نشده؛ می‌تونم به چند موضوع ساده پاسخ بدم. کمی واضح‌تر بگو تا بهتر راهنمایی‌ات کنم."
    }
}
