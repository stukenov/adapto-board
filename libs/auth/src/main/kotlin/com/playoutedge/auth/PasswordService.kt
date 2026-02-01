package com.playoutedge.auth

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordService(
    private val costFactor: Int = 12
) {
    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(costFactor, password.toCharArray())
    }

    fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
