package com.fpt.edu.healthtracking.adapters.food

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.MemberMeal
import com.fpt.edu.healthtracking.databinding.ItemMemberMealListBinding

class MealMemberAdapter(
    private val onMealClick: (MemberMeal) -> Unit,
    private val onAddClick: (MemberMeal) -> Unit,
    private val onDeleteClick: (MemberMeal) -> Unit
) : RecyclerView.Adapter<MealMemberAdapter.MealMemberViewHolder>() {

    private var meals = mutableListOf<MemberMeal>()

    fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            private val deleteIcon = ContextCompat.getDrawable(
                recyclerView.context,
                R.drawable.ic_delete
            )
            private val background = ColorDrawable(Color.RED)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val meal = meals[position]
                
                AlertDialog.Builder(recyclerView.context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa bữa ăn này?")
                    .setPositiveButton("Xóa") { _, _ ->
                        // Xóa item khỏi list và cập nhật UI ngay lập tức
                        meals.removeAt(position)
                        notifyItemRemoved(position)
                        // Gọi callback để xử lý xóa trên server
                        onDeleteClick(meal)
                    }
                    .setNegativeButton("Hủy") { _, _ ->
                        // Khôi phục lại item nếu người dùng hủy
                        notifyItemChanged(position)
                    }
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive
                )
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    inner class MealMemberViewHolder(private val binding: ItemMemberMealListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: MemberMeal) {
            binding.apply {
                tvMealName.text = meal.nameMealPlanMember
                tvCalories.text = "${meal.totalCalories} cal"
                tvMacros.text = "P:${meal.totalProtein}g C:${meal.totalCarb}g F:${meal.totalFat}g"

                Glide.with(itemView)
                    .load(meal.image)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .into(ivMeal)

                root.setOnClickListener { onMealClick(meal) }
                btnAddMeal.setOnClickListener { onAddClick(meal) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealMemberViewHolder {
        val binding = ItemMemberMealListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MealMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealMemberViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount() = meals.size

    fun submitList(newMeals: List<MemberMeal>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }
}