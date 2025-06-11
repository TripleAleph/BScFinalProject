package com.example.pawsitivelife.ui.settings

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.example.pawsitivelife.SignInActivity
import com.example.pawsitivelife.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

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

        // Navigate to notifications screen (implement later if needed)
        binding.notificationsRow.setOnClickListener {
            // findNavController().navigate(R.id.action_settings_to_notificationsFragment)
        }

        // Handle dark theme switch toggle
        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->

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
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Navigate to SignInActivity and clear the back stack
            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
