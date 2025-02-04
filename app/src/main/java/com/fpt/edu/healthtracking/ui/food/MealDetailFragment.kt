package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.MealDetailFoodAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.MealDetailRepository
import com.fpt.edu.healthtracking.databinding.FragmentMealDetailBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class MealDetailFragment : BaseFragment<MealDetailViewModel, FragmentMealDetailBinding, MealDetailRepository>() {

    private lateinit var foodAdapter: MealDetailFoodAdapter
    private var diaryId: Int = -1
    private var mealType: Int = -1
    private var mealId: Int = -1

    override fun getViewModel(): Class<MealDetailViewModel> {
        return MealDetailViewModel::class.java
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMealDetailBinding {
        return FragmentMealDetailBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepository(): MealDetailRepository {
        val api = remoteDataSource.buildApi(FoodApi::class.java)
        return MealDetailRepository(
            api = api,
            preferences = userPreferences
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mealId = arguments?.getInt("meal_id", -1) ?: -1
        mealType = arguments?.getInt("meal_type", -1) ?: -1
        diaryId = arguments?.getInt("diary_id", -1) ?: -1

        setupViews()
        setupObservers()
        viewModel.loadMealDetail(mealId)
        setupFragmentResultListener()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener{
                findNavController().navigateUp()
            }

            foodAdapter = MealDetailFoodAdapter(
                onDeleteClick = { mealFood ->
                }
            )
            rvMealItems.apply {
                adapter = foodAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
            binding.apply {
                btnSave.setOnClickListener {
                    viewModel.addMealToDiary(diaryId, mealId, mealType)
                }
            }


        }
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
                        .load(imageUrl)
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
                is MealDetailViewModel.SavingState.Idle -> {
                    binding.loadingOverlay.visibility = View.GONE
                }
                is MealDetailViewModel.SavingState.Saving -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                }
                is MealDetailViewModel.SavingState.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(context, "Meal saved successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.navigation_food_log, false)
                }
                is MealDetailViewModel.SavingState.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun setupFragmentResultListener() {
        setFragmentResultListener("selected_food") { _, bundle ->
            val food = Food(
                foodId = bundle.getInt("food_id"),
                foodName = bundle.getString("food_name", ""),
                calories = bundle.getDouble("calories"),
                protein = bundle.getFloat("protein"),
                carbs = bundle.getFloat("carbs"),
                fat = bundle.getFloat("fat"),
                foodImage = bundle.getString("food_image", ""),
                dietName = bundle.getString("diet_name", "")
            )
            val quantity = bundle.getInt("quantity", 1)
            Log.d("CreateMealFragment", "Food added: ${food.foodName} with quantity: $quantity")
            Snackbar.make(binding.root, "Added ${food.foodName}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateNutritionDisplay(info: MealDetailViewModel.NutritionInfo) {
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


}