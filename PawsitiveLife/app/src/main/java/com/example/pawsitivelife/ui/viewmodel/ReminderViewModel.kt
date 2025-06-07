package com.example.pawsitivelife.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawsitivelife.model.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderViewModel : ViewModel() {

    var isCalendarExpanded: Boolean = true

    private val remindersByDate = mutableMapOf<LocalDate, MutableList<Reminder>>()
    private val _reminders = MutableLiveData<List<Reminder>>(emptyList())
    val reminders: LiveData<List<Reminder>> get() = _reminders

    private var selectedDate: LocalDate = LocalDate.now()

    private val loadedDates = mutableSetOf<LocalDate>()

    fun addReminder(reminder: Reminder) {
        val date = reminder.date.toLocalDate()
        val remindersForDate = remindersByDate.getOrPut(date) { mutableListOf() }
        remindersForDate.add(reminder)
        remindersForDate.sortBy { it.date }

        if (date == selectedDate) {
            _reminders.value = remindersForDate.toList()
        }

        loadedDates.add(date)
    }

    fun setSelectedDate(date: LocalDate, userId: String) {
        selectedDate = date

        if (!loadedDates.contains(date)) {
            loadRemindersFromFirestore(userId, date)
        }

        val remindersForDate = remindersByDate[date]?.sortedBy { it.date } ?: emptyList()
        _reminders.value = remindersForDate
    }

    fun getRemindersForDate(date: LocalDate): List<Reminder> {
        return remindersByDate[date]?.sortedBy { it.date } ?: emptyList()
    }

    fun clearReminders() {
        remindersByDate.clear()
        loadedDates.clear()
        _reminders.value = emptyList()
    }

    private fun loadRemindersFromFirestore(userId: String, targetDate: LocalDate) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("dogs")
            .get()
            .addOnSuccessListener { dogs ->
                for (dog in dogs) {
                    val dogId = dog.id
                    val dogName = dog.getString("name") ?: continue
                    val imagePath = dog.getString("imageUrl") ?: ""

                    db.collection("users").document(userId)
                        .collection("dogs").document(dogId)
                        .collection("reminders")
                        .whereGreaterThanOrEqualTo("date", targetDate.toString())
                        .whereLessThan("date", targetDate.plusDays(1).toString())
                        .get()
                        .addOnSuccessListener { reminders ->
                            val list = remindersByDate.getOrPut(targetDate) { mutableListOf() }

                            for (reminderDoc in reminders) {
                                val title = reminderDoc.getString("title") ?: continue
                                val dateStr = reminderDoc.getString("date") ?: continue
                                val dogNameFromDoc = reminderDoc.getString("dogName") ?: dogName
                                val image = reminderDoc.getString("imagePath") ?: imagePath
                                val notes = reminderDoc.getString("notes") ?: ""

                                val dateTime = LocalDateTime.parse(dateStr)

                                val reminder = Reminder(
                                    title = title,
                                    date = dateTime,
                                    dogId = dogId,
                                    dogName = dogNameFromDoc,
                                    imagePath = image,
                                    notes = notes,
                                    reminderId = reminderDoc.id
                                )


                                list.add(reminder)
                            }

                            list.sortBy { it.date }
                            remindersByDate[targetDate] = list

                            if (targetDate == selectedDate) {
                                _reminders.value = list
                            }

                            loadedDates.add(targetDate)
                        }
                }
            }
    }

    fun deleteReminder(dogId: String, reminderId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val date = selectedDate

        // Step 1: Delete from Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("reminders")
            .document(reminderId)
            .delete()
            .addOnSuccessListener {
                // Step 2: Remove from local cache
                val list = remindersByDate[date]
                if (list != null) {
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val reminder = iterator.next()
                        if (reminder.reminderId == reminderId) {
                            iterator.remove()
                            break
                        }
                    }
                    // Step 3: Update UI
                    _reminders.value = list.sortedBy { it.date }
                }
            }
    }

}
