package com.example.pawsitivelife.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentAddAppointmentBinding
import com.example.pawsitivelife.model.Reminder
import com.example.pawsitivelife.ui.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddReminderFragment : Fragment() {
    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now()

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateDateTimeDisplay()
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveReminder()
        }

        binding.datePickerField.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                showTimePicker()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
                updateDateTimeDisplay()
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
        timePickerDialog.show()
    }

    private fun updateDateTimeDisplay() {
        binding.datePickerField.text = "${selectedDate.format(dateFormatter)} at ${selectedTime.format(timeFormatter)}"
    }

    private fun saveReminder() {
        val title = binding.appointmentTitleInput.text.toString()
        val notes = binding.notesInput.text.toString()

        if (title.isBlank()) {
            // Show error message
            return
        }

        val reminder = Reminder(
            title = if (notes.isNotBlank()) "$title\n$notes" else title,
            date = LocalDateTime.of(selectedDate, selectedTime),
            imageResId = R.drawable.img_chubbie // You should get this from the selected dog
        )

        reminderViewModel.addReminder(reminder)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
