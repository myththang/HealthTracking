package com.fpt.edu.healthtracking.adapters.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.databinding.ItemAddFoodBinding

class FoodItemAdapter(
    private val onFoodClick: (Food) -> Unit,
    private val onAddClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder>() {

    private var foods: List<Food> = emptyList()

    inner class FoodViewHolder(private val binding: ItemAddFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: Food) {
            binding.apply {
                tvFoodName.text = food.foodName
                tvCalories.text = "${food.calories} cal"
                tvNutrients.text = "P:${food.protein}g C:${food.carbs}g F:${food.fat}g"
                tvDietType.text = food.dietName

                Glide.with(itemView.context)
                    .load(food.foodImage)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .into(ivFood)

                btnAdd.setOnClickListener { onAddClick(food) }
                root.setOnClickListener { onFoodClick(food) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemAddFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foods[position])
    }

    override fun getItemCount() = foods.size

    fun submitList(newFoods: List<Food>) {
        foods = newFoods
        notifyDataSetChanged()
    }
}