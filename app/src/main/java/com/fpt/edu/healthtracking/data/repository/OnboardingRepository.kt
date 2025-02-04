package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.OnboardingPage


class OnboardingRepository : BaseRepository() {


    suspend fun getOnboardingPages(): List<OnboardingPage> {

        return listOf(
            OnboardingPage(
                R.drawable.onboard1,
                "Xin chào!",
                "Đây là một khởi đầu tuyệt vời cho một cuộc sống mới, khỏe mạnh hơn."
            ),
            OnboardingPage(
                R.drawable.onboard2,
                "Quản lý bữa ăn hiệu quả",
                "Cập nhật thực đơn hàng ngày một cách đơn giản."
            ),
            OnboardingPage(
                R.drawable.onboard3,
                "Ghi chép nhanh gọn các buổi tập",
                "Tùy chỉnh và theo dõi lịch trình tập luyện của riêng bạn"
            )
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {

    }
}