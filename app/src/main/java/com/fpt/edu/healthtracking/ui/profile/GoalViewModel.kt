package com.fpt.edu.healthtracking.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.GoalRequestDTO
import com.fpt.edu.healthtracking.data.model.GoalResponseDTO
import com.fpt.edu.healthtracking.data.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalViewModel(
   private var repository: GoalRepository) :ViewModel (){

   private val _goalData = MutableLiveData<GoalResponseDTO?>()
   val goalData: LiveData<GoalResponseDTO?> = _goalData

   private val _loading = MutableLiveData<Boolean>()
   val loading: LiveData<Boolean> = _loading

   private val _error = MutableLiveData<String?>()
   val error: LiveData<String?> = _error


   fun setLoading(isLoading: Boolean) {
      _loading.value = isLoading
   }

   suspend fun insertGoal(goalRequestDTO: GoalRequestDTO): Resource<Any> {
      return try {
         // Set loading to true
         _loading.postValue(true)

         // Perform API call
         val result = repository.insertGoal(goalRequestDTO)

         // Set loading to false and return success
         _loading.postValue(false)
         Resource.Success(result)
      } catch (e: Exception) {
         // Set loading to false and return failure
         _loading.postValue(false)
         Resource.Failure(
             true, 404,
             errorBody = TODO()
         )
      }
   }


   // Function to get goal details
   fun getGoalDetail() {
      viewModelScope.launch {
         _loading.value = true
         try {
            when (val result = repository.getGoalDetail()) {
               is Resource.Success -> {
                  _goalData.value = result.value
                  _error.value = null
               }
               is Resource.Failure -> {
                  _error.value = "Failed to load goal details: ${result.errorBody?.string()}"
                  _goalData.value = null
               }
            }
         } catch (e: Exception) {
            _error.value = "Error: ${e.message}"
            _goalData.value = null
         } finally {
            _loading.value = false
         }
      }
   }
}