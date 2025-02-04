package com.fpt.edu.healthtracking.adapters.food

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.responses.FoodDetail
import com.fpt.edu.healthtracking.databinding.ItemMealFoodBinding
import kotlin.math.roundToInt

class MealDetailFoodAdapter(
    private val onDeleteClick: (FoodDetail) -> Unit
) : ListAdapter<FoodDetail, MealDetailFoodAdapter.MealDetailFoodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealDetailFoodViewHolder {
        val binding = ItemMealFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MealDetailFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealDetailFoodViewHolder, position: Int) {
        val foodDetail = getItem(position)
        Log.d("Adapter", "Binding item at position $position: $foodDetail")
        holder.bind(foodDetail)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d("Adapter", "Total items in adapter: $count")
        return count
    }

    inner class MealDetailFoodViewHolder(private val binding: ItemMealFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foodDetail: FoodDetail) {
            binding.apply {
                tvFoodName.text = foodDetail.foodName
                tvCalories.text = "Calories: ${foodDetail.calories}"
                tvCarbs.text = "Carbs: ${foodDetail.carbs.roundToInt()}g"
                tvFat.text = "Fat: ${foodDetail.fat.roundToInt()}g"
                tvProtein.text = "Protein: ${foodDetail.protein.roundToInt()}g"

                Glide.with(itemView.context)
                    .load(foodDetail.foodImage)
                    .placeholder(R.drawable.placeholder_food)
                    .into(ivFoodImage)

                ivDeleteFood.setOnClickListener {
                    onDeleteClick(foodDetail)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FoodDetail>() {
        override fun areItemsTheSame(oldItem: FoodDetail, newItem: FoodDetail): Boolean {
            return oldItem.foodId == newItem.foodId &&
                    oldItem.foodName == newItem.foodName &&
                    oldItem.calories == newItem.calories
        }

        override fun areContentsTheSame(oldItem: FoodDetail, newItem: FoodDetail): Boolean {
            return oldItem == newItem
        }
    }

    fun submitFoodList(newList: List<FoodDetail>) {
        val uniqueList = newList.mapIndexed { index, food ->
            if (food.foodId == 0) {
                food.copy(foodId = -(index + 1))
            } else {
                food
            }
        }
        submitList(uniqueList)
        Log.d("Adapter", "Submitting new list with size: ${uniqueList.size}")
    }
}