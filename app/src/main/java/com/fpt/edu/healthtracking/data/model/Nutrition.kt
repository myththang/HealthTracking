package com.fpt.edu.healthtracking.data.model

data class DailyCaloriesDto(
    val caloriesByMeal: Map<String, Double> = emptyMap(), // Calories phân theo MealType
    val totalCalories: Double, // Tổng calories
    val netCalories: Double, // Net calories (Total - tiêu thụ)
    val goalCalories: Double, // Mục tiêu calories
    val foodsWithHighestCalories: FoodCaloriesDto // Danh sách món ăn
)

data class FoodCaloriesDto(
    val foodDiaryDetailId: Int,
    val foodName: String,
    val calories: Double
)

data class DailyNutritionDto(
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalVitaminA: Double,
    val totalVitaminC: Double,
    val totalVitaminD: Double,
    val totalVitaminE: Double,
    val totalVitaminB1: Double,
    val totalVitaminB2: Double,
    val totalVitaminB3: Double
)

data class MacroNutrientDto(
    val totalCarbs: Double,
    val totalFat: Double,
    val totalProtein: Double,
    val highestCarbFood: FoodMacroDto,
    val highestFatFood: FoodMacroDto,
    val highestProteinFood: FoodMacroDto
)

data class FoodMacroDto(
    val foodName: String,
    val quantity: Double,
    val macroValue: Double
)
