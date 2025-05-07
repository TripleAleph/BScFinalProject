package com.example.pawsitivelife.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.adapter.ReminderAdapter
import com.example.pawsitivelife.databinding.FragmentAppointmentsBinding
import com.example.pawsitivelife.ui.viewmodel.AppointmentsViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AppointmentsFragment : Fragment() {
    private val isCalendarExpanded = true

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val appointmentsViewModel: AppointmentsViewModel by activityViewModels()
    private lateinit var reminderAdapter: ReminderAdapter


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

        reminderAdapter = ReminderAdapter()
        binding.itemReminders.adapter = reminderAdapter

        selectedDate = today
        updateRemindersForDate(today)

        setupMonthCalendar()
        setupWeekCalendar()

        syncCalendarsWithState()

        binding.calendarToggleButton.setOnClickListener {
            appointmentsViewModel.isCalendarExpanded = !appointmentsViewModel.isCalendarExpanded
            syncCalendarsWithState()
        }

        appointmentsViewModel.reminders.observe(viewLifecycleOwner) {
            updateRemindersForDate(selectedDate ?: today)
        }

        binding.fabAddTask.setOnClickListener {
            val action = AppointmentsFragmentDirections.actionAppointmentsFragmentToAddAppointmentFragment()
            findNavController().navigate(action)
        }
    }


    private fun syncCalendarsWithState() {
        if (appointmentsViewModel.isCalendarExpanded) {
            binding.monthCalendarView.visibility = View.VISIBLE
            binding.weekCalendarView.visibility = View.GONE
            binding.calendarToggleButton.setImageResource(R.drawable.ic_expand_less)

            selectedDate?.let {
                binding.monthCalendarView.scrollToMonth(it.yearMonth)
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
        val allReminders = appointmentsViewModel.reminders.value.orEmpty()
        val remindersForDate = allReminders.filter { it.date == date }

        reminderAdapter.submitList(remindersForDate)
        binding.emptyMessage.visibility = if (remindersForDate.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupMonthCalendar() {
        binding.monthCalendarView.monthScrollListener = { month ->
            updateMonthTitle(month.yearMonth)
        }

        val currentMonth = YearMonth.now()
        binding.monthCalendarView.setup(
            currentMonth.minusMonths(12),
            currentMonth.plusMonths(12),
            DayOfWeek.SUNDAY
        )
        binding.monthCalendarView.scrollToMonth(currentMonth)
        updateMonthTitle(currentMonth)

        binding.monthCalendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                textView.setTextColor(
                    if (data.position == DayPosition.MonthDate)
                        ContextCompat.getColor(requireContext(), R.color.black)
                    else
                        ContextCompat.getColor(requireContext(), R.color.alto_400)
                )

                when {
                    data.date == today && selectedDate == today -> {
                        textView.setBackgroundResource(R.drawable.bg_today_day)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                    data.date == today -> {
                        textView.background = null
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.french_rose_600))
                    }
                    data.date == selectedDate -> {
                        textView.setBackgroundResource(R.drawable.bg_selected_day)
                    }
                    else -> {
                        textView.background = null
                    }
                }

                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) {
                        selectedDate = if (selectedDate == data.date) null else data.date
                        binding.monthCalendarView.notifyCalendarChanged()
                        binding.weekCalendarView.notifyCalendarChanged()
                        updateRemindersForDate(data.date)
                    }
                }
            }
        }
    }

    private fun setupWeekCalendar() {
        val currentDate = LocalDate.now()
        binding.weekCalendarView.setup(
            currentDate.minusDays(365),
            currentDate.plusDays(365),
            DayOfWeek.SUNDAY
        )
        binding.weekCalendarView.scrollToDate(currentDate)

        binding.weekCalendarView.weekScrollListener = { week ->
            val days = week.days
            val middleDate = days[days.size / 2].date
            updateMonthTitle(YearMonth.from(middleDate))
        }


        binding.weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

                when {
                    data.date == today && selectedDate == today -> {
                        textView.setBackgroundResource(R.drawable.bg_today_day)
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                    data.date == today -> {
                        textView.background = null
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.french_rose_600))
                    }
                    data.date == selectedDate -> {
                        textView.setBackgroundResource(R.drawable.bg_selected_day)
                    }
                    else -> {
                        textView.background = null
                    }
                }

                container.view.setOnClickListener {
                    selectedDate = if (selectedDate == data.date) null else data.date
                    binding.weekCalendarView.notifyCalendarChanged()
                    binding.monthCalendarView.notifyCalendarChanged()
                    updateRemindersForDate(data.date)
                }
            }
        }
    }


    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM, yyyy")
        binding.titleCalendar.text = yearMonth.format(formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
