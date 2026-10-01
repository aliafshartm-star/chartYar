package com.chartyar.app.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chartyar.app.MainActivity
import com.chartyar.app.R
import com.chartyar.app.data.AlertChecker
import com.chartyar.app.data.AppDatabase
import com.chartyar.app.data.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * سرویس پیش‌زمینه برای پایش لحظه‌ای قیمت‌ها
 * هر ۱۵ ثانیه قیمت‌ها رو چک می‌کنه و اگه هشدار فعال شد، نوتیفیکیشن می‌فرسته
 */
class AlertMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "alert_monitor_channel"
        private const val CHANNEL_NAME = "پایش قیمت"
        private const val CHANNEL_DESC = "پایش لحظه‌ای قیمت‌ها برای هشدارها"
        private const val NOTIFICATION_ID = 9999
        private const val CHECK_INTERVAL_MS = 15_000L // ۱۵ ثانیه

        fun start(context: Context) {
            try {
                val intent = Intent(context, AlertMonitorService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // silent
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, AlertMonitorService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                // silent
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(NOTIFICATION_ID, buildNotification())
            startMonitoring()
        } catch (e: Exception) {
            // silent
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                try {
                    val db = AppDatabase.get(applicationContext)
                    val cache = CacheManager(applicationContext)
                    AlertChecker.checkAllAlerts(applicationContext, db, cache)
                } catch (e: Exception) {
                    // silent
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC
                setShowBadge(false)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("چارت‌یار — پایش قیمت فعال")
            .setContentText("هشدارهای شما در حال پایش هستند")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}