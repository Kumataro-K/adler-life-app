package com.example.adlerlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adlerlife.data.local.AdlerDatabase
import com.example.adlerlife.data.repository.AdlerRepository
import com.example.adlerlife.domain.HybridAiCoach
import com.example.adlerlife.ui.screens.AdlerLifeApp
import com.example.adlerlife.ui.theme.AdlerLifeTheme
import com.example.adlerlife.viewmodel.AdlerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AdlerRepository(
            dao = AdlerDatabase.getInstance(applicationContext).adlerDao(),
            aiCoach = HybridAiCoach()
        )

        setContent {
            AdlerLifeTheme {
                val viewModel = viewModel<AdlerViewModel>(factory = AdlerViewModel.Factory(repository))
                AdlerLifeApp(viewModel)
            }
        }
    }
}
