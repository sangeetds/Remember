package com.example.remember.events.data

import androidx.lifecycle.LiveData
import com.example.remember.events.data.model.Event

class EventsRepository(private val eventDatabaseDao: EventDatabaseDao) {

  var cacheEvents: List<Event>? = null

  val allEvents: LiveData<List<Event>> = this.eventDatabaseDao.getAll()

  suspend fun saveEvents(events: List<Event>) {
    this.eventDatabaseDao.insertAll(events = events)
    this.cacheEvents = events
  }

  suspend fun updateEvents(events: List<Event>) = this.eventDatabaseDao.updateAll(event = events)
}
