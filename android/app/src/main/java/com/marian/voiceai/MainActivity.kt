package com.marian.voiceai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MarianVoiceApp() }
    }
}

@Composable
private fun MarianVoiceApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var status by remember { mutableStateOf("Ready when you are") }
    var isConnecting by remember { mutableStateOf(false) }
    var sessionMessage by remember { mutableStateOf("Your conversations will appear here.") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        status = if (granted) "Microphone permission granted" else "Microphone permission is required"
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF080D1A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MARIAN", color = Color(0xFF9FB6FF), fontSize = 13.sp, letterSpacing = 4.sp)
                    Text(
                        "Voice AI",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(status, color = Color(0xFFABB6CC), fontSize = 14.sp)
                }

                VoiceOrb(active = isConnecting)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isConnecting) "Preparing your voice session" else "Tap to start talking",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        sessionMessage,
                        color = Color(0xFF8793AA),
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (!hasMicPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                scope.launch {
                                    isConnecting = true
                                    status = "Requesting secure session"
                                    val result = RealtimeSessionClient.createSession()
                                    isConnecting = false
                                    status = result.fold(
                                        onSuccess = { "Secure session credential received" },
                                        onFailure = { "Could not connect: ${it.message ?: "unknown error"}" }
                                    )
                                    sessionMessage = result.fold(
                                        onSuccess = "Voice transport integration is the next build milestone.",
                                        onFailure = "Check the backend URL and server logs."
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF536DFF)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.Mic, contentDescription = null)
                        Text("  Start voice session", modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                Text(
                    "AI assistant • Your data stays under your control",
                    color = Color(0xFF65718A),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun VoiceOrb(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (active) 1.10f else 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(230.dp)
            .scale(pulse)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF526DFF), Color(0xFF273C91), Color(0xFF111A3B))
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF9BAAFF), Color(0xFF4B65D8))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Light)
        }
    }
}
