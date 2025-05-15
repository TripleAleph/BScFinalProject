package com.example.pawsitivelife.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Reminder

class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.reminder_title)
    private val time: TextView = itemView.findViewById(R.id.reminder_time)
    private val image: ImageView = itemView.findViewById(R.id.reminder_image)

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun bind(reminder: Reminder) {
        title.text = reminder.title
        time.text = reminder.date.format(formatter)
        image.setImageResource(reminder.imageResId) // או Glide/Picasso אם זה URL
    }
}
