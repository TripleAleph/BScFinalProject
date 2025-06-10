package com.example.pawsitivelife.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 1: Create notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminders"
            val descriptionText = "Channel for dog care reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(
                "reminders_channel_id",
                name,
                importance
            ).apply {
                description = descriptionText
            }

            val notificationManager = requireContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Step 2: Ask for POST_NOTIFICATIONS permission if needed (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val hasPermission = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    // Case 1: we can still ask
                    requestPermissions(arrayOf(permission), 1001)
                } else {
                    // Case 2: denied and can't ask → show dialog to open settings
                    AlertDialog.Builder(requireContext())
                        .setTitle("Notifications Disabled")
                        .setMessage("To receive important reminders, please enable notifications in your device settings.")
                        .setPositiveButton("Go to Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", requireContext().packageName, null)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }

        // Step 3: Load saved switch state
        val sharedPref = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("reminders_enabled", false)
        binding.remindersSwitch.isChecked = isEnabled

        // Step 4: Switch toggle logic
        binding.remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save new switch state
            with(sharedPref.edit()) {
                putBoolean("reminders_enabled", isChecked)
                apply()
            }

            if (isChecked) {
                val builder = NotificationCompat.Builder(requireContext(), "reminders_channel_id")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("🐶 Dog Care Reminders Enabled!")
                    .setContentText("You’ll now receive smart notifications to help care for your dog.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                // Only send notification if permission was granted
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    with(NotificationManagerCompat.from(requireContext())) {
                        notify(1002, builder.build())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
