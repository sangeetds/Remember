package com.example.remember.events.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "events")
data class Event(

  @PrimaryKey(autoGenerate = true)
  var id: Long = 0L,

  @ColumnInfo(name = "title")
  val title: String,

  @ColumnInfo(name = "start_time")
  val startTime: String,

  @ColumnInfo(name = "end_time")
  val endTime: String,

  @ColumnInfo(name = "alarm_set")
  var alarmSet: Boolean = false
) : Parcelable