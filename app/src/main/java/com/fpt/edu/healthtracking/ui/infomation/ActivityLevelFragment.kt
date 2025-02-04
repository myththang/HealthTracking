package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentActivityLevelBinding

class ActivityLevelFragment : Fragment() {
    private lateinit var binding: FragmentActivityLevelBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentActivityLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.isEnabled = false

        setupActivityLevelSelection()
        setupContinueButton()
        setupBackButton()
        observeViewModel()

    }

    private fun setupActivityLevelSelection() {
        binding.cardInactive.setOnClickListener {
            viewModel.setActivityLevel(ActivityLevel.INACTIVE)
            binding.btnContinue.isEnabled = true
        }
        binding.cardLightlyActive.setOnClickListener {
            viewModel.setActivityLevel(ActivityLevel.LIGHTLY_ACTIVE)
            binding.btnContinue.isEnabled = true
        }
        binding.cardVeryActive.setOnClickListener {
            viewModel.setActivityLevel(ActivityLevel.VERY_ACTIVE)
            binding.btnContinue.isEnabled = true
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_activityLevelFragment_to_personalInfoFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.activityLevel.observe(viewLifecycleOwner) { activityLevel ->
            updateActivityLevelSelection(activityLevel)
            binding.btnContinue.isEnabled = activityLevel != null
            updateContinueButtonAppearance(activityLevel != null)
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

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateActivityLevelSelection(selectedLevel: ActivityLevel) {
        binding.apply {
            layoutInactive.setBackgroundResource(
                if (selectedLevel == ActivityLevel.INACTIVE) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            (layoutInactive.getChildAt(0) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.INACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )
            (layoutInactive.getChildAt(1) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.INACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )

            layoutLightlyActive.setBackgroundResource(
                if (selectedLevel == ActivityLevel.LIGHTLY_ACTIVE) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            (layoutLightlyActive.getChildAt(0) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.LIGHTLY_ACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )
            (layoutLightlyActive.getChildAt(1) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.LIGHTLY_ACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )

            layoutVeryActive.setBackgroundResource(
                if (selectedLevel == ActivityLevel.VERY_ACTIVE) R.drawable.bg_weight_change_selected
                else R.drawable.bg_weight_change_unselected
            )
            (layoutVeryActive.getChildAt(0) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.VERY_ACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )
            (layoutVeryActive.getChildAt(1) as TextView).setTextColor(
                ContextCompat.getColor(requireContext(),
                    if (selectedLevel == ActivityLevel.VERY_ACTIVE) android.R.color.white
                    else android.R.color.black
                )
            )
        }
    }
}