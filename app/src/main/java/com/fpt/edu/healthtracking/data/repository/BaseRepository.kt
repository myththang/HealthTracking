package com.fpt.edu.healthtracking.data.repository

import retrofit2.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.fpt.edu.healthtracking.api.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

abstract class BaseRepository {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        Resource.Failure(true, null, null)
                    }
                }
            }
        }
    }
}