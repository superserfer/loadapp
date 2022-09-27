package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var notificationId: Int = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var customButton: LoadingButton
    private lateinit var radioGroup: RadioGroup
    private var selectedFile = File.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        customButton = findViewById(R.id.custom_button)
        radioGroup = findViewById(R.id.fileRadioGroup)
        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager


        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        customButton.setOnClickListener {
            when(radioGroup.checkedRadioButtonId) {
                R.id.glideRadioButton -> {
                    selectedFile = File.GLIDE
                }
                R.id.loadAppRadioButton -> {
                    selectedFile = File.LOAD_APP
                }
                R.id.retrofitRadioButton -> {
                    selectedFile = File.RETROFIT
                }
                else -> {
                    selectedFile = File.NONE
                    Toast.makeText(this, "No File selected", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            checkPermission()
        }

        createChannel(CHANNEL_ID, "Download")
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            download()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PermissionInfo.PROTECTION_DANGEROUS)
            customButton.buttonState = ButtonState.Completed
        }
    }

    private fun download() {
        customButton.buttonState = ButtonState.Loading
        val request =
            DownloadManager.Request(Uri.parse(selectedFile.url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, selectedFile.fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToFirst()) {
            when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                DownloadManager.STATUS_FAILED -> {
                    sendNotification(false)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    customButton.buttonState = ButtonState.Completed
                    sendNotification(true)
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                customButton.buttonState = ButtonState.Completed
                sendNotification(true)
            }
        }

    }

    private fun sendNotification(isSuccess: Boolean) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra("nameId", getString(selectedFile.titleId))
        contentIntent.putExtra("isSuccess", isSuccess)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_assistant_black_24dp, "Check status", pendingIntent)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "File Downloaded Notification"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        enum class File(val titleId: Int, val url: String, val fileName: String) {
            GLIDE(R.string.glide_text, "https://github.com/bumptech/glide/archive/master.zip", "glide.zip"),
            LOAD_APP(R.string.load_app_text, "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip", "load-app.zip"),
            RETROFIT(R.string.retrofit_text, "https://github.com/square/retrofit/archive/master.zip", "retrofit.zip"),
            NONE(0,"","")
        }
    }
}
