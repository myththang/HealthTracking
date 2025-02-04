package com.fpt.edu.healthtracking.util

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateAxisValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    override fun getFormattedValue(value: Float): String {
        val date = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, value.toInt())
        }.time
        return dateFormat.format(date)
    }
} 