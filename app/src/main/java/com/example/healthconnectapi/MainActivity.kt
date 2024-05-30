package com.example.healthconnectapi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.platform.client.permission.Permission
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {
    private val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    private suspend fun hasAllPermissions(healthConnectClient: HealthConnectClient): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(requiredPermissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        val txtSteps: TextView = findViewById(R.id.txtSteps)
        val txtHeartRate: TextView = findViewById(R.id.txtHeartRate)
        val txtWeight: TextView = findViewById(R.id.txtWeight)
        val txtSleep: TextView = findViewById(R.id.txtSleep)
        readStepData(healthConnectClient, txtSteps)
        readHeartRateData(healthConnectClient, txtHeartRate)
        readWeightData(healthConnectClient, txtWeight)
        readSleepData(healthConnectClient, txtSleep)

    }

    // Function to read steps data from Health Connect API
    private fun readStepData(healthConnectClient: HealthConnectClient, txtSteps: TextView) {
        lifecycleScope.launch {
            if (hasAllPermissions(healthConnectClient)) {
                try {
                    val end = Instant.now()
                    val start = end.minus(30, ChronoUnit.DAYS) // Adjust time range as necessary
                    val request = ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end)
                    )
                    Log.d("HomeFragment", "Requesting steps data from $start to $end")
                    val response = healthConnectClient.readRecords(request)
                    Log.d("HomeFragment", "Received response with ${response.records.size} records")
                    val latestStepsRecord = response.records.map { it as StepsRecord }
                        .maxByOrNull { it.endTime }

                    val stepsDisplay = latestStepsRecord?.let {
                        "Steps Count:${it.count}"
                    } ?: "No data"

                    txtSteps.post { txtSteps.text = stepsDisplay }
                    Log.d("HealthConnectApiData", stepsDisplay)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error reading steps data", e)
                    txtSteps.post { txtSteps.text = "Failed to load data: ${e.localizedMessage}" }
                }
            } else {
                txtSteps.post { txtSteps.text = "Permissions needed" }
            }
        }
    }

    // Function to read HeartRate Data from HealthConnect API
    private fun readHeartRateData(healthConnectClient: HealthConnectClient, txtHeartRate: TextView) {
        lifecycleScope.launch {
            if (hasAllPermissions(healthConnectClient)) {
                try {
                    val end = Instant.now()
                    val start = end.minus(30, ChronoUnit.DAYS)
                    val request = ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end)
                    )
                    Log.d("HomeFragment", "Requesting heart rate data from $start to $end")
                    val response = healthConnectClient.readRecords(request)
                    Log.d("HomeFragment", "Received response with ${response.records.size} records")
                    val latestHeartRateRecord = response.records.map { it as HeartRateRecord }
                        .maxByOrNull { it.endTime }

                    val heartRateDisplay = latestHeartRateRecord?.let {
                        "Heart Rate: ${it.samples.lastOrNull()?.beatsPerMinute} BPM"
                    } ?: "No data"

                    txtHeartRate.post { txtHeartRate.text = heartRateDisplay }
                    Log.d("HealthConnectApiData", heartRateDisplay)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error reading heart rate data", e)
                    txtHeartRate.post {
                        txtHeartRate.text = "Failed to load data: ${e.localizedMessage}"
                    }
                }
            } else {
                txtHeartRate.post { txtHeartRate.text = "Permissions needed" }
            }
        }
    }

    // Function to read Weight Data from HealthConnect API
    private fun readWeightData(healthConnectClient: HealthConnectClient, txtWeight: TextView) {
        lifecycleScope.launch {
            if (hasAllPermissions(healthConnectClient)) {
                try {
                    val end = Instant.now()
                    val start = end.minus(30, ChronoUnit.DAYS)
                    val request = ReadRecordsRequest(
                        recordType = WeightRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end)
                    )
                    Log.d("HomeFragment", "Requesting weight data from $start to $end")
                    val response = healthConnectClient.readRecords(request)
                    Log.d("HomeFragment", "Received response with ${response.records.size} records")
                    val latestWeightRecord = response.records.map { it as WeightRecord }
                        .maxByOrNull { it.time }

                    val weightDisplay = latestWeightRecord?.weight?.let {
                        "%.2f kg".format(it.inKilograms)
                    } ?: "No data"

                    txtWeight.post {
                        txtWeight.text = weightDisplay
                        Log.d("HealthConnectApiData", "Weight: " + weightDisplay)
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error reading weight data", e)
                    txtWeight.post { txtWeight.text = "Failed to load data: ${e.localizedMessage}" }
                }
            } else {
                txtWeight.post { txtWeight.text = "Permissions needed" }
            }
        }
    }

    // Function to read Sleep Data from HealthConnect API
    private fun readSleepData(healthConnectClient: HealthConnectClient, txtSleep: TextView) {
        lifecycleScope.launch {
            if (hasAllPermissions(healthConnectClient)) {
                try {
                    val end = Instant.now()
                    val start = end.minus(30, ChronoUnit.DAYS)
                    val request = ReadRecordsRequest(
                        recordType = SleepSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end)
                    )
                    Log.d("HomeFragment", "Requesting sleep data from $start to $end")
                    val response = healthConnectClient.readRecords(request)
                    Log.d("HomeFragment", "Received response with ${response.records.size} records")
                    val latestSleepRecord = response.records.map { it as SleepSessionRecord }
                        .maxByOrNull { it.endTime }

                    val sleepDisplay = latestSleepRecord?.let {
                        val durationInMinutes = ChronoUnit.MINUTES.between(it.startTime, it.endTime)
                        "${durationInMinutes / 60}h ${durationInMinutes % 60}m"
                    } ?: "No data"

                    txtSleep.post {
                        txtSleep.text = sleepDisplay
                        Log.d("Sleep", sleepDisplay)
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error reading sleep data", e)
                    txtSleep.post { txtSleep.text = "Failed to load data: ${e.localizedMessage}" }
                }
            } else {
                txtSleep.post { txtSleep.text = "Permissions needed" }
            }
        }
    }
}