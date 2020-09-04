package com.diplom.kotlindiplom.models.recyclerViewItems

import androidx.core.view.isVisible
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Lesson
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lessons_my_schedule_item.view.*

class LessonMyScheduleItem(val lesson: Lesson, val number: Int) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (lesson.homework.isNotEmpty()) viewHolder.itemView.homeworkIndicatorImageView?.isVisible = true
        viewHolder.itemView.lessonNameTextView.text = lesson.name
        viewHolder.itemView.lessonTimeTextView.text = "Время: ${lesson.time}"
        viewHolder.itemView.cabinetTextView.text = "Кабинет: ${lesson.cabinet}"
    }

    override fun getLayout(): Int {
        return R.layout.lessons_my_schedule_item
    }
}