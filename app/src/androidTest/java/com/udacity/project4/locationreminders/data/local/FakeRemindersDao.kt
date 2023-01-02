package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class FakeRemindersDao(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : RemindersDao {
    override suspend fun getReminders(): List<ReminderDTO> {
        if (reminders == null) {
            return listOf()
        }
        return reminders!!.toList()
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        reminders?.let {
            for (reminder in it) {
                if (reminderId == reminder.id) {
                    return reminder
                }
            }
        }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}