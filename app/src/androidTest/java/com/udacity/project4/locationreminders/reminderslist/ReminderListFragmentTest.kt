package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun before() {
        dataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        stopKoin()

        val myModule = module {
            single {
                remindersListViewModel
            }
        }

        startKoin {
            modules(listOf(myModule))
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun displayReminderList() = runBlockingTest {
        // GIVEN - reminders are saved in the database
        val reminder = buildReminderDTO()
        dataSource.saveReminder(reminder)

        // WHEN - Fragment is launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - empty list message is NOT displayed, and reminder is displayed
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView)).perform(
            // scrollTo will fail the test if no item matches.
            RecyclerViewActions.scrollTo<DataBindingViewHolder<ReminderDataItem>>(
                hasDescendant(withText(reminder.title))
            )
        )
    }

    @Test
    fun displayEmptyList() = runBlockingTest {
        // GIVEN - database is empty
        dataSource.deleteAllReminders()

        // WHEN - Fragment is launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - empty list message is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(withText("No Data")))
    }

    @Test
    fun navigateToAddReminder() {
        // GIVEN - Fragment is launched
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the Add Reminder button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // THEN - Verify that you navigate to the Add Reminder fragment
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    private fun buildReminderDTO(): ReminderDTO {
        return ReminderDTO(
            "title",
            "description",
            "location",
            15.0292,
            16.2339
        )
    }
}