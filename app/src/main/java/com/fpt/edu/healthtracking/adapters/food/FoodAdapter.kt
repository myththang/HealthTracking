package com.fpt.edu.healthtracking.adapters.food

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.FoodLog
import com.fpt.edu.healthtracking.data.model.Meal
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.responses.FoodDiaryItem
import com.fpt.edu.healthtracking.databinding.ItemFoodBinding
import com.fpt.edu.healthtracking.databinding.ItemMealBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    private var foods: List<FoodLog> = emptyList()
    var onDeleteClick: ((Int) -> Unit)? = null

    inner class FoodViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodLog) {
            binding.apply {
                tvFoodName.text = food.foodName
                tvFoodAmount.text = food.servingSize
                tvCalories.text = "${food.calories} cal"

                root.setOnLongClickListener {
                    showDeleteDialog(itemView.context, food)
                    true
                }
            }
        }
        private fun showDeleteDialog(context: Context, food: FoodLog) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Diary")
                .setMessage("Bạn có muốn xóa món ăn này khỏi nhật ký?")
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Xoá") { dialog, _ ->
                    onDeleteClick?.invoke(food.diaryDetailId)
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foods[position])
    }

    override fun getItemCount() = foods.size

    fun submitList(newFoods: List<FoodLog>) {
        foods = newFoods
        notifyDataSetChanged()
    }

}


class MealAdapter : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {
    private var meals: List<Meal> = emptyList()
    var onAddClick: ((MealType) -> Unit)? = null
    var onDeleteClick: ((Int) -> Unit)? = null
    inner class MealViewHolder(private val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val foodAdapter = FoodAdapter().apply {
            onDeleteClick = this@MealAdapter.onDeleteClick
        }

        init {
            binding.rvFoods.apply {
                adapter = foodAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        fun bind(meal: Meal) {
            binding.apply {
                ivMealIcon.setImageResource(when(meal.type) {
                    MealType.BREAKFAST -> R.drawable.ic_breakfast
                    MealType.LUNCH -> R.drawable.ic_lunch
                    MealType.DINNER -> R.drawable.ic_dinner
                    MealType.SNACK -> R.drawable.ic_snack
                })

                tvMealTitle.text = when(meal.type) {
                    MealType.BREAKFAST -> "Bữa sáng"
                    MealType.LUNCH -> "Bữa trưa"
                    MealType.DINNER -> "Bữa tối"
                    MealType.SNACK -> "Ăn Vặt"
                }

                tvCalories.text = "${meal.consumedCalories} of ${meal.targetCalories} Cal"

                tvWarning.visibility = if (meal.consumedCalories > meal.targetCalories) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                tvWarning.text = when(meal.type) {
                    MealType.BREAKFAST -> "Bạn đã vượt quá lượng calories cho bữa sáng!"
                    MealType.LUNCH -> "Bạn đã vượt quá lượng calories cho bữa trưa!"
                    MealType.DINNER -> "Bạn đã vượt quá lượng calories cho bữa tối!"
                    MealType.SNACK -> "Bạn đã vượt quá lượng calories cho bữa phụ!"
                }

                if (meal.foods.isEmpty()) {
                    rvFoods.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = when(meal.type) {
                        MealType.BREAKFAST -> "Đừng để bụng rống kêu réo nữa nào! Đã đến giờ nạp năng lượng cho một ngày mới đầy sảng khoái rồi đấy!"
                        MealType.LUNCH -> "Đừng để cơn đói làm bạn xao nhãng công việc nữa nhé! Đã đến giờ thưởng thức bữa trưa ngon lành rồi!"
                        MealType.DINNER -> "Mặt trời đã lặn, bụng chúng mình cũng đang \"lặn\" rồi! Đã đến giờ nạp năng lượng cho một đêm ngon giấc!"
                        MealType.SNACK -> "Snack là bữa ăn nhẹ quan trọng, các bạn nhớ nhé!"
                    }
                } else {
                    rvFoods.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    foodAdapter.submitList(meal.foods)
                }

                btnAdd.setOnClickListener {
                    onAddClick?.invoke(meal.type)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount() = meals.size

    fun submitList(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}


