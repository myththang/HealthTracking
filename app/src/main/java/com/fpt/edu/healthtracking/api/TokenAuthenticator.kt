import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.TokenRequest
import com.fpt.edu.healthtracking.data.request.LoginRequest
import kotlinx.coroutines.flow.first

class TokenAuthenticator(
    private val userPreferences: UserPreferences,
    private val authApi: AuthApi
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuth"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            Log.e(TAG, "Auth started for ${response.request.url}")

            val authState = userPreferences.authStateFlow.first()
            if (authState.accessToken == null || authState.refreshToken == null) {
                Log.e(TAG, "No tokens available")
                userPreferences.clearAuth()
                return@runBlocking null
            }

            try {
                Log.e(TAG, "Attempting token refresh")
                val refreshResult = authApi.renewToken(
                    TokenRequest(
                        accessToken = authState.accessToken,
                        refreshToken = authState.refreshToken
                    )
                )

                Log.e(TAG, "Refresh response: success=${refreshResult.success}, message=${refreshResult.message}")

                // Check both success flag and data
                if (!refreshResult.success || refreshResult.data == null) {
                    Log.e(TAG, "Token refresh failed, attempting auto login")

                    // Try auto login if credentials exist
                    if (authState.email != null && authState.password != null) {
                        Log.e(TAG, "Attempting auto login for ${authState.email}")

                        val loginResult = authApi.login(
                            LoginRequest(
                                phoneNumber = authState.email,
                                password = authState.password
                            )
                        )

                        if (loginResult.success) {
                            Log.e(TAG, "Auto login successful")
                            userPreferences.saveAuthTokens(
                                loginResult.data.accessToken,
                                loginResult.data.refreshToken
                            )

                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", "Bearer ${loginResult.data.accessToken}")
                                .build()
                        }
                    }

                    Log.e(TAG, "Authentication failed completely")
                    userPreferences.clearAuth()
                    return@runBlocking null
                }

                // Token refresh successful
                Log.e(TAG, "Token refresh successful")
                userPreferences.saveAuthTokens(
                    refreshResult.data.accessToken,
                    refreshResult.data.refreshToken
                )

                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshResult.data.accessToken}")
                    .build()

            } catch (e: Exception) {
                Log.e(TAG, "Error during auth: ${e.message}", e)
                userPreferences.clearAuth()
                return@runBlocking null
            }
        }
    }
}