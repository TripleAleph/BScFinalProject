package com.example.pawsitivelife.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawsitivelife.model.Reminder

class AppointmentsViewModel : ViewModel() {

    // Controls whether calendar is currently expanded (month view) or collapsed (week view)
    var isCalendarExpanded: Boolean = true

    // Internal list of reminders
    private val _reminders = MutableLiveData<List<Reminder>>(emptyList())
    val reminders: LiveData<List<Reminder>> get() = _reminders

    // Add a new reminder to the list
    fun addReminder(reminder: Reminder) {
        val updatedList = _reminders.value.orEmpty().toMutableList().apply {
            add(reminder)
        }
        _reminders.value = updatedList
    }

    // Optional: Clear all reminders
    fun clearReminders() {
        _reminders.value = emptyList()
    }
}
