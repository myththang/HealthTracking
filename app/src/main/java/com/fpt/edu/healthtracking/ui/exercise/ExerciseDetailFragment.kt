package com.fpt.edu.healthtracking.ui.exercise

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.ExerciseApi
import com.fpt.edu.healthtracking.data.repository.ExerciseRepository
import com.fpt.edu.healthtracking.databinding.FragmentWorkoutDetailBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment

class WorkoutDetailFragment : BaseFragment<ExerciseViewModel, FragmentWorkoutDetailBinding, ExerciseRepository>(){
    private var isCardio: Int = 1
    private var diaryId: Int = -1
    private var exerciseId: Int = -1
    private var currentMet: Double = 0.0
    override fun getViewModel() = ExerciseViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWorkoutDetailBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): ExerciseRepository {
        val api = remoteDataSource.buildApi(ExerciseApi::class.java)
        return ExerciseRepository(api, userPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exerciseId = arguments?.getInt("exercise_id") ?: return
        diaryId = arguments?.getInt("diary_id") ?: return
        isCardio = arguments?.getInt("is_cardio") ?: 1

        setupViews()
        setupObservers()
        viewModel.loadExerciseDetail(exerciseId, isCardio)
    }
    private fun setupViews() {
        with(binding) {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnAdd.setOnClickListener {
                handleAddToDiaryClick()
            }

            etDuration.isEnabled = false
            etCalories.isEnabled = false

            if (isCardio==1 || isCardio==3) {
                etDuration.isEnabled = true

                etDuration.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        calculateCalories(s?.toString()?.toIntOrNull() ?: 0)
                    }
                })
            }
        }
        binding.layoutCardio.visibility = if(isCardio==1 || isCardio==3) View.VISIBLE else View.GONE
        binding.layoutStrength.visibility = if(isCardio==1 || isCardio==3) View.GONE else View.VISIBLE

    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.exerciseDetail.observe(viewLifecycleOwner) { exerciseInfo ->
            binding.apply {
                tvExerciseName.text = exerciseInfo.exerciseName
                tvDescription.text = exerciseInfo.description
                currentMet = exerciseInfo.met

                workoutLevelSection.visibility = if (isCardio == 3) View.GONE else View.VISIBLE
                Glide.with(requireContext())
                    .load(exerciseInfo.exerciseImage)
                    .placeholder(R.drawable.workout_placeholder)
                    .error(R.drawable.workout_placeholder)
                    .into(ivExercise)
            }
        }

        viewModel.workoutLevels.observe(viewLifecycleOwner) { levels ->
            setupWorkoutLevelSpinner(levels)
            binding.spinnerWorkoutLevel.setText("Nhẹ", false)
            updateFieldsForLevel(levels, 0)
        }

        viewModel.addToDiaryResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Thêm bài tập thành công", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun setupWorkoutLevelSpinner(levels: List<WorkoutLevel>) {
        val levelNames = levels.map { it.level }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, levelNames)
        binding.spinnerWorkoutLevel.setAdapter(adapter)

        binding.spinnerWorkoutLevel.setOnItemClickListener { _, _, position, _ ->
            updateFieldsForLevel(levels, position)
        }
    }

    private fun updateFieldsForLevel(levels: List<WorkoutLevel>, position: Int) {
        if (levels.isEmpty() || position >= levels.size) return

        val level = levels[position]
        binding.apply {
            if (isCardio==1) {
                etDuration.setText(level.minutes?.toString() ?: "")
                calculateCalories(level.minutes ?: 0)
            } else {
                tvExerciseTime.text = level.minutes.toString() + " Phút"
                etReps.setText(level.reps?.toString() ?: "")
                etSets.setText(level.sets?.toString() ?: "")
            }
        }
    }

    private fun handleAddToDiaryClick() {

        if (diaryId == -1) {
            Toast.makeText(context, "Invalid diary ID", Toast.LENGTH_SHORT).show()
            return
        }
        val durations :Int = if(isCardio==1 || isCardio==3){
            binding.etDuration.text.toString().toIntOrNull()?: 1
        }else binding.tvExerciseTime.text.toString().toInt()

        val calories = binding.etCalories.text.toString().toIntOrNull()?: 0

        viewModel.exerciseDetail.value?.let { exercise ->
            viewModel.addToDiary(
                diaryId = diaryId,
                exerciseId = exercise.exerciseId,
                duration = durations,
                isPractice = true,
                caloriesBurned = calories
            )
        }
    }

    private fun calculateCalories(durationMinutes: Int) {
        val prefs = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userWeight = prefs.getFloat("weight", 70f)

        val durationHours = durationMinutes / 60.0
        Log.e("met","$currentMet * $userWeight * $durationHours")
        val calories = (currentMet * userWeight * durationMinutes*3.5/200).toInt()

        binding.etCalories.setText(calories.toString())
    }

}