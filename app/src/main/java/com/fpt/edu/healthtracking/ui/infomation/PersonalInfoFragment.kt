package com.fpt.edu.healthtracking.ui.infomation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentPersonalInfoBinding
import java.util.Calendar

class PersonalInfoFragment : Fragment() {
    private lateinit var binding: FragmentPersonalInfoBinding
    private val viewModel: InformationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.isEnabled = false
        setupGenderSelection()
        setupSliders()
        setupDOBInput()
        setupContinueButton()
        setupBackButton()
        observeViewModel()
    }

    private fun setupDOBInput() {
        binding.etDOB.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private val calendar = Calendar.getInstance()
            private var previousLength = 0
            private var previousStr = ""
            private var cursorPositionBeforeChange = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousLength = s?.length ?: 0
                previousStr = s?.toString() ?: ""
                cursorPositionBeforeChange = binding.etDOB.selectionStart
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val currentStr = s?.toString() ?: ""
                val str = currentStr.replace("/", "")
                val isDeleting = currentStr.length < previousStr.length

                if (str.length <= 8) {
                    var formatted = ""
                    var cursorPosition = binding.etDOB.selectionStart

                    if (isDeleting) {
                        if ((cursorPositionBeforeChange == 3 || cursorPositionBeforeChange == 6) &&
                            previousStr.length > cursorPositionBeforeChange - 1 &&
                            previousStr[cursorPositionBeforeChange - 1] == '/') {

                            if (cursorPositionBeforeChange == 3) {
                                formatted = str
                                cursorPosition = formatted.length
                            }
                            else if (cursorPositionBeforeChange == 6 && str.length >= 2) {
                                formatted = str.substring(0, 2) + "/"
                                if (str.length > 2) {
                                    formatted += str.substring(2)
                                }
                                cursorPosition = formatted.length
                            }
                        } else {
                            if (str.isNotEmpty()) {
                                if (str.length >= 2) {
                                    formatted = str.substring(0, 2) + "/"

                                    if (str.length > 2) {
                                        val monthPart = str.substring(2, minOf(4, str.length))
                                        formatted += monthPart
                                        if (monthPart.length == 2) {
                                            formatted += "/"
                                        }

                                        if (str.length > 4) {
                                            formatted += str.substring(4)
                                        }
                                    }
                                } else {
                                    formatted = str
                                }
                            }
                            cursorPosition = minOf(cursorPosition, formatted.length)
                        }
                    } else {
                        if (str.isNotEmpty()) {
                            // Xử lý ngày
                            if (str.length >= 2) {
                                val dayPart = str.substring(0, 2)
                                val day = dayPart.toIntOrNull() ?: 0
                                formatted = if (day > 31) "31" else dayPart
                                formatted += "/"
                            } else if (str.length == 1 && (str[0].toString().toIntOrNull() ?: 0) >= 4) {
                                formatted = "0${str}/"
                            } else {
                                formatted = str
                            }

                            // Xử lý tháng
                            if (str.length > 2) {
                                val monthPart = str.substring(2, minOf(4, str.length))
                                if (monthPart.length == 2) {
                                    val month = monthPart.toIntOrNull() ?: 0
                                    formatted += if (month > 12) "12" else monthPart
                                    formatted += "/" // Luôn thêm dấu "/" sau khi nhập đủ 2 số tháng
                                } else if (monthPart.length == 1 && (monthPart.toIntOrNull() ?: 0) > 1) {
                                    formatted += "0$monthPart/"
                                } else {
                                    formatted += monthPart
                                }

                                // Xử lý năm
                                if (str.length > 4) {
                                    formatted += str.substring(4, minOf(8, str.length))
                                }
                            }
                        }
                        cursorPosition = formatted.length
                    }

                    if (formatted != s.toString()) {
                        binding.etDOB.setText(formatted)
                        binding.etDOB.setSelection(minOf(cursorPosition, formatted.length))
                    }

                    if (str.length == 8) {
                        validateAndShowError(formatted)
                    } else {
                        binding.tilDOB.error = null
                    }
                }
                isFormatting = false
            }
        })

        binding.etDOB.apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            hint = "DD/MM/YYYY"
        }

        binding.etDOB.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateAndShowError(binding.etDOB.text?.toString() ?: "")
            }
        }
    }

    private fun validateAndShowError(dateStr: String) {
        val error = when {
            dateStr.isEmpty() -> {
                "Vui lòng nhập ngày sinh"
            }
            dateStr.length < 10 -> {
                "Vui lòng nhập đầy đủ ngày sinh"
            }
            else -> {
                try {
                    val parts = dateStr.split("/")
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()

                    when {
                        !isValidDate(day, month, year) -> {
                            "Ngày sinh không hợp lệ"
                        }
                        !isValidAge(year, month, day) -> {
                            "Tuổi phải từ 15 đến 100"
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                    "Định dạng ngày sinh không hợp lệ"
                }
            }
        }

        binding.tilDOB.error = error
        if (error == null) {
            viewModel.setDob(dateStr)
        }
        checkFormCompletion()
    }

    private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
        if (month < 1 || month > 12) return false
        if (year < 1900 || year > 2100) return false

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return day in 1..maxDays
    }

    private fun isValidAge(year: Int, month: Int, day: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        var age = currentYear - year
        
        if (month > currentMonth || (month == currentMonth && day > currentDay)) {
            age--
        }
        
        return age in 15..100
    }

    private fun checkFormCompletion() {
        val dobText = binding.etDOB.text?.toString() ?: ""
        val isDateValid = binding.tilDOB.error == null && dobText.length == 10
        val isFormComplete = viewModel.gender.value != null && isDateValid

        binding.btnContinue.isEnabled = isFormComplete
        updateContinueButtonAppearance(isFormComplete)
    }

    private fun setupGenderSelection() {
        binding.cardMale.setOnClickListener {
            viewModel.setGender(Gender.MALE)
            updateGenderSelection(Gender.MALE)
            checkFormCompletion()
        }
        binding.cardFemale.setOnClickListener {
            viewModel.setGender(Gender.FEMALE)
            updateGenderSelection(Gender.FEMALE)
            checkFormCompletion()
        }
    }

    private fun updateGenderSelection(selectedGender: Gender) {
        binding.layoutMale.setBackgroundResource(
            if (selectedGender == Gender.MALE) R.drawable.card_background_selected
            else R.drawable.card_background
        )
        binding.layoutFemale.setBackgroundResource(
            if (selectedGender == Gender.FEMALE) R.drawable.card_background_selected
            else R.drawable.card_background
        )
    }

    private fun setupSliders() {
        binding.apply {
            sliderHeight.addOnChangeListener { _, value, _ ->
                if (validateHeight(value.toInt())) {
                    tvHeightValue.text = "${value.toInt()} cm"
                    tvHeightValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    tilHeight.error = null
                    viewModel.setHeight(value.toInt())
                } else {
                    tvHeightValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    tilHeight.error = "Chiều cao phải từ 140cm đến 200cm"
                }
            }

            sliderWeight.addOnChangeListener { _, value, _ ->
                if (validateWeight(value.toInt())) {
                    tvWeightValue.text = "${value.toInt()} kg"
                    tvWeightValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    tilWeight.error = null
                    viewModel.setWeight(value.toInt())
                } else {
                    tvWeightValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    tilWeight.error = "Cân nặng phải từ 40kg đến 120kg"
                }
            }
        }
    }

    private fun validateHeight(height: Int): Boolean {
        return height in 140..200
    }

    private fun validateWeight(weight: Int): Boolean {
        return weight in 40..120
    }

    private fun observeViewModel() {
        viewModel.height.observe(viewLifecycleOwner) { height ->
            binding.tvHeightValue.text = "$height cm"
        }
        viewModel.weight.observe(viewLifecycleOwner) { weight ->
            binding.tvWeightValue.text = "$weight kg"
        }
        viewModel.gender.observe(viewLifecycleOwner) { selectedGender ->
            updateGenderSelection(selectedGender)
            checkFormCompletion()
        }
        viewModel.dob.observe(viewLifecycleOwner) { dob ->
            checkFormCompletion()
        }
    }

    private fun updateContinueButtonAppearance(enabled: Boolean) {
        binding.btnContinue.apply {
            if (enabled) {
                setBackgroundResource(R.drawable.button_enabled_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                setBackgroundResource(R.drawable.button_disabled_background)
                //setTextColor(ContextCompat.getColor(requireContext(), R.color.))
            }
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_personalInfoFragment_to_medicalConditionsFragment)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}