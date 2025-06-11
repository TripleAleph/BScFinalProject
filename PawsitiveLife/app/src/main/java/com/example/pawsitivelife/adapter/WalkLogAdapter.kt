package com.example.pawsitivelife.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.databinding.ItemWalkBinding
import com.example.pawsitivelife.model.WalkLogEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WalkLogAdapter(
    private val walkLogs: List<WalkLogEntry>,
    private val listener: OnWalkLogChangeListener
) : RecyclerView.Adapter<WalkLogAdapter.WalkLogViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var dogId: String
    private var selectedDate: LocalDate = LocalDate.now()

    fun setDogId(id: String) {
        dogId = id
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkLogViewHolder {
        val binding = ItemWalkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalkLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalkLogViewHolder, position: Int) {
        holder.bind(walkLogs[position])
    }

    override fun getItemCount() = walkLogs.size

    inner class WalkLogViewHolder(private val binding: ItemWalkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: WalkLogEntry) {
            binding.walkItemDate.text = entry.date

            binding.walkItemCheckbox.setOnCheckedChangeListener(null)
            binding.walkItemCheckbox.isChecked = entry.completed
            binding.walkItemCheckbox.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                entry.completed = isChecked
                listener.onWalkLogChanged(entry, isChecked, entry.note)
                save(entry)
            }

            binding.walkItemNote.setText(entry.note)
            binding.walkItemNote.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newNote = s.toString()
                    if (entry.note != newNote) {
                        entry.note = newNote
                        listener.onWalkLogChanged(entry, entry.completed, newNote)
                        save(entry)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        private fun save(entry: WalkLogEntry) {
            if (userId.isNotEmpty() && ::dogId.isInitialized) {
                val dateKey = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val updates = mapOf(
                    "completed" to entry.completed,
                    "note" to entry.note
                )
                db.collection("users").document(userId)
                    .collection("dogs").document(dogId)
                    .collection("walk_log").document(dateKey)
                    .set(updates, SetOptions.merge())
            }
        }
    }

    interface OnWalkLogChangeListener {
        fun onWalkLogChanged(walkEntry: WalkLogEntry, isChecked: Boolean, note: String)
    }
}
