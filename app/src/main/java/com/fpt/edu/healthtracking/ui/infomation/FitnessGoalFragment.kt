package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentFitnessGoalBinding

class FitnessGoalFragment : Fragment() {
    private lateinit var binding: FragmentFitnessGoalBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFitnessGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.isEnabled = false
        setupFitnessGoalSelection()
        setupContinueButton()
        setupBackButton()
        observeViewModel()
    }

    private fun setupFitnessGoalSelection() {
        binding.cardLoseWeight.setOnClickListener {
            viewModel.setFitnessGoal(FitnessGoal.LOSE_WEIGHT)
            binding.btnContinue.isEnabled = true
        }
        binding.cardMaintainWeight.setOnClickListener {
            viewModel.setFitnessGoal(FitnessGoal.MAINTAIN_WEIGHT)
            binding.btnContinue.isEnabled = true
        }
        binding.cardGainWeight.setOnClickListener {
            viewModel.setFitnessGoal(FitnessGoal.GAIN_WEIGHT)
            binding.btnContinue.isEnabled = true
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            when (viewModel.fitnessGoal.value) {
                FitnessGoal.MAINTAIN_WEIGHT -> findNavController().navigate(R.id.action_fitnessGoalFragment_to_registerFragment)
                FitnessGoal.LOSE_WEIGHT, FitnessGoal.GAIN_WEIGHT -> findNavController().navigate(R.id.action_fitnessGoalFragment_to_desiredWeightFragment)
                else -> { /* Do nothing */ }
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.fitnessGoal.observe(viewLifecycleOwner) { goal ->
            updateFitnessGoalSelection(goal)
            binding.btnContinue.isEnabled = goal != null
            updateContinueButtonAppearance(goal != null)
        }
    }

    private fun updateContinueButtonAppearance(enabled: Boolean) {
        binding.btnContinue.apply {
            if (enabled) {
                setBackgroundResource(R.drawable.button_enabled_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                setBackgroundResource(R.drawable.button_disabled_background)
                //setTextColor(ContextCompat.getColor(requireContext(), R.color.))
            }
        }
    }

    private fun LinearLayout.updateFitnessGoalState(isSelected: Boolean) {
        setBackgroundResource(
            if (isSelected) R.drawable.bg_weight_change_selected
            else R.drawable.bg_weight_change_unselected
        )

        (getChildAt(0) as TextView).setTextColor(
            ContextCompat.getColor(context,
                if (isSelected) android.R.color.white
                else android.R.color.black
            )
        )
        (getChildAt(1) as TextView).setTextColor(
            ContextCompat.getColor(context,
                if (isSelected) android.R.color.white
                else android.R.color.black
            )
        )
    }

    private fun updateFitnessGoalSelection(goal: FitnessGoal) {
        binding.apply {
            layoutLoseWeight.updateFitnessGoalState(goal == FitnessGoal.LOSE_WEIGHT)
            layoutMaintainWeight.updateFitnessGoalState(goal == FitnessGoal.MAINTAIN_WEIGHT)
            layoutGainWeight.updateFitnessGoalState(goal == FitnessGoal.GAIN_WEIGHT)
        }
    }
}