package com.fpt.edu.healthtracking.ui.nutrition

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.NutritionApi
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import com.fpt.edu.healthtracking.databinding.FragmentAccountDetailBinding
import com.fpt.edu.healthtracking.databinding.FragmentNutritionBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.profile.AccountDetailViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class NutritionFragment : BaseFragment<NutritionViewModel, FragmentNutritionBinding, NutritionRepository>() {



    private var currentSelectedButton: TextView? = null
    private var status= 1

    override fun getViewModel() = NutritionViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentNutritionBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NutritionRepository {
        val api = remoteDataSource.buildApi(NutritionApi::class.java)
        return NutritionRepository(
            api = api,
            userPreferences = userPreferences
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(
            view.findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.setDatePicked(LocalDate.now().toString())


        // Initialize views
        binding.arrowLeft.setOnClickListener {
            val currentDate = viewModel.datePicked.value
            // Parse string to LocalDate, subtract a day, then format back to string
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Adjust pattern to match your date format
            val date = LocalDate.parse(currentDate, formatter)
            val newDate = date.minusDays(1).format(formatter)
            viewModel.setDatePicked(newDate)
        }

        binding.arrowRight.setOnClickListener {
            val currentDate = viewModel.datePicked.value
            // Parse string to LocalDate, add a day, then format back to string
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Adjust pattern to match your date format
            val date = LocalDate.parse(currentDate, formatter)
            val newDate = date.plusDays(1).format(formatter)
            viewModel.setDatePicked(newDate)
        }
        setupListeners()
        observerViewModel()
        // Set default fragment and button state
        if (savedInstanceState == null) {
            switchFragment(NutrientsFragment(), binding.btnNutrients!!)
        }
    }

    private fun observerViewModel() {
        viewModel.datePicked.observe(viewLifecycleOwner) { date ->
            // Update UI with the picked date, for example, updating a TextView
            binding.datePickerButton.text = date
            when (status) {
                1 -> viewModel.fetchDailyCalories(date)
                2 -> viewModel.fetchDailyNutrition(date)
                3 -> viewModel.fetchDailyMacros(date)
            }

        }
    }



    private fun setupListeners() {
        binding.btnCalories.setOnClickListener { v: View? ->
            switchFragment(
                CaloriesFragment(),
                binding.btnCalories

            )
            status=1

        }
        binding.btnNutrients.setOnClickListener { v: View? ->
            switchFragment(
                NutrientsFragment(),
                binding.btnNutrients
            )
            status=2

        }
        binding.btnMacros.setOnClickListener { v: View? ->
            switchFragment(
                MacrosFragment(),
                binding.btnMacros
            )
            status=3
        }
        binding.datePickerButton.setOnClickListener{
            val calendar = Calendar.getInstance()

            // Create a DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    // When the user selects a date, update the calendar
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Format the date and set it to the button
                    val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                    viewModel.setDatePicked(selectedDate)

                },
                calendar.get(Calendar.YEAR),  // Initial year
                calendar.get(Calendar.MONTH),  // Initial month
                calendar.get(Calendar.DAY_OF_MONTH)  // Initial day
            )

            // Show the DatePickerDialog
            datePickerDialog.show()

        }
    }

    private fun switchFragment(fragment: Fragment, selectedButton: TextView) {
        if (currentSelectedButton === selectedButton) {
            return  // Don't switch if the same button is clicked
        }
        // Update button states
        updateButtonStates(selectedButton)
        // Switch fragment
        parentFragmentManager
            .beginTransaction() // .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)  // Optional: add animations
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateButtonStates(selectedButton: TextView) {
        // Reset current selected button if exists
        if (currentSelectedButton != null) {
            currentSelectedButton!!.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.segment_unselected)
        }

        // Update new selected button
        selectedButton.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.segment_selected)
        currentSelectedButton = selectedButton
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current selected tab
        if (currentSelectedButton != null) {
            outState.putInt("selectedTab", currentSelectedButton!!.id)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Restore the selected tab
        if (savedInstanceState != null) {
            val selectedTabId = savedInstanceState.getInt("selectedTab", -1)
            if (selectedTabId != -1) {
                val selectedButton = requireView().findViewById<TextView>(selectedTabId)
                if (selectedButton != null) {
                    val fragment = if (selectedButton.id == R.id.btnCalories) {
                        CaloriesFragment()
                    } else if (selectedButton.id == R.id.btnNutrients) {
                        NutrientsFragment()
                    } else {
                        MacrosFragment()
                    }
                    switchFragment(fragment, selectedButton)
                }
            }
        }
    }
}

