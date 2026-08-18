package com.spcrk.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.spcrk.app.data.Repository

class VideoDownloaderApp : Application() {

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
        const val DOWNLOAD_CHANNEL_NAME = "视频下载"
        const val SCHEDULE_CHANNEL_ID = "schedule_channel"
        const val SCHEDULE_CHANNEL_NAME = "定时任务"
        private const val PREFS_NAME = "spcrk_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    val repository by lazy { Repository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createScheduleChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                DOWNLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示视频下载进度"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createScheduleChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SCHEDULE_CHANNEL_ID,
                SCHEDULE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "定时任务执行通知"
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun isDarkTheme(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(dark: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
    }
}
