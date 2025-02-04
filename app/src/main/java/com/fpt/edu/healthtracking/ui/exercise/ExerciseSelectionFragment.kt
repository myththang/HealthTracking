package com.fpt.edu.healthtracking.ui.exercise

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.workout.ExerciseSelectionAdapter
import com.fpt.edu.healthtracking.api.ExerciseApi
import com.fpt.edu.healthtracking.data.model.ExerciseCategory
import com.fpt.edu.healthtracking.data.repository.ExerciseRepository
import com.fpt.edu.healthtracking.databinding.FragmentWorkoutSelectionBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutSelectionFragment : BaseFragment<ExerciseViewModel, FragmentWorkoutSelectionBinding, ExerciseRepository>() {

    private lateinit var exerciseAdapter: ExerciseSelectionAdapter
    private var searchJob: Job? = null
    private var diaryId: Int = -1

    override fun getViewModel() = ExerciseViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWorkoutSelectionBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): ExerciseRepository {
        val api = remoteDataSource.buildApi(ExerciseApi::class.java)
        return ExerciseRepository(api, userPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diaryId = arguments?.getInt("diary_id") ?: return

        setupUI()
        setupObservers()
        viewModel.loadExercises()
    }

    private fun setupUI() {
        setupToolbar()
        setupSearch()
        setupRecyclerView()
        setupChipGroup()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    s?.toString()?.let { query ->
                        if (query.isEmpty()) {
                            viewModel.loadExercises()
                        } else {
                            viewModel.searchExercises(query)
                        }
                    }
                }
            }
        })

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                viewModel.searchExercises(query)
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseSelectionAdapter().apply {
            onExerciseClick = { exercise ->
                findNavController().navigate(
                    R.id.action_workoutSelectionFragment_to_workoutDetailFragment,
                    bundleOf(
                        "diary_id" to diaryId,
                        "exercise_id" to exercise.exerciseId,
                        "is_cardio" to (when {
                            exercise.categoryExercise.equals("cardio", ignoreCase = true) -> 1
                            exercise.categoryExercise.equals("Kháng lực", ignoreCase = true) -> 2
                            else -> 3
                        })
                    )
                )
            }
        }

        binding.rvWorkouts.apply {
            adapter = exerciseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupChipGroup() {
        binding.apply {
            chipAll.setOnClickListener {
                viewModel.setCategory(ExerciseCategory.ALL)
                updateChipSelection(chipAll)
            }
            chipCardio.setOnClickListener {
                viewModel.setCategory(ExerciseCategory.CARDIO)
                updateChipSelection(chipCardio)
            }
            chipStrength.setOnClickListener {
                viewModel.setCategory(ExerciseCategory.STRENGTH)
                updateChipSelection(chipStrength)
            }
            chipOther.setOnClickListener {
                viewModel.setCategory(ExerciseCategory.OTHER)
                updateChipSelection(chipOther)
            }
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        binding.apply {
            chipAll.isChecked = selectedChip == chipAll
            chipCardio.isChecked = selectedChip == chipCardio
            chipStrength.isChecked = selectedChip == chipStrength
            chipOther.isChecked = selectedChip == chipOther
        }
    }


    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun setupObservers() {
        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            exerciseAdapter.submitList(exercises)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

}