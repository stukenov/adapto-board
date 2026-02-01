package com.playoutedge.player.enrollment

/**
 * Enrollment state.
 */
sealed class EnrollmentState {
    data object EnterCode : EnrollmentState()
    data object Enrolling : EnrollmentState()
    data class WaitingForChannel(val deviceId: String) : EnrollmentState()
    data class Enrolled(val deviceId: String, val channelId: String) : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}
