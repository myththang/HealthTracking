package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.model.FoodDetail
import com.fpt.edu.healthtracking.data.repository.FoodDetailRepository
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import com.fpt.edu.healthtracking.data.responses.FoodDetailResponse
import com.fpt.edu.healthtracking.databinding.FragmentFoodDetailBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class FoodDetailFragment : BaseFragment<FoodDetailViewModel, FragmentFoodDetailBinding, FoodRepository>() {
    private var foodId: Int = -1
    private var diaryId: Int = -1
    private var mealType: Int = 1
    private var isInputValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            foodId = bundle.getInt("food_id", -1)
            diaryId = bundle.getInt("diary_id", -1)
            mealType = bundle.getInt("meal_type", 1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        setupServingInput()
        viewModel.loadFoodDetail(foodId)
    }

    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnAddDiary.setOnClickListener {
                handleAddToDiaryClick()
            }
            
            btnAddDiary.isEnabled = false
        }
    }

    private fun setupServingInput() {
        binding.apply {
            // Theo dõi thay đổi text
            etAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    validateAndUpdateNutrition(s?.toString())
                }
            })
        }
    }

    private fun validateAndUpdateNutrition(amountStr: String?) {
        binding.apply {
            if (amountStr.isNullOrEmpty()) {
                tilAmount.error = "Vui lòng nhập số lượng"
                btnAddDiary.isEnabled = false
                isInputValid = false
                return
            }

            try {
                val amount = amountStr?.toFloatOrNull() ?: 0f
                
                when {
                    amount <= 0 -> {
                        tilAmount.error = "Số lượng phải lớn hơn 0"
                        btnAddDiary.isEnabled = false
                        isInputValid = false
                    }
                    amount > 4 -> { 
                        tilAmount.error = "Bạn đang ăn hơi nhiều"
                        btnAddDiary.isEnabled = false
                        isInputValid = false
                    }
                    !amountStr.matches("^\\d*\\.?\\d*$".toRegex()) -> {
                        tilAmount.error = "Vui lòng chỉ nhập số"
                        btnAddDiary.isEnabled = false
                        isInputValid = false
                    }
                    else -> {
                        tilAmount.error = null
                        btnAddDiary.isEnabled = true
                        isInputValid = true
                        
                        viewModel.foodDetail.value?.let { food ->
                            // Cập nhật hiển thị dinh dưỡng theo số serving
                            tvCalories.text = "${(food.calories * amount).roundToInt()} cal"
                            tvProtein.text = "${(food.protein * amount).roundToInt()}g"
                            tvCarbs.text = "${(food.carbs * amount).roundToInt()}g"
                            tvFats.text = "${(food.fat * amount).roundToInt()}g"
                            
                            // Cập nhật phần trăm mục tiêu
                            updateGoalPercentages(food, amount)
                        }
                    }
                }

            } catch (e: NumberFormatException) {
                tilAmount.error = "Vui lòng nhập số hợp lệ"
                btnAddDiary.isEnabled = false
                isInputValid = false
            }
        }
    }

    private fun updateGoalPercentages(food: FoodDetailResponse, servings: Float) {
        binding.apply {
            // Tính phần trăm dựa trên số serving
            val proteinPercentage = (food.protein * servings / food.totalProtein * 100).roundToInt()
            val carbsPercentage = (food.carbs * servings / food.totalCarb * 100).roundToInt()
            val fatsPercentage = (food.fat * servings / food.totalFat * 100).roundToInt()

            // Cập nhật progress
            goalProtein.progress = proteinPercentage
            progressGoalCarbs.progress = carbsPercentage
            goalFats.progress = fatsPercentage

            // Cập nhật text phần trăm
            tvProteinPercentage.text = "$proteinPercentage%"
            tvCarbsPercentage.text = "$carbsPercentage%"
            tvFatsPercentage.text = "$fatsPercentage%"

            // Cập nhật text hiển thị
            tvGoalProtein.text = "${(food.protein * servings).roundToInt()}g"
            tvGoalCarbs.text = "${(food.carbs * servings).roundToInt()}g"
            tvGoalFats.text = "${(food.fat * servings).roundToInt()}g"
        }
    }

    private fun setupObservers() {
        viewModel.foodDetail.observe(viewLifecycleOwner) { food ->
            binding.apply {
                tvFoodName.text = food.foodName
                tvFoodCalories.text = "${food.calories} cal"
                tvUnit.text = food.portion
                tvCalories.text = "${food.calories} cal"
                goalProtein.progress = viewModel.calculatePercentage(food.protein, food.totalProtein)
                progressGoalCarbs.progress = viewModel.calculatePercentage(food.carbs, food.totalCarb)
                goalFats.progress = viewModel.calculatePercentage(food.fat, food.totalFat)

                tvProtein.text = "${food.protein}g"
                tvCarbs.text = "${food.carbs}g"
                tvFats.text = "${food.fat}g"

                tvGoalProtein.text = "${food.totalProtein}g"
                tvGoalCarbs.text = "${food.totalCarb}g"
                tvGoalFats.text = "${food.totalFat}g"

                Glide.with(requireContext())
                    .load(food.foodImage)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .into(ivFood)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.addToDiaryResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Thêm món ăn thành công", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun handleAddToDiaryClick() {
        if (!isInputValid) {
            Toast.makeText(context, "Vui lòng kiểm tra lại số lượng", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = binding.etAmount.text.toString().toFloatOrNull() ?: return

        if (diaryId == -1) {
            Toast.makeText(context, "Invalid diary ID", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.foodDetail.value?.let { food ->
            viewModel.addFoodToDiary(
                foodId = food.foodId,
                diaryId = diaryId,
                mealType = mealType,
                quantity = amount.toInt()
            )
        }
    }

    override fun getViewModel() = FoodDetailViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFoodDetailBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = FoodRepository(remoteDataSource.buildApi(FoodApi::class.java), userPreferences)

    companion object {
        fun newInstance(foodId: Int) = FoodDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("food_id", foodId)
            }
        }
    }
}