package com.example.remember.common.di

import android.content.Context
import androidx.room.Room
import com.example.remember.events.data.EventDatabaseDao
import com.example.remember.events.data.EventsDatabase
import com.example.remember.events.data.EventsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Module {

  @Singleton
  @Provides
  fun provideYourDatabase(
    @ApplicationContext app: Context
  ) = Room.databaseBuilder(
    app,
    EventsDatabase::class.java,
    "your_db_name"
  ).build() // The reason we can construct a database for the repo

  @Singleton
  @Provides
  fun provideYourDao(eventsDatabase: EventsDatabase) = eventsDatabase.eventsDao()

  @Singleton
  @Provides
  fun productRepository(eventDatabaseDao: EventDatabaseDao) =
    EventsRepository(eventDatabaseDao = eventDatabaseDao)
}