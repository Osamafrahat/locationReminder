package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.util.BaseAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : BaseAndroidTest() {

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveAndGetReminders() = runBlockingTest {
        // GIVEN - A reminder is inserted in database
        val reminderDTO = buildReminderDTO()
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - retrieve reminders from database
        val remindersFromDb = database.reminderDao().getReminders()

        // THEN - The new reminder is obtained
        assertThat(remindersFromDb.size, `is`(1) )
        assertReminder(remindersFromDb[0], reminderDTO)
    }

    @Test
    fun saveAndGetReminderById() = runBlockingTest {
        // GIVEN - A reminder is inserted in database
        val reminderDTO = buildReminderDTO()
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - retrieve reminder from database
        val reminderFromDb = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN - The new reminder is obtained
        assertReminder(reminderFromDb, reminderDTO)
    }

    @Test
    fun deleteReminder() = runBlockingTest {
        // GIVEN - A reminder is inserted in database
        val reminderDTO = buildReminderDTO()
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - delete the reminder
        database.reminderDao().deleteAllReminders()

        // THEN - the database is empty
        val remindersFromDb = database.reminderDao().getReminders()
        Assert.assertTrue(remindersFromDb.isEmpty())
    }
}