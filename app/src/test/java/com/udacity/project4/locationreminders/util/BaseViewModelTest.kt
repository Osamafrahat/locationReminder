package com.udacity.project4.locationreminders.util

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Rule

open class BaseViewModelTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var dataSource: FakeDataSource
    lateinit var app: Application

    fun buildReminderDTO(): ReminderDTO {
        return ReminderDTO(
            "title",
            "description",
            "location",
            15.0292,
            16.2339
        )
    }
}