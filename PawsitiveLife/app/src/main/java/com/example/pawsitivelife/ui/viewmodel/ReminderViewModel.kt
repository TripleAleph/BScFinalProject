package com.example.pawsitivelife.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawsitivelife.model.Reminder
import java.time.LocalDate

class ReminderViewModel : ViewModel() {

    // Controls whether calendar is currently expanded (month view) or collapsed (week view)
    var isCalendarExpanded: Boolean = true

    // Store reminders by date
    private val remindersByDate = mutableMapOf<LocalDate, MutableList<Reminder>>()

    // Expose reminders for the currently selected date
    private val _reminders = MutableLiveData<List<Reminder>>(emptyList())
    val reminders: LiveData<List<Reminder>> get() = _reminders

    // Current selected date
    private var selectedDate: LocalDate = LocalDate.now()

    // Add a new reminder to the list
    fun addReminder(reminder: Reminder) {
        val date = reminder.date.toLocalDate()
        val remindersForDate = remindersByDate.getOrPut(date) { mutableListOf() }
        remindersForDate.add(reminder)
        
        // Sort reminders by time
        remindersForDate.sortBy { it.date }
        
        // If this is for the currently selected date, update the LiveData
        if (date == selectedDate) {
            _reminders.value = remindersForDate.toList()
        }
    }

    // Set the selected date and update the LiveData
    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        val remindersForDate = remindersByDate[date]?.sortedBy { it.date } ?: emptyList()
        _reminders.value = remindersForDate
    }

    // Get reminders for a specific date
    fun getRemindersForDate(date: LocalDate): List<Reminder> {
        return remindersByDate[date]?.sortedBy { it.date } ?: emptyList()
    }

    // Optional: Clear all reminders
    fun clearReminders() {
        remindersByDate.clear()
        _reminders.value = emptyList()
    }


}
