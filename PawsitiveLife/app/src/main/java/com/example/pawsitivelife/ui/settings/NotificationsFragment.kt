package com.example.pawsitivelife.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentNotificationsBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check if the Android version is 13 (TIRAMISU) or higher
        // Create notification channel for Android 8+ (Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminders"
            val descriptionText = "Channel for dog care reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH

            val channel = android.app.NotificationChannel(
                "reminders_channel_id",  // must match the ID used in NotificationCompat.Builder
                name,
                importance
            ).apply {
                description = descriptionText
            }

            val notificationManager = requireContext()
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val sharedPref=requireActivity().getSharedPreferences("settings",Context.MODE_PRIVATE)
        val isEnabled= sharedPref.getBoolean("reminders_enabled",false)
        binding.remindersSwitch.isChecked=isEnabled

        binding.remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            with(sharedPref.edit()){
                putBoolean("reminders_enabled",isChecked)
                apply()
            }

            if (isChecked) {
                // Create the notification content
                if (isChecked) {
                    // Create the notification content
                    val builder = NotificationCompat.Builder(requireContext(), "reminders_channel_id")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("🐶 Dog Care Reminders Enabled!")
                        .setContentText("You’ll now receive smart notifications to help care for your dog.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)


                    // Show the notification using NotificationManagerCompat
                    with(NotificationManagerCompat.from(requireContext())) {
                        notify(1002, builder.build()) // 1002 is the notification ID
                    }
                }


            }
        }
    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding=null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotificationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


