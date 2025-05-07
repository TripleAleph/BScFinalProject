package com.example.pawsitivelife.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Reminder
import java.time.LocalDateTime

class AppointmentsViewModel : ViewModel() {

    private val _reminders = MutableLiveData<List<Reminder>>(
        listOf(
            Reminder(
                title = "Vet Appointment",
                date = LocalDateTime.of(2025, 4, 21, 10, 0),
                vetName = "Dr. Mary",
                imageResId = R.drawable.img_chubbie
            ),
            Reminder(
                title = "Grooming",
                date = LocalDateTime.of(2025, 4, 22, 15, 0),
                vetName = "Pet Spa",
                imageResId = R.drawable.img_chubbie
            ),
            Reminder(
                title = "Walk",
                date = LocalDateTime.of(2025, 4, 20, 8, 30),
                vetName = "Trainer Joe",
                imageResId = R.drawable.img_chubbie
            )
        )
    )

    val reminders: LiveData<List<Reminder>> get() = _reminders

    fun addReminder(reminder: Reminder) {
        _reminders.value = _reminders.value.orEmpty() + reminder
    }
}
