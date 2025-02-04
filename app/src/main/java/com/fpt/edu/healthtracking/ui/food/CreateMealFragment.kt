package com.fpt.edu.healthtracking.ui.food

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.MealFoodAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.CreateMealRepository
import com.fpt.edu.healthtracking.databinding.FragmentCreateMealBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class CreateMealFragment : BaseFragment<CreateMealViewModel, FragmentCreateMealBinding, CreateMealRepository>() {

    private lateinit var foodAdapter: MealFoodAdapter
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.setSelectedImage(it)
        }
    }

    override fun getViewModel(): Class<CreateMealViewModel> {
        return CreateMealViewModel::class.java
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateMealBinding {
        return FragmentCreateMealBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepository(): CreateMealRepository {
        val api = remoteDataSource.buildApi(FoodApi::class.java)
        return CreateMealRepository(
            api = api,
            preferences = userPreferences
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        setupFragmentResultListener()
        setupImageHandling()
    }

    private fun setupViews() {
        binding.apply {
            etMealName.addTextChangedListener {
                viewModel.setMealName(it?.toString() ?: "")
            }

            btnBack.setOnClickListener{
                findNavController().navigateUp()
            }

            foodAdapter = MealFoodAdapter(
                onDeleteClick = { mealFood ->
                   viewModel.removeFood(mealFood)
               }
            )
            binding.rvMealItems.apply {
                adapter = foodAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }

            fabAdd.setOnClickListener {
                findNavController().navigate(
                    R.id.action_createMealFragment_to_addFoodToMealFragment
                )
            }

            btnSave.setOnClickListener {
                saveMeal()
            }
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            canSave.observe(viewLifecycleOwner) { canSave ->
                binding.btnSave.visibility = if (canSave) View.VISIBLE else View.GONE
            }

            selectedFoods.observe(viewLifecycleOwner) { foods ->
                Log.d("CreateMealFragment", "Foods in list: ${foods.size}")
                foodAdapter.submitList(foods)
                binding.rvMealItems.visibility = if (foods.isNotEmpty()) View.VISIBLE else View.GONE
                binding.layoutEmptyState.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
            }

            nutritionInfo.observe(viewLifecycleOwner) { info ->
                updateNutritionDisplay(info)
            }
            savingState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is CreateMealViewModel.SavingState.Saving -> {
                        Log.d("meal","saving")
                        binding.loadingOverlay.visibility = View.VISIBLE
                        binding.btnSave.isEnabled = false
                        binding.fabAdd.isEnabled = false
                    }
                    is CreateMealViewModel.SavingState.Success -> {
                        binding.loadingOverlay.visibility = View.GONE
                        Snackbar.make(binding.root, "Thêm bữa ăn thành công", Snackbar.LENGTH_SHORT).show()
                        Log.d("meal","saved")
                        findNavController().navigateUp()
                    }
                    is CreateMealViewModel.SavingState.Error -> {
                        binding.loadingOverlay.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        binding.fabAdd.isEnabled = true
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> { /* do nothing */ }
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
            viewModel.addFood(food, quantity)
            Log.d("CreateMealFragment", "Food added: ${food.foodName} with quantity: $quantity")
            Snackbar.make(binding.root, "Added ${food.foodName}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateNutritionDisplay(info: CreateMealViewModel.NutritionInfo) {
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

    private fun saveMeal() {
        viewModel.saveMeal(requireContext())
    }

    private fun setupImageHandling() {
        binding.apply {
            addPhotoLayout.setOnClickListener {
                launchImagePicker()
            }

            mealImage.setOnClickListener {
                launchImagePicker()
            }
        }

        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            updateImageView(uri)
        }
    }

    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun updateImageView(uri: Uri?) {
        binding.apply {
            if (uri != null) {
                addPhotoLayout.visibility = View.GONE
                mealImage.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .into(mealImage)
            } else {
                addPhotoLayout.visibility = View.VISIBLE
                mealImage.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.hasSelectedImage()) {
            viewModel.selectedImageUri.value?.let { uri ->
                updateImageView(uri)
            }
        }
    }

}