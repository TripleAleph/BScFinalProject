package com.example.pawsitivelife.ui.notifications

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pawsitivelife.model.Reminder

class AppointmentsViewModel : Fragment() {

    private val _reminders = MutableLiveData<List<Reminder>>(emptyList())
    val reminders: LiveData<List<Reminder>> get() = _reminders

    fun addReminder(reminder: Reminder) {
        val currentList = _reminders.value.orEmpty().toMutableList()
        currentList.add(reminder)
        _reminders.value = currentList
    }
}
