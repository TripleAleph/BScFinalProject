package com.example.pawsitivelife.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.adapter.WalkLogAdapter
import com.example.pawsitivelife.databinding.FragmentWalksBinding
import com.example.pawsitivelife.model.FixedWalkTime
import com.example.pawsitivelife.model.WalkLogEntry
import com.example.pawsitivelife.model.WalkType
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.res.ResourcesCompat

class WalksFragment : Fragment() {

    private var _binding: FragmentWalksBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var dogId: String

    private val defaultTimes = mapOf(
        WalkType.MORNING to LocalTime.of(7, 0),
        WalkType.AFTERNOON to LocalTime.of(13, 0),
        WalkType.EVENING to LocalTime.of(19, 0)
    )

    private val today = LocalDate.now()
    private var selectedDate: LocalDate = today

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalksBinding.inflate(inflater, container, false)
        dogId = arguments?.getString("dogId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWeekCalendar()
        binding.addWalkFab.setOnClickListener { showAddWalkTimeDialog() }

        ensureDefaultWalkTimes()
    }

    private fun setupWeekCalendar() {
        val currentDate = LocalDate.now()

        binding.weekCalendarView.setup(
            currentDate.minusMonths(12),
            currentDate.plusMonths(12),
            DayOfWeek.SUNDAY
        )

        binding.weekCalendarView.scrollToDate(currentDate)
        updateDateTitle(selectedDate)

        binding.weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: WeekDay) {
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

                when {
                    day.date == today && selectedDate == today -> {
                        setRoundedBackground(textView, R.color.malibu_400)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                    day.date == today -> {
                        setRoundedBackground(textView, R.color.malibu_400)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                    day.date == selectedDate -> {
                        setRoundedBackground(textView, R.color.french_rose_300)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                    else -> {
                        setRoundedBackground(textView, R.color.malibu_200)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                }



                container.view.setOnClickListener {
                    selectedDate = day.date
                    binding.weekCalendarView.notifyCalendarChanged()
                    updateDateTitle(selectedDate)
                    loadWalkLog()
                }
            }
        }
    }

    private fun setRoundedBackground(textView: TextView, colorResId: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.walk_bg_today_day, null)?.mutate()
        drawable?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), colorResId))
            textView.background = it
        }
    }

    private fun updateDateTitle(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("en", "USA"))
        val formatted = date.format(formatter)
        binding.walksTitleDate.text = formatted.replaceFirstChar { it.uppercase() }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun ensureDefaultWalkTimes() {
        val userId = auth.currentUser?.uid ?: return
        val dogRef = db.collection("users").document(userId).collection("dogs").document(dogId)
        val fixedWalksRef = dogRef.collection("fixed_walk_times")

        fixedWalksRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                defaultTimes.forEach { (type, time) ->
                    fixedWalksRef.document(type.name.lowercase()).set(mapOf("time" to time.toString()))
                }
            }
            loadFixedWalkTimes()
            loadWalkLog()
        }
    }

    private fun loadFixedWalkTimes() {
        val userId = auth.currentUser?.uid ?: return
        binding.fixedWalksContainer.removeAllViews()

        db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("fixed_walk_times")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    showInitialPrompt()
                } else {
                    binding.addWalkFab.visibility = View.VISIBLE
                    for (doc in snapshot.documents) {
                        val type = WalkType.valueOf(doc.id.uppercase())
                        val timeStr = doc.getString("time") ?: continue
                        val time = LocalTime.parse(timeStr)
                        addFixedWalkTimeView(FixedWalkTime(type, time))
                    }
                    loadWalkLog()
                }
            }
    }

    private fun loadWalkLog() {
        val userId = auth.currentUser?.uid ?: return
        val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val logRef = db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("walk_log")
            .document(formattedDate)

        logRef.get().addOnSuccessListener { doc ->
            val entries = defaultTimes.map { (type, _) ->
                val completed = doc?.getBoolean("${type.name.lowercase()}_done") ?: false
                val note = doc?.getString("${type.name.lowercase()}_note") ?: ""
                WalkLogEntry(
                    date = "$formattedDate - ${type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    completed = completed,
                    note = note
                )
            }

            val adapter = WalkLogAdapter(entries, object : WalkLogAdapter.OnWalkLogChangeListener {
                override fun onWalkLogChanged(walkEntry: WalkLogEntry, isChecked: Boolean, note: String) {
                    val updates = mapOf(
                        walkEntry.date.substringAfter(" - ").lowercase() + "_done" to isChecked,
                        walkEntry.date.substringAfter(" - ").lowercase() + "_note" to note
                    )
                    logRef.set(updates, SetOptions.merge())
                }
            })
            adapter.setDogId(dogId)
            adapter.setSelectedDate(selectedDate)
            binding.walksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.walksRecyclerView.adapter = adapter
        }
    }

    private fun showInitialPrompt() {
        val context = requireContext()
        binding.fixedWalksContainer.removeAllViews()

        val message = TextView(context).apply {
            text = "You haven't set any fixed walk times yet."
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        }

        binding.fixedWalksContainer.addView(message)
        binding.addWalkFab.visibility = View.VISIBLE
    }

    private fun addFixedWalkTimeView(walkTime: FixedWalkTime) {
        val view = layoutInflater.inflate(R.layout.item_fixed_walk_time, binding.fixedWalksContainer, false)

        view.findViewById<TextView>(R.id.walk_type).text =
            walkTime.type.name.lowercase().replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.walk_time).text = walkTime.time.toString()

        view.findViewById<View>(R.id.edit_button).setOnClickListener {
            showEditWalkTimeDialog(walkTime)
        }

        binding.fixedWalksContainer.addView(view)
    }

    private fun showAddWalkTimeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_walk_time, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)
        val spinner = dialogView.findViewById<Spinner>(R.id.walk_type_spinner)

        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, WalkType.values())

        AlertDialog.Builder(requireContext())
            .setTitle("Add Walk Time")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val type = spinner.selectedItem as WalkType
                val time = LocalTime.of(hour, minute)
                saveFixedWalkTime(type, time)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditWalkTimeDialog(walkTime: FixedWalkTime) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_walk_time, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.hour = walkTime.time.hour
        timePicker.minute = walkTime.time.minute

        AlertDialog.Builder(requireContext())
            .setTitle("Edit ${walkTime.type.name.lowercase().replaceFirstChar { it.uppercase() }} Time")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newTime = LocalTime.of(timePicker.hour, timePicker.minute)
                saveFixedWalkTime(walkTime.type, newTime)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveFixedWalkTime(type: WalkType, time: LocalTime) {
        val userId = auth.currentUser?.uid ?: return

        val data = mapOf("time" to time.toString())

        db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("fixed_walk_times")
            .document(type.name.lowercase())
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Walk time saved.", Toast.LENGTH_SHORT).show()
                ensureDefaultWalkTimes()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
