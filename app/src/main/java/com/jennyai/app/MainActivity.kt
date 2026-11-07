package com.jennyai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JennyApp() }
    }
}

@Composable
private fun JennyApp() {
    var message by remember { mutableStateOf("") }
    var conversation by remember {
        mutableStateOf("سلام! من جنی هستم. نسخه‌ی اولیه‌ی من آماده شده و به‌زودی می‌توانیم با هم صحبت کنیم.")
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF10121B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF191D30), Color(0xFF10121B))
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Jenny AI",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "دستیار هوشمند فارسی",
                    color = Color(0xFFB8BBD0),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.size(28.dp))

                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J",
                        color = Color.White,
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))
                Text(
                    text = "جنی",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "آماده‌ام با تو صحبت کنم",
                    color = Color(0xFFB8BBD0),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.size(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = conversation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        color = Color.White,
                        textAlign = TextAlign.Right,
                        fontSize = 16.sp,
                        lineHeight = 27.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("پیامت را بنویس...", color = Color(0xFF9DA3B8))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                conversation = "تو: $message\n\nجنی: بخش اتصال هوش مصنوعی در مرحله‌ی بعد فعال می‌شود."
                                message = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7657E8)
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "ارسال")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ارسال")
                    }

                    Button(
                        onClick = {
                            conversation = "قابلیت دریافت صدای فارسی در مرحله‌ی بعد فعال می‌شود."
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF343A50)
                        )
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "میکروفون")
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "نسخه‌ی آزمایشی ۰.۱ • امکانات صوتی و هوشمند در حال توسعه",
                    color = Color(0xFF858BA2),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
