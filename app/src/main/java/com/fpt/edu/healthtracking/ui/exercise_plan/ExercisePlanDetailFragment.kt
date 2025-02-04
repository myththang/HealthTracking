package com.fpt.edu.healthtracking.ui.exercise_plan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.ExercisePlanApi
import com.fpt.edu.healthtracking.data.repository.ExercisePlanRepository
import com.fpt.edu.healthtracking.databinding.FragmentExercisePlanDetailBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class ExercisePlanDetailFragment : BaseFragment<
        ExercisePlanViewModel,
        FragmentExercisePlanDetailBinding,
        ExercisePlanRepository
        >() {

    private val exerciseAdapter = ExercisePlanDetailAdapter()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private var planId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planId = arguments?.getInt("planId") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        viewModel.loadPlanDetail(planId)
    }

    private fun setupViews() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            rvExercises.apply {
                adapter = exerciseAdapter
                layoutManager = LinearLayoutManager(context)
            }

            btnAssignPlan.setOnClickListener {
                showDatePicker()
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val day = tab?.position?.plus(1) ?: 1
                    filterExercisesByDay(day)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupObservers() {
        viewModel.planDetail.observe(viewLifecycleOwner) { plan ->
            binding.apply {
                tvPlanName.text = plan.name
                tvCalories.text = "${plan.totalCaloriesBurned} calories"

                Glide.with(requireContext())
                    .load(plan.exercisePlanImage)
                    .placeholder(R.drawable.workout_placeholder)
                    .error(R.drawable.workout_placeholder)
                    .into(ivPlanImage)

                tabLayout.removeAllTabs()
                plan.details.groupBy { it.day }.keys.sorted().forEach { day ->
                    tabLayout.addTab(tabLayout.newTab().setText("Ngày $day"))
                }

                filterExercisesByDay(1)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.assignSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Đã thêm kế hoạch vào nhật ký", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun filterExercisesByDay(day: Int) {
        viewModel.planDetail.value?.let { plan ->
            val exercises = plan.details.filter { it.day == day }
            exerciseAdapter.submitList(exercises)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateString = dateFormat.format(calendar.time)
                viewModel.assignPlan(planId, dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun getViewModel() = ExercisePlanViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentExercisePlanDetailBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = ExercisePlanRepository(
        remoteDataSource.buildApi(ExercisePlanApi::class.java),
        userPreferences
    )
} 