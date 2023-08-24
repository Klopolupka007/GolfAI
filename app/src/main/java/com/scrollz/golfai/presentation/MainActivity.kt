package com.scrollz.golfai.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.scrollz.golfai.presentation.navigation.Navigation
import com.scrollz.golfai.ui.theme.GolfAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
