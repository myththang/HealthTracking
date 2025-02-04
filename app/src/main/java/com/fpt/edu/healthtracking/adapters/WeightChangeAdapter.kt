package com.fpt.edu.healthtracking.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.ItemWeightChangeOptionBinding
import com.fpt.edu.healthtracking.ui.infomation.WeightChangeRateFragment

class WeightChangeAdapter(
    private val options: List<WeightChangeRateFragment.WeightChangeOption>,
    private val onOptionSelected: (Float) -> Unit
) : RecyclerView.Adapter<WeightChangeAdapter.WeightChangeViewHolder>() {

    private var selectedPosition = -1

    inner class WeightChangeViewHolder(
        private val binding: ItemWeightChangeOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: WeightChangeRateFragment.WeightChangeOption, position: Int) {
            binding.apply {
                tvRate.text = "${option.rate} kg/tuần"
                if (option.isRecommended) {
                    tvRate.text = "${option.rate} kg/tuần (Khuyến nghị)"
                }
                tvDescription.text = option.description

                // Cập nhật trạng thái được chọn
                root.setBackgroundResource(
                    if (position == selectedPosition) 
                        R.drawable.bg_weight_change_selected 
                    else 
                        R.drawable.bg_weight_change_unselected
                )

                // Cập nhật màu chữ
                val textColor = if (position == selectedPosition) {
                    ContextCompat.getColor(root.context, R.color.white)
                } else {
                    ContextCompat.getColor(root.context, R.color.black)
                }
                tvRate.setTextColor(textColor)
                tvDescription.setTextColor(textColor)

                // Xử lý sự kiện click
                root.setOnClickListener {
                    val oldPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onOptionSelected(option.rate)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightChangeViewHolder {
        val binding = ItemWeightChangeOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightChangeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightChangeViewHolder, position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount() = options.size
} 