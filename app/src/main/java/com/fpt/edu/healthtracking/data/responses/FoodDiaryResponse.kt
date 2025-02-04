package com.fpt.edu.healthtracking.data.responses

data class FoodDiaryResponse(
    val foodDiaryInforMember: FoodDiaryInfo,
    val foodDiaryForMealBreakfast: List<FoodDiaryItem>,
    val foodDiaryForMealLunch: List<FoodDiaryItem>,
    val foodDiaryForMealDinner: List<FoodDiaryItem>,
    val foodDiaryForMealSnack: List<FoodDiaryItem>
)

data class FoodDiaryInfo(
    val diaryId: Int,
    val memberId: Int,
    val date: String,
    val goalCalories: Int,
    val calories: Double,
    val protein: Float,
    val fat: Float,
    val carbs: Float
)

data class FoodDiaryItem(
    val diaryDetailId: Int,
    val diaryId: Int,
    val foodId: Int,
    val foodName: String,
    val calories: Double,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val quantity: Int,
    val mealType: Int,
    val portion: String
)