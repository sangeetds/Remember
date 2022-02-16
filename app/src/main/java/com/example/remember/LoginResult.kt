package com.example.remember


/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
  var success: List<Event>? = null,
  val created: String? = null,
  val error: String? = null
)