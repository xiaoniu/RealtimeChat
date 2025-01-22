package com.example.realtimechat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.realtimechat.ui.theme.RealtimeChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VoiceChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 请求权限
        requestPermission()

        setContent {
            DisposableEffect(Unit) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            RealtimeChatTheme {
                val isCalling by viewModel.isCalling.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "实时对话式 AI",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 通话按钮
                        CallButton(
                            isCalling = isCalling,
                            onCallStateChange = { calling ->
                                if (calling) {
                                    viewModel.startCall()
                                } else {
                                    viewModel.endCall()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO
            )

            if (permissions.any { permission ->
                    ContextCompat.checkSelfPermission(this, permission) !=
                            PackageManager.PERMISSION_GRANTED
                }) {
                requestPermissions(permissions, 22)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.endCall()
    }
}

@Composable
fun CallButton(
    isCalling: Boolean,
    onCallStateChange: (Boolean) -> Unit
) {
    val icon = if (isCalling) Icons.Default.Close else Icons.Default.Call
    val iconTint = if (isCalling) Color.Red else Color.Green

    IconButton(
        onClick = { onCallStateChange(!isCalling) },
        modifier = Modifier
            .size(98.dp)
            .background(iconTint, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isCalling) "End Call" else "Make Call",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CallButtonPreview() {
    RealtimeChatTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Doubao-pro-32k",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 通话按钮
                CallButton(
                    isCalling = false,
                    onCallStateChange = {}
                )
            }
        }
    }
}