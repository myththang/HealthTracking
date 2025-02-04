package com.fpt.edu.healthtracking.util

import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun fromOneBased(year: Int, month: Int, day: Int): CalendarDay {
    return CalendarDay.from(year, month - 1, day)
}

object DateUtils {
    private val vietnameseLocale = Locale("vi", "VN")

    fun Calendar.formatVietnameseDate(format: DateFormat = DateFormat.FULL): String {
        return when {
            isToday() -> "Hôm nay"
            isYesterday() -> "Hôm qua"
            isTomorrow() -> "Ngày mai"
            else -> when(format) {
                DateFormat.FULL -> {
                    val formatter = SimpleDateFormat("'Ngày' dd 'Tháng' MM 'Năm' yyyy", vietnameseLocale)
                    formatter.format(time)
                }
                DateFormat.MEDIUM -> {
                    val formatter = SimpleDateFormat("dd 'Tháng' MM, yyyy", vietnameseLocale)
                    formatter.format(time)
                }
                DateFormat.SHORT -> {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", vietnameseLocale)
                    formatter.format(time)
                }
            }
        }
    }

    enum class DateFormat {
        FULL,
        MEDIUM,
        SHORT
    }

    fun Calendar.isToday(): Boolean {
        val today = Calendar.getInstance()
        return this[Calendar.YEAR] == today[Calendar.YEAR] &&
                this[Calendar.MONTH] == today[Calendar.MONTH] &&
                this[Calendar.DAY_OF_MONTH] == today[Calendar.DAY_OF_MONTH]
    }

    fun Calendar.isYesterday(): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        return this[Calendar.YEAR] == yesterday[Calendar.YEAR] &&
                this[Calendar.MONTH] == yesterday[Calendar.MONTH] &&
                this[Calendar.DAY_OF_MONTH] == yesterday[Calendar.DAY_OF_MONTH]
    }

    fun Calendar.isTomorrow(): Boolean {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        return this[Calendar.YEAR] == tomorrow[Calendar.YEAR] &&
                this[Calendar.MONTH] == tomorrow[Calendar.MONTH] &&
                this[Calendar.DAY_OF_MONTH] == tomorrow[Calendar.DAY_OF_MONTH]
    }
    fun reformatSqlDateAndCalculateAge(dob: String): Pair<String, Int> {
        // Define the input format for SQL DATETIME (e.g., "yyyy-MM-dd HH:mm:ss")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // Define the desired output format (e.g., "dd/MM/yyyy")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return try {
            // Parse the input date string
            val date = inputFormat.parse(dob)
            if (date != null) {
                // Reformat the date to "dd/MM/yyyy"
                val formattedDate = outputFormat.format(date)

                // Calculate age
                val currentDate = Calendar.getInstance()
                val dobCalendar = Calendar.getInstance().apply { time = date }

                var age = currentDate.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

                // If the current date is before the birth date in this year, subtract one year
                if (currentDate.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }

                Pair(formattedDate, age) // Return both formatted date and age
            } else {
                Pair(dob, -1) // Return the original value if parsing fails and age as -1
            }
        } catch (e: Exception) {
            Pair(dob, -1) // Return the original value and age as -1 in case of error
        }
    }
    fun formatDate(date: Date?): String {
        val outputDateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // Desired format
        return date?.let {
            outputDateFormat.format(it)
        } ?: "" // Return empty string if date is null
    }
     fun isValidDate(day: Int, month: Int, year: Int): Boolean {
        if (month < 1 || month > 12) return false
        if (year < 1900 || year > 2100) return false

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return day in 1..maxDays
    }

     fun isValidYear(year: Int, month: Int, day: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        return year < currentYear
    }
}

