package com.example.pawsitivelife.ui.mydogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.R

class EditNotesFragment : Fragment() {

    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        notesEditText = view.findViewById(R.id.editNotes_EDT_notes)
        saveButton = view.findViewById(R.id.editNotes_BTN_save)

        // Optional: preload previous text (from ViewModel, SharedPreferences, etc.)
        // notesEditText.setText(...)

        saveButton.setOnClickListener {
            val newNotes = notesEditText.text.toString()
            // Save the newNotes somewhere: ViewModel / SharedPreferences / Database

            findNavController().popBackStack() // return to previous screen
        }
    }
}
