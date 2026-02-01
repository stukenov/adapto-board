package com.playoutedge.player.enrollment

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playoutedge.player.network.DeviceInfo
import com.playoutedge.player.network.EnrollRequest
import com.playoutedge.player.network.PlayerApiClient
import com.playoutedge.player.network.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for enrollment flow.
 */
class EnrollmentViewModel(
    private val apiClient: PlayerApiClient,
    private val tokenStorage: TokenStorage,
    private val appVersion: String
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.EnterCode)
    val state: StateFlow<EnrollmentState> = _state.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    /**
     * Update code input.
     */
    fun updateCode(newCode: String) {
        _code.value = newCode.uppercase().filter { it.isLetterOrDigit() }.take(6)
    }

    /**
     * Submit enrollment code.
     */
    fun submitCode() {
        if (_code.value.length < 6) return

        _state.value = EnrollmentState.Enrolling

        viewModelScope.launch {
            val request = EnrollRequest(
                code = _code.value,
                deviceInfo = DeviceInfo(
                    model = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = Build.VERSION.RELEASE,
                    appVersion = appVersion
                )
            )

            val result = apiClient.enroll(request)

            result.fold(
                onSuccess = { response ->
                    // Save tokens
                    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                    tokenStorage.saveDeviceInfo(response.deviceId, response.tenantId)

                    // Update state
                    _state.value = if (response.channelId != null) {
                        EnrollmentState.Enrolled(response.deviceId, response.channelId)
                    } else {
                        EnrollmentState.WaitingForChannel(response.deviceId)
                    }
                },
                onFailure = { error ->
                    _state.value = EnrollmentState.Error(
                        message = when {
                            error.message?.contains("expired", ignoreCase = true) == true ->
                                "Code expired. Please request a new code."
                            error.message?.contains("invalid", ignoreCase = true) == true ->
                                "Invalid code. Please check and try again."
                            else -> error.message ?: "Enrollment failed. Please try again."
                        }
                    )
                }
            )
        }
    }

    /**
     * Retry enrollment.
     */
    fun retry() {
        _code.value = ""
        _state.value = EnrollmentState.EnterCode
    }
}
