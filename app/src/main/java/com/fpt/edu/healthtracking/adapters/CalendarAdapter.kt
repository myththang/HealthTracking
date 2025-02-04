package com.fpt.edu.healthtracking.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.ItemCalendarDayBinding

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {
    private var days: List<CalendarDay> = emptyList()

    data class CalendarDay(
        val date: Int,
        val isStreakDay: Boolean = false,
        val isLoggedDay: Boolean = false,
        val isCurrentMonth: Boolean = true,
        val isHighlighted: Boolean = false
    )

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            binding.apply {
                tvDay.visibility = View.GONE
                dotView.visibility = if (day.isCurrentMonth) View.VISIBLE else View.GONE

                dotView.backgroundTintList = when {
                    !day.isCurrentMonth -> ColorStateList.valueOf(
                        ContextCompat.getColor(root.context, R.color.gray_300)
                    )
                    day.isLoggedDay || day.isStreakDay -> ColorStateList.valueOf(
                        ContextCompat.getColor(root.context, R.color.yellow)
                    )
                    else -> ColorStateList.valueOf(
                        ContextCompat.getColor(root.context, R.color.gray_300)
                    )
                }
                ivStreak.isVisible = day.isStreakDay
                //bgHighlight.isVisible = day.isHighlighted
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder(
            ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size

    fun submitList(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}