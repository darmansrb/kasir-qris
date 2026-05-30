package com.moluccasdev.poskasirqris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moluccasdev.poskasirqris.ui.POSKasirApp
import com.moluccasdev.poskasirqris.ui.theme.POSKasirQRISTheme

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POSKasirQRISTheme {
                POSKasirApp()
            }
        }
    }
}