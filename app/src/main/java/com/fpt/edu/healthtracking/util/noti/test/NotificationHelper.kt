package com.fpt.edu.healthtracking.util.noti.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.provider.Settings
import android.net.Uri
import com.fpt.edu.healthtracking.ui.home.HomePageActivity

enum class MealType(val hour: Int, val title: String, val message: String) {
    BREAKFAST(7, "Giờ ăn sáng!", "Đến lúc ăn sáng lành mạnh để bắt đầu ngày mới!"),
    LUNCH(12, "Giờ nghỉ trưa!", "Hãy dành chút thời gian để thưởng thức bữa trưa và nạp năng lượng."),
    DINNER(19, "Giờ ăn tối!", "Đến lúc cho bữa tối của bạn - đừng bỏ bữa!")
}

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "meal_reminders"
        private const val CHANNEL_NAME = "Meal Reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily meal reminders"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMealReminder(mealType: MealType) {
        val intent = Intent(context, HomePageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("meal_type", mealType.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            mealType.ordinal,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(mealType.title)
            .setContentText(mealType.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(mealType.ordinal, notification)
    }
}


