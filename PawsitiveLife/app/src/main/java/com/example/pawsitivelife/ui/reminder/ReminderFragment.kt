package com.example.pawsitivelife.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.adapter.ReminderAdapter
import com.example.pawsitivelife.databinding.FragmentAppointmentsBinding
import com.example.pawsitivelife.ui.viewmodel.ReminderViewModel
import com.google.firebase.auth.FirebaseAuth
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReminderFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private lateinit var reminderAdapter: ReminderAdapter

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val argsDate = arguments?.getString("selectedDate")?.let { LocalDate.parse(it) }
        val dateToUse = argsDate ?: today

        selectedDate = dateToUse
        reminderViewModel.setSelectedDate(today, userId)
        updateTodayLabel(dateToUse)

        reminderAdapter = ReminderAdapter()
        binding.itemReminders.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        selectedDate = dateToUse
        reminderViewModel.setSelectedDate(dateToUse, userId)
        updateTodayLabel(dateToUse)


        setupMonthCalendar()
        setupWeekCalendar()
        syncCalendarsWithState()

        binding.calendarToggleButton.setOnClickListener {
            reminderViewModel.isCalendarExpanded = !reminderViewModel.isCalendarExpanded
            syncCalendarsWithState()
        }

        reminderViewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            reminderAdapter.submitList(reminders)
            binding.emptyMessage.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE

            if (reminders.isNotEmpty()) {
                binding.itemReminders.post {
                    binding.itemReminders.smoothScrollToPosition(reminders.size - 1)
                }
            }
        }

        binding.fabAddTask.setOnClickListener {
            val bottomSheet = AddReminderBottomSheet.newInstance(selectedDate ?: today)
            bottomSheet.show(childFragmentManager, "AddReminderBottomSheet")
        }
    }

    private fun syncCalendarsWithState() {
        if (reminderViewModel.isCalendarExpanded) {
            binding.monthCalendarView.visibility = View.VISIBLE
            binding.weekCalendarView.visibility = View.GONE
            binding.calendarToggleButton.setImageResource(R.drawable.ic_expand_less)
            selectedDate?.let {
                binding.monthCalendarView.scrollToMonth(YearMonth.from(it))
            }
        } else {
            binding.monthCalendarView.visibility = View.GONE
            binding.weekCalendarView.visibility = View.VISIBLE
            binding.calendarToggleButton.setImageResource(R.drawable.ic_expand_more)
            selectedDate?.let {
                binding.weekCalendarView.scrollToDate(it)
            }
        }

        binding.monthCalendarView.notifyCalendarChanged()
        binding.weekCalendarView.notifyCalendarChanged()
    }

    private fun updateRemindersForDate(date: LocalDate) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        reminderViewModel.setSelectedDate(date, userId)
    }


    private fun setupMonthCalendar() {
        val currentMonth = YearMonth.now()

        binding.monthCalendarView.setup(
            currentMonth.minusMonths(12),
            currentMonth.plusMonths(12),
            DayOfWeek.SUNDAY
        )
        binding.monthCalendarView.scrollToMonth(currentMonth)
        updateMonthTitle(currentMonth)

        binding.monthCalendarView.monthScrollListener = { month ->
            updateMonthTitle(month.yearMonth)
        }

        val monthDayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                textView.setTextColor(
                    if (day.position == DayPosition.MonthDate)
                        ContextCompat.getColor(requireContext(), R.color.black)
                    else
                        ContextCompat.getColor(requireContext(), R.color.alto_400)
                )

                when {
                    day.date == today && selectedDate == today -> {
                        textView.setBackgroundResource(R.drawable.bg_today_day)
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                    }

                    day.date == today -> {
                        textView.background = null
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.french_rose_600
                            )
                        )
                    }

                    day.date == selectedDate -> {
                        textView.setBackgroundResource(R.drawable.bg_selected_day)
                    }

                    else -> {
                        textView.background = null
                    }
                }

                container.view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectedDate = if (selectedDate == day.date) null else day.date
                        binding.monthCalendarView.notifyCalendarChanged()
                        binding.weekCalendarView.notifyCalendarChanged()
                        updateTodayLabel(day.date)
                        updateRemindersForDate(day.date)
                    }
                }
            }
        }
        binding.monthCalendarView.dayBinder = monthDayBinder
    }

    private fun setupWeekCalendar() {
        val currentDate = LocalDate.now()

        binding.weekCalendarView.setup(
            currentDate.minusDays(365),
            currentDate.plusDays(365),
            DayOfWeek.SUNDAY
        )
        binding.weekCalendarView.scrollToDate(currentDate)

        val weekDayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: WeekDay) {
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

                when {
                    day.date == today && selectedDate == today -> {
                        textView.setBackgroundResource(R.drawable.bg_today_day)
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                    }

                    day.date == today -> {
                        textView.background = null
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.french_rose_600
                            )
                        )
                    }

                    day.date == selectedDate -> {
                        textView.setBackgroundResource(R.drawable.bg_selected_day)
                    }

                    else -> {
                        textView.background = null
                    }
                }

                container.view.setOnClickListener {
                    selectedDate = if (selectedDate == day.date) null else day.date
                    binding.weekCalendarView.notifyCalendarChanged()
                    binding.monthCalendarView.notifyCalendarChanged()
                    updateRemindersForDate(day.date)
                    updateTodayLabel(day.date)
                    updateMonthTitle(YearMonth.from(day.date))
                }
            }
        }
        binding.weekCalendarView.dayBinder = weekDayBinder
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM, yyyy")
        binding.titleCalendar.text = yearMonth.format(formatter)
    }

    private fun updateTodayLabel(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.getDefault())
        val formattedDate = date.format(formatter)
        binding.todayLabel.text = formattedDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
