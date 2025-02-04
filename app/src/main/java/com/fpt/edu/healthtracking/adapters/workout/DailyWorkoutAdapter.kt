package com.fpt.edu.healthtracking.adapters.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.responses.ExerciseDiaryDetail
import com.fpt.edu.healthtracking.databinding.ItemWorkoutBinding

class DailyWorkoutAdapter : RecyclerView.Adapter<DailyWorkoutAdapter.WorkoutViewHolder>() {

    private var workouts: List<ExerciseDiaryDetail> = emptyList()
    var onWorkoutChanged: ((ExerciseDiaryDetail) -> Unit)? = null

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: ExerciseDiaryDetail) {
            binding.apply {
                tvExerciseName.text = workout.exerciseName

                Glide.with(itemView.context)
                    .load(workout.exerciseImage)
                    .placeholder(R.drawable.workout_placeholder)
                    .error(R.drawable.workout_placeholder)
                    .centerCrop()
                    .into(ivExercise)

                tvDuration.text = workout.duration.toInt().toString() + " phút"
                tvCalories.text = workout.caloriesBurned.toInt().toString() + " kcal"

                checkboxCompleted.setOnCheckedChangeListener(null)

                checkboxCompleted.isChecked = workout.isPractice

                checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != workout.isPractice) {
                        workout.isPractice = isChecked
                        onWorkoutChanged?.invoke(workout)
                    }
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size

    fun submitList(newWorkouts: List<ExerciseDiaryDetail>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}