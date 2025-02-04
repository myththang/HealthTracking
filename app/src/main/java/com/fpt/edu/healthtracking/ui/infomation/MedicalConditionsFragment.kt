package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentMedicalConditionsBinding

class MedicalConditionFragment : Fragment() {
    private lateinit var binding: FragmentMedicalConditionsBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMedicalConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.isEnabled = false
        setupMedicalConditionSelection()
        setupContinueButton()
        setupBackButton()
        observeViewModel()
    }

    private fun setupMedicalConditionSelection() {
        binding.cardNoCondition.setOnClickListener {
            viewModel.toggleCondition(MedicalCondition.NONE)
            binding.btnContinue.isEnabled = true
        }
        binding.cardDiabetes.setOnClickListener {
            viewModel.toggleCondition(MedicalCondition.DIABETES)
            binding.btnContinue.isEnabled = true
        }
        binding.cardGout.setOnClickListener {
            viewModel.toggleCondition(MedicalCondition.GOUT)
            binding.btnContinue.isEnabled = true
        }
        binding.cardHypertension.setOnClickListener {
            viewModel.toggleCondition(MedicalCondition.HYPERTENSION)
            binding.btnContinue.isEnabled = true
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_medicalConditionsFragment_to_dietPreferenceFragment)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedConditions.observe(viewLifecycleOwner) { conditions ->
            updateMedicalConditionSelection(conditions)
            binding.btnContinue.isEnabled = conditions != null
            updateContinueButtonAppearance(conditions != null)
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

    private fun updateMedicalConditionSelection(conditions: Set<MedicalCondition>) {
        binding.tvNoCondition.apply {
            setBackgroundResource(
                if (MedicalCondition.NONE in conditions) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            setTextColor(
                if (MedicalCondition.NONE in conditions) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black)
            )
        }

        binding.tvDiabetes.apply {
            setBackgroundResource(
                if (MedicalCondition.DIABETES in conditions) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            setTextColor(
                if (MedicalCondition.DIABETES in conditions) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black)
            )
        }

        binding.tvGout.apply {
            setBackgroundResource(
                if (MedicalCondition.GOUT in conditions) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            setTextColor(
                if (MedicalCondition.GOUT in conditions) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black)
            )
        }

        binding.tvHypertension.apply {
            setBackgroundResource(
                if (MedicalCondition.HYPERTENSION in conditions) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            setTextColor(
                if (MedicalCondition.HYPERTENSION in conditions) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black)
            )
        }

    }
}