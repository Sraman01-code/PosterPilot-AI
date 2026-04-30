package com.opengraphlabs.posterpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.opengraphlabs.posterpilot.core.theme.PosterPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PosterPilotTheme {
                PosterPilotApp()
            }
        }
    }
}
