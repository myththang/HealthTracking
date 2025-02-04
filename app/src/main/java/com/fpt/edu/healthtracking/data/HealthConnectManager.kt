package com.fpt.edu.healthtracking.data


import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing foot step data (StepsRecord) from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * Reads step count data for a given time range.
     */
    suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord> {
        // Create a time range filter for the given start and end time
        val timeRangeFilter = TimeRangeFilter.between(start, end)

        // Create a ReadRecordsRequest specifically for StepsRecord
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = timeRangeFilter
        )

        // Read the step records from Health Connect and return the results
        return healthConnectClient.readRecords(request).records
    }



    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK



}


enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}
