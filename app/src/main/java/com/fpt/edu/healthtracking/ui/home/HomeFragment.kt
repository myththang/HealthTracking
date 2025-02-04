package com.fpt.edu.healthtracking.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.health.connect.client.time.TimeRangeFilter

import android.os.Bundle
import android.util.Log
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.DashboardApi
import com.fpt.edu.healthtracking.data.HealthConnectAvailability
import com.fpt.edu.healthtracking.data.HealthConnectManager
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.DashboardData
import com.fpt.edu.healthtracking.data.repository.DashBoardRepository
import com.fpt.edu.healthtracking.databinding.FragmentHomeBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.file.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit


class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, DashBoardRepository>() {
    private var count = 0
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(requireContext()) // Initialize here
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        checkWeightUpdateNeeded()
        bindingView()
        observeViewModel()
        checkPermissionsAndRun(false)
        loadApi(LocalDate.now())
        setupDatePicker()
        checkWater()


    }

    private fun checkWeightUpdateNeeded() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastCheckDate = sharedPreferences.getString("last_weight_check_date", null)

        Log.d("WeightCheck", "Today: $today")
        Log.d("WeightCheck", "Last check date: $lastCheckDate")

        if (lastCheckDate != today) {

            sharedPreferences.edit().putString("last_weight_check_date", today).apply()

            val lastWeightUpdateStr = sharedPreferences.getString("last_weight_change_date", null)

            Log.d("WeightCheck", "Last weight update date: $lastWeightUpdateStr")

            if (lastWeightUpdateStr == null) {
                Log.d("WeightCheck", "No last weight update date found")
                sharedPreferences.edit()
                    .putString("last_weight_change_date", today)
                    .apply()
                return
            }

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val lastUpdateDate = dateFormat.parse(lastWeightUpdateStr)
                val currentDate = Date()

                Log.d("WeightCheck", "Parsed last update date: $lastUpdateDate")
                Log.d("WeightCheck", "Current date: $currentDate")

                val diffInMillies = currentDate.time - lastUpdateDate.time
                val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)

                Log.d("WeightCheck", "Days difference: $diffInDays")

                if (diffInDays > 7) {
                    Log.d("WeightCheck", "Showing dialog")
                    showWeightUpdateDialog()
                }
            } catch (e: Exception) {
                Log.e("WeightCheck", "Error parsing date", e)
                sharedPreferences.edit()
                    .putString("last_weight_change_date", today)
                    .apply()
            }
        } else {
            Log.d("WeightCheck", "Already checked today")
        }
    }

    private fun showWeightUpdateDialog() {
        WeightUpdateDialog().apply {
            setOnUpdateClickListener {
                //findNavController().navigate(R.id.action_navigation_home_to_goalFragment)
            }
        }.show(parentFragmentManager, "weight_update_dialog")
    }

    private fun checkWater() {
        val sharedPreferences = requireContext().getSharedPreferences("WaterBottlePrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Get the saved count and last saved date
        val savedCount = sharedPreferences.getInt("count", 0)
        val lastSavedDate = sharedPreferences.getString("lastSavedDate", "")
        Log.d("duydbug", savedCount.toString()+" "+lastSavedDate)
        // Get today's date in a specific format (e.g., yyyy-MM-dd)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Reset the count if it's a new day
        if (lastSavedDate != today) {
            count = 0
            editor.putString("lastSavedDate", today) // Save today's date
            changeWaterBottleImage(0,editor)
        } else {
            count = savedCount // Restore the saved count if the same day
            changeWaterBottleImage(0,editor)
        }
    }

    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    // Register for the permission result
    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions granted, proceed with HealthConnect operations
            val healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
        //    HealthConnectManager(requireContext()).readSteps()
        } else {
            // Permissions denied, you can show a message or retry logic here
            Toast.makeText(requireContext(), "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getViewModel() = HomeViewModel::class.java
    private fun setupDatePicker() {


        binding.datePickerBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    binding.txtDate.text = formatDateDisplay(selectedDate)

                    // Load dashboard data for selected date
                    loadApi(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun formatDateDisplay(date: LocalDate): String {
        return when {
            date == LocalDate.now() -> "Hôm nay"
            date == LocalDate.now().minusDays(1) -> "Hôm qua"
            else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)


    override fun getFragmentRepository(): DashBoardRepository {
        val api = remoteDataSource.buildApi(DashboardApi::class.java)
        return DashBoardRepository(
            api = api,
            preferences = userPreferences
        )
    }

    private fun loadApi(date: LocalDate) {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                authState.accessToken?.let { token ->
                    viewModel.loadDashboardData(date)
                }
            }
        }
    }



    private fun observeViewModel() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            data?.let { updateNutrients(it) } // Update views with the data
            viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
                errorMsg?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
            viewModel.aiTip.observe(viewLifecycleOwner) { tip ->
                if (!tip.isNullOrEmpty()) {
                    binding.tvAITips.text = tip
                    if (binding.cvTips.visibility != View.VISIBLE) {
                        binding.cvTips.alpha = 0f
                        binding.cvTips.scaleX = 0f
                        binding.cvTips.scaleY = 0f
                        binding.cvTips.visibility = View.VISIBLE
                        binding.cvTips.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(500)
                            .setInterpolator(OvershootInterpolator(1.2f))
                            .start()
                    }
                } else {
                    binding.cvTips.animate()
                        .alpha(0f)
                        .scaleX(0f)
                        .scaleY(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.cvTips.visibility = View.GONE
                        }
                }
            }
        }



    private fun updateNutrients(data: DashboardData) {
        binding.apply {
            tvExerciseBurn.text = data.caloriesBurn.toInt().toString()+ " Cal"
            tvExerciseTime.text = data.totalDuration.toString()+" phút"
            tvBurnCalories.text = data.caloriesBurn.toInt().toString()
            
            // Cập nhật BMI và tình trạng cơ thể
            tvTitle.text = "BMI: ${String.format("%.1f", data.bmi)}"
            tvContent.text = when {
                data.bmi < 18.5 -> "Cân nặng thấp (gầy)"
                data.bmi in 18.5..22.9 -> "Bình thường"
                data.bmi in 23.0..24.9 -> "Thừa cân"
                data.bmi in 25.0..29.9 -> "Béo phì độ I"
                data.bmi in 30.0..39.9 -> "Béo phì độ II" 
                else -> "Béo phì độ III"
            }

            // Đặt màu sắc cho text tùy theo tình trạng
            tvContent.setTextColor(when {
                data.bmi < 18.5 -> ContextCompat.getColor(requireContext(), R.color.orange_400)
                data.bmi in 18.5..22.9 -> ContextCompat.getColor(requireContext(), R.color.green)
                else -> ContextCompat.getColor(requireContext(), R.color.red)
            })

            tvTotalCalories.text = data.totalCalories.toInt().toString()
            tvCaloriesIntake.text = data.caloriesIntake.toInt().toString()
            
            tvStreak.text = data.streakNumberFood.toString()
            streakExercise.text = data.streakNumberExercise.toString()

            // Carbs
            if (data.carbsIntake > data.totalCarb) {
                val overCarbs = data.carbsIntake - data.totalCarb
                txtCarb.text = "Quá ${ceil(overCarbs).toInt()}g"
            } else {
                txtCarb.text = "${ceil(data.carbsIntake).toInt()}g / ${ceil(data.totalCarb).toInt()}g"
            }

            // Fat
            if (data.fatIntake > data.totalFat) {
                val overFat = data.fatIntake - data.totalFat
                txtFat.text = "Quá ${ceil(overFat).toInt()}g"
            } else {
                txtFat.text = "${ceil(data.fatIntake).toInt()}g / ${ceil(data.totalFat).toInt()}g"
            }

            // Protein
            if (data.proteinIntake > data.totalProtein) {
                val overProtein = data.proteinIntake - data.totalProtein
                txtProtein.text = "Quá ${ceil(overProtein).toInt()}g"
            } else {
                txtProtein.text =
                    "${ceil(data.proteinIntake).toInt()}g / ${ceil(data.totalProtein).toInt()}g"
            }

            //luu vao preferencedata.weight
            updateProgressAndValues(data.totalCalories.toFloat(),data.caloriesIntake.toFloat())
            // Update progress bars
            val carbPercentage = calculatePercentage(data.carbsIntake, data.totalCarb)
            val fatPercentage = calculatePercentage(data.fatIntake, data.totalFat)
            val proteinPercentage = calculatePercentage(data.proteinIntake, data.totalProtein)

            // Launch coroutine to update progress bars
            lifecycleScope.launch {
                // Use ValueAnimator to animate the progress change for each progress bar
                animateProgressBar(progressBarCarb, carbPercentage)
                animateProgressBar(progressBarFat, fatPercentage)
                animateProgressBar(progressBarProtein, proteinPercentage)
            }
            val imageUrl =data.imageMember
            if(imageUrl!=""){
                Glide.with(requireContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.default_avatar)
                    .into(profileImg)
            }
            val sharedPreferences = context?.getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val oldWeight = sharedPreferences?.getFloat("weight", 0f)
            Log.d("WeightDebug", "Old weight: $oldWeight")
            Log.d("WeightDebug", "New weight from API: ${data.weight}")

            val editor = sharedPreferences?.edit()
            editor?.putString("profile_image_path", data.imageMember)
            editor?.putString("fullname", data.fullName)
            editor?.putFloat("weight", data.weight.toFloat())
            editor?.putFloat("totalCarb", data.totalCarb.toFloat())
            editor?.putFloat("totalFat", data.totalFat.toFloat())
            editor?.putFloat("totalProtein", data.totalProtein.toFloat())
            editor?.apply()

            val newWeight = sharedPreferences?.getFloat("weight", 0f)
            Log.d("WeightDebug", "Saved weight: $newWeight")

            //profileImg.setImageURI(ImageUtils.setImage(requireContext()))

        }
    }

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    fun animateProgressBar(progressBar: ProgressBar, targetProgress: Int) {
        val currentProgress = progressBar.progress
        val animator = ValueAnimator.ofInt(currentProgress, targetProgress)
        animator.duration = 1000 // Duration of animation (in ms)

        animator.addUpdateListener { valueAnimator ->
            progressBar.progress = valueAnimator.animatedValue as Int
        }

        animator.start()
    }

    private fun calculatePercentage(value: Double, goal: Double): Int {
        return ((value / goal) * 100).toInt()
    }
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            requireContext().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    private fun bindingView() {
        binding.healthConnectCardView.setOnClickListener{
            checkPermissionsAndRun(true)
        }

        binding.loginStreakImg.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_home_to_streakFragment)
        }
        binding.streakImg.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_home_to_exerciseStreakFragment)
        }

        binding.profileImg.setOnClickListener {
            val navController: NavController = Navigation.findNavController(it)
            navController.navigate(R.id.navigation_profile)
        }

        binding.notiImg.setOnClickListener {
            val navController: NavController = Navigation.findNavController(it)
            navController.navigate(R.id.navigation_notifications)
        }

        binding.addWater.setOnClickListener { changeWaterBottleImage(1,editor) }
        binding.minusWater.setOnClickListener { changeWaterBottleImage(-1,editor) }

        binding.btnAddExercise.setOnClickListener {
            val navController: NavController = Navigation.findNavController(it)
            navController.navigate(R.id.action_navigation_home_to_workoutLogFragment)
        }
    }
    private fun checkPermissionsAndRun(click:Boolean) {
        val healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
        // Check if permissions are granted
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(PERMISSIONS)) {
                // Permissions granted, proceed with the operation
                binding.healthConnectCardView.visibility= View.GONE
                binding.stepCardView.visibility= View.VISIBLE
                readStepsForToday(healthConnectClient)
            } else  if(click){
                // Request permissions
                binding.stepCardView.visibility= View.INVISIBLE
                requestPermissions.launch(PERMISSIONS)
            }
        }

    }

    private fun readStepsForToday(healthConnectClient: HealthConnectClient) {
        // Get the start and end times for today
        val now = Instant.now()
        val startOfDay = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atTime(23, 59, 59).toInstant(ZoneOffset.UTC)

        // Call readSteps with today's date range
        lifecycleScope.launch {
            val steps = readSteps(healthConnectClient, startOfDay, endOfDay)
            // Update the UI with the retrieved steps
            if (steps.isNotEmpty()) {
                val totalSteps = steps.sumOf { it.count }
                binding.tvSteps.text = "$totalSteps"
                updateStepProgress(totalSteps.toInt())
            } else {
                binding.tvSteps.text = "0"
                updateStepProgress(0)
            }
        }
    }

    private fun updateStepProgress(totalSteps: Int) {
        // Define your step goal (e.g., 10,000 steps for the day)
        val stepGoal = 2000

        // Normalize the total steps to a percentage of the step goal (max 100%)
        val progress = if (totalSteps > stepGoal) 100 else (totalSteps.toFloat() / stepGoal * 100).toInt()

        // Update the circular progress bar with the calculated progress
        binding.circularProgressStep.progress = progress
    }

    suspend fun readSteps(
        healthConnectClient: HealthConnectClient,
        start: Instant,
        end: Instant
    ): List<StepsRecord> {
        // Create a time range filter for the given start and end time
        val timeRangeFilter = TimeRangeFilter.between(start, end)

        // Log the start and end times in a readable format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())


        // Create a ReadRecordsRequest specifically for StepsRecord
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = timeRangeFilter
        )

        // Read the step records from Health Connect and return the results
        return healthConnectClient.readRecords(request).records
    }


    private fun updateProgressAndValues(canNap: Float, daNap: Float) {
        // Calculate the percentage
        val percentage = (daNap / canNap) * 100
        val targetProgress = percentage.toInt()

        // Check if the target progress is different from the current progress to avoid unnecessary animations
        if (binding.circularProgress.progress != targetProgress) {
            // Create an ObjectAnimator to smoothly animate the progress update
            val animator = ObjectAnimator.ofInt(binding.circularProgress, "progress", binding.circularProgress.progress, targetProgress)
            animator.duration = 1000 // Animation duration in milliseconds (1 second here)
            animator.interpolator = DecelerateInterpolator() // Smooth out the animation
            animator.start()
        }
    }

    private fun changeWaterBottleImage(i: Int,editor: SharedPreferences.Editor) {

        count += i
        var waterBottleIv = binding.waterBottleIv
        // Update the water bottle image based on the count
        when (count) {
            0 -> waterBottleIv.setImageResource(R.drawable.waterbottle_0)
            1 -> waterBottleIv.setImageResource(R.drawable.waterbottle_1)
            2 -> waterBottleIv.setImageResource(R.drawable.waterbottle_2)
            3 -> waterBottleIv.setImageResource(R.drawable.waterbottle_3)
            4 -> waterBottleIv.setImageResource(R.drawable.waterbottle_4)
            5 -> waterBottleIv.setImageResource(R.drawable.waterbottle_5)
            6 -> waterBottleIv.setImageResource(R.drawable.waterbottle_6)
            7 -> waterBottleIv.setImageResource(R.drawable.waterbottle_7)
            else -> count -= i // Revert the increment if count exceeds limits
        }

        // Save the updated count
        editor.putInt("count", count)
        editor.apply() // Apply changes to SharedPreferences
    }


    private fun navigateToNotifications() {
        val navController = Navigation.findNavController(
            requireActivity(), R.id.nav_host_fragment_activity_home_page
        )
        navController.navigate(R.id.navigation_notifications)
    }


    private fun updateStepsUI(totalSteps: Int) {
        // Assuming you have a TextView and ProgressBar for showing steps
        val tvStepCount: TextView = binding.root.findViewById(R.id.tvSteps) // Adjust ID
        val stepProgressBar: ProgressBar = binding.root.findViewById(R.id.circularProgressStep) // Adjust ID

        // Set the text to show the total steps
        tvStepCount.text = "$totalSteps steps"

        // Set progress bar based on a daily step goal (e.g., 10,000 steps)
        val dailyStepGoal = 10000
        stepProgressBar.max = dailyStepGoal
        stepProgressBar.progress = totalSteps.coerceAtMost(dailyStepGoal) // Don't exceed max

        // Optionally, show a message if goal is reached
        if (totalSteps >= dailyStepGoal) {
            Toast.makeText(
                requireContext(),
                "Congratulations! You've reached your step goal.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    suspend fun readStepsByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            var totalSteps = 0
            for (stepRecord in response.records) {
                totalSteps += stepRecord.count.toInt()
            }

            // Update UI with the step count
            updateStepsUI(totalSteps)
        } catch (e: Exception) {
            // Handle any errors
            Toast.makeText(requireContext(), "Failed to read steps: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    fun startReadingStepsEvery3Min(
        healthConnectClient: HealthConnectClient,

    ) {
        val (startTime, endTime) = getStartAndEndTime()

        lifecycleScope.launch {
            while (isActive) {
                // Call the suspend function
                readStepsByTimeRange(healthConnectClient, startTime, endTime)

                // Delay for 3 minutes (180000 milliseconds)
                delay(180_000L)
            }
        }
    }
    fun getStartAndEndTime(): Pair<Instant, Instant> {
        // Get the current date and time zone
        val zoneId = ZoneId.systemDefault()

        // Get the start of today (midnight) in the current time zone
        val startOfDay = LocalDate.now().atStartOfDay(zoneId).toInstant()

        // Get the current time (now)
        val now = Instant.now()

        return Pair(startOfDay, now)
    }

}




