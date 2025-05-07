package com.example.pawsitivelife.ui

import android.view.View
import android.widget.TextView
import com.example.pawsitivelife.R
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
}
