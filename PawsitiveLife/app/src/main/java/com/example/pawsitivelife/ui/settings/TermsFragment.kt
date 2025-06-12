package com.example.pawsitivelife.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pawsitivelife.R

class TermsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_terms, container, false)

        // Find the email TextView
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)

        emailTextView.setOnClickListener {
            // Create an intent to open an email app
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                // This ensures only email apps can handle this intent
                data = Uri.parse("mailto:pawsitivelife.team@gmail.com")

                // Pre-fill the email subject
                putExtra(Intent.EXTRA_SUBJECT, "App Feedback / Question")
            }

            try {
                // Try to launch the email app
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // If no email app is available, show a message to the user
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }
}
