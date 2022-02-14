package com.socialbox.login.ui

import com.socialbox.login.data.model.User

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
  var success: User? = null,
  val created: User? = null,
  val error: String? = null
)