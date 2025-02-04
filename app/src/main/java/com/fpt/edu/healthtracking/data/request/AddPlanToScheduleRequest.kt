package com.fpt.edu.healthtracking.data.request

data class AddPlanToScheduleRequest(
    val planId: Int,
    val day: Int,
)