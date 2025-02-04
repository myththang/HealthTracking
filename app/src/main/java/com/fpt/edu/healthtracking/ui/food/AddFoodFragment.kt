package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.adapters.food.FoodPagerAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.repository.AddFoodRepository
import com.fpt.edu.healthtracking.databinding.FragmentAddFoodBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddFoodFragment : BaseFragment<AddFoodViewModel, FragmentAddFoodBinding, AddFoodRepository>() {
    private lateinit var viewPagerAdapter: FoodPagerAdapter
    private var mealType: MealType? = null
    private var searchJob: Job? = null
    private var diaryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mealType = arguments?.getSerializable("meal_type") as? MealType
        diaryId = arguments?.getInt("diary_id", -1) ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (diaryId == -1) {
            Toast.makeText(context, "Invalid diary ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        setupViewPager()
        setupUI()
        setupSearch()
    }

    private fun setupViewPager() {
        viewPagerAdapter = FoodPagerAdapter(this,diaryId, mealType)
        binding.viewPager.apply {
            adapter = viewPagerAdapter
            isUserInputEnabled = true
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Tất cả"
                1 -> "Bữa của tôi"
               // 2 -> "Món ăn của tôi"
                else -> ""
            }
        }.attach()
    }

    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            tvMealTitle.text = when(mealType) {
                MealType.BREAKFAST -> "Bữa sáng"
                MealType.LUNCH -> "Bữa trưa"
                MealType.DINNER -> "Bữa tối"
                MealType.SNACK -> "Ăn vặt"
                null -> "Add Food"
            }

        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300) // Add delay
                text?.toString()?.let { query ->
                    if(query.length >= 2) { // Only search if query is at least 2 chars
                        viewModel.searchFood(query)
                    }
                }
            }
        }
    }

    override fun getViewModel() = AddFoodViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddFoodBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): AddFoodRepository {
        return AddFoodRepository(
            remoteDataSource.buildApi(FoodApi::class.java),
            userPreferences
        )
    }
}