package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.BaseViewModelTest
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : BaseViewModelTest() {

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun before() {
        dataSource = FakeDataSource()
        app = ApplicationProvider.getApplicationContext()
        saveReminderViewModel = SaveReminderViewModel(
            app,
            dataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_WithInvalidData() {
        // GIVEN - invalid reminder data
        val reminderDataItem = ReminderDataItem(
            null,
            null,
            null,
            null,
            null
        )

        // WHEN - validate the reminder
        val result = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - a error message is set
        assertFalse(result)
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun validateEnteredData_WithValidData() {
        // GIVEN - valid reminder data
        val reminderDataItem = validReminderDataItem()

        // WHEN - validate the reminder
        val result = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - result is success
        assertTrue(result)
    }

    private fun validReminderDataItem(): ReminderDataItem {
        return ReminderDataItem(
            "title",
            "description",
            "location",
            36.1013,
            28.5833
        )
    }

    @Test
    fun saveReminder() {
        // GIVEN - valid reminder data
        val reminderDataItem = validReminderDataItem()

        // WHEN - save the reminder
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN - assert that the progress indicator is hidden and toast is set
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`(app.getString(R.string.reminder_saved))
        )
    }
}