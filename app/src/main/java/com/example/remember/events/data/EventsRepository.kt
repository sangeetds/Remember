package com.example.remember.events.data

import com.example.remember.events.data.model.Event

interface EventsRepository {

  var cacheEvents: List<Event>?

  fun saveEvents(events: List<Event>)

  fun updateEvents(events: List<Event>)
}
