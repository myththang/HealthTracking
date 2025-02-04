package com.fpt.edu.healthtracking.util.noti.test

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MealReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val mealType = inputData.getString("meal_type")?.let {
            MealType.valueOf(it)
        } ?: return Result.failure()

        notificationHelper.showMealReminder(mealType)
        return Result.success()
    }

    companion object {
        fun scheduleMealReminders(context: Context) {
            // Schedule all meal reminders
            MealType.values().forEach { mealType ->
                scheduleSingleMealReminder(context, mealType)
            }
        }

        private fun scheduleSingleMealReminder(context: Context, mealType: MealType) {
            val currentDate = Calendar.getInstance()
            val scheduledTime = Calendar.getInstance()

            // Set exact time for the meal
            scheduledTime.set(Calendar.HOUR_OF_DAY, mealType.hour)
            scheduledTime.set(Calendar.MINUTE, 0) // Assuming MealType has a minute field
            scheduledTime.set(Calendar.SECOND, 0)
            scheduledTime.set(Calendar.MILLISECOND, 0)

            // Calculate initial delay
            var timeDiff = scheduledTime.timeInMillis - currentDate.timeInMillis

            // If the time has already passed today, calculate for next occurrence
            if (timeDiff < 0) {
                // Instead of adding 24 hours, set it to the next day at exact meal time
                scheduledTime.add(Calendar.DAY_OF_MONTH, 1)
                timeDiff = scheduledTime.timeInMillis - currentDate.timeInMillis
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            val inputData = workDataOf("meal_type" to mealType.name)

            val dailyWorkRequest = PeriodicWorkRequestBuilder<MealReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("meal_reminder")
                .addTag(mealType.name.lowercase())
                // Add precise timing request
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "meal_reminder_${mealType.name.lowercase()}",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    dailyWorkRequest
                )
        }
        fun cancelAllMealReminders(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("meal_reminder")
        }

        fun cancelSpecificMealReminder(context: Context, mealType: MealType) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(mealType.name.lowercase())
        }
    }
}


