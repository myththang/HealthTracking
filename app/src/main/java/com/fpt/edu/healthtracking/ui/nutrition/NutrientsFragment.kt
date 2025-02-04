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
import com.fpt.edu.healthtracking.data.model.DailyNutritionDto
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import com.fpt.edu.healthtracking.databinding.FragmentMacrosBinding
import com.fpt.edu.healthtracking.databinding.FragmentNutrientsBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment


class NutrientsFragment :
    BaseFragment<NutritionViewModel, FragmentNutrientsBinding, NutritionRepository>() {


    override fun getViewModel() = NutritionViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentNutrientsBinding.inflate(inflater,container,false)

    override fun getFragmentRepository(): NutritionRepository {
        val api=remoteDataSource.buildApi(NutritionApi::class.java)
        return NutritionRepository(
            api = api,
            userPreferences = userPreferences
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.fetchDailyNutrition(viewModel.datePicked.value.toString())
    }

    private fun observeViewModel() {
        viewModel.nutrientData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FragmentNutrientsBinding", "FragmentNutrientsBinding observed: $data")
                updateNutritionDisplay(data)
            } else {
                Log.d("FragmentNutrientsBinding", "FragmentNutrientsBinding is null")
            }
        }
        viewModel.datePicked.observe(viewLifecycleOwner) { date ->
            // Use the date as needed
            viewModel.fetchDailyNutrition(viewModel.datePicked.value.toString())
        }
    }

    private fun updateNutritionDisplay(data: DailyNutritionDto) {
        val sharedPreferences = context?.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        var goalCarb = 0f
        var goalFat = 0f
        var goalProtein = 0f

        if (sharedPreferences != null) {
            goalCarb = sharedPreferences.getFloat("totalCarb", 0f)
            goalFat = sharedPreferences.getFloat("totalFat", 0f)
            goalProtein = sharedPreferences.getFloat("totalProtein", 0f)
        }

        binding.apply {
            // Format với 1 số sau dấu phẩy
            tvCarbsValue.text = String.format("%.1fg", data.totalCarbs)
            tvFatValue.text = String.format("%.1fg", data.totalFat) 
            tvProteinValue.text = String.format("%.1fg", data.totalProtein)
            tvVitA.text = String.format("%.1fg", data.totalVitaminA)
            tvVitB1.text = String.format("%.1fg", data.totalVitaminB1)
            tvVitB2.text = String.format("%.1fg", data.totalVitaminB2) // Sửa B1 thành B2
            tvVitB3.text = String.format("%.1fg", data.totalVitaminB3) // Sửa B1 thành B3
            tvVitC.text = String.format("%.1fg", data.totalVitaminC)
            tvVitD.text = String.format("%.1fg", data.totalVitaminD)
            tvVitE.text = String.format("%.1fg", data.totalVitaminE)

            // Tính toán phần trăm
            val carbsPercentage = if (goalCarb > 0) (data.totalCarbs / goalCarb) * 100 else 0f
            val fatPercentage = if (goalFat > 0) (data.totalFat / goalFat) * 100 else 0f
            val proteinPercentage = if (goalProtein > 0) (data.totalProtein / goalProtein) * 100 else 0f

            // Cập nhật progress bars
            pbCarbs.progress = carbsPercentage.toInt()
            pbFat.progress = fatPercentage.toInt()
            pbProtein.progress = proteinPercentage.toInt()
        }
    }

}