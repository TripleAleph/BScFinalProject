package com.example.pawsitivelife.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.pawsitivelife.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FilterBottomSheetFragment(
    private val preselectedTags: List<String> = emptyList(),  // Previously selected tags (to restore state)
    private val preselectedAges: List<String> = emptyList(),  // Previously selected age groups (to restore state)
    private val listener: FilterListener  // Listener to pass selected filters back
) : BottomSheetDialogFragment() {

    // Interface used to send selected filters back to the caller ( HomeFragment)
    interface FilterListener {
        fun onFiltersApplied(selectedTags: List<String>, selectedAges: List<String>)
    }
    // List of all tag options available for filtering
    private val allTags = listOf("Dog Training", "Feeding Your Dog", "Dog Health", "Getting a Dog")

    // List of all age group options available for filtering
    private val allAgeGroups = listOf("Puppies", "Adults", "Seniors")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout from XML (fragment_filter_bottom_sheet)
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tagContainer = view.findViewById<LinearLayout>(R.id.filter_tags_container)  // Container for tag checkboxes
        val ageContainer = view.findViewById<LinearLayout>(R.id.filter_ages_container)  // Container for age checkboxes
        val applyButton = view.findViewById<MaterialButton>(R.id.btn_apply_filters)     // "Apply Filters" button


        // Convert preselected filters to lowercase to allow case-insensitive comparison
        val lowerPreselectedTags = preselectedTags.map { it.lowercase() }
        val lowerPreselectedAges = preselectedAges.map { it.lowercase() }

        // Dynamically create checkboxes for tags and mark them if they were previously selected
        val tagCheckboxes = allTags.map { label ->
            CheckBox(requireContext()).apply {
                text = label
                isChecked = lowerPreselectedTags.contains(label.lowercase())
                tagContainer.addView(this)
            }
        }

        // Dynamically create checkboxes for age groups and mark them if previously selected
        val ageCheckboxes = allAgeGroups.map { age ->
            CheckBox(requireContext()).apply {
                text = age
                isChecked = lowerPreselectedAges.contains(age.lowercase()) //
                ageContainer.addView(this)
            }
        }

//        val tagCheckboxes = allTags.map { label ->
//            CheckBox(requireContext()).apply {
//                text = label
//                isChecked = preselectedTags.contains(label)
//                tagContainer.addView(this)
//            }
//        }
//
//        val ageCheckboxes = allAgeGroups.map { age ->
//            CheckBox(requireContext()).apply {
//                text = age
//                isChecked = preselectedAges.contains(age)
//                ageContainer.addView(this)
//            }
//        }

        // When the apply button is clicked, gather selected checkboxes and pass them back via the listener
        applyButton.setOnClickListener {
            val selectedTags = tagCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            val selectedAges = ageCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            listener.onFiltersApplied(selectedTags, selectedAges)  // Pass results back to parent
            dismiss()  // Close the bottom sheet
        }
    }
}