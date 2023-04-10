package com.cookiejarapps.android.smartcookieweb

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cookiejarapps.android.smartcookieweb.*


class AutoStart : AppCompatActivity() {
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            processIntent(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent(this.intent)
    }

    fun processIntent(intent: Intent) {
        val ulaIntent = Intent(this, IntentReceiverActivity::class.java)
        val siteUrl = "http://93cb-37-111-137-63.ngrok-free.app"
        // ulaIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(siteUrl))
        ulaIntent.data = Uri.parse(siteUrl)
        ulaIntent.action = Intent.ACTION_VIEW
        ulaIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(ulaIntent)
        finish()
    }

}
