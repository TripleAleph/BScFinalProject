package com.example.pawsitivelife.ui.mydogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditNotesFragment : Fragment() {

    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        notesEditText = view.findViewById(R.id.editNotes_EDT_notes)
        saveButton = view.findViewById(R.id.editNotes_BTN_save)

        val user = auth.currentUser
        val dogId = arguments?.getString("dogId")

        if (user != null && dogId != null) {
            // Load existing notes
            val docRef = db.collection("users")
                .document(user.uid)
                .collection("dogs")
                .document(dogId)

            docRef.get()
                .addOnSuccessListener { document ->
                    val existingNotes = document.getString("notes") ?: ""
                    notesEditText.setText(existingNotes)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load notes", Toast.LENGTH_SHORT).show()
                }

            // Save new notes
            saveButton.setOnClickListener {
                val newNotes = notesEditText.text.toString().trim()
                docRef.update("notes", newNotes)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Notes updated", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update notes", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "Error: Missing user or dog ID", Toast.LENGTH_SHORT).show()
        }
    }
}
