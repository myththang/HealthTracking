package com.fpt.edu.healthtracking.util

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.DialogCustomCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomCalendarDialog(
    private val onDateSelected: (CalendarDay) -> Unit,
    private val specialDays: List<CalendarDay>,
    private val selectedDay: CalendarDay
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogCustomCalendarBinding.inflate(inflater, container, false)

        binding.yearText.text = Calendar.getInstance().get(Calendar.YEAR).toString()
        binding.dateText.text = SimpleDateFormat("EEE, dd MMM", Locale("vi")).format(Date())

        binding.calendarView.apply {
            addDecorators(
                TodayDecorator(requireContext()),
                SelectedDayDecorator(requireContext(),selectedDay),
                SpecialDaysDecorator(requireContext(), specialDays)
            )

            setOnDateChangedListener { _, date, _ ->
                onDateSelected(date)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        return binding.root
    }

    private class TodayDecorator(context: Context) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.selected_background)

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == CalendarDay.today()
        }

        override fun decorate(view: DayViewFacade) {
            drawable?.let { view.setBackgroundDrawable(it) }
        }
    }

    private class SelectedDayDecorator(context: Context, private val selectedDay: CalendarDay) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.today_background)

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return selectedDay == day
        }

        override fun decorate(view: DayViewFacade) {
            drawable?.let { view.setBackgroundDrawable(it) }
        }
    }

    private class SpecialDaysDecorator(
        context: Context,
        private val specialDays: List<CalendarDay>
    ) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.special_day_background)

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return specialDays.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            drawable?.let { view.setBackgroundDrawable(it) }
        }
    }
}
