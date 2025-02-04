package com.fpt.edu.healthtracking.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.MenuApi
import com.fpt.edu.healthtracking.data.model.MenuDetail
import com.fpt.edu.healthtracking.data.model.MenuFood
import com.fpt.edu.healthtracking.data.repository.MenuRepository
import com.fpt.edu.healthtracking.databinding.FragmentMenuDetailBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuDetailFragment : BaseFragment<MenuViewModel, FragmentMenuDetailBinding, MenuRepository>() {

    private var mealPlanId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mealPlanId = arguments?.getInt("meal_plan_id", -1) ?: -1

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mealPlanId == -1) {
            findNavController().navigateUp()
            return
        }

        setupUI()
        setupObservers()
        viewModel.loadMenuDetail(mealPlanId)
    }

    private fun setupUI() {
        setupToolbar()
        setupDayTabs()
        setupActionButtons()
    }

    private fun setupActionButtons() {
        binding.apply {
            btnAddFullWeek.setOnClickListener {
                showDatePickerDialog { startDate ->
                    viewModel.addFullMenuPlan(startDate)
                }
            }

            btnAddDay.setOnClickListener {
                val currentDay = viewModel.selectedDay.value ?: 1
                showDatePickerDialog { date ->
                    viewModel.addDayToDate(currentDay, date)
                }
            }

            btnAddBreakfast.setOnClickListener {
                addMealForType(1)
            }

            btnAddLunch.setOnClickListener {
                addMealForType(2)
            }

            btnAddDinner.setOnClickListener {
                addMealForType(3)
            }

            btnAddSnack.setOnClickListener {
                addMealForType(4)
            }
        }
    }

    private fun addMealForType(mealType: Int) {
        val currentDay = viewModel.selectedDay.value ?: 1
        showDatePickerDialog { date ->
            viewModel.addMealToDate(
                selectedDay = currentDay,
                mealTypeDay = mealType,
                selectedMealType = mealType,
                selectedDate = date
            )
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày")
            .build()

        picker.addOnPositiveButtonClickListener { timestamp ->
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(Date(timestamp))
            onDateSelected(date)
        }

        picker.show(parentFragmentManager, "date_picker")
    }


    private fun setupToolbar() {
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupDayTabs() {
        binding.tabDays.apply {
            removeAllTabs()
            for (i in 1..7) {
                addTab(newTab().setText("Ngày $i"))
            }

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val day = tab.position + 1
                    viewModel.setDay(day)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupObservers() {
        viewModel.menuDetail.observe(viewLifecycleOwner) { menuDetail ->
            updateUI(menuDetail)
        }
        viewModel.actionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Thêm thực đơn thành công", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

    }

    private fun updateUI(menuDetail: MenuDetail) {
        binding.apply {
            Glide.with(requireContext())
                .load(menuDetail.mealPlanImage)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(ivMenuImage)

            tvMenuTitle.text = menuDetail.name
            tvCalories.text = "${menuDetail.totalCalories.toInt()} cal"
            tvShortDescription.text = menuDetail.shortDescription
            tvLongDescription.text = menuDetail.longDescription

            tvBreakfastTitle.text = "Bữa sáng (${calculateMealCalories(menuDetail.breakfastFoods)} cal)"
            tvLunchTitle.text = "Bữa trưa (${calculateMealCalories(menuDetail.lunchFoods)} cal)"
            tvDinnerTitle.text = "Bữa tối (${calculateMealCalories(menuDetail.dinnerFoods)} cal)"
            tvSnackTitle.text = "Bữa ăn vặt (${calculateMealCalories(menuDetail.snackFoods)} cal)"

            Glide.with(requireContext())
                .load(menuDetail.mainFoodImageForBreakfast)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(ivBreakfast)
            Glide.with(requireContext())
                .load(menuDetail.mainFoodImageForLunch)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(ivLunch)
            Glide.with(requireContext())
                .load(menuDetail.mainFoodImageForDinner)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(ivDinner)
            Glide.with(requireContext())
                .load(menuDetail.mainFoodImageForSnack)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(ivSnack)
            updateMealSection(layoutBreakfastItems, menuDetail.breakfastFoods)
            updateMealSection(layoutLunchItems, menuDetail.lunchFoods)
            updateMealSection(layoutDinnerItems, menuDetail.dinnerFoods)
            updateMealSection(layoutSnackItems, menuDetail.snackFoods)
        }
    }
    private fun calculateMealCalories(foods: List<MenuFood>): Int {
        return foods.sumOf { (it.calories * it.quantity).toInt() }
    }

    private fun updateMealSection(container: LinearLayout, foods: List<MenuFood>) {
        container.removeAllViews()
        foods.forEach { food ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_menu_meal, container, false)

            itemView.apply {
                findViewById<TextView>(R.id.tvFoodName).text = food.foodName
                findViewById<TextView>(R.id.tvFoodQuantity).text = "${food.quantity}x"
                findViewById<TextView>(R.id.tvFoodCalories).text = "${food.calories.toInt()} cal"

                findViewById<TextView>(R.id.tvFoodNutrients)?.text =
                    "P:${food.protein}g C:${food.carbs}g F:${food.fat}g"
            }
            container.addView(itemView)
        }
    }

    override fun getViewModel() = MenuViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMenuDetailBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = MenuRepository(
        remoteDataSource.buildApi(MenuApi::class.java),
        userPreferences
    )
}