package com.fpt.edu.healthtracking.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.GoalApi
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.GoalRequestDTO
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.GoalRepository
import com.fpt.edu.healthtracking.databinding.FragmentGoalBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GoalFragment : BaseFragment<GoalViewModel,FragmentGoalBinding,GoalRepository>() {

    private var weightGoalType =0.0
    private var selectedGoalType= 0// 1 for increase, -1 for decrease, 0 for maintain
    private var activityLevel=0
    private var weightGoal: Double = 0.0

    override fun getViewModel()=GoalViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentGoalBinding.inflate(inflater, container, false)


    override fun getFragmentRepository(): GoalRepository {
        val api = remoteDataSource.buildApi(GoalApi::class.java)
        return GoalRepository(api = api,
            userPreferences = userPreferences)    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.getGoalDetail()


        popupWeeklyGoal()
        popupEditText()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showExitConfirmationDialog()
        }


    }



    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cập nhật dữ liệu")
            .setMessage("Bạn có muốn đợi để cập nhật thông tin trước khi thoát không?")
            .setPositiveButton("Cập nhật") { _, _ ->
                // Trigger the data update and wait for completion
                updateData()
            }
            .setNegativeButton("Thoát mà không cập nhật") { _, _ ->
                // Allow the user to leave the fragment without updating
                requireActivity().supportFragmentManager.popBackStack()
            }
            .setCancelable(false) // Prevent dialog from being dismissed without a decision
            .show()
    }
    private fun observeViewModel() {
        // Observe goal data and update UI when data changes
        viewModel.goalData.observe(viewLifecycleOwner) { goalData ->
            goalData?.let {
                // Set UI elements with goal data
                binding.tvCurrentWeight.text = "${goalData.currentWeight}kg"
                binding.tvGoalWeight.text = "${goalData.weightGoal}kg"
                binding.tvStartWeight.text= "${goalData.startWeight}kg"+", ${ goalData.dateInitial}"
                binding.tvActivityLevel.text= when (it.exerciseLevel.toInt()) {
                    1 -> "Nhẹ"        // Light
                    2 -> "Trung bình" // Medium
                    3 -> "Nặng"       // Heavy
                    else -> "Không xác định" // Unknown
                }
                // Set the weekly goal based on the data
                setWeeklyGoal(goalData.goalType.toDouble())
            }
        }


        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Show loading indicator
            } else {
                // Hide loading indicator
            }
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Show the error message, e.g., with a Snackbar or Toast
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showError(textView: TextView, message: String) {
        textView.error = message
    }

    private fun popupWeeklyGoal() {
        binding.ivBackButton.setOnClickListener {
            showExitConfirmationDialog()
        }
        // Optionally, override the system back button behavior
        binding.exerciseLevel.setOnClickListener {
            // Define the options for exercise level
            val options = arrayOf("Nhẹ", "Vừa", "Nặng")

            // Create an AlertDialog to display the options
            AlertDialog.Builder(requireContext())
                .setTitle("Chọn mức độ tập luyện")
                .setItems(options) { dialog, which ->
                    // Set the selected option as the text of the exercise level TextView
                    binding.tvActivityLevel.text = options[which]
                    activityLevel = when (which) {
                        0 -> 1  // "Nhẹ" is 1
                        1 -> 2  // "Vừa" is 2
                        2 -> 3  // "Nặng" is 3
                        else -> 0  // Default to 0 if no selection
                    }
                }
                .show()
        }
        binding.tvWeeklyGoal.setOnClickListener {
            // Define the options based on the weight goal type (positive, negative, or zero)
            val options = when {
                selectedGoalType > 0 -> {
                    // Show positive options for "Tăng cân"
                    arrayOf(
                        "Tăng 1kg mỗi tuần",
                        "Tăng 0.75kg mỗi tuần",
                        "Tăng 0.5kg mỗi tuần",
                        "Tăng 0.25kg mỗi tuần"
                    )
                }

                selectedGoalType < 0 -> {
                    // Show negative options for "Giảm cân"
                    arrayOf(
                        "Giảm 1.5kg mỗi tuần",
                        "Giảm 1kg mỗi tuần",
                        "Giảm 0.75kg mỗi tuần",
                        "Giảm 0.5kg mỗi tuần",
                        "Giảm 0.25kg mỗi tuần"
                    )
                }

                else -> {
                    // Show zero option for "Duy trì cân nặng"
                    arrayOf("Duy trì cân nặng")
                }
            }

            var isOptionSelected = false

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Chọn mục tiêu hàng tuần")
                .setItems(options) { _, which ->
                    // Update the text based on the selected option
                    binding.tvWeeklyGoal.text = options[which]
                    isOptionSelected = true

                    // Set the corresponding weight goal type based on selection
                    weightGoalType = when (which) {
                        0 -> if (selectedGoalType > 0) 1.0 else -1.5
                        1 -> if (selectedGoalType > 0) 0.75 else -1.0
                        2 -> if (selectedGoalType > 0) 0.5 else -0.75
                        3 -> if (selectedGoalType > 0) 0.25 else -0.5
                        4 -> if (selectedGoalType > 0) 0.0 else -0.25
                        else -> 0.0 // Maintaining weight
                    }
                }
                .setOnDismissListener {
                    // If no option is selected when the dialog is dismissed, set option 3 as default
                    if (!isOptionSelected) {
                        if(selectedGoalType>0){
                            binding.tvWeeklyGoal.text = options[3] // Default to option 3
                            weightGoalType =  0.25
                        }else if(selectedGoalType<0){
                            binding.tvWeeklyGoal.text = options[4] // Default to option 3
                            weightGoalType =  -2.5
                        }else{
                            binding.tvWeeklyGoal.text = options[0] // Default to option 3
                            weightGoalType =  0.0
                        }
                         // Default value
                    }
                }
                .create()

            dialog.show()
        }

    }

    private fun setWeeklyGoal(weeklyGoal: Double) {

        // Set the appropriate text based on the selected weekly goal
        when (weeklyGoal) {
            1.0 -> binding.tvWeeklyGoal.text = "Tăng 1kg mỗi tuần"
            0.75-> binding.tvWeeklyGoal.text = "Tăng 0.75kg mỗi tuần"
            0.5 -> binding.tvWeeklyGoal.text = "Tăng 0.5kg mỗi tuần"
            0.25-> binding.tvWeeklyGoal.text = "Tăng 0.25kg mỗi tuần"
            0.0 -> binding.tvWeeklyGoal.text = "Duy trì cân nặng"
            -0.25-> binding.tvWeeklyGoal.text = "Tăng 0.25kg mỗi tuần"
            -0.5 -> binding.tvWeeklyGoal.text = "Giảm 0.5kg mỗi tuần"
            -0.75-> binding.tvWeeklyGoal.text = "Tăng 0.25kg mỗi tuần"
            -1.0 -> binding.tvWeeklyGoal.text = "Giảm 1kg mỗi tuần"
        }
        weightGoalType = weeklyGoal
    }


    private fun popupEditText() {
        binding.tvCurrentWeight.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            val editText = EditText(requireContext())

            editText.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                hint = "Nhập cân nặng"
                setText(binding.tvCurrentWeight.text.toString().replace("kg", ""))
                filters = arrayOf(InputFilter.LengthFilter(5))
            }

            dialog.setTitle("Nhập cân nặng")
                .setView(editText)
                .setPositiveButton("OK", null)
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.cancel()
                }

            val alertDialog = dialog.create()

            alertDialog.setOnShowListener {
                val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val weight = editText.text.toString()
                    when {
                        weight.isEmpty() -> {
                            editText.error = "Vui lòng nhập cân nặng"
                        }
                        weight.toFloatOrNull() == null -> {
                            editText.error = "Cân nặng không hợp lệ"
                        }
                        weight.toFloat() < 20 -> {
                            editText.error = "Cân nặng phải lớn hơn 20kg"
                        }
                        weight.toFloat() > 300 -> {
                            editText.error = "Cân nặng phải nhỏ hơn 300kg"
                        }
                        else -> {
                            binding.tvCurrentWeight.text = "${weight}kg"

                            alertDialog.dismiss()
                        }
                    }
                }
            }

            alertDialog.show()
        }

        binding.tvGoalWeight.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            val editText = EditText(requireContext())
            val radioGroup = RadioGroup(requireContext())
            val radioIncrease = RadioButton(requireContext()).apply { text = "Tăng cân" }
            val radioDecrease = RadioButton(requireContext()).apply { text = "Giảm cân" }
            val radioMaintain = RadioButton(requireContext()).apply { text = "Giữ cân" }

            // Add radio buttons to the group
            radioGroup.apply {
                addView(radioIncrease)
                addView(radioDecrease)
                addView(radioMaintain)
                orientation = RadioGroup.VERTICAL
            }

            // Initially hide the EditText
            editText.visibility = View.GONE

            // Create a LinearLayout to hold the radio buttons and EditText
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                addView(radioGroup)
                addView(editText)
            }

            dialog.setTitle("Nhập cân nặng mục tiêu")
                .setView(layout)
                .setPositiveButton("OK", null)
                .setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }

            val alertDialog = dialog.create()

            alertDialog.setOnShowListener {
                val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val weight = editText.text.toString()
                    val currentWeight = binding.tvCurrentWeight.text.toString().replace("kg", "").toFloatOrNull() ?: 0f
                    val temp=selectedGoalType
                     selectedGoalType = when {
                        radioIncrease.isChecked -> 1
                        radioDecrease.isChecked -> -1
                        radioMaintain.isChecked -> 0
                        else -> -1 // default to Decrease weight
                    }

                    // Check if a radio button is selected
                    if (!radioIncrease.isChecked && !radioDecrease.isChecked && !radioMaintain.isChecked) {
                        // Show an error if no goal is selected
                        Toast.makeText(requireContext(), "Vui lòng chọn mục tiêu", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // If the user has selected a goal, show the EditText
                    if (radioIncrease.isChecked || radioDecrease.isChecked || radioMaintain.isChecked) {
                        editText.visibility = View.VISIBLE
                    }

                    // Perform validation and save the selected goal and target weight
                    when {
                        weight.isEmpty() -> {
                            editText.error = "Vui lòng nhập cân nặng"
                        }
                        weight.toFloatOrNull() == null -> {
                            editText.error = "Cân nặng không hợp lệ"
                        }
                        weight.toFloat() < 20 -> {
                            editText.error = "Cân nặng phải lớn hơn 20kg"
                        }
                        weight.toFloat() > 300 -> {
                            editText.error = "Cân nặng phải nhỏ hơn 300kg"
                        }
                        selectedGoalType > 0 && weight.toFloat() < currentWeight -> {
                            editText.error = "Cân nặng mục tiêu phải lớn hơn hoặc bằng cân nặng hiện tại"
                        }
                        selectedGoalType < 0 && weight.toFloat() > currentWeight -> {
                            editText.error = "Cân nặng mục tiêu phải nhỏ hơn hoặc bằng cân nặng hiện tại"
                        }
                        else -> {
                            val goalText = when (selectedGoalType) {
                                1 -> "Tăng cân  "
                                0 -> "Giữ cân  "
                                -1 -> "Giảm cân  "
                                else -> ""
                            }
                            weightGoal=weight.toDouble()
                            binding.tvGoalWeight.text = "$goalText, ${weight}kg"
                            alertDialog.dismiss()
                            if(temp+selectedGoalType==0){
                                if(selectedGoalType>0){
                                    //giam sang tang
                                    binding.tvWeeklyGoal.performClick()
                                }else if(selectedGoalType<0){
                                    //tang sang giam
                                    binding.tvWeeklyGoal.performClick()

                                }
                            }

                        }
                    }

                }
            }

            alertDialog.show()
        }



    }

    // Helper function to validate and update goal weight if needed
    private fun validateGoalWeight() {
        val currentWeight = binding.tvCurrentWeight.text.toString().replace("kg", "").toFloatOrNull() ?: 0f
        val goalWeight = binding.tvGoalWeight.text.toString().replace("kg", "").toFloatOrNull() ?: 0f

        when {
            weightGoalType >0 && goalWeight < currentWeight -> {
                binding.tvGoalWeight.text = "${currentWeight}kg"
            }
            weightGoalType < 0 && goalWeight > currentWeight -> {
                binding.tvGoalWeight.text = "${currentWeight}kg"
            }
            weightGoalType == 0.0  -> {
                binding.tvGoalWeight.text = "${currentWeight}kg"
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Update goal data before closing
        // Gather data from the bindings (text fields, etc.)
        // Gather data from the bindings (text fields, etc.)
       updateData()
    }

    private fun updateData() {
        val currentWeight = binding.tvCurrentWeight.text.toString().replace("kg", "").toDoubleOrNull() ?: 0.0

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_weight_change_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        editor.apply()
        // Create the GoalRequestDTO model
        val goalRequestDTO = GoalRequestDTO(
            goalType = weightGoalType.toFloat(),
            weight = currentWeight,
            exerciseLevel = activityLevel,
            targetWeight = weightGoal,
        )

        // Show a loading dialog while the update is happening
        val loadingDialog = createLoadingDialog()

        // Use a LifecycleCoroutineScope to ensure the coroutine lives beyond the fragment's view lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Set loading state
                viewModel.setLoading(true)
                loadingDialog.show()

                // Call the ViewModel to insert the goal and wait for the result
                // Handle the result
                when (val result = viewModel.insertGoal(goalRequestDTO)) {
                    is Resource.Success -> {
                        showToast(result.value.toString()+ "Goal updated successfully")
                        // After the update, close the fragment or navigate back
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    is Resource.Failure -> {
                        showErrorDialog(result.errorBody?.string() ?: "Failed to update goal")
                    }
                }
            } catch (e: Exception) {
                showErrorDialog("Error: ${e.message}")
            } finally {
                // Ensure loading state is reset
                viewModel.setLoading(false)
                loadingDialog.dismiss()
            }
        }
    }

    private fun createLoadingDialog(): AlertDialog {
        val dialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        return dialog
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Lỗi")
            .setMessage(errorMessage)
            .setPositiveButton("OK", null)
            .show()
    }

}
