package com.example.remember.events.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.remember.events.data.model.Event

@Dao
interface EventDatabaseDao {

  @Query("SELECT * from events")
  fun getAll(): LiveData<List<Event>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(events: List<Event>)

  @Update
  suspend fun updateAll(event: List<Event>)
}