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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.adapter.FeedLogAdapter
import com.example.pawsitivelife.databinding.FragmentFeedingBinding
import com.example.pawsitivelife.model.FeedLogEntry
import com.example.pawsitivelife.model.FeedType
import com.example.pawsitivelife.model.FixedFeedTime
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

class FeedingFragment : Fragment() {

    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var dogId: String

    private val defaultTimes = mapOf(
        FeedType.MORNING to LocalTime.of(8, 0),
        FeedType.AFTERNOON to LocalTime.of(14, 0),
        FeedType.EVENING to LocalTime.of(20, 0)
    )

    private val feedTypeOrder = listOf(FeedType.MORNING, FeedType.AFTERNOON, FeedType.EVENING)
    private val today = LocalDate.now()
    private var selectedDate: LocalDate = today

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedingBinding.inflate(inflater, container, false)
        dogId = arguments?.getString("dogId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWeekCalendar()
        ensureDefaultFeedTimes()
    }

    private fun setupWeekCalendar() {
        val currentDate = LocalDate.now()

        binding.weekCalendarView.setup(
            currentDate.minusMonths(12),
            currentDate.plusMonths(12),
            DayOfWeek.SUNDAY
        )

        binding.weekCalendarView.scrollToDate(currentDate)

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
                    }
                    day.date == selectedDate -> {
                        setRoundedBackground(textView, R.color.french_rose_300)
                    }
                    else -> {
                        setRoundedBackground(textView, R.color.malibu_200)
                    }
                }

                container.view.setOnClickListener {
                    selectedDate = day.date
                    binding.weekCalendarView.notifyCalendarChanged()
                    updateHeaderWithSelectedDate()
                    loadFeedLog()
                }
            }
        }

        updateHeaderWithSelectedDate()
    }

    private fun updateHeaderWithSelectedDate() {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        binding.feedsTitleDate.text = selectedDate.format(formatter)
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun ensureDefaultFeedTimes() {
        val userId = auth.currentUser?.uid ?: return
        val dogRef = db.collection("users").document(userId).collection("dogs").document(dogId)
        val fixedFeedsRef = dogRef.collection("fixed_feed_times")

        fixedFeedsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                defaultTimes.forEach { (type, time) ->
                    fixedFeedsRef.document(type.name.lowercase())
                        .set(mapOf("time" to time.toString()))
                }
            }
            loadFixedFeedTimes()
            loadFeedLog()
        }
    }

    private fun loadFixedFeedTimes() {
        val userId = auth.currentUser?.uid ?: return
        binding.fixedFeedsContainer.removeAllViews()

        db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("fixed_feed_times")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val defaultFeedTimes = defaultTimes.map { FixedFeedTime(it.key, it.value) }
                    val sortedFeedTimes = defaultFeedTimes.sortedBy { feedTypeOrder.indexOf(it.type) }
                    sortedFeedTimes.forEach { addFixedFeedTimeView(it) }
                } else {
                    val feedTimes = snapshot.documents.mapNotNull { doc ->
                        val type = FeedType.values().firstOrNull {
                            it.name.equals(doc.id, ignoreCase = true)
                        } ?: return@mapNotNull null
                        val timeStr = doc.getString("time") ?: return@mapNotNull null
                        val time = LocalTime.parse(timeStr)
                        FixedFeedTime(type, time)
                    }

                    val sortedFeedTimes = feedTimes.sortedBy { feedTypeOrder.indexOf(it.type) }
                    sortedFeedTimes.forEach { addFixedFeedTimeView(it) }
                }

                loadFeedLog()
            }
    }

    private fun loadFeedLog() {
        val userId = auth.currentUser?.uid ?: return
        val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val logRef = db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("feed_log")
            .document(formattedDate)

        logRef.get().addOnSuccessListener { doc ->
            val entries = defaultTimes.map { (type, _) ->
                val completed = doc?.getBoolean("${type.name.lowercase()}_done") ?: false
                val note = doc?.getString("${type.name.lowercase()}_note") ?: ""
                FeedLogEntry(
                    date = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    completed = completed,
                    note = note
                )
            }

            val adapter = FeedLogAdapter(entries, object : FeedLogAdapter.OnFeedLogChangeListener {
                override fun onFeedLogChanged(feedEntry: FeedLogEntry, isChecked: Boolean, note: String) {
                    val updates = mapOf(
                        feedEntry.date.lowercase() + "_done" to isChecked,
                        feedEntry.date.lowercase() + "_note" to note
                    )
                    logRef.set(updates, SetOptions.merge())
                }
            })
            adapter.setDogId(dogId)
            adapter.setSelectedDate(selectedDate)
            binding.feedsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.feedsRecyclerView.adapter = adapter
        }
    }

    private fun showInitialPrompt() {
        val context = requireContext()
        binding.fixedFeedsContainer.removeAllViews()

        val message = TextView(context).apply {
            text = "You haven't set any fixed feeding times yet."
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        }

        binding.fixedFeedsContainer.addView(message)
    }

    private fun addFixedFeedTimeView(feedTime: FixedFeedTime) {
        val view = layoutInflater.inflate(R.layout.item_fixed_feed_time, binding.fixedFeedsContainer, false)

        view.findViewById<TextView>(R.id.feed_type).text =
            feedTime.type.name.lowercase().replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.feed_time).text = feedTime.time.toString()

        view.findViewById<View>(R.id.edit_button).setOnClickListener {
            showEditFeedTimeDialog(feedTime)
        }

        binding.fixedFeedsContainer.addView(view)
    }

    private fun showEditFeedTimeDialog(feedTime: FixedFeedTime) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_feed_time, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.feed_time_picker)
        timePicker.hour = feedTime.time.hour
        timePicker.minute = feedTime.time.minute

        AlertDialog.Builder(requireContext())
            .setTitle("Edit ${feedTime.type.name.lowercase().replaceFirstChar { it.uppercase() }} Time")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newTime = LocalTime.of(timePicker.hour, timePicker.minute)
                saveFixedFeedTime(feedTime.type, newTime)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveFixedFeedTime(type: FeedType, time: LocalTime) {
        val userId = auth.currentUser?.uid ?: return

        val data = mapOf("time" to time.toString())

        db.collection("users")
            .document(userId)
            .collection("dogs")
            .document(dogId)
            .collection("fixed_feed_times")
            .document(type.name.lowercase())
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Feed time saved.", Toast.LENGTH_SHORT).show()
                ensureDefaultFeedTimes()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setRoundedBackground(textView: TextView, colorResId: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.walk_bg_today_day, null)?.mutate()
        drawable?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), colorResId))
            textView.background = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
