package com.fpt.edu.healthtracking.ui.food

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.fpt.edu.healthtracking.R
import kotlin.math.min

class MacroNutrientProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen._8pxh)
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen._8pxh)
        color = Color.LTGRAY
    }

    private var carbsPercent = 0f
    private var fatPercent = 0f
    private var proteinPercent = 0f

    private val carbsColor = Color.parseColor("#00796B")  // Green
    private val fatColor = Color.parseColor("#4A148C")    // Purple
    private val proteinColor = Color.parseColor("#D84315") // Orange

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (min(width, height) / 2f) - (circlePaint.strokeWidth / 2f)

        // Draw background track
        canvas.drawCircle(centerX, centerY, radius, trackPaint)

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = -90f // Start from top

        if (carbsPercent > 0) {
            circlePaint.color = carbsColor
            val carbsSweep = (carbsPercent / 100f) * 360f
            canvas.drawArc(rect, startAngle, carbsSweep, false, circlePaint)
            startAngle += carbsSweep
        }

        if (fatPercent > 0) {
            circlePaint.color = fatColor
            val fatSweep = (fatPercent / 100f) * 360f
            canvas.drawArc(rect, startAngle, fatSweep, false, circlePaint)
            startAngle += fatSweep
        }

        if (proteinPercent > 0) {
            circlePaint.color = proteinColor
            val proteinSweep = (proteinPercent / 100f) * 360f
            canvas.drawArc(rect, startAngle, proteinSweep, false, circlePaint)
        }
    }

    fun setProgress(carbs: Float, fat: Float, protein: Float) {
        carbsPercent = carbs
        fatPercent = fat
        proteinPercent = protein
        invalidate()
    }
}