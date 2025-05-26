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
    private val preselectedTags: List<String> = emptyList(),
    private val preselectedAges: List<String> = emptyList(),
    private val listener: FilterListener
) : BottomSheetDialogFragment() {

    interface FilterListener {
        fun onFiltersApplied(selectedTags: List<String>, selectedAges: List<String>)
    }

    private val allTags = listOf("Dog Training", "Feeding Your Dog", "Dog Health", "Getting a Dog")
    private val allAgeGroups = listOf("Puppies", "Adults", "Seniors")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tagContainer = view.findViewById<LinearLayout>(R.id.filter_tags_container)
        val ageContainer = view.findViewById<LinearLayout>(R.id.filter_ages_container)
        val applyButton = view.findViewById<MaterialButton>(R.id.btn_apply_filters)

        val tagCheckboxes = allTags.map { label ->
            CheckBox(requireContext()).apply {
                text = label
                isChecked = preselectedTags.contains(label)
                tagContainer.addView(this)
            }
        }

        val ageCheckboxes = allAgeGroups.map { age ->
            CheckBox(requireContext()).apply {
                text = age
                isChecked = preselectedAges.contains(age)
                ageContainer.addView(this)
            }
        }

        applyButton.setOnClickListener {
            val selectedTags = tagCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            val selectedAges = ageCheckboxes.filter { it.isChecked }.map { it.text.toString() }
            listener.onFiltersApplied(selectedTags, selectedAges)
            dismiss()
        }
    }
}