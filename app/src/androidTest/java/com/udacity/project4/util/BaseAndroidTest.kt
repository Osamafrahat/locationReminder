package com.udacity.project4.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Rule

open class BaseAndroidTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    fun buildReminderDTO(): ReminderDTO {
        return ReminderDTO(
            "title",
            "description",
            "location",
            15.0292,
            16.2339
        )
    }

    fun assertReminder(
        reminderFromDb: ReminderDTO?,
        reminderDTO: ReminderDTO
    ) {
        MatcherAssert.assertThat(reminderFromDb, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(reminderFromDb?.id, CoreMatchers.`is`(reminderDTO.id))
        MatcherAssert.assertThat(reminderFromDb?.title, CoreMatchers.`is`(reminderDTO.title))
        MatcherAssert.assertThat(
            reminderFromDb?.description,
            CoreMatchers.`is`(reminderDTO.description)
        )
        MatcherAssert.assertThat(reminderFromDb?.location, CoreMatchers.`is`(reminderDTO.location))
        MatcherAssert.assertThat(reminderFromDb?.latitude, CoreMatchers.`is`(reminderDTO.latitude))
        MatcherAssert.assertThat(
            reminderFromDb?.longitude,
            CoreMatchers.`is`(reminderDTO.longitude)
        )
    }
}