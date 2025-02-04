package com.fpt.edu.healthtracking.util

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.fpt.edu.healthtracking.R
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CustomMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource), IMarker {

    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val tvWeight: TextView = findViewById(R.id.tvWeight)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun refreshContent(e: Entry, highlight: Highlight) {
        try {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = TimeUnit.DAYS.toMillis(e.x.toLong())

            tvDate.text = dateFormat.format(calendar.time)
            tvWeight.text = String.format("%.1f kg", e.y)
        } catch (e: Exception) {
            Log.e("CustomMarkerView", "Error formatting date", e)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}