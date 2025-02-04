package com.fpt.edu.healthtracking.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.food.MealMemberAdapter
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.MemberMeal
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import com.fpt.edu.healthtracking.databinding.FragmentMyMealBinding
import kotlinx.coroutines.launch

class MyMealsFragment : Fragment() {
    private var _binding: FragmentMyMealBinding? = null
    private val binding get() = _binding!!
    private lateinit var mealAdapter: MealMemberAdapter
    private val repository: FoodRepository by lazy { FoodRepository(
        RemoteDataSource().buildApi(
        FoodApi::class.java), UserPreferences(requireContext())
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadMeals()
    }

    private fun setupViews() {
        binding.cardCreateMeal.setOnClickListener {
            findNavController().navigate(R.id.action_myMealsFragment_to_createMealFragment)
        }

        mealAdapter = MealMemberAdapter(
            onMealClick = { meal -> 
                //navigateToMealDetail(meal.mealMemberId)
            },
            onAddClick = { meal ->
               // handleAddMeal(meal)
            },
            onDeleteClick = { meal ->
                handleDeleteMeal(meal)
            }
        )
        
        binding.rvMeals.apply {
            adapter = mealAdapter
            layoutManager = LinearLayoutManager(context)
            mealAdapter.setupSwipeToDelete(this)
        }
    }

    private fun handleDeleteMeal(meal: MemberMeal) {
        lifecycleScope.launch {
            try {
                when (val result = repository.deleteMealMember(meal.mealMemberId)) {
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Xóa bữa ăn thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadMeals()
                    }
                    is Resource.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Không thể xóa bữa ăn. Vui lòng thử lại sau",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Đã xảy ra lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadMeals() {
        lifecycleScope.launch {
            try {
                when (val result = repository.getMemberMeals()) {
                    is Resource.Success -> {
                        val meals = result.value
                        if (meals.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvMeals.visibility = View.GONE
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvMeals.visibility = View.VISIBLE
                            mealAdapter.submitList(meals)
                        }
                    }
                    is Resource.Failure -> {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.rvMeals.visibility = View.GONE
                        Toast.makeText(context, "Không thể tải danh sách bữa ăn", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvMeals.visibility = View.GONE
                Toast.makeText(context, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}