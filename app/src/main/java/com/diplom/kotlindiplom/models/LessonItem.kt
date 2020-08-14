package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lesson_row.view.*
import kotlinx.android.synthetic.main.task_row.view.*

class LessonItem(val lesson: Lesson) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.lesson_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.lessonNameTextView.text = lesson.name
        if (lesson.cabinet.isEmpty())
            viewHolder.itemView.numberCabinetAndTimeTextView.text = "${lesson.form} ${lesson.time}"
        else{
            viewHolder.itemView.numberCabinetAndTimeTextView.text = "${lesson.form} ${lesson.time} кабинет:${lesson.cabinet}"
        }
    }

}