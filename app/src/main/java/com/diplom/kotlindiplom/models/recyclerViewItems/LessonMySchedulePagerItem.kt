package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Lesson
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lessons_my_schedule_item.view.*

class LessonMySchedulePagerItem(val lesson: Lesson, val number:Int): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (lesson.lessonName.isNotEmpty()){
            viewHolder.itemView.lessonNameTextView.text = lesson.lessonName
            viewHolder.itemView.lessonTimeTextView.text = "Время: ${lesson.time}"
            viewHolder.itemView.cabinetTextView.text = "Кабинет: ${lesson.cabinet}"
        }
    }

    override fun getLayout(): Int {
        if (lesson.lessonName.isNotEmpty()){
            return R.layout.lessons_my_schedule_item
        }else{
            return R.layout.lessons_my_schedule_empty_item
        }
    }
}