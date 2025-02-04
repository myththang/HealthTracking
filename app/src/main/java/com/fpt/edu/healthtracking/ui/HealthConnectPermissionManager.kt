package com.fpt.edu.healthtracking.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.HealthConnectManager
import kotlinx.coroutines.launch

class PermissionsRationaleActivity : AppCompatActivity() {

    // Define permissions to read and write StepsRecord data
    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    // Create the permissions launcher
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    // Register for the permission result
    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions successfully granted
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
        } else {
            // Lack of required permissions
            // Optionally display a message or redirect to the rationale again
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_rationale)

        val privacyPolicyText: TextView = findViewById(R.id.privacy_policy_text)
        val agreeButton: Button = findViewById(R.id.agree_button)

        // Load the privacy policy text
        privacyPolicyText.text = getString(R.string.privacy_policy)

        // Handle the agree button click
        agreeButton.setOnClickListener {
            lifecycleScope.launch {
                checkPermissionsAndRun()
            }

        }
    }

    // Function to check permissions and proceed
    private suspend fun checkPermissionsAndRun() {
        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        // Check if permissions are granted
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions already granted, proceed with the operation

        } else {
            // Request missing permissions
            requestPermissions.launch(PERMISSIONS)
        }
    }

    // Function to perform Health Connect operations after permissions are granted
    private suspend fun startHealthConnectOperations() {
    }
}
