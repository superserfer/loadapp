package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        notificationManager.cancelAll()

        val isSuccess = intent.getBooleanExtra("isSuccess", true)
        findViewById<TextView>(R.id.textFileName).text = intent.getStringExtra("nameId")
        val status = findViewById<TextView>(R.id.textStatus)

        if (isSuccess) {
            status.text = getString(R.string.success)
        } else {
            status.text = getString(R.string.fail)
            status.setTextColor(resources.getColor(R.color.fail))
        }

        findViewById<Button>(R.id.OkButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}
