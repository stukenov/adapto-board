package com.playoutedge.auth

import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

interface EmailService {
    suspend fun sendPasswordResetEmail(to: String, resetToken: String, baseUrl: String)
    suspend fun sendVerificationEmail(to: String, verifyToken: String, baseUrl: String)
    suspend fun sendInviteEmail(to: String, tempPassword: String, inviterName: String, baseUrl: String)
}

class SmtpEmailService(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val fromAddress: String,
    private val fromName: String = "Playout Edge"
) : EmailService {

    companion object {
        fun fromEnvironment(): SmtpEmailService {
            return SmtpEmailService(
                host = System.getenv("SMTP_HOST") ?: "localhost",
                port = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 587,
                username = System.getenv("SMTP_USERNAME") ?: "",
                password = System.getenv("SMTP_PASSWORD") ?: "",
                fromAddress = System.getenv("SMTP_FROM_ADDRESS") ?: "noreply@playoutedge.com",
                fromName = System.getenv("SMTP_FROM_NAME") ?: "Playout Edge"
            )
        }
    }

    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
    }

    private fun sendEmail(to: String, subject: String, htmlBody: String) {
        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(fromAddress, fromName))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject
            setContent(htmlBody, "text/html; charset=UTF-8")
        }
        Transport.send(message)
    }

    override suspend fun sendPasswordResetEmail(to: String, resetToken: String, baseUrl: String) {
        val resetUrl = "$baseUrl/admin/reset-password?token=$resetToken"
        sendEmail(
            to = to,
            subject = "Reset Your Password - Playout Edge",
            htmlBody = """
                <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Reset Your Password</h2>
                    <p>You requested a password reset. Click the link below to set a new password:</p>
                    <p><a href="$resetUrl" style="display: inline-block; padding: 12px 24px; background: #2563eb; color: white; text-decoration: none; border-radius: 6px;">Reset Password</a></p>
                    <p>This link expires in 1 hour.</p>
                    <p>If you didn't request this, you can safely ignore this email.</p>
                </div>
            """.trimIndent()
        )
    }

    override suspend fun sendVerificationEmail(to: String, verifyToken: String, baseUrl: String) {
        val verifyUrl = "$baseUrl/verify-email?token=$verifyToken"
        sendEmail(
            to = to,
            subject = "Verify Your Email - Playout Edge",
            htmlBody = """
                <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Verify Your Email</h2>
                    <p>Thanks for signing up! Please verify your email address:</p>
                    <p><a href="$verifyUrl" style="display: inline-block; padding: 12px 24px; background: #2563eb; color: white; text-decoration: none; border-radius: 6px;">Verify Email</a></p>
                    <p>This link expires in 24 hours.</p>
                </div>
            """.trimIndent()
        )
    }

    override suspend fun sendInviteEmail(to: String, tempPassword: String, inviterName: String, baseUrl: String) {
        val loginUrl = "$baseUrl/admin/login"
        sendEmail(
            to = to,
            subject = "You've Been Invited to Playout Edge",
            htmlBody = """
                <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Welcome to Playout Edge</h2>
                    <p>$inviterName has invited you to join their organization.</p>
                    <p>Your temporary credentials:</p>
                    <ul>
                        <li><strong>Email:</strong> $to</li>
                        <li><strong>Password:</strong> $tempPassword</li>
                    </ul>
                    <p><a href="$loginUrl" style="display: inline-block; padding: 12px 24px; background: #2563eb; color: white; text-decoration: none; border-radius: 6px;">Sign In</a></p>
                    <p>You will be asked to change your password on first login.</p>
                </div>
            """.trimIndent()
        )
    }
}

/**
 * No-op email service for development/testing.
 */
class NoOpEmailService : EmailService {
    override suspend fun sendPasswordResetEmail(to: String, resetToken: String, baseUrl: String) {
        println("[EMAIL] Password reset for $to: token=$resetToken")
    }
    override suspend fun sendVerificationEmail(to: String, verifyToken: String, baseUrl: String) {
        println("[EMAIL] Verification for $to: token=$verifyToken")
    }
    override suspend fun sendInviteEmail(to: String, tempPassword: String, inviterName: String, baseUrl: String) {
        println("[EMAIL] Invite for $to: password=$tempPassword")
    }
}
