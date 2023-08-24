package com.scrollz.golfai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.scrollz.golfai.presentation.navigation.Navigation
import com.scrollz.golfai.ui.theme.GolfAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GolfAITheme {
                Navigation()
            }
        }
    }
}
