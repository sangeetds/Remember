package com.example.remember.events.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.remember.events.data.model.Event

@Database(entities = [Event::class], version = 1)
abstract class EventsDatabase : RoomDatabase() {

  abstract fun eventsDao(): EventDatabaseDao
}
