package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.MealDetailFoodAdapter
import com.fpt.edu.healthtracking.adapters.food.MealFoodAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.PreviousMealRepository
import com.fpt.edu.healthtracking.data.responses.PreviousMealResponse
import com.fpt.edu.healthtracking.databinding.FragmentPreviousMealBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PreviousMealFragment : BaseFragment<PreviousFoodViewModel, FragmentPreviousMealBinding, PreviousMealRepository>() {
    private lateinit var foodAdapter: MealDetailFoodAdapter
    private var diaryId: Int = -1
    private var mealType: Int = -1
    private var currentDiaryId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)

        diaryId = arguments?.getInt("diary_id") ?: -1
        mealType = arguments?.getInt("meal_type") ?: -1
        currentDiaryId = arguments?.getInt("current") ?: -1


        setupViews()
        setupObservers()
        viewModel.getPreviousMeal(diaryId,mealType)
    }

    private fun setupObservers() {
        viewModel.apply {
            loading.observe(viewLifecycleOwner) { isLoading ->
                binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            error.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }

            mealName.observe(viewLifecycleOwner) { name ->
                binding.etMealName.setText(name)
            }

            mealImage.observe(viewLifecycleOwner) { imageUrl ->
                imageUrl?.let {
                    binding.addPhotoLayout.visibility = View.GONE
                    Glide.with(requireContext())
                        .load(R.drawable.placeholder_food)
                        .placeholder(R.drawable.placeholder_food)
                        .error(R.drawable.error_food)
                        .centerCrop()
                        .into(binding.mealImage)
                }
            }

            mealFoods.observe(viewLifecycleOwner) { foods ->
                Log.d("MealDetail", "Received foods size: ${foods?.size}")
                foods?.let { nonNullFoods ->
                    Log.d("MealDetail", "Foods: $nonNullFoods")
                    foodAdapter.submitList(nonNullFoods)
                    foodAdapter.notifyDataSetChanged()
                }
            }
            nutritionInfo.observe(viewLifecycleOwner) { info ->
                updateNutritionDisplay(info)
            }
        }
        viewModel.savingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PreviousFoodViewModel.SavingState.Idle -> {
                    binding.loadingOverlay.visibility = View.GONE
                }
                is PreviousFoodViewModel.SavingState.Saving -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                }
                is PreviousFoodViewModel.SavingState.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(context, "Sao chép bữa ăn thành công", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.navigation_food_log, false)
                }
                is PreviousFoodViewModel.SavingState.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun updateNutritionDisplay(info: PreviousFoodViewModel.NutritionInfo) {
        binding.apply {

            tvCalories.text = "${info.calories}"


            macroProgress.setProgress(
                info.carbsPercent.toFloat(),
                info.fatPercent.toFloat(),
                info.proteinPercent.toFloat()
            )

            tvCarbsPercent.text = "${info.carbsPercent}%"
            tvCarbs.text = "${info.carbs.roundToInt()}g"

            tvFatPercent.text = "${info.fatPercent}%"
            tvFat.text = "${info.fat.roundToInt()}g"

            tvProteinPercent.text = "${info.proteinPercent}%"
            tvProtein.text = "${info.protein.roundToInt()}g"
        }
    }

    private fun setupViews() {
        foodAdapter = MealDetailFoodAdapter{}

        binding.rvMealItems.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            if (diaryId != -1 && mealType != -1 && currentDiaryId != -1) {
                viewModel.copyPreviousMeal(diaryId, mealType, currentDiaryId)
            } else {
                Toast.makeText(context, "Không thể sao chép bữa ăn này", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateUI(data: PreviousMealResponse) {
        binding.apply {
            tvCalories.text = data.totalCalories.toString()

            val total = data.totalProtein + data.totalCarb + data.totalFat
            val proteinPercent = (data.totalProtein / total * 100).toInt()
            val carbsPercent = (data.totalCarb / total * 100).toInt()
            val fatPercent = (data.totalFat / total * 100).toInt()

            //macroProgress.setPercentages(carbsPercent.toFloat(), fatPercent.toFloat(), proteinPercent.toFloat())

            tvProteinPercent.text = "$proteinPercent%"
            tvCarbsPercent.text = "$carbsPercent%"
            tvFatPercent.text = "$fatPercent%"

            tvProtein.text = "${data.totalProtein.toInt()}g"
            tvCarbs.text = "${data.totalCarb.toInt()}g"
            tvFat.text = "${data.totalFat.toInt()}g"

            foodAdapter.submitList(data.foodDetails)
        }
    }

    override fun getViewModel() = PreviousFoodViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPreviousMealBinding {
        return FragmentPreviousMealBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepository() = PreviousMealRepository(remoteDataSource.buildApi(FoodApi::class.java), userPreferences)
}