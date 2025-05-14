package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Reminder


class ReminderAdapter : RecyclerView.Adapter<ReminderViewHolder>() {

    private var reminders: List<Reminder> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }

    override fun getItemCount(): Int = reminders.size

    fun submitList(newList: List<Reminder>) {
        reminders = newList
        notifyDataSetChanged()
    }
}