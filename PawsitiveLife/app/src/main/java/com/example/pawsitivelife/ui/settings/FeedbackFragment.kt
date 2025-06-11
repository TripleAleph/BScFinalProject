package com.example.pawsitivelife.ui.settings


import android.widget.EditText
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import android.widget.RatingBar
import android.widget.Toast
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedbackFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val commentInput = view.findViewById<EditText>(R.id.commentInput)
        val charCountText = view.findViewById<TextView>(R.id.charCountText)

        commentInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCountText.text = "Chars: $currentLength/300"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Find the send button in the layout
        val sendButton = view.findViewById<Button>(R.id.sendButton)

        // Set click listener for the button
        sendButton.setOnClickListener {
            // Get the selected rating from the RatingBar
            val rating = ratingBar.rating

            // Get the comment text and remove leading/trailing spaces
            val comment = commentInput.text.toString().trim()

            // If no rating was selected, show an error message and stop
            if (rating == 0f) {
                Toast.makeText(requireContext(), "Please rate your experience before sending.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Get the current user ID
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // If no user is logged in, show an error
            if (userId == null) {
                Toast.makeText(requireContext(), "You must be logged in to send feedback.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a feedback object as a map
            val feedbackData = hashMapOf(
                "userId" to userId,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            // Save the feedback to Firestore under "feedbacks" collection
            FirebaseFirestore.getInstance().collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener {
                    // Show success message
                    Toast.makeText(requireContext(), "Feedback sent successfully!", Toast.LENGTH_SHORT).show()

                    // Go back to the previous screen (SettingsFragment)
                    findNavController().navigateUp()
                }

                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to send feedback: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

}