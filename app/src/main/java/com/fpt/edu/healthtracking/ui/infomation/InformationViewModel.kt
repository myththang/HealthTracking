package com.fpt.edu.healthtracking.ui.infomation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InformationViewModel : ViewModel() {

    private val _fitnessGoal = MutableLiveData<FitnessGoal>()
    val fitnessGoal: LiveData<FitnessGoal> = _fitnessGoal

    private val _desiredWeight = MutableLiveData<Int>()
    val desiredWeight: LiveData<Int> = _desiredWeight

    private val _activityLevel = MutableLiveData<ActivityLevel>()
    val activityLevel: LiveData<ActivityLevel> = _activityLevel

    private val _dietPreference = MutableLiveData<DietPreference>()
    val dietPreference: LiveData<DietPreference> = _dietPreference

    private val _gender = MutableLiveData<Gender>()
    val gender: LiveData<Gender> = _gender

    private val _height = MutableLiveData<Int>()
    val height: LiveData<Int> = _height

    private val _weight = MutableLiveData<Int>()
    val weight: LiveData<Int> = _weight

    private val _dob = MutableLiveData<String>()
    val dob: LiveData<String> = _dob

    private val _selectedConditions = MutableLiveData<Set<MedicalCondition>>(setOf())
    val selectedConditions: LiveData<Set<MedicalCondition>> = _selectedConditions

    private val _weightChangeRate = MutableLiveData<Float>()
    val weightChangeRate: LiveData<Float> = _weightChangeRate

    fun setGender(gender: Gender) {
        _gender.value = gender
    }

    fun setHeight(height: Int) {
        _height.value = height
    }

    fun setWeight(weight: Int) {
        _weight.value = weight
    }

    fun setDob(date: String) {
        _dob.value = date
    }

    fun setActivityLevel(level: ActivityLevel) {
        _activityLevel.value = level
    }

    fun setFitnessGoal(goal: FitnessGoal) {
        _fitnessGoal.value = goal

    }

    fun setDesiredWeight(weight: Int) {
        if(desiredWeight==null){
            val currentWeight = _weight.value ?: return
            val fitnessGoal = _fitnessGoal.value ?: return

            val validWeight = when (fitnessGoal) {
                FitnessGoal.LOSE_WEIGHT -> weight.coerceIn(45, currentWeight - 1)
                FitnessGoal.GAIN_WEIGHT -> weight.coerceIn(currentWeight + 1, 150)
                FitnessGoal.MAINTAIN_WEIGHT -> weight.coerceIn(45, 150)
            }
            _desiredWeight.value = validWeight
        }else _desiredWeight.value = weight
    }


    fun setDietPreference(preference: DietPreference) {
        _dietPreference.value = preference
    }

    fun toggleCondition(condition: MedicalCondition) {
        val currentConditions = _selectedConditions.value.orEmpty().toMutableSet()
        if (condition == MedicalCondition.NONE) {
            currentConditions.clear()
            currentConditions.add(MedicalCondition.NONE)
        } else {
            currentConditions.remove(MedicalCondition.NONE)
            if (currentConditions.contains(condition)) {
                currentConditions.remove(condition)
            } else {
                currentConditions.add(condition)
            }
        }
        _selectedConditions.value = currentConditions
    }

    fun setWeightChangeRate(rate: Float) {
        _weightChangeRate.value = rate
    }

    fun isGainingWeight(): Boolean {
        val currentWeight = weight.value ?: 0
        val targetWeight = desiredWeight.value ?: 0
        return targetWeight > currentWeight
    }
}

enum class FitnessGoal {
    LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_WEIGHT
}

enum class ActivityLevel {
    INACTIVE, LIGHTLY_ACTIVE, VERY_ACTIVE
}

enum class DietPreference {
    NORMAL, LOW_CARB, VEGETARIAN, CLEAN_EATING
}

enum class Gender {
    MALE, FEMALE
}

enum class MedicalCondition {
    NONE, DIABETES, GOUT, HYPERTENSION
}