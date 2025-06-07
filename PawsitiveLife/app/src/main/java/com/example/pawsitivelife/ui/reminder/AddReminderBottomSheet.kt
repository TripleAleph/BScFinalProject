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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val dogNameToIdMap = mutableMapOf<String, String>()
    private val dogNameToImageUrlMap = mutableMapOf<String, String>()
    private var selectedDogId: String? = null
    private var selectedDogImageUrl: String? = null

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
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid).collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                val dogNames = mutableListOf<String>()
                for (doc in result) {
                    val dogName = doc.getString("name") ?: continue
                    val dogId = doc.id
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    dogNames.add(dogName)
                    dogNameToIdMap[dogName] = dogId
                    dogNameToImageUrlMap[dogName] = imageUrl
                }

                val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, dogNames)
                binding.dogSelectorDropdown.setAdapter(adapter)

                binding.dogSelectorDropdown.setOnItemClickListener { _, _, position, _ ->
                    val selectedName = dogNames[position]
                    selectedDogId = dogNameToIdMap[selectedName]
                    selectedDogImageUrl = dogNameToImageUrlMap[selectedName]
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load dogs", Toast.LENGTH_SHORT).show()
            }
    }    private fun setupTimePicker() {
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
        val user = auth.currentUser ?: return
        val type = selectedAppointmentType
        val dogId = selectedDogId
        val imageUrl = selectedDogImageUrl

        if (type.isNullOrBlank() || dogId == null) {
            Toast.makeText(requireContext(), "Please select both appointment type and dog", Toast.LENGTH_SHORT).show()
            return
        }

        val finalImageUrl = if (imageUrl.isNullOrBlank()) {
            "android.resource://${requireContext().packageName}/${R.drawable.missing_img_dog}"
        } else {
            imageUrl
        }

        val dateTime = LocalDateTime.of(
            selectedDate,
            selectedTime ?: LocalTime.of(9, 0)
        )

        val selectedDogName = binding.dogSelectorDropdown.text.toString()

        val notesText = binding.notesText.text.toString().take(100)

        val data = hashMapOf(
            "title" to selectedAppointmentType,
            "date" to dateTime.toString(),
            "dogId" to dogId,
            "dogName" to selectedDogName,
            "imagePath" to finalImageUrl,
            "notes" to notesText
        )


        db.collection("users").document(user.uid)
            .collection("dogs").document(dogId)
            .collection("reminders")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Reminder saved!", Toast.LENGTH_SHORT).show()

                val reminder = Reminder(
                    title = selectedAppointmentType ?: "",
                    date = dateTime,
                    dogId = dogId,
                    dogName = selectedDogName,
                    imagePath = finalImageUrl,
                    notes = notesText
                )
                viewModel.addReminder(reminder)

                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save reminder", Toast.LENGTH_SHORT).show()
            }
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
