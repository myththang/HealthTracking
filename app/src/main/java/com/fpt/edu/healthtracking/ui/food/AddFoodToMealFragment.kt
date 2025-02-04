package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.FoodItemAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.AddFoodRepository
import com.fpt.edu.healthtracking.databinding.FragmentAddFoodToMealBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar

class AddFoodToMealFragment : BaseFragment<AddFoodToMealViewModel, FragmentAddFoodToMealBinding, AddFoodRepository>() {

    private lateinit var foodAdapter: FoodItemAdapter
    private var mealId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupObservers()

        viewModel.loadAllFoods()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodItemAdapter(
            onFoodClick = { food ->
//                findNavController().navigate(
//                    navigateToFoodDetail(food.foodId)
//                )
            },
            onAddClick = { food ->
                addFoodToMeal(food)
            }
        )

        binding.rvFoods.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun navigateToFoodDetail(foodId: Int) {
       findNavController().navigate(
          R.id.action_addFoodFragment_to_foodDetailFragment,
           bundleOf("food_id" to foodId)
        )
    }

    private fun setupSearchBar() {
        binding.searchBar.apply {
            addTextChangedListener { text ->
                viewModel.searchFoods(text?.toString() ?: "")
            }
        }
    }

    private fun setupObservers() {
        viewModel.foods.observe(viewLifecycleOwner) { foods ->
            foodAdapter.submitList(foods)
            updateEmptyState(foods.isEmpty())
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { viewModel.loadAllFoods() }
                    .show()
                viewModel.clearError()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            rvFoods.isVisible = !isEmpty
            emptyStateLayout.isVisible = isEmpty

            if (isEmpty) {
                if (searchBar.text.isNullOrBlank()) {
                    emptyStateText.text = "No foods available"
                } else {
                    emptyStateText.text = "No foods found matching your search"
                }
            }
        }
    }

    private fun addFoodToMeal(food: Food) {
        setFragmentResult(
            "selected_food",
            bundleOf(
                "food_id" to food.foodId,
                "food_name" to food.foodName,
                "calories" to food.calories,
                "protein" to food.protein,
                "carbs" to food.carbs,
                "fat" to food.fat,
                "food_image" to food.foodImage,
                "diet_name" to food.dietName,
                "quantity" to 1
            )
        )
        findNavController().navigateUp()
    }

    override fun getViewModel() = AddFoodToMealViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddFoodToMealBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AddFoodRepository(remoteDataSource.buildApi(FoodApi::class.java), userPreferences)
}