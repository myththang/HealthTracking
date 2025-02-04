package com.fpt.edu.healthtracking.data.model

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("fullName"             ) var fullName             : String ,
    @SerializedName("totalCalories"        ) var totalCalories        : Double    ,
    @SerializedName("bmi"                  ) var bmi                  : Double    ,
    @SerializedName("totalProtein"         ) var totalProtein         : Double ,
    @SerializedName("totalCarb"            ) var totalCarb            : Double ,
    @SerializedName("totalFat"             ) var totalFat             : Double ,
    @SerializedName("weight"               ) var weight               : Double    ,
    @SerializedName("targetWeight"         ) var targetWeight         : Double    ,
    @SerializedName("streakNumberFood"     ) var streakNumberFood     : Int    ,
    @SerializedName("streakNumberExercise" ) var streakNumberExercise : Int    ,
    @SerializedName("caloriesBurn"         ) var caloriesBurn         : Double    ,
    @SerializedName("totalDuration"        ) var totalDuration : Int    ,
    @SerializedName("height"               ) var height               : Double    ,
    @SerializedName("gender"               ) var gender               : String ,
    @SerializedName("exerciseLevel"        ) var exerciseLevel        : Int    ,
    @SerializedName("ageMember"            ) var ageMember            : Int    ,
    @SerializedName("diaryExerciseId"      ) var diaryExerciseId      : Int    ,
    @SerializedName("diaryFoodId"          ) var diaryFoodId          : Int    ,
    @SerializedName("imageMember"          ) var imageMember          : String ,
    @SerializedName("targetDate"           ) var targetDate           : String ,
    @SerializedName("selectDate"           ) var selectDate           : String ,
    @SerializedName("goalType"             ) var goalType             : String ,
    @SerializedName("weightDifference"     ) var weightDifference     : Double    ,
    @SerializedName("caloriesIntake"       ) var caloriesIntake       : Double    ,
    @SerializedName("amountWater"          ) var amountWater          : Double    ,
    @SerializedName("proteinIntake"        ) var proteinIntake        : Double    ,
    @SerializedName("fatIntake"            ) var fatIntake            : Double    ,
    @SerializedName("carbsIntake"          ) var carbsIntake          : Double
)