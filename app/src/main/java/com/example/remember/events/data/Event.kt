package com.example.remember.events.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(val title: String, val startTime: String, val endTime: String) : Parcelable