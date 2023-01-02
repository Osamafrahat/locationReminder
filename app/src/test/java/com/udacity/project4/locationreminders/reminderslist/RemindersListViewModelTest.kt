package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.BaseViewModelTest
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : BaseViewModelTest() {

    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun before() {
        dataSource = FakeDataSource()
        app = ApplicationProvider.getApplicationContext()
        remindersListViewModel = RemindersListViewModel(app, dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_WithSuccess() = mainCoroutineRule.runBlockingTest {
        // GIVEN - reminders are saved in DB
        val reminder = buildReminderDTO()
        dataSource.saveReminder(reminder)

        // WHEN - load reminders
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN - loading is hidden and reminders are retrieved
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.size, `is`(1))
        assertThat(remindersList[0].id, `is`(reminder.id))
    }

    @Test
    fun loadReminders_WithError() = mainCoroutineRule.runBlockingTest {
        // GIVEN - getReminders will return an error
        dataSource.setReturnError(true)

        // WHEN - load reminders
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN - loading is hidden and snackbar contains error message
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(FakeDataSource.ERROR_MESSAGE))
    }
}