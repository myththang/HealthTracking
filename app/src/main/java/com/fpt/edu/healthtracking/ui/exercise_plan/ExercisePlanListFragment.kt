package com.fpt.edu.healthtracking.ui.exercise_plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.ExercisePlanApi
import com.fpt.edu.healthtracking.data.repository.ExercisePlanRepository
import com.fpt.edu.healthtracking.databinding.FragmentExercisePlanListBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment

class ExercisePlanListFragment : BaseFragment<ExercisePlanViewModel, FragmentExercisePlanListBinding, ExercisePlanRepository>() {

    private lateinit var planAdapter: ExercisePlanAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        setupObservers()

        viewModel.loadPlans()
    }

    private fun setupRecyclerView() {
        planAdapter = ExercisePlanAdapter().apply {
            onItemClick = { plan ->
                findNavController().navigate(
                    R.id.action_exercisePlanListFragment_to_exercisePlanDetailFragment,
                    bundleOf("planId" to plan.exercisePlanId)
                )
            }
        }

        binding.rvExercisePlans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = planAdapter // Gán adapter đã được khởi tạo
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            etSearch.addTextChangedListener { text ->
                text?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        viewModel.searchPlans(query)
                    } else {
                        viewModel.loadPlans()
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.plans.observe(viewLifecycleOwner) { plans ->
            planAdapter.submitList(plans)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getViewModel() = ExercisePlanViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentExercisePlanListBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = ExercisePlanRepository(
        remoteDataSource.buildApi(ExercisePlanApi::class.java),
        userPreferences
    )
} 