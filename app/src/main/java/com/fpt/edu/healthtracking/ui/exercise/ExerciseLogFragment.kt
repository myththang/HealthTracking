package com.fpt.edu.healthtracking.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.workout.DailyWorkoutAdapter
import com.fpt.edu.healthtracking.api.ExerciseApi
import com.fpt.edu.healthtracking.data.repository.ExerciseRepository
import com.fpt.edu.healthtracking.databinding.FragmentWorkoutLogBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.DateUtils.isToday
import com.fpt.edu.healthtracking.util.DateUtils.isTomorrow
import com.fpt.edu.healthtracking.util.DateUtils.isYesterday
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExerciseLogFragment : BaseFragment<ExerciseViewModel, FragmentWorkoutLogBinding, ExerciseRepository>() {

    private lateinit var workoutAdapter: DailyWorkoutAdapter
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun getViewModel() = ExerciseViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWorkoutLogBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): ExerciseRepository {
        val api = remoteDataSource.buildApi(ExerciseApi::class.java)
        return ExerciseRepository(api, userPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupDatePicker()
        setupRecyclerView()
        setupObservers()
        loadExerciseDiary()
    }

    private fun setupDatePicker() {
        binding.apply {
            val datePickerDialog = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePickerDialog.addOnPositiveButtonClickListener { selection ->
                calendar.timeInMillis = selection
                updateDateDisplay()
                loadDataForDate(calendar.time)
            }

            datePickerButton.setOnClickListener {
                datePickerDialog.show(parentFragmentManager, "date_picker")
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
    private fun loadDataForDate(date: Date) {
        viewModel.loadExerciseDiary(date)
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

    }
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnAddWorkout.setOnClickListener {
                viewModel.exerciseDiary.value?.let { diary ->
                    findNavController().navigate(
                        R.id.action_workoutLogFragment_to_workoutSelectionFragment,
                        bundleOf(
                            "diary_id" to diary.exerciseDiaryId
                        )
                    )
                }

            }
        }
    }



    private fun setupRecyclerView() {
        workoutAdapter = DailyWorkoutAdapter().apply {
            onWorkoutChanged = { workout->
                viewModel.updateStatus(workout)
            }
        }
        binding.rvExercises.apply {
            adapter = workoutAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

    }

    private fun setupObservers() {
        viewModel.exerciseDiary.observe(viewLifecycleOwner) { diary ->
            binding.apply {
                tvTotalDuration.text = "${diary.totalDuration.toInt()} phút"

                tvCaloriesBurned.text = "${diary.totalCaloriesBurned.toInt()} calories"

                emptyStateLayout.isVisible = diary.exerciseDiaryDetails.isEmpty()
                rvExercises.isVisible = diary.exerciseDiaryDetails.isNotEmpty()

                if (diary.exerciseDiaryDetails.isNotEmpty()) {
                    workoutAdapter.submitList(diary.exerciseDiaryDetails)
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.updateToDiaryResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "thành công", Toast.LENGTH_SHORT).show()
                loadExerciseDiary()
            }
        }
    }

    private fun loadExerciseDiary() {
        viewModel.loadExerciseDiary(calendar.time)
    }
}