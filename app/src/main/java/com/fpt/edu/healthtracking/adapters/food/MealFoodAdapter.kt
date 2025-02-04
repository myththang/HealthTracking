package com.fpt.edu.healthtracking.adapters.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.MealFood
import com.fpt.edu.healthtracking.databinding.ItemMealFoodBinding
import kotlin.math.roundToInt

class MealFoodAdapter(
    private val onDeleteClick: (MealFood) -> Unit
) : ListAdapter<MealFood, MealFoodAdapter.MealFoodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealFoodViewHolder {
        val binding = ItemMealFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MealFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealFoodViewHolder, position: Int) {
        val mealFood = getItem(position)
        holder.bind(mealFood)
    }

    inner class MealFoodViewHolder(private val binding: ItemMealFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mealFood: MealFood) {
            binding.apply {
                val food = mealFood.food
                tvFoodName.text = food.foodName
                tvCalories.text = "Calories: ${mealFood.totalCalories}"
                tvCarbs.text = "Carbs: ${mealFood.totalCarbs.roundToInt()}g"
                tvFat.text = "Fat: ${mealFood.totalFat.roundToInt()}g"
                tvProtein.text = "Protein: ${mealFood.totalProtein.roundToInt()}g"

                // Use Glide to load the food image
                Glide.with(itemView.context)
                    .load(food.foodImage)
                    .placeholder(R.drawable.placeholder_food)
                    .into(ivFoodImage)

                // Set click listener for deleting the food item
                ivDeleteFood.setOnClickListener {
                    onDeleteClick(mealFood)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealFood>() {
        override fun areItemsTheSame(oldItem: MealFood, newItem: MealFood): Boolean {
            return oldItem.food.foodId == newItem.food.foodId
        }

        override fun areContentsTheSame(oldItem: MealFood, newItem: MealFood): Boolean {
            return oldItem == newItem
        }
    }
}
