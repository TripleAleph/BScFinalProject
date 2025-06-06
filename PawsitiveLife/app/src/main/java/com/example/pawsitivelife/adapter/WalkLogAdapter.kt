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

class WalkLogAdapter(
    private val walkLogs: List<WalkLogEntry>,
    private val listener: OnWalkLogChangeListener
) : RecyclerView.Adapter<WalkLogAdapter.WalkLogViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var dogId: String

    fun setDogId(id: String) {
        dogId = id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkLogViewHolder {
        android.util.Log.d("WalkLogAdapter", "Creating ViewHolder")
        val binding = ItemWalkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalkLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalkLogViewHolder, position: Int) {
        android.util.Log.d("WalkLogAdapter", "Binding item at position $position: ${walkLogs[position].date}")
        holder.bind(walkLogs[position])
    }

    override fun getItemCount(): Int {
        android.util.Log.d("WalkLogAdapter", "Item count: ${walkLogs.size}")
        return walkLogs.size
    }

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
                    entry.note = newNote
                    listener.onWalkLogChanged(entry, entry.completed, newNote)
                    save(entry)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        private fun save(entry: WalkLogEntry) {
            if (userId.isNotEmpty() && ::dogId.isInitialized) {
                val dateKey = entry.date.substringBefore(" - ")
                val typeKey = entry.date.substringAfter(" - ").lowercase()
                val updates = mapOf(
                    "${typeKey}_done" to entry.completed,
                    "${typeKey}_note" to entry.note
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
