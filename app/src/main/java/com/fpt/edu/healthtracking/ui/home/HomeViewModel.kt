package com.fpt.edu.healthtracking.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.BuildConfig
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.DashboardData
import com.fpt.edu.healthtracking.data.repository.DashBoardRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt

class HomeViewModel(
    private val repository: DashBoardRepository
) : ViewModel() {

    private val _aiTip = MutableLiveData<String>()
    val aiTip: LiveData<String> = _aiTip

    private val _dashboardData = MutableLiveData<DashboardData?>()
    val dashboardData: LiveData<DashboardData?> = _dashboardData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboardData(date: LocalDate) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getDashboardData(date)) {
                    is Resource.Success -> {
                        _dashboardData.value = result.value
                        result.value.let {
                            fetchAiTip(it)
                        }
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load dashboard data: ${result.errorBody?.string()}"
                        _dashboardData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _dashboardData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveDiaryIds(context: Context, diaryExerciseId: Int, diaryFoodId: Int) {
        val sharedPreferences = context.getSharedPreferences("diaryId", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        diaryExerciseId.let { editor.putInt("diaryExerciseId", it) }
        diaryFoodId.let { editor.putInt("diaryFoodId", it) }
        editor.apply()
    }


    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.API_KEY
    )

    private val chat = model.startChat()

    fun fetchAiTip(aiRequestBody: DashboardData) {
        viewModelScope.launch {
            try {
                _loading.value = true
                
                val prompt = buildAiPrompt(aiRequestBody)
                
                val response = chat.sendMessage(prompt)

                delay(300)
                _aiTip.value = response.text

            } catch (e: Exception) {
                _error.value = e.message ?: "Có lỗi xảy ra khi lấy gợi ý từ AI"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun buildAiPrompt(data: DashboardData): String {
        val hasNoIntake = data.caloriesIntake.toFloat() == 0f && data.proteinIntake.toFloat() == 0f &&
                data.carbsIntake.toFloat() == 0f && data.fatIntake.toFloat() == 0f

        val vietnamTime = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        val currentHour = vietnamTime.hour

        val expectedPercentage = when (currentHour) {
            in 0..5 -> 0
            in 6..9 -> 25
            in 10..13 -> 50
            in 14..17 -> 75
            else -> 100
        }

        return """
        ${if (hasNoIntake)
            """
            Người dùng chưa tiêu thụ gì cả. Hiện tại là $vietnamTime, vào thời điểm này nên đã tiêu thụ khoảng ${expectedPercentage}% chỉ tiêu trong ngày.
            Hãy đưa ra một câu động viên ngắn gọn (dưới 50 từ) để khuyến khích ăn uống, tập trung vào mục tiêu ${data.goalType} của họ và thời gian trong ngày.
            """
        else
            """
            Dựa trên dữ liệu dinh dưỡng dưới đây và thời gian hiện tại là $vietnamTime (nên đã tiêu thụ khoảng ${expectedPercentage}% chỉ tiêu),
            hãy đưa ra một câu phản hồi ngắn gọn (dưới 50 từ) về tình trạng tiêu thụ của người dùng. 
            Tập trung vào việc cảnh báo nếu vượt quá hoặc khuyến khích nếu đang đúng hướng, hoặc đề xuất nếu cần cải thiện.
            """
        }

        Thông tin cá nhân:
        - Giới tính: ${data.gender}
        - Tuổi: ${data.ageMember}
        - Chiều cao: ${data.height} cm
        - Cân nặng: ${data.weight} kg
        - Mức độ vận động: ${getActivityLevelDescription(data.exerciseLevel)}
        - Mục tiêu: ${data.goalType}
        
        Chỉ số dinh dưỡng mục tiêu:
        - Calories: ${data.totalCalories} kcal
        - Protein: ${data.totalProtein}g
        - Carbs: ${data.totalCarb}g
        - Chất béo: ${data.totalFat}g
        
        Đã tiêu thụ:
        - Calories: ${data.caloriesIntake} kcal (${(data.caloriesIntake / data.totalCalories * 100).roundToInt()}% mục tiêu)
        - Protein: ${data.proteinIntake}g (${(data.proteinIntake / data.totalProtein * 100).roundToInt()}% mục tiêu)
        - Carbs: ${data.carbsIntake}g (${(data.carbsIntake / data.totalCarb * 100).roundToInt()}% mục tiêu)
        - Chất béo: ${data.fatIntake}g (${(data.fatIntake / data.totalFat * 100).roundToInt()}% mục tiêu)
                
        Chỉ trả về duy nhất một câu phản hồi, không bao gồm tiêu đề hay định dạng khác.
    """.trimIndent()
    }

    private fun getActivityLevelDescription(level: Int): String {
        return when (level) {
            1 -> "Ít vận động"
            2 -> "Vận động nhẹ"
            3 -> "Vận động nhiều"
            else -> "Không xác định"
        }
    }

}

