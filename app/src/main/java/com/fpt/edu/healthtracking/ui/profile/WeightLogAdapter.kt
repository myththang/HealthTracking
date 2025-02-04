package com.fpt.edu.healthtracking.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.data.model.WeightDTO
import com.fpt.edu.healthtracking.databinding.ItemWeightLogBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeightLogAdapter : RecyclerView.Adapter<WeightLogAdapter.WeightViewHolder>() {
    private var weightLogs = listOf<WeightDTO>()

    fun submitList(logs: List<WeightDTO>) {
        weightLogs = logs.sortedByDescending { parseDate(it.date) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WeightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(weightLogs[position])
    }

    override fun getItemCount() = weightLogs.size

    class WeightViewHolder(private val binding: ItemWeightLogBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(weightLog: WeightDTO) {
            val date = parseDate(weightLog.date)
            binding.tvDate.text = date.format(DateTimeFormatter.ofPattern("EEEE, dd 'thg' M, yyyy"))
            binding.tvWeight.text = "${weightLog.weight} kg"
        }

    }

    companion object {
        private fun parseDate(date: String): LocalDate {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        }
    }
} 