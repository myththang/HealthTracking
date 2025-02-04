package com.fpt.edu.healthtracking.ui.nutrition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.NutritionApi
import com.fpt.edu.healthtracking.data.model.DailyCaloriesDto
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import com.fpt.edu.healthtracking.databinding.FragmentCaloriesBinding
import com.fpt.edu.healthtracking.databinding.FragmentMacrosBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment

class CaloriesFragment : BaseFragment<NutritionViewModel, FragmentCaloriesBinding, NutritionRepository>() {

    override fun getViewModel()= NutritionViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentCaloriesBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NutritionRepository {
        val api=remoteDataSource.buildApi(NutritionApi::class.java)
        return NutritionRepository(
            api = api,
            userPreferences = userPreferences)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        viewModel.fetchDailyCalories(viewModel.datePicked.value.toString())
    }
    private fun observeViewModel() {
        viewModel.caloriesData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("AccountDetailFragment", "ProfileData observed: $data")
                setUpView(data)
            } else {
                Log.d("AccountDetailFragment", "ProfileData is null")
            }
        }
        viewModel.datePicked.observe(viewLifecycleOwner) { date ->
            // Use the date as needed
            viewModel.fetchDailyCalories(viewModel.datePicked.value.toString())
        }

    }
    fun setUpView(data:DailyCaloriesDto){
        val buaSangCalories = data.caloriesByMeal["BuaSang"]?.toFloat() ?: 0f
        val buaTruaCalories = data.caloriesByMeal["BuaTrua"]?.toFloat() ?: 0f
        val buaToiCalories = data.caloriesByMeal["BuaToi"]?.toFloat() ?: 0f
        val buaPhuCalories = data.caloriesByMeal["BuaPhu"]?.toFloat() ?: 0f

        val totalMealCalories = buaSangCalories + buaTruaCalories + buaToiCalories + buaPhuCalories

// Calculate the percentage for each meal based on the total meal calories
        val buaSangPercentage = if (totalMealCalories > 0) (buaSangCalories / totalMealCalories) * 100 else 0f
        val buaTruaPercentage = if (totalMealCalories > 0) (buaTruaCalories / totalMealCalories) * 100 else 0f
        val buaToiPercentage = if (totalMealCalories > 0) (buaToiCalories / totalMealCalories) * 100 else 0f
        val buaPhuPercentage = if (totalMealCalories > 0) (buaPhuCalories / totalMealCalories) * 100 else 0f
        binding.customCircularProgressBar.setPercentages(buaSangPercentage,buaPhuPercentage, buaTruaPercentage, buaToiPercentage)

        val percentage= totalMealCalories/data.goalCalories *100
        binding.tvProgressPercentage.text= percentage.toInt().toString()+"%"
        binding.tvBreakfastPercent.text= buaSangPercentage.toInt().toString()+"%"
        binding.tvLunchPercent.text= buaTruaPercentage.toInt().toString()+"%"
        binding.tvDinnerPercent.text=buaToiPercentage.toInt().toString()+"%"
        binding.tvSnackPercent.text=buaPhuPercentage.toInt().toString()+"%"
        binding.tvTotalCalories.text= data.totalCalories.toString()
        binding.tvNetCalories.text= data.netCalories.toString()
        binding.tvGoalCalories.text= data.goalCalories.toString()
        binding.tvFood1.text= data.foodsWithHighestCalories.foodName + " " +data.foodsWithHighestCalories.calories+"g"
    }

}