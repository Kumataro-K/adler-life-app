package com.forestmood.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchMain = Runnable {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            launchMain.run()
        } else {
            Handler(Looper.getMainLooper()).postDelayed(launchMain, 700L)
        }
    }
}
