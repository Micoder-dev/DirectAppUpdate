package com.micoder.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

                /**
                 * Initialize DirectAppUpdate with the following parameters to enable the library in your app.
                 */
                val configUrl = "https://cloud-multiapp-default-rtdb.firebaseio.com/ctn-iptv.json"
                DirectAppUpdate(activity = this@MainActivity, configUrl = configUrl, appIcon = R.mipmap.ic_launcher)

            }
        }
    }
}