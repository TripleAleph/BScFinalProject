package com.example.pawsitivelife.ui.mydogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.adapter.ReminderAdapter
import com.example.pawsitivelife.databinding.FragmentMyDogsBinding
import com.example.pawsitivelife.ui.viewmodel.AppointmentsViewModel
import java.time.LocalDateTime

class MyDogsFragment : Fragment() {

    private val appointmentsViewModel: AppointmentsViewModel by activityViewModels()

    private var _binding: FragmentMyDogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderAdapter = ReminderAdapter()
        binding.myDogsLSTReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }

        appointmentsViewModel.reminders.observe(viewLifecycleOwner) { allReminders ->
            val upcoming = allReminders.filter { it.date.isAfter(LocalDateTime.now()) }
            reminderAdapter.submitList(upcoming)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
