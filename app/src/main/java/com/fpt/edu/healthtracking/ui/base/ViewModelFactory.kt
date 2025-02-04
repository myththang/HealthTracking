package com.fpt.edu.healthtracking.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.AddFoodRepository
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.repository.BaseRepository
import com.fpt.edu.healthtracking.data.repository.ChatRepository
import com.fpt.edu.healthtracking.data.repository.CreateMealRepository
import com.fpt.edu.healthtracking.data.repository.DashBoardRepository
import com.fpt.edu.healthtracking.data.repository.ExercisePlanRepository
import com.fpt.edu.healthtracking.data.repository.ExerciseRepository
import com.fpt.edu.healthtracking.data.repository.FoodLogRepository
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import com.fpt.edu.healthtracking.data.repository.GoalRepository
import com.fpt.edu.healthtracking.data.repository.MealDetailRepository
import com.fpt.edu.healthtracking.data.repository.MenuRepository
import com.fpt.edu.healthtracking.data.repository.NotificationRepository
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import com.fpt.edu.healthtracking.data.repository.OnboardingRepository
import com.fpt.edu.healthtracking.data.repository.PreviousMealRepository
import com.fpt.edu.healthtracking.data.repository.StreakRepository
import com.fpt.edu.healthtracking.ui.auth.AuthViewModel
import com.fpt.edu.healthtracking.ui.chat.ChatTrainerViewModel
import com.fpt.edu.healthtracking.ui.food.AddFoodToMealViewModel
import com.fpt.edu.healthtracking.ui.food.AddFoodViewModel
import com.fpt.edu.healthtracking.ui.food.CreateMealViewModel
import com.fpt.edu.healthtracking.ui.food.FoodDetailViewModel
import com.fpt.edu.healthtracking.ui.food.FoodListViewModel
import com.fpt.edu.healthtracking.ui.food.FoodLogViewModel
import com.fpt.edu.healthtracking.ui.food.MealDetailViewModel
import com.fpt.edu.healthtracking.ui.home.HomeViewModel
import com.fpt.edu.healthtracking.ui.notifications.NotificationsViewModel
import com.fpt.edu.healthtracking.ui.nutrition.NutritionViewModel
import com.fpt.edu.healthtracking.ui.onboarding.OnboardingViewModel
import com.fpt.edu.healthtracking.ui.profile.AccountDetailViewModel
import com.fpt.edu.healthtracking.ui.exercise.ExerciseViewModel
import com.fpt.edu.healthtracking.ui.exercise_plan.ExercisePlanViewModel
import com.fpt.edu.healthtracking.ui.food.PreviousFoodViewModel
import com.fpt.edu.healthtracking.ui.menu.MenuViewModel
import com.fpt.edu.healthtracking.ui.profile.GoalViewModel
import com.fpt.edu.healthtracking.ui.profile.ProgressViewModel
import com.fpt.edu.healthtracking.ui.streak.StreakViewModel


@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(repository as OnboardingRepository) as T
            modelClass.isAssignableFrom(FoodLogViewModel::class.java) -> FoodLogViewModel(repository as FoodLogRepository) as T
            modelClass.isAssignableFrom(AddFoodViewModel::class.java) -> AddFoodViewModel(repository as AddFoodRepository) as T
            modelClass.isAssignableFrom(FoodDetailViewModel::class.java) -> FoodDetailViewModel(repository as FoodRepository) as T

            modelClass.isAssignableFrom(FoodListViewModel::class.java) -> FoodListViewModel(repository as FoodRepository) as T
            modelClass.isAssignableFrom(AddFoodToMealViewModel::class.java) -> AddFoodToMealViewModel(repository as AddFoodRepository) as T
            modelClass.isAssignableFrom(CreateMealViewModel::class.java) -> CreateMealViewModel(repository as CreateMealRepository) as T
            modelClass.isAssignableFrom(ExerciseViewModel::class.java) -> ExerciseViewModel(repository as ExerciseRepository) as T
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(repository as MenuRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository as DashBoardRepository) as T
            modelClass.isAssignableFrom(AccountDetailViewModel::class.java) -> AccountDetailViewModel(repository as AccountRepository) as T
            modelClass.isAssignableFrom(MealDetailViewModel::class.java) -> MealDetailViewModel(repository as MealDetailRepository) as T
            modelClass.isAssignableFrom(GoalViewModel::class.java) -> GoalViewModel(repository as GoalRepository)as T
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> NotificationsViewModel(repository as NotificationRepository) as T
            modelClass.isAssignableFrom(NutritionViewModel::class.java) ->NutritionViewModel(repository as NutritionRepository) as T
            modelClass.isAssignableFrom(ProgressViewModel::class.java) -> ProgressViewModel(repository as GoalRepository) as T
            modelClass.isAssignableFrom(StreakViewModel::class.java) -> StreakViewModel(repository as StreakRepository) as T
            modelClass.isAssignableFrom(ExercisePlanViewModel::class.java) -> ExercisePlanViewModel(repository as ExercisePlanRepository) as T
            modelClass.isAssignableFrom(ChatTrainerViewModel::class.java) -> {
                val chatRepository = repository as ChatRepository
                ChatTrainerViewModel(chatRepository) as T
            }            modelClass.isAssignableFrom(PreviousFoodViewModel::class.java) -> PreviousFoodViewModel(repository as PreviousMealRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}