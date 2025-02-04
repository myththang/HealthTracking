package com.fpt.edu.healthtracking.ui.home

import QuickActionBottomSheet
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.StartActivity
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.databinding.ActivityHomePageBinding
import kotlinx.coroutines.launch
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.fpt.edu.healthtracking.util.handler.PermissionHandler
// Or for more specific import:
import com.fpt.edu.healthtracking.util.noti.test.MealReminderWorker
import com.fpt.edu.healthtracking.util.noti.test.NotificationHelper
import com.fpt.edu.healthtracking.util.noti.test.BootReceiver
import android.util.Log
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var navController: NavController
    private lateinit var userPreferences: UserPreferences
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var permissionHandler: PermissionHandler
    private var backPressedTime: Long = 0
    private var permissionCheckComplete = false
    private val BACK_PRESS_INTERVAL = 2000
    private val PREFS_NAME = "AppPermissionPrefs"
    private val PERMISSION_REQUESTED_KEY = "notification_permission_requested"

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                if (!authState.isLoggedIn) {
                    startActivity(Intent(this@HomePageActivity, StartActivity::class.java))
                    finish()
                    return@collect
                }
            }
        }
        permissionHandler = PermissionHandler(this)
        supportActionBar?.hide()

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(this, R.id.nav_host_fragment_activity_home_page)
        setupBottomNavigation()

        checkNotificationPermission()

        setupBackPressHandling()


    }


    private fun checkNotificationPermission() {
        Log.d("checkLag","laggggg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val permissionRequested = prefs.getBoolean(PERMISSION_REQUESTED_KEY, false)

            when {
                hasNotificationPermission() -> {
                    MealReminderWorker.scheduleMealReminders(this)
                    permissionCheckComplete = true
                }
                !permissionRequested -> {
                    requestNotificationPermission()
                    prefs.edit().putBoolean(PERMISSION_REQUESTED_KEY, true).apply()
                }
                else -> {
                    // Mark as complete if we've already asked before
                    permissionCheckComplete = true
                }
            }
        } else {
            MealReminderWorker.scheduleMealReminders(this)
            permissionCheckComplete = true
        }
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController(R.id.nav_host_fragment_activity_home_page)
                val currentDestination = navController.currentDestination

                when (currentDestination?.id) {
                    R.id.navigation_home,
                    R.id.navigation_food_log,
                    R.id.navigation_nutrition,
                    R.id.navigation_menumoremenu -> {
                        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
                            finish()
                        } else {
                            backPressedTime = System.currentTimeMillis()
                            Toast.makeText(
                                this@HomePageActivity, 
                                "Nhấn BACK lần nữa để thoát", 
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    else -> {
                        navController.navigateUp()
                    }
                }
            }
        })
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for older Android versions
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, start service

            } else {
                // Permission denied, handle accordingly
                // Maybe show a rationale or disable notification features
            }
        }
    }


    private fun setupBottomNavigation() {
        binding.navView.apply {
            setupWithNavController(this, navController)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navController.navigate(R.id.navigation_home)
                        true
                    }

                    R.id.navigation_food_log -> {
                        navController.navigate(R.id.navigation_food_log)
                        true
                    }
                    R.id.navigation_nutrition ->{
                        navController.navigate(R.id.navigation_nutrition)
                        true
                    }
                    R.id.navigation_menumoremenu ->{


                        navController.navigate(R.id.navigation_menumoremenu)
                        true
                    }
                    else -> false
                }
            }
        }

        binding.fab.setOnClickListener {
            QuickActionBottomSheet().apply {
                show(supportFragmentManager, "quickAction")
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home, R.id.navigation_food_log,R.id.navigation_nutrition,R.id.navigation_menumoremenu -> {
                    binding.fab.show()
                    binding.bottomAppBar.performShow()
                }
                else -> {
                    binding.fab.hide()
                    binding.bottomAppBar.performHide()
                }
            }
        }
    }

    fun logout() {
        lifecycleScope.launch {
            userPreferences.clearAuth()
            startActivity(Intent(this@HomePageActivity, StartActivity::class.java))
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkAndRequestPermissions() {
        when {

            !permissionHandler.hasNotificationPermission() -> {
                if(!permissionCheckComplete){
                    permissionHandler.requestNotificationPermission()
                }
            }
            else -> {
                // All permissions granted, schedule reminders
                scheduleMealReminders()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("The app needs these permissions to function properly. Please grant them in Settings.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun scheduleMealReminders() {
        MealReminderWorker.scheduleMealReminders(this)
    }


    override fun onResume() {
        super.onResume()
        checkAuthState()
        checkAndRequestPermissions()
    }

    private fun checkAuthState() {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                if (authState.accessToken == null || authState.refreshToken == null) {
                    startActivity(Intent(this@HomePageActivity, StartActivity::class.java))
                    finish()
                }
            }
        }
    }

}