package com.fpt.edu.healthtracking.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fpt.edu.healthtracking.R;

public class CustomCircularProgressBar extends View {

    private Paint paintBreakfast, paintSnack, paintLunch, paintDinner, paintBackground;
    private float breakfastPercentage = 0.2f; // 20%
    private float snackPercentage = 0.1f;     // 10%
    private float lunchPercentage = 0.3f;     // 30%
    private float dinnerPercentage = 0.4f;    // 40%

    public CustomCircularProgressBar(Context context) {
        super(context);
        init(context);  // Pass context to the init method
    }

    public CustomCircularProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);  // Pass context to the init method
    }

    private void init(Context context) {
        // Initialize paints with context to access resources
        paintBreakfast = new Paint();
        paintBreakfast.setColor(ContextCompat.getColor(context, R.color.colorBreakfast));
        paintBreakfast.setStyle(Paint.Style.FILL);
        paintBreakfast.setAntiAlias(true);

        paintSnack = new Paint();
        paintSnack.setColor(ContextCompat.getColor(context, R.color.colorSnack));
        paintSnack.setStyle(Paint.Style.FILL);
        paintSnack.setAntiAlias(true);

        paintLunch = new Paint();
        paintLunch.setColor(ContextCompat.getColor(context, R.color.colorLunch));
        paintLunch.setStyle(Paint.Style.FILL);
        paintLunch.setAntiAlias(true);

        paintDinner = new Paint();
        paintDinner.setColor(ContextCompat.getColor(context, R.color.colorDinner));
        paintDinner.setStyle(Paint.Style.FILL);
        paintDinner.setAntiAlias(true);

        // Background Paint
        paintBackground = new Paint();
        paintBackground.setColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the center and radius
        int width = getWidth();
        int height = getHeight();
        float radius = Math.min(width, height) / 2f;
        float cx = width / 2f;
        float cy = height / 2f;

        // Define the rectangle for the pie chart
        RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw the background circle (this will be covered by the pie chart)
        canvas.drawCircle(cx, cy, radius, paintBackground);

        // Calculate sweep angles for each meal
        float startAngle = -90; // Start at the top
        float sweepAngleBreakfast = 3.6f * breakfastPercentage;
        float sweepAngleSnack = 3.6f * snackPercentage;
        float sweepAngleLunch = 3.6f * lunchPercentage;
        float sweepAngleDinner =3.6f * dinnerPercentage;

        // Draw breakfast slice
        canvas.drawArc(rectF, startAngle, sweepAngleBreakfast, true, paintBreakfast);
        startAngle += sweepAngleBreakfast;

        // Draw snack slice
        canvas.drawArc(rectF, startAngle, sweepAngleSnack, true, paintSnack);
        startAngle += sweepAngleSnack;

        // Draw lunch slice
        canvas.drawArc(rectF, startAngle, sweepAngleLunch, true, paintLunch);
        startAngle += sweepAngleLunch;

        // Draw dinner slice
        canvas.drawArc(rectF, startAngle, sweepAngleDinner, true, paintDinner);
    }

    // Optionally create setters to update the percentages dynamically
    public void setPercentages(float breakfast, float snack, float lunch, float dinner) {
        this.breakfastPercentage = breakfast;
        this.snackPercentage = snack;
        this.lunchPercentage = lunch;
        this.dinnerPercentage = dinner;
        invalidate(); // Redraw the view
    }
}
