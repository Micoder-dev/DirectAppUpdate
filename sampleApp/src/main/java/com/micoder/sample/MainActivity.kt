package com.micoder.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.micoder.directappupdate.DirectAppUpdate
import com.micoder.sample.ui.theme.DirectAppUpdateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DirectAppUpdateTheme {

                Box(modifier = Modifier.fillMaxSize().background(Color.Black))

                /**
                 * Initialize DirectAppUpdate with the following parameters to enable the library in your app.
                 */
                val configUrl = "https://castle-e6369-default-rtdb.firebaseio.com/update.json"
                DirectAppUpdate(activity = this@MainActivity, configUrl = configUrl, appIcon = R.mipmap.ic_launcher)

            }
        }
    }
}