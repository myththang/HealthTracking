import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.MealType
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.databinding.FragmentAddQuickActionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuickActionBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddQuickActionBinding
    private var diaryExerciseId : Int? = -1
    private var diaryFoodId : Int? = -1
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var authApi: AuthApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("QuickActionBottomSheet", "onCreateView called")
        binding = FragmentAddQuickActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authApi = RemoteDataSource().buildApi(AuthApi::class.java)
        userPreferences = UserPreferences(requireContext())
        authRepository = AuthRepository(authApi,userPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = context?.getSharedPreferences("diaryId", Context.MODE_PRIVATE)
        diaryExerciseId = sharedPreferences?.getInt("diaryExerciseId", -1)
        diaryFoodId = sharedPreferences?.getInt("diaryFoodId", -1)
        Log.d("testId", "${diaryExerciseId} + ${diaryFoodId}")
        setupClickListeners()
    }

    private fun showWeightInputDialog() {
        val currentContext = requireContext()
        val dialogView = LayoutInflater.from(currentContext).inflate(R.layout.dialog_weight_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextWeight)
        
        val dialog = AlertDialog.Builder(currentContext, R.style.WeightDialogTheme)
            .setView(dialogView)
            .create()

        editText.apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            filters = arrayOf(InputFilter.LengthFilter(5))
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
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
                    lifecycleScope.launch {
                        try {
                            val token = userPreferences.authStateFlow.first().accessToken
                            if (token != null) {
                                val result = authRepository.saveBodyMeasurement(weight.toFloat(), token)
                                val sharedPreferences = context?.getSharedPreferences("UserPreferences", MODE_PRIVATE)
                                val editor = sharedPreferences?.edit()
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                    Date()
                                )
                                editor?.putString("last_weight_change_date", today)
                                editor?.apply()
                                if (isAdded && context != null) {
                                    when (result) {
                                        is Resource.Success -> {

                                            Toast.makeText(requireContext(), "Đã cập nhật cân nặng", Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                            dismiss()
                                        }
                                        is Resource.Failure -> {
                                            Toast.makeText(
                                                requireContext(),
                                                "Lỗi: ${if (result.isNetworkError) "Kiểm tra kết nối mạng" else "Không thể cập nhật"}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            if (isAdded && context != null) {
                                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnWeight.setOnClickListener {
                showWeightInputDialog()
            }

            btnNewGoal.setOnClickListener {
                //findNavController().navigate(R.id.action_quickAction_to_newGoal)
                dismiss()
            }

            btnBreakfast.setOnClickListener {
               findNavController().navigate(R.id.addFoodFragment, bundleOf("meal_type" to MealType.BREAKFAST,"diary_id" to diaryFoodId))
                dismiss()
            }

            btnLunch.setOnClickListener {
               findNavController().navigate(R.id.addFoodFragment, bundleOf("meal_type" to MealType.LUNCH,"diary_id" to diaryFoodId))
                dismiss()
            }

            btnDinner.setOnClickListener {
                findNavController().navigate(R.id.addFoodFragment, bundleOf("meal_type" to MealType.DINNER,"diary_id" to diaryFoodId))
                dismiss()
            }

            btnSnacks.setOnClickListener {
                findNavController().navigate(R.id.addFoodFragment, bundleOf("meal_type" to MealType.SNACK,"diary_id" to diaryFoodId))
                dismiss()
            }

            btnExercise.setOnClickListener {
                findNavController().navigate(R.id.workoutSelectionFragment, bundleOf("diary_id" to diaryExerciseId))
                dismiss()
            }
        }
    }
} 