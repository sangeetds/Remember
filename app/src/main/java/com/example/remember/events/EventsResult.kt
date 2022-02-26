package com.example.remember.events

import com.example.remember.events.data.model.Event

/**
 * Authentication result : success (user details) or error message.
 */
data class EventsResult(
  val loading: Boolean = true,
  val success: List<Event>? = null,
  val error: Int? = null
)