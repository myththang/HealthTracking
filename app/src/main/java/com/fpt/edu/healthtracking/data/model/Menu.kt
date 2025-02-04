package com.fpt.edu.healthtracking.data.model

data class Menu(
    val mealPlanId: Int,
    val name: String,
    val shortDescription: String?,
    val mealPlanImage: String,
    val totalCalories: Int
)
data class MenuDetail(
    val mealPlanId: Int,
    val mealPlanImage: String,
    val shortDescription: String?,
    val longDescription: String?,
    val dietId: Int,
    val name: String,
    val totalCalories: Double,
    val mainFoodImageForBreakfast: String,
    val breakfastFoods: List<MenuFood>,
    val mainFoodImageForLunch: String,
    val lunchFoods: List<MenuFood>,
    val mainFoodImageForDinner: String,
    val dinnerFoods: List<MenuFood>,
    val mainFoodImageForSnack: String,
    val snackFoods: List<MenuFood>
)

data class MenuFood(
    val foodId: Int,
    val foodName: String,
    val foodImage: String,
    val calories: Double,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val dietName: String,
    val quantity: Int
)