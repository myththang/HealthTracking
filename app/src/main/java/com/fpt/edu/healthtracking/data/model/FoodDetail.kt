package com.fpt.edu.healthtracking.data.model

data class FoodDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val baseProtein: Float,
    val baseCarbs: Float,
    val baseFats: Float,
    val baseCalories: Float,
    val protein: Float = baseProtein,
    val carbs: Float = baseCarbs,
    val fats: Float = baseFats,
    val calories: Float = baseCalories,
    val servingSize: Float? = 100f,
    val cupSize: Float? = 240f,
    val currentAmount: Float = 1f,
    val currentUnit: String = "g",
    val availableUnits: List<String> = listOf("g", "oz", "serving", "cup")
) {
    val proteinPercentage: Int get() = calculatePercentage(protein)
    val carbsPercentage: Int get() = calculatePercentage(carbs)
    val fatsPercentage: Int get() = calculatePercentage(fats)

    private fun calculatePercentage(value: Float): Int {
        val maxValue = 100f // You might want to adjust this based on daily recommended values
        return ((value / maxValue) * 100).toInt().coerceIn(0, 100)
    }

    fun getDisplayAmount(): String = "$currentAmount $currentUnit"
    fun getDisplayCalories(): String = "$currentAmount $currentUnit - $calories Cal"
}