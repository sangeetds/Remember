package com.example.remember


/**
 * Authentication result : success (user details) or error message.
 */
data class EventsResult(
  val loading: Boolean = true,
  val success: List<Event>? = null,
  val error: String? = null
)