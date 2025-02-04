package com.fpt.edu.healthtracking.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CalendarItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(2, 2, 2, 2)
    }
}