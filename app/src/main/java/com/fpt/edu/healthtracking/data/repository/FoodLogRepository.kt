package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.adapters.CalendarAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.FoodLog
import com.fpt.edu.healthtracking.data.model.FoodLogState
import com.fpt.edu.healthtracking.data.model.Meal
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.responses.FoodDiaryItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log
import kotlin.math.roundToInt

class FoodLogRepository(
    private val api: FoodApi,
    private val userPreferences: UserPreferences
) : BaseRepository() {
    suspend fun getFoodLog(token: String, date: Date): Resource<FoodLogState> {
        return safeApiCall {
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            val response = api.getFoodDiary("Bearer $token", dateString)
            val goalCalories = response.foodDiaryInforMember.goalCalories.toInt() // Thêm toInt() ở đây
            val consumedCalories = response.foodDiaryInforMember.calories.roundToInt() // Thêm roundToInt() ở đây
            val breakfastTarget = (goalCalories * 0.30).toInt()
            val lunchTarget = (goalCalories * 0.35).toInt()
            val dinnerTarget = (goalCalories * 0.25).toInt()
            val snackTarget = (goalCalories * 0.10).toInt()
            FoodLogState(
                targetCalories = goalCalories,
                consumedCalories = consumedCalories,
                exerciseCalories = 0,
                remainingCalories = goalCalories - consumedCalories,
                diaryId = response.foodDiaryInforMember.diaryId,
                meals = listOf(
                    createMeal(MealType.BREAKFAST, response.foodDiaryForMealBreakfast, breakfastTarget),
                    createMeal(MealType.LUNCH, response.foodDiaryForMealLunch, lunchTarget),
                    createMeal(MealType.DINNER, response.foodDiaryForMealDinner, dinnerTarget),
                    createMeal(MealType.SNACK, response.foodDiaryForMealSnack, snackTarget)
                )
            )
        }
    }
    suspend fun getFoodStreak(token: String, date: String) = safeApiCall {
        api.getFoodStreak("Bearer $token", date)
    }

    private fun createMeal(type: MealType, items: List<FoodDiaryItem>, targetCalories: Int): Meal {
        val foods = items.map { item ->
            FoodLog(
                diaryDetailId = item.diaryDetailId,
                foodId = item.foodId,
                foodName = item.foodName,
                servingSize = "${item.quantity} x ${item.portion}",
                calories = (item.calories * item.quantity).toInt(),
                mealType = item.mealType
            )
        }

        return Meal(
            type = type,
            foods = foods,
            targetCalories = targetCalories
        )
    }
    suspend fun deleteFoodFromDiary(diaryDetailId: Int) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.deleteFoodFromDiary(diaryDetailId, token)
    }

    suspend fun getSpecialDays(token: String): Resource<List<CalendarDay>> {
        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            when (val result = getFoodStreak(token, currentDate)) {
                is Resource.Success -> {
                    val streakDays = result.value.dates.mapNotNull { dateStr ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = formatter.parse(dateStr) ?: return@mapNotNull null
                        val calendar = Calendar.getInstance().apply {
                            time = date
                        }
                        CalendarDay.from(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    Resource.Success(streakDays)
                }
                is Resource.Failure -> Resource.Failure(false, null, null)
            }
        } catch (e: Exception) {
            Resource.Failure(false, null, null)
        }
    }

}