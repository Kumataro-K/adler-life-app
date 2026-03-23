package com.forestmood.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestmood.app.data.local.AdlerDatabase
import com.forestmood.app.data.local.AppPreferences
import com.forestmood.app.data.repository.AdlerRepository
import com.forestmood.app.domain.HybridAiCoach
import com.forestmood.app.ui.screens.AdlerLifeApp
import com.forestmood.app.ui.theme.ForestMoodTheme
import com.forestmood.app.util.InterstitialAdManager
import com.forestmood.app.viewmodel.AdlerViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    lateinit var interstitialAdManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        interstitialAdManager = InterstitialAdManager(this)
        interstitialAdManager.loadAd()

        val repository = AdlerRepository(
            dao = AdlerDatabase.getInstance(applicationContext).adlerDao(),
            aiCoach = HybridAiCoach()
        )
        val preferences = AppPreferences(this)

        setContent {
            ForestMoodTheme {
                val viewModel = viewModel<AdlerViewModel>(factory = AdlerViewModel.Factory(repository))
                AdlerLifeApp(
                    viewModel = viewModel,
                    showDisclaimerInitially = !preferences.hasAcceptedDisclaimer(),
                    onDisclaimerAccepted = preferences::setDisclaimerAccepted,
                    onTraceSaved = { interstitialAdManager.onRecordSaved(this) }
                )
            }
        }
    }
}
