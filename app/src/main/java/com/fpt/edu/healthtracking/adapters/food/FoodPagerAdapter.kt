package com.fpt.edu.healthtracking.adapters.food

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.ui.food.FoodListFragment

class FoodPagerAdapter(
    fragment: Fragment,
    private val diaryId: Int,
    private val mealType: MealType?
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FoodListFragment.newInstance(FoodListType.ALL, diaryId, mealType)
            1 -> FoodListFragment.newInstance(FoodListType.MY_MEALS, diaryId, mealType)
            //2 -> FoodListFragment.newInstance(FoodListType.MY_FOODS, diaryId, mealType)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
enum class FoodListType {
    ALL,
    MY_MEALS,
    MY_FOODS
}