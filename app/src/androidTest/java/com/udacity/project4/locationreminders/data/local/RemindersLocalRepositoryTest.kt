package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.BaseAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : BaseAndroidTest() {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun saveAndGetReminders() = mainCoroutineRule.runBlockingTest {
        // GIVEN - a reminder is saved in database
        val reminder = buildReminderDTO()
        remindersLocalRepository.saveReminder(reminder)

        // WHEN - retrieve reminders
        val result = remindersLocalRepository.getReminders() as Result.Success

        // THEN - the reminder is retrieved from database
        assertThat(result.data.size, `is`(1) )
        assertReminder(result.data[0], reminder)
    }

    @Test
    fun saveAndGetReminderById() = mainCoroutineRule.runBlockingTest {
        // GIVEN - a reminder is saved in database
        val reminder = buildReminderDTO()
        remindersLocalRepository.saveReminder(reminder)

        // WHEN - retrieve reminder by id
        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Success

        // THEN - the reminder is retrieved from database
        assertReminder(result.data, reminder)
    }

    @Test
    fun getReminderByIdReturnsError() = mainCoroutineRule.runBlockingTest {
        // GIVEN - the database is empty
        remindersLocalRepository.deleteAllReminders()

        // WHEN - retrieve reminder by id
        val result = remindersLocalRepository.getReminder("fakeId") as Result.Error

        // THEN - it returns an error
        Assert.assertNotNull(result)
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun saveAndDeleteReminders() = mainCoroutineRule.runBlockingTest {
        // GIVEN - a reminder is saved in database
        val reminder = buildReminderDTO()
        remindersLocalRepository.saveReminder(reminder)

        // WHEN - delete all reminders
        remindersLocalRepository.deleteAllReminders()

        // THEN - the reminder list is empty
        val result = remindersLocalRepository.getReminders() as Result.Success
        assertThat(result.data.size, `is`(0) )
    }
}