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
import com.fpt.edu.healthtracking.databinding.FragmentDietPreferenceBinding

class DietPreferenceFragment : Fragment() {
    private lateinit var binding: FragmentDietPreferenceBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDietPreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.isEnabled = false
        setupDietPreferenceSelection()
        setupContinueButton()
        setupBackButton()
        observeViewModel()
    }

    private fun setupDietPreferenceSelection() {
        binding.cardNormalDiet.setOnClickListener {
            viewModel.setDietPreference(DietPreference.NORMAL)
            binding.btnContinue.isEnabled = true
        }
        binding.cardLowCarb.setOnClickListener {
            viewModel.setDietPreference(DietPreference.LOW_CARB)
            binding.btnContinue.isEnabled = true
        }
        binding.cardVegetarian.setOnClickListener {
            viewModel.setDietPreference(DietPreference.VEGETARIAN)
            binding.btnContinue.isEnabled = true
        }
        binding.cardCleanEating.setOnClickListener {
            viewModel.setDietPreference(DietPreference.CLEAN_EATING)
            binding.btnContinue.isEnabled = true
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_dietPreferenceFragment_to_fitnessGoalFragment)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.dietPreference.observe(viewLifecycleOwner) { preference ->
            updateDietPreferenceSelection(preference)
            binding.btnContinue.isEnabled = preference != null
            updateContinueButtonAppearance(preference != null)
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

    private fun LinearLayout.updateDietSelectionState(isSelected: Boolean) {
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
    }

    private fun updateDietPreferenceSelection(preference: DietPreference) {
        binding.apply {
            layoutNormalDiet.updateDietSelectionState(preference == DietPreference.NORMAL)
            layoutLowCarb.updateDietSelectionState(preference == DietPreference.LOW_CARB)
            layoutVegetarian.updateDietSelectionState(preference == DietPreference.VEGETARIAN)
            layoutCleanEating.updateDietSelectionState(preference == DietPreference.CLEAN_EATING)
        }
    }
}