package com.example.pawsitivelife.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved preference for dark mode from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("dark_mode", false)

// Set the switch state based on the saved value
        binding.darkThemeSwitch.isChecked = isDarkMode


        // Set listener for dark mode switch
        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the selected mode (true = dark mode, false = light mode) in SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("dark_mode", isChecked)  // Save the new value
                apply() // Apply the change asynchronously
            }

            // Apply the theme immediately based on the new selection
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }



        // Navigate to profile screen (implement later if needed)
        binding.profileRow.setOnClickListener {
            // findNavController().navigate(R.id.action_settings_to_profileFragment)
        }

        // Navigate to notifications screen (implement later if needed)
        binding.notificationsRow.setOnClickListener {
            // findNavController().navigate(R.id.action_settings_to_notificationsFragment)
        }

        // Handle dark theme switch toggle
        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle dark mode preference (e.g., using SharedPreferences)
            // Example:
            // AppCompatDelegate.setDefaultNightMode(
            //     if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            // )
        }

        // Navigate to feedback screen
        binding.feedbackRow.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_FeedbackFragment)
        }

        binding.termsRow.setOnClickListener {
             findNavController().navigate(R.id.action_SettingsFragment_to_TermsFragment)
        }

        binding.notificationsRow.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_NotificationsFragment)
        }



        // Handle logout logic
        binding.logoutRow.setOnClickListener {
            // Clear user session, go back to login screen, etc.
            // Example:
            // FirebaseAuth.getInstance().signOut()
            // findNavController().navigate(R.id.action_global_loginFragment)
        }

        // Show app version dynamically
        val versionName = getAppVersion()
        binding.appVersionText.text = "App version: $versionName"
    }

    // Get current app version from package manager
    private fun getAppVersion(): String {
        return try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
