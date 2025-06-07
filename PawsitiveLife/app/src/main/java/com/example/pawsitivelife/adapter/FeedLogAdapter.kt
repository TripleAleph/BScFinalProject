package com.example.pawsitivelife.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.databinding.ItemFeedBinding
import com.example.pawsitivelife.model.FeedLogEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FeedLogAdapter(
    private val feedLogs: List<FeedLogEntry>,
    private val listener: OnFeedLogChangeListener
) : RecyclerView.Adapter<FeedLogAdapter.FeedLogViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedLogViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedLogViewHolder, position: Int) {
        holder.bind(feedLogs[position])
    }

    override fun getItemCount() = feedLogs.size

    inner class FeedLogViewHolder(private val binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: FeedLogEntry) {
            binding.feedItemDate.text = entry.date

            binding.feedItemCheckbox.setOnCheckedChangeListener(null)
            binding.feedItemCheckbox.isChecked = entry.completed
            binding.feedItemCheckbox.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                entry.completed = isChecked
                listener.onFeedLogChanged(entry, isChecked, entry.note)
                save(entry)
            }

            binding.feedItemNote.setText(entry.note)
            binding.feedItemNote.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newNote = s.toString()
                    if (entry.note != newNote) {
                        entry.note = newNote
                        listener.onFeedLogChanged(entry, entry.completed, newNote)
                        save(entry)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        private fun save(entry: FeedLogEntry) {
            if (userId.isNotEmpty() && ::dogId.isInitialized) {
                val dateKey = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val updates = mapOf(
                    "completed" to entry.completed,
                    "note" to entry.note
                )
                db.collection("users").document(userId)
                    .collection("dogs").document(dogId)
                    .collection("feed_log").document(dateKey)
                    .set(updates, SetOptions.merge())
            }
        }
    }

    interface OnFeedLogChangeListener {
        fun onFeedLogChanged(feedEntry: FeedLogEntry, isChecked: Boolean, note: String)
    }
}
