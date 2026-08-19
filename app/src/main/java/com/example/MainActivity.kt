package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.presentation.navigation.MediBridgeNavHost
import com.example.presentation.screens.splash.MediBridgeSplashScreen
import com.example.ui.theme.MediBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediBridgeTheme {
                // Splash runs exclusively during application startup and does not re-trigger on navigation
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 400),
                    label = "SplashCrossfade"
                ) { isSplash ->
                    if (isSplash) {
                        MediBridgeSplashScreen(
                            onSplashFinished = {
                                showSplash = false
                            }
                        )
                    } else {
                        MediBridgeNavHost()
                    }
                }
            }
        }
    }
}
