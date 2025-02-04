package com.fpt.edu.healthtracking.ui.food

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.MealAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.repository.FoodLogRepository
import com.fpt.edu.healthtracking.databinding.FragmentFoodLogBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.CustomCalendarDialog
import com.fpt.edu.healthtracking.util.DateUtils.isToday
import com.fpt.edu.healthtracking.util.DateUtils.isTomorrow
import com.fpt.edu.healthtracking.util.DateUtils.isYesterday
import com.fpt.edu.healthtracking.util.fromOneBased
import com.google.android.material.datepicker.MaterialDatePicker
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodLogFragment : BaseFragment<FoodLogViewModel, FragmentFoodLogBinding, FoodLogRepository>() {
    private lateinit var mealAdapter: MealAdapter
    private val calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupDatePicker()
        setupObservers()
        setupFragmentResultListener()
        loadData(calendar.time)
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("food_added", viewLifecycleOwner) { _, _ ->

            loadData(calendar.time)
        }
    }

    private fun setupDatePicker() {
        binding.apply {
            // Set up initial calendar configuration
            val datePickerDialog = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePickerDialog.addOnPositiveButtonClickListener { selection ->
                calendar.timeInMillis = selection
                updateDateDisplay()
                loadDataForDate(calendar.time)
            }

            // Load streak days from API and show calendar dialog
            datePickerButton.setOnClickListener {
                lifecycleScope.launch {
                    userPreferences.authStateFlow.collect { authState ->
                        authState.accessToken?.let { token ->
                            viewModel.getSpecialDays(token) { specialDays ->
                                val dialog = CustomCalendarDialog({ selectedDate ->
                                    calendar.set(Calendar.YEAR, selectedDate.year)
                                    calendar.set(Calendar.MONTH, selectedDate.month)
                                    calendar.set(Calendar.DAY_OF_MONTH, selectedDate.day)
                                    updateDateDisplay()
                                    loadDataForDate(calendar.time)
                                }, specialDays, CalendarDay.from(calendar.time))
                                dialog.show(parentFragmentManager, "custom_calendar")
                            }
                        }
                    }
                }
            }


            btnPrevDay.setOnClickListener {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                updateDateDisplay()
                loadDataForDate(calendar.time)
            }

            btnNextDay.setOnClickListener {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                updateDateDisplay()
                loadDataForDate(calendar.time)
            }

            updateDateDisplay()
        }
    }

    private fun updateDateDisplay() {
        val vietnameseLocale = Locale("vi", "VN")
        val dateText = with(calendar) {
            when {
                isToday() -> "Hôm nay"
                isYesterday() -> "Hôm qua"
                isTomorrow() -> "Ngày mai"
                else -> {
                    val formatter = SimpleDateFormat("dd 'Tháng' MM, yyyy", vietnameseLocale)
                    formatter.format(time)
                }
            }
        }
        binding.tvCurrentDate.text = dateText
        
        Log.d("FoodLogFragment", "Current calendar date: ${calendar.time}")
    }

    private fun loadDataForDate(date: Date) {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                authState.accessToken?.let { token ->
                    // Thêm log để debug
                    Log.d("FoodLogFragment", "Loading data for date: ${date}")
                    
                    // Kiểm tra xem ngày có hợp lệ không
                    val currentDate = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    val selectedDate = Calendar.getInstance().apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Nếu là ngày trong tương lai, hiển thị dữ liệu mặc định
                    if (selectedDate.after(currentDate)) {
                        viewModel.loadFoodLog(token, date)
                    } else {
                        viewModel.loadFoodLog(token, date)
                    }
                }
            }
        }
    }
    private fun setupUI() {
        binding.apply {
            mealAdapter = MealAdapter().apply {
                onAddClick = { mealType ->
                    navigateToAddFood(mealType)
                }
                onDeleteClick = { diaryDetailId ->
                    viewModel.deleteFoodFromDiary(diaryDetailId)
                }
            }

            rvMeals.apply {
                adapter = mealAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

    private fun loadData(date: Date) {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                authState.accessToken?.let { token ->
                    Log.e("eee",date.toString())
                    viewModel.loadFoodLog(token, date)
                } ?: run {
                    Toast.makeText(context, "Please log in again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToAddFood(mealType: MealType) {
        viewModel.foodLogState.value?.let { state ->

            val bundle = Bundle().apply {
                putSerializable("meal_type", mealType)
                putInt("diary_id", state.diaryId)
            }
            findNavController().navigate(R.id.action_fragment_food_log_to_addFoodFragment, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.delete.observe(viewLifecycleOwner) { state ->
            if(state) loadData(calendar.time)
        }

        viewModel.foodLogState.observe(viewLifecycleOwner) { state ->
            binding.apply {

                val goalCaloriesText = state.targetCalories.toString()
                val foodCaloriesText = state.consumedCalories.toString()
                val remainingCaloriesText = state.remainingCalories.toString()


                val goalTextView = (binding.root.findViewById<LinearLayout>(R.id.goal_calories_container)
                    .getChildAt(1) as TextView)
                goalTextView.text = goalCaloriesText


                val foodTextView = (binding.root.findViewById<LinearLayout>(R.id.food_calories_container)
                    .getChildAt(1) as TextView)
                foodTextView.text = foodCaloriesText


                tvRemainingCalories.text = remainingCaloriesText

                val progressPercentage = ((state.remainingCalories.toFloat() / state.targetCalories) * 100).toInt()

                animateProgressBar(progressCalories, 100, progressPercentage)

                if (state.remainingCalories <= 0) {
                    tvCaloriesWarning.visibility = View.VISIBLE
                    progressCalories.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                    )
                    tvRemainingCalories.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                    )
                } else {
                    tvCaloriesWarning.visibility = View.GONE
                    progressCalories.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                    tvRemainingCalories.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                }
                mealAdapter.submitList(state.meals)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun animateProgressBar(progressBar: android.widget.ProgressBar, start: Int, end: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", start, end).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun getViewModel() = FoodLogViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFoodLogBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): FoodLogRepository {
        val api = remoteDataSource.buildApi(FoodApi::class.java)
        return FoodLogRepository(api,userPreferences)
    }
}