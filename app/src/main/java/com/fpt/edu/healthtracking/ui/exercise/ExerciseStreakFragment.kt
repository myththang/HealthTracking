package com.fpt.edu.healthtracking.ui.exercise

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fpt.edu.healthtracking.adapters.CalendarAdapter
import com.fpt.edu.healthtracking.api.DashboardApi
import com.fpt.edu.healthtracking.data.repository.StreakRepository
import com.fpt.edu.healthtracking.data.responses.ExerciseStreakResponse
import com.fpt.edu.healthtracking.data.responses.FoodStreakResponse
import com.fpt.edu.healthtracking.databinding.FragmentExerciseStreakBinding
import com.fpt.edu.healthtracking.databinding.FragmentStreakBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.streak.StreakViewModel
import com.fpt.edu.healthtracking.util.CalendarItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class ExerciseStreakFragment : BaseFragment<StreakViewModel, FragmentExerciseStreakBinding, StreakRepository>() {
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupCalendar()
        loadData()
        observeData()
    }

    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnPrevMonth.setOnClickListener {
                viewModel.navigateMonth(-1)
            }

            btnNextMonth.setOnClickListener {
                viewModel.navigateMonth(1)
            }
        }
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()
        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            addItemDecoration(CalendarItemDecoration())
        }
    }

    private fun loadData() {
        viewModel.getExerciseStreak()
    }

    private fun observeData() {
        viewModel.streakExerciseData.observe(viewLifecycleOwner) { data ->
            updateStreakUI(data)
        }

        viewModel.currentMonth.observe(viewLifecycleOwner) { calendar ->
            updateMonthDisplay(calendar)
            //viewModel.getStreak(calendar)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.showCongratulations.observe(viewLifecycleOwner) { streakCount ->
            streakCount?.let {
                showCongratulationsDialog(it)
                viewModel.congratulationsShown()
            }
        }
    }

    private fun updateStreakUI(streakData:  ExerciseStreakResponse) {
        binding.apply {
            if (streakData.streakCount > 0) {
                val animator = ValueAnimator.ofInt(0, streakData.streakCount)
                animator.duration = 800
                animator.interpolator = AccelerateDecelerateInterpolator()

                animator.addUpdateListener { animation ->
                    val currentValue = animation.animatedValue as Int
                    tvStreakNumber.text = currentValue.toString()

                    if (currentValue > 0) {
                        tvStreakNumber.animate()
                            .scaleX(1.2f)
                            .scaleY(1.2f)
                            .setDuration(200)
                            .withEndAction {
                                tvStreakNumber.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start()
                            }
                            .start()
                    }
                }

                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (streakData.streakCount > 0) {
                            viewModel.showCongratulations()
                        }
                    }
                })

                animator.start()
            } else {
                tvStreakNumber.text = "0"
            }

            updateCalendarWithDates(streakData.streakDates)
        }
    }

    private fun updateMonthDisplay(calendar: Calendar) {
        val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale("vi"))
        binding.tvMonthYear.text = monthYearFormatter.format(calendar.time)

        viewModel.streakData.value?.let { data ->
            updateCalendarWithDates(data.dates)
        }
    }

    private fun updateCalendarWithDates(streakDates: List<String>) {
        val calendar = viewModel.currentMonth.value ?: return
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val standardDates = streakDates.mapNotNull { dateStr ->
            try {
                formatter.parse(dateStr)?.let { date ->
                    outputFormatter.format(date)
                }
            } catch (e: Exception) {
                null
            }
        }.sorted()

        val streakDays = mutableSetOf<String>()
        if (standardDates.isNotEmpty()) {
            var currentStreak = mutableSetOf<String>()
            var previousDate: Calendar? = null

            for (dateStr in standardDates) {
                val parsedDate = try {
                    outputFormatter.parse(dateStr)
                } catch (e: Exception) {
                    null
                }

                if (parsedDate == null) {
                    continue
                }

                val currentDate = Calendar.getInstance().apply {
                    time = parsedDate
                }

                if (previousDate == null) {
                    currentStreak.add(dateStr)
                } else {
                    val dayDiff = ((currentDate.timeInMillis - previousDate.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
                    if (dayDiff == 1) {
                        currentStreak.add(dateStr)
                    } else {
                        if (currentStreak.size > streakDays.size) {
                            streakDays.clear()
                            streakDays.addAll(currentStreak)
                        }
                        currentStreak = mutableSetOf(dateStr)
                    }
                }
                previousDate = currentDate
            }

            if (currentStreak.size > streakDays.size) {
                streakDays.clear()
                streakDays.addAll(currentStreak)
            }
        }

        val days = mutableListOf<CalendarAdapter.CalendarDay>()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val padding = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        repeat(padding) {
            days.add(
                CalendarAdapter.CalendarDay(
                date = 0,
                isCurrentMonth = false,
                isStreakDay = false,
                isLoggedDay = false,
                isHighlighted = false
            ))
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val dateString = "$currentYear-${String.format("%02d", currentMonth + 1)}-${String.format("%02d", i)}"

            val isStreakDay = streakDays.contains(dateString)
            val isLoggedDay = standardDates.contains(dateString)

            val isHighlighted = if (isStreakDay) {
                val prevDayIsStreak = i > 1 && days.lastOrNull()?.isStreakDay == true
                val nextDayString = "$currentYear-${String.format("%02d", currentMonth + 1)}-${String.format("%02d", i + 1)}"
                val nextDayIsStreak = streakDays.contains(nextDayString)
                prevDayIsStreak || nextDayIsStreak
            } else false

            days.add(
                CalendarAdapter.CalendarDay(
                date = i,
                isStreakDay = isStreakDay,
                isLoggedDay = isLoggedDay,
                isHighlighted = isHighlighted,
                isCurrentMonth = true
            ))
        }

        val remainingDays = (7 - (days.size % 7)) % 7
        repeat(remainingDays) {
            days.add(
                CalendarAdapter.CalendarDay(
                date = 0,
                isCurrentMonth = false,
                isStreakDay = false,
                isLoggedDay = false,
                isHighlighted = false
            ))
        }

        calendarAdapter.submitList(days)
    }

    private fun showCongratulationsDialog(streakCount: Int) {
        val message = when {
            streakCount >= 30 -> "Tuyệt vời! Bạn đã duy trì thói quen trong ${streakCount} ngày liên tiếp. Hãy tiếp tục phát huy nhé!"
            streakCount >= 7 -> "Chúc mừng! Bạn đã duy trì thói quen trong ${streakCount} ngày liên tiếp. Cố gắng duy trì chuỗi ngày này nhé!"
            else -> "Chúc mừng! Bạn đã duy trì thói quen trong ${streakCount} ngày liên tiếp. Hãy tiếp tục cố gắng nhé!"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 Chúc mừng!")
            .setMessage(message)
            .setPositiveButton("Cảm ơn") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun getViewModel() = StreakViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentExerciseStreakBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): StreakRepository {
        val api = remoteDataSource.buildApi(DashboardApi::class.java)
        return StreakRepository(api, userPreferences)
    }

}