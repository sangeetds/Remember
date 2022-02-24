package com.example.remember.events.ui

import androidx.lifecycle.Observer
import com.example.remember.TestCoroutineRule
import com.example.remember.events.EventsResult
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class EventsViewModelTest : TestCase() {

  @get:Rule
  val testCoroutineRule = TestCoroutineRule()

  private lateinit var eventsViewModel: EventsViewModel
  private val loginResultObserver: Observer<EventsResult> = spyk()

  @Before
  override fun setUp() {
    eventsViewModel = EventsViewModel()
  }

  @Test
  fun `sampleTest`() {

  }
}