package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.WeightChangeAdapter
import com.fpt.edu.healthtracking.databinding.FragmentWeightChangeRateBinding

class WeightChangeRateFragment : Fragment() {
    private var _binding: FragmentWeightChangeRateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InformationViewModel by activityViewModels()
    
    data class WeightChangeOption(
        val rate: Float,
        val description: String,
        val isRecommended: Boolean = false
    )
    
    private val gainWeightOptions = listOf(
        WeightChangeOption(0.25f, "Tăng chậm và an toàn, lý tưởng cho việc xây dựng cơ bắp mà không tích lũy mỡ thừa"),
        WeightChangeOption(0.5f, "Tăng cân ở mức trung bình. Phù hợp khi kết hợp chế độ ăn giàu dinh dưỡng và tập luyện", true),
        WeightChangeOption(0.75f, "Tăng nhanh, nhưng có nguy cơ tích lũy mỡ thừa cao"),
        WeightChangeOption(1f, "Không khuyến nghị. Tăng cân nhanh chóng có thể gây tích tụ mỡ thừa")
    )
    
    private val loseWeightOptions = listOf(
        WeightChangeOption(0.25f, "Giảm chậm, phù hợp cho những người muốn duy trì sức khỏe lâu dài"),
        WeightChangeOption(0.5f, "Mức giảm cân an toàn và phổ biến", true),
        WeightChangeOption(0.75f, "Giảm cân nhanh hơn mức trung bình, có thể gây mệt mỏi"),
        WeightChangeOption(1f, "Chỉ khuyến nghị cho người béo phì hoặc thừa cân nhiều"),
        WeightChangeOption(1.5f, "Không khuyến nghị. Giảm cân quá nhanh có thể gây hại cho sức khỏe")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightChangeRateBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupWeightChangeOptions()
        setupContinueButton()
    }
    
    private fun setupWeightChangeOptions() {
        val options = if (viewModel.isGainingWeight()) gainWeightOptions else loseWeightOptions
        val weightChangeAdapter = WeightChangeAdapter(options) { rate ->
            viewModel.setWeightChangeRate(if (viewModel.isGainingWeight()) rate else -rate)
        }
        binding.rvWeightChangeOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weightChangeAdapter
        }
    }
    
    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            if (viewModel.weightChangeRate.value != null) {
                findNavController().navigate(R.id.action_weightChangeRateFragment_to_registerFragment)
            } else {
                Toast.makeText(context, "Vui lòng chọn mức độ thay đổi cân nặng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 