package com.example.pawsitivelife.ui.mydogs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R

class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.reminder_title)
    private val date: TextView = itemView.findViewById(R.id.reminder_date)

    fun bind(reminder: Reminder) {
        title.text = reminder.title
        date.text = reminder.date
    }
}
