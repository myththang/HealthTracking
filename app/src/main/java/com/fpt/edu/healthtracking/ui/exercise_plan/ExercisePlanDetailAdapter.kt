package com.fpt.edu.healthtracking.ui.exercise_plan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.data.model.ExercisePlanDetailItem
import com.fpt.edu.healthtracking.databinding.ItemExercisePlanDetailBinding

class ExercisePlanDetailAdapter : ListAdapter<ExercisePlanDetailItem, ExercisePlanDetailAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExercisePlanDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExerciseViewHolder(
        private val binding: ItemExercisePlanDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExercisePlanDetailItem) {
            binding.apply {
                tvExerciseName.text = exercise.exerciseName
                tvDuration.text = "${exercise.duration} phút"
            }
        }
    }

    private class ExerciseDiffCallback : DiffUtil.ItemCallback<ExercisePlanDetailItem>() {
        override fun areItemsTheSame(oldItem: ExercisePlanDetailItem, newItem: ExercisePlanDetailItem): Boolean {
            return oldItem.exercisePlanDetailId == newItem.exercisePlanDetailId
        }

        override fun areContentsTheSame(oldItem: ExercisePlanDetailItem, newItem: ExercisePlanDetailItem): Boolean {
            return oldItem == newItem
        }
    }
} 