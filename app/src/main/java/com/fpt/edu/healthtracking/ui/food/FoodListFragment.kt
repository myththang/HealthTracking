package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.FoodItemAdapter
import com.fpt.edu.healthtracking.adapters.food.FoodListType
import com.fpt.edu.healthtracking.adapters.food.MealMemberAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.model.MemberMeal
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import com.fpt.edu.healthtracking.databinding.FragmentAllFoodBinding
import com.fpt.edu.healthtracking.databinding.FragmentMyFoodBinding
import com.fpt.edu.healthtracking.databinding.FragmentMyMealBinding
import kotlinx.coroutines.launch

class FoodListFragment : Fragment() {
    private var _binding: ViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodAdapter: FoodItemAdapter
    private var listType: FoodListType = FoodListType.ALL
    private lateinit var viewModel: AddFoodViewModel
    private var diaryId: Int = -1
    private var mealType: MealType? = null

    private lateinit var userPreferences: UserPreferences
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: FoodRepository
    private lateinit var mealAdapter: MealMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getSerializable(ARG_LIST_TYPE) as? FoodListType ?: FoodListType.ALL
            diaryId = it.getInt(ARG_DIARY_ID, -1)
            mealType = it.getSerializable(ARG_MEAL_TYPE) as? MealType
        }

        userPreferences = UserPreferences(requireContext())
        remoteDataSource = RemoteDataSource()
        val api = remoteDataSource.buildApi(FoodApi::class.java)
        repository = FoodRepository(api, userPreferences)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = when (listType) {
            FoodListType.ALL -> FragmentAllFoodBinding.inflate(inflater, container, false)
            FoodListType.MY_FOODS -> FragmentMyFoodBinding.inflate(inflater, container, false)
            FoodListType.MY_MEALS -> FragmentMyMealBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireParentFragment() as AddFoodFragment).viewModel
        when (listType) {
            FoodListType.ALL -> setupAllFoodsView()
            FoodListType.MY_FOODS -> setupMyFoodsView()
            FoodListType.MY_MEALS -> setupMyMealsView()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.foods.observe(viewLifecycleOwner) { foods ->
            when (listType) {
                FoodListType.ALL -> {
                    val allFoodBinding = _binding as FragmentAllFoodBinding
                    val isSearching = foods.isNotEmpty()
                    allFoodBinding.apply {
                        layoutHistory.visibility = if(isSearching) View.GONE else View.VISIBLE
                        layoutSuggestions.visibility = if(isSearching) View.GONE else View.VISIBLE

                        if(isSearching) {
                            rvSearchResults.visibility = View.VISIBLE
                            (rvSearchResults.adapter as? FoodItemAdapter)?.submitList(foods)
                        } else {
                            rvSearchResults.visibility = View.GONE
                            viewModel.loadHistoryFoods()
                            viewModel.loadSuggestedFoods()
                        }
                    }
                }
                FoodListType.MY_FOODS -> {
                    val myFoodBinding = _binding as FragmentMyFoodBinding
                    (myFoodBinding.rvMyFoods.adapter as? FoodItemAdapter)?.submitList(foods)
                }
                FoodListType.MY_MEALS -> {
                }
            }
        }

        viewModel.historyFoods.observe(viewLifecycleOwner) { historyFoods ->
            when (listType) {
                FoodListType.ALL -> {
                    val allFoodBinding = _binding as FragmentAllFoodBinding
                    if (viewModel.foods.value?.isEmpty() != false) { // Only show if not searching
                        allFoodBinding.apply {
                            layoutHistory.visibility = if (historyFoods.isEmpty()) View.GONE else View.VISIBLE
                            rvHistory.visibility = if (historyFoods.isEmpty()) View.GONE else View.VISIBLE
                            (rvHistory.adapter as? FoodItemAdapter)?.submitList(historyFoods)
                        }
                    }
                }
                else -> { /* Do nothing */ }
            }
        }

        viewModel.suggestedFoods.observe(viewLifecycleOwner) { suggestedFoods ->
            when (listType) {
                FoodListType.ALL -> {
                    val allFoodBinding = _binding as FragmentAllFoodBinding
                    if (viewModel.foods.value?.isEmpty() != false) { // Only show if not searching
                        allFoodBinding.apply {
                            layoutSuggestions.visibility = if (suggestedFoods.isEmpty()) View.GONE else View.VISIBLE
                            rvSuggestions.visibility = if (suggestedFoods.isEmpty()) View.GONE else View.VISIBLE
                            (rvSuggestions.adapter as? FoodItemAdapter)?.submitList(suggestedFoods)
                        }
                    }
                }
                else -> { /* Do nothing */ }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            when (listType) {
                FoodListType.ALL -> {
                    (_binding as FragmentAllFoodBinding).progressBar.visibility =
                        if (isLoading) View.VISIBLE else View.GONE
                }

                FoodListType.MY_FOODS -> {

                }

                FoodListType.MY_MEALS -> {
                }
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToFoodDetail(foodId: Int) {
        val mealTypeValue = when (mealType) {
            MealType.BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.DINNER -> 3
            MealType.SNACK -> 4
            null -> 1
        }

        findNavController().navigate(
            R.id.action_addFoodFragment_to_foodDetailFragment,
            bundleOf(
                "food_id" to foodId,
                "diary_id" to diaryId,
                "meal_type" to mealTypeValue
            )
        )
    }
    private fun navigateToMealDetail(foodId: Int) {
        val mealTypeValue = when (mealType) {
            MealType.BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.DINNER -> 3
            MealType.SNACK -> 4
            null -> 1
        }

        findNavController().navigate(
            R.id.action_addFoodFragment_to_mealDetailFragment,
            bundleOf(
                "meal_id" to foodId,
                "diary_id" to diaryId,
                "meal_type" to mealTypeValue
            )
        )
    }

    private fun setupAllFoodsView() {
        val binding = _binding as FragmentAllFoodBinding

        val historyAdapter = FoodItemAdapter(
            onFoodClick = { food -> navigateToFoodDetail(food.foodId) },
            onAddClick = { food -> handleAddFood(food.foodId) }
        )
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val suggestionsAdapter = FoodItemAdapter(
            onFoodClick = { food -> navigateToFoodDetail(food.foodId) },
            onAddClick = { food -> handleAddFood(food.foodId) }
        )
        binding.rvSuggestions.apply {
            adapter = suggestionsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val searchAdapter = FoodItemAdapter(
            onFoodClick = { food -> navigateToFoodDetail(food.foodId) },
            onAddClick = { food -> handleAddFood(food.foodId) }
        )
        binding.rvSearchResults.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.loadHistoryFoods()
        viewModel.loadSuggestedFoods()
    }

    private fun handleAddFood(foodId: Int) {
        val mealTypeValue = when (mealType) {
            MealType.BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.DINNER -> 3
            MealType.SNACK -> 4
            null -> 1
        }
        lifecycleScope.launch {
            try {
                when (val result = repository.addFoodToDiary(
                    foodId = foodId,
                    diaryId = diaryId,
                    mealType = mealTypeValue,
                    quantity = 1
                )) {
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Thêm món ăn thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Không thể thêm món ăn. Vui lòng thử lại sau",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Đã xảy ra lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupMyFoodsView() {
        val binding = _binding as FragmentMyFoodBinding

        binding.btnCreateMeal.setOnClickListener {
        }

        binding.btnQuickAdd.setOnClickListener {
        }

        val myFoodsAdapter = FoodItemAdapter(
            onFoodClick = { food ->
                navigateToFoodDetail(food.foodId)
            },
            onAddClick = { food ->
                setFragmentResult("selected_food", bundleOf(
                    "food_id" to food.foodId,
                    "amount" to 1f,
                    "unit" to "g"
                ))
                findNavController().navigateUp()
            }
        )
        binding.rvMyFoods.apply {
            adapter = myFoodsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        loadMyFoods(myFoodsAdapter)
    }

    private fun handleDeleteMeal(meal: MemberMeal) {
        lifecycleScope.launch {
            try {
                when (val result = repository.deleteMealMember(meal.mealMemberId)) {
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Xóa bữa ăn thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        setupMyFoodsView()
                    }
                    is Resource.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Không thể xóa bữa ăn. Vui lòng thử lại sau",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun setupMyMealsView() {
        val binding = _binding as FragmentMyMealBinding

        binding.cardCreateMeal.setOnClickListener {
            findNavController().navigate(R.id.action_addFoodFragment_to_createMealFragment)
        }
        val mealTypeValue  = when (mealType) {
            MealType.BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.DINNER -> 3
            MealType.SNACK -> 4
            null -> 1
        }
        lifecycleScope.launch {
            try {
                when (val result = repository.checkPreviousMeals(mealTypeValue)) {
                    is Resource.Success -> {
                        val hasPreviousMeal = result.value
                        binding.cardCopyMeal.apply {
                            if (hasPreviousMeal > 0) {
                                binding.ivCopyMeal.apply {
                                    setColorFilter(
                                        ContextCompat.getColor(requireContext(), R.color.teal)
                                    )
                                    alpha = 1.0f
                                }
                                binding.tvCopyMeal.apply {
                                    setTextColor(ContextCompat.getColor(requireContext(), R.color.teal))
                                    alpha = 1.0f
                                }
                                isEnabled = true
                                setOnClickListener {
                                    findNavController().navigate(
                                        R.id.action_addFoodFragment_to_previousMealFragment,
                                        bundleOf(
                                            "diary_id" to result.value,
                                            "meal_type" to mealTypeValue,
                                            "current" to diaryId
                                        )
                                    )
                                }
                            } else {
                                binding.ivCopyMeal.apply {
                                    setColorFilter(
                                        ContextCompat.getColor(requireContext(), R.color.gray_600)
                                    )
                                    alpha = 0.5f
                                }
                                binding.tvCopyMeal.apply {
                                    setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600))
                                    alpha = 0.5f
                                }
                                isEnabled = false
                            }
                        }
                    }
                    is Resource.Failure -> {
                        Toast.makeText(context, "Lỗi kiểm tra bữa ăn trước", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        val mealAdapter = MealMemberAdapter(
            onMealClick = { meal ->
                navigateToMealDetail(meal.mealMemberId)
            },
            onAddClick = { meal ->
                handleAddMeal(meal)
            },
            onDeleteClick = { meal ->
                handleDeleteMeal(meal)
            }
        )

        binding.rvMeals.apply {
            adapter = mealAdapter
            layoutManager = LinearLayoutManager(context)
            mealAdapter.setupSwipeToDelete(this)
        }

        lifecycleScope.launch {
            try {
                when (val result = repository.getMemberMeals()) {
                    is Resource.Success -> {
                        val meals = result.value
                        if (meals.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvMeals.visibility = View.GONE
                            binding.tvMeals.visibility = View.GONE
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvMeals.visibility = View.VISIBLE
                            binding.tvMeals.visibility = View.VISIBLE
                            mealAdapter.submitList(meals)
                        }
                    }
                    is Resource.Failure -> {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.rvMeals.visibility = View.GONE
                        binding.tvMeals.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvMeals.visibility = View.GONE
                binding.tvMeals.visibility = View.GONE
                Toast.makeText(
                    context,
                    "Đã xảy ra lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleAddMeal(meal: MemberMeal) {
        val mealTypeValue = when (mealType) {
            MealType.BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.DINNER -> 3
            MealType.SNACK -> 4
            null -> 1
        }

        lifecycleScope.launch {
            try {
                val result = repository.addMealToDiary(
                    diaryId = diaryId,
                    mealId = meal.mealMemberId,
                    mealType = mealTypeValue
                )

                Log.d("AddMeal", "Result: $result")

                when (result) {
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Thêm bữa ăn thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Failure -> {
                        val errorMessage = when {
                            result.isNetworkError -> "Lỗi kết nối mạng"
                            result.errorCode != null -> "Lỗi máy chủ: ${result.errorCode}"
                            else -> "Không thể thêm bữa ăn. Vui lòng thử lại sau"
                        }
                        Toast.makeText(
                            requireContext(),
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AddMeal", "Error adding meal", e)
                Toast.makeText(
                    requireContext(),
                    "Đã xảy ra lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    private fun loadMyFoods(adapter: FoodItemAdapter) {
//        val foods = listOf(
//            Food(
//                foodId = 1,
//                foodName = "Cơm trắng",
//                foodImage = "path/to/rice.jpg",
//                calories = 130,
//                protein = 2f,
//                carbs = 28f,
//                fat = 0.3f,
//                dietName = "Bình thường"
//            ),
//            Food(
//                foodId = 2,
//                foodName = "Thịt gà luộc",
//                foodImage = "path/to/chicken.jpg",
//                calories = 165,
//                protein = 31f,
//                carbs = 0f,
//                fat = 3.6f,
//                dietName = "Bình thường"
//            ),
//            Food(
//                foodId = 3,
//                foodName = "Trứng chiên",
//                foodImage = "path/to/egg.jpg",
//                calories = 90,
//                protein = 6f,
//                carbs = 1f,
//                fat = 7f,
//                dietName = "Low-carb"
//            )
//        )
//        adapter.submitList(foods)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LIST_TYPE = "list_type"
        private const val ARG_DIARY_ID = "diary_id"
        private const val ARG_MEAL_TYPE = "meal_type"

        fun newInstance(
            type: FoodListType,
            diaryId: Int,
            mealType: MealType?
        ) = FoodListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_LIST_TYPE, type)
                putInt(ARG_DIARY_ID, diaryId)
                putSerializable(ARG_MEAL_TYPE, mealType)
            }
        }
    }
}
