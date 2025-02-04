package com.fpt.edu.healthtracking.ui.nutrition

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.NutritionApi
import com.fpt.edu.healthtracking.data.model.MacroNutrientDto
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import com.fpt.edu.healthtracking.databinding.FragmentMacrosBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment

class MacrosFragment : BaseFragment<NutritionViewModel, FragmentMacrosBinding,NutritionRepository>() {




    override fun getViewModel()= NutritionViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentMacrosBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NutritionRepository {
        val api=remoteDataSource.buildApi(NutritionApi::class.java)
        return NutritionRepository(
            api = api,
            userPreferences = userPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.fetchDailyMacros(viewModel.datePicked.value.toString())
    }

    private fun observeViewModel() {
        viewModel.macroData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("AccountDetailFragment", "ProfileData observed: $data")
                setUpView(data)
            } else {
                Log.d("AccountDetailFragment", "ProfileData is null")
            }
        }
        viewModel.datePicked.observe(viewLifecycleOwner) { date ->
            // Use the date as needed
            viewModel.fetchDailyMacros(viewModel.datePicked.value.toString())
        }

    }

    fun setUpView(data:MacroNutrientDto){

        val sharedPreferences = context?.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        var goalCarb   =""
        var goalFat    =""
        var goalProtein =""


        if (sharedPreferences != null) {
            goalCarb = sharedPreferences.getFloat("totalCarb",0f).toString()
            goalFat = sharedPreferences.getFloat("totalFat",0f ).toString()
            goalProtein = sharedPreferences.getFloat("totalProtein",0f).toString()
        }


        val totalCarbsGoal = goalCarb.toFloatOrNull() ?: 1f // Avoid division by zero by defaulting to 1
        val totalFatGoal = goalFat.toFloatOrNull() ?: 1f
        val totalProteinGoal = goalProtein.toFloatOrNull() ?: 1f

        val total = data.totalCarbs+data.totalFat+data.totalProtein
        // Calculate percentages
        val carbsPercentage = (data.totalCarbs / total) * 100
        val fatPercentage = (data.totalFat / total) * 100
        val proteinPercentage = (data.totalProtein / total) * 100

        val adjustedCarbsPercentage = if (carbsPercentage > 45) 45 else carbsPercentage
        val adjustedFatPercentage = if (fatPercentage > 25) 25 else fatPercentage
        val adjustedProteinPercentage = if (proteinPercentage > 30) 30 else proteinPercentage


        // Update the progress bar with percentages
        binding.customCircularProgressBar.setPercentages(carbsPercentage.toFloat(), fatPercentage.toFloat(), proteinPercentage.toFloat(),0f)
        binding.tvCarbsTotal.text=carbsPercentage.toInt().toString()
        binding.tvCarbsGoal.text= "45%"
        binding.tvFatTotal.text=fatPercentage.toInt().toString()
        binding.tvFatGoal.text="25%"
        binding.tvProteinTotal.text=proteinPercentage.toInt().toString()
        binding.tvProteinGoal.text="30%"
        binding.tvHighestProtein.text= data.highestProteinFood.foodName
        binding.tvProteinGram.text=data.highestProteinFood.macroValue.toString()
        binding.tvHighestFat.text=data.highestFatFood.foodName
        binding.tvFatGram.text=data.highestFatFood.macroValue.toString()
        binding.tvHighestCarb.text=data.highestCarbFood.foodName
        binding.tvCarbGram.text=data.highestCarbFood.macroValue.toString()
    }


}