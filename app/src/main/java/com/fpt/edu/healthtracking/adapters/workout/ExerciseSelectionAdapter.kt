package com.fpt.edu.healthtracking.adapters.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.Exercise
import com.fpt.edu.healthtracking.data.responses.ExerciseListResponseItem
import com.fpt.edu.healthtracking.databinding.ItemWorkoutSelectionBinding

class ExerciseSelectionAdapter: RecyclerView.Adapter<ExerciseSelectionAdapter.WorkoutViewHolder>() {

    var onExerciseClick: ((ExerciseListResponseItem) -> Unit)? = null
    private var exercises = listOf<ExerciseListResponseItem>()

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExerciseListResponseItem) {
            binding.apply {
                tvWorkoutName.text = exercise.exerciseName
                tvCategory.text = exercise.categoryExercise

                Glide.with(itemView.context)
                    .load(exercise.exerciseImage)
                    .placeholder(R.drawable.workout_placeholder)
                    .error(R.drawable.workout_placeholder)
                    .into(ivWorkout)

                root.setOnClickListener {
                    onExerciseClick?.invoke(exercise)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount() = exercises.size

    fun submitList(newExercises: List<ExerciseListResponseItem>) {
        exercises = newExercises
        notifyDataSetChanged()
    }

}