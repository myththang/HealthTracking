package com.fpt.edu.healthtracking.data.model

data class GoalResponseDTO(
    val goalId: Int,
    val goalType: Float,
    val exerciseLevel: String,
    val weightGoal: Double,
    val targetDate: String,
    val currentWeight: Double?,
    val startWeight: Double?,
    val dateInitial: String?
)
data class GoalRequestDTO(
    val weight: Double,
    val exerciseLevel: Int?, // Nullable integer
    val targetWeight: Double,
    val goalType: Float
)

data class  WeightDTO(
    val date:String,
    val weight:Double

)

data class GraphData(
    val currentWeight: List<WeightDTO>,
    val goalWeight: List<WeightDTO>
)