package com.example.pawsitivelife.ui.reminder

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.BottomSheetAddAppointmentBinding
import com.example.pawsitivelife.model.Reminder
import com.example.pawsitivelife.ui.viewmodel.ReminderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddReminderBottomSheet(private val selectedDate: LocalDate) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddAppointmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReminderViewModel
    private var selectedTime: LocalTime? = null
    private var selectedAppointmentType: String? = null
    private var selectedDogImageResId: Int = R.drawable.img_chubbie // default image

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (requireActivity()).run {
            androidx.lifecycle.ViewModelProvider(this)[ReminderViewModel::class.java]
        }

        setupAppointmentTypeDropdown()
        setupDogSelector()
        setupTimePicker()

        binding.saveButton.setOnClickListener {
            saveReminder()
        }

        // Display the selected date
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        binding.selectedDateText.setText(selectedDate.format(dateFormatter))
    }

    private fun setupAppointmentTypeDropdown() {
        val types = listOf(
            "Vaccination", "Vet Check-up", "Playdate",
            "Grooming Appointment", "Buy Dog Food", "Renew Dog Insurance"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, types)
        binding.appointmentTypeDropdown.setAdapter(adapter)

        binding.appointmentTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedAppointmentType = types[position]
        }
    }

    private fun setupDogSelector() {
        val dogs = listOf("Chubbie") // Replace with actual dog list from ViewModel later
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, dogs)
        binding.dogSelectorDropdown.setAdapter(adapter)

        binding.dogSelectorDropdown.setOnItemClickListener { _, _, _, _ ->
            selectedDogImageResId = R.drawable.img_chubbie // Replace dynamically later
        }
    }

    private fun setupTimePicker() {
        binding.timeText.setOnClickListener {
            val now = LocalTime.now()
            val timePicker = TimePickerDialog(requireContext(),
                { _: TimePicker, hour: Int, minute: Int ->
                    selectedTime = LocalTime.of(hour, minute)
                    binding.timeText.setText(String.format("%02d:%02d", hour, minute))
                },
                now.hour, now.minute, true
            )
            timePicker.show()
        }
    }

    private fun saveReminder() {
        val type = selectedAppointmentType
        if (type.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please select appointment type", Toast.LENGTH_SHORT).show()
            return
        }

        val dateTime = LocalDateTime.of(
            selectedDate,
            selectedTime ?: LocalTime.of(9, 0) // Default time if none selected
        )

        val reminder = Reminder(
            title = type,
            date = dateTime,
            imageResId = selectedDogImageResId
        )

        // Add the new reminder to ViewModel
        viewModel.addReminder(reminder)

        // Dismiss the bottom sheet
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(selectedDate: LocalDate): AddReminderBottomSheet {
            return AddReminderBottomSheet(selectedDate)
        }
    }
}
