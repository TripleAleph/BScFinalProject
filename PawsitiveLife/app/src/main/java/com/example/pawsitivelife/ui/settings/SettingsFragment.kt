package com.example.pawsitivelife.ui.settings

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

        // Navigate to terms of service screen (implement later if needed)
        binding.termsRow.setOnClickListener {
            // findNavController().navigate(R.id.action_settings_to_termsFragment)
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
