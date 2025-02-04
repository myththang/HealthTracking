import android.util.Patterns
import java.util.Calendar

class ProfileValidator {

    // Validate Name
    fun validateName(name: String): Boolean {
        return name.isNotEmpty()
    }

    // Validate Email
    fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate Phone Number
    fun validatePhone(phone: String): Boolean {
        return phone.isNotEmpty() && phone.length >= 10
    }

    // Validate Date of Birth (DD/MM/YYYY)
    fun validateDob(dob: String): String? {
        if (dob.isEmpty()) {
            return "Vui lòng nhập ngày sinh"
        }
        if (dob.length < 10) {
            return "Vui lòng nhập đầy đủ ngày sinh"
        }

        val parts = dob.split("/")
        if (parts.size != 3) {
            return "Định dạng ngày sinh không hợp lệ"
        }

        return try {
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            when {
                !isValidDate(day, month, year) -> "Ngày sinh không hợp lệ"
                !isValidYear(year, month, day) -> "Tuổi phải từ 18 đến 100"
                else -> null
            }
        } catch (e: Exception) {
            "Định dạng ngày sinh không hợp lệ"
        }
    }

    // Validate Height
    fun validateHeight(height: String): Boolean {
        return try {
            val heightValue = height.toInt()
            heightValue in 50..250 // Assuming height in cm and valid range is 50 to 300 cm
        } catch (e: NumberFormatException) {
            false
        }
    }

    // Validate Weight
    fun validateWeight(weight: String): Boolean {
        return try {
            val weightValue = weight.toInt()
            weightValue in 20..500 // Assuming weight in kg and valid range is 20 to 500 kg
        } catch (e: NumberFormatException) {
            false
        }
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

    private fun isValidYear(year: Int, month: Int, day: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        return (currentYear - year) in 18..100
    }
}
