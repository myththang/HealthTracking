package com.fpt.edu.healthtracking.ui.exercise_plan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.ExercisePlan
import com.fpt.edu.healthtracking.databinding.ItemExercisePlanBinding

class ExercisePlanAdapter : RecyclerView.Adapter<ExercisePlanAdapter.PlanViewHolder>() {

    private var plans: List<ExercisePlan> = emptyList()
    var onItemClick: ((ExercisePlan) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(
            ItemExercisePlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(plans[position])
    }

    override fun getItemCount(): Int = plans.size

    fun submitList(newPlans: List<ExercisePlan>) {
        plans = newPlans
        notifyDataSetChanged()
    }

    inner class PlanViewHolder(
        private val binding: ItemExercisePlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(plans[position])
                }
            }
        }

        fun bind(plan: ExercisePlan) {
            binding.apply {
                tvPlanName.text = plan.name
                tvCalories.text = "~${plan.totalCaloriesBurned ?: 0} calories"

                Glide.with(itemView.context)
                    .load(plan.exercisePlanImage)
                    .placeholder(R.drawable.workout_placeholder)
                    .error(R.drawable.workout_placeholder)
                    .centerCrop()
                    .into(ivPlanImage)
            }
        }
    }
} 