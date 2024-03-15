package com.taewooyo.bolt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.taewooyo.bolt.ui.theme.BoltTheme
import com.taewooyo.okhttp.OkHttpWebSocketClient
import com.taewooyo.stomp.core.StompClient
import com.taewooyo.stomp.core.StompSession
import com.taewooyo.stomp.core.env.StompConfig
import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.header.StompSendHeaders
import com.taewooyo.stomp.core.frame.header.StompSubscribeHeaders
import com.taewooyo.stomp.core.heartbeats.HeartBeat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.io.bytestring.decodeToString
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      BoltTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Greeting("Android")
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  BoltTheme {
    Greeting("Android")
  }
}