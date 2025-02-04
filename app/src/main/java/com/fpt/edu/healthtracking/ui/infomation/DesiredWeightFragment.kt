package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentDesiredWeightBinding
import kotlin.math.roundToInt

class DesiredWeightFragment : Fragment() {
    private lateinit var binding: FragmentDesiredWeightBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDesiredWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSliders()
        setupContinueButton()
        setupBackButton()
        observeViewModel()
    }

    private fun setupSliders() {
        val currentWeight = viewModel.weight.value ?: 70

        viewModel.fitnessGoal.value?.let { goal ->
            when (goal) {
                FitnessGoal.LOSE_WEIGHT -> {
                    binding.slider.valueFrom = 45f
                    binding.slider.valueTo = currentWeight.toFloat() - 1
                    binding.slider.value = (currentWeight - 5).toFloat()
                }
                FitnessGoal.GAIN_WEIGHT -> {
                    binding.slider.valueFrom = currentWeight.toFloat() + 1
                    binding.slider.valueTo = 150f
                    binding.slider.value = (currentWeight + 5).toFloat()
                }
                FitnessGoal.MAINTAIN_WEIGHT -> {
                    binding.slider.valueFrom = 45f
                    binding.slider.valueTo = 150f
                    binding.slider.value = currentWeight.toFloat()
                }
            }
        }

        binding.slider.addOnChangeListener { _, value, _ ->
            Log.e("setDesiredWeight","$value")
            viewModel.setDesiredWeight(value.toInt())
        }
    }

    private fun observeViewModel() {
        viewModel.desiredWeight.observe(viewLifecycleOwner) { weight ->
            binding.tvWeightValue.text = "$weight kg"
        }

        viewModel.fitnessGoal.observe(viewLifecycleOwner) { fitnessGoal ->
            viewModel.weight.observe(viewLifecycleOwner) { currentWeight ->
                val defaultDesiredWeight = when (fitnessGoal) {
                    FitnessGoal.LOSE_WEIGHT -> currentWeight - 5
                    FitnessGoal.GAIN_WEIGHT -> currentWeight + 5
                    FitnessGoal.MAINTAIN_WEIGHT -> currentWeight
                    else -> currentWeight
                }
                binding.slider.value = defaultDesiredWeight.toFloat()
                binding.tvWeightValue.text = "$defaultDesiredWeight kg"
                viewModel.setDesiredWeight(defaultDesiredWeight)
            }
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_desiredWeightFragment_to_weightChangeRateFragment)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


}