package com.diplom.kotlindiplom.models

import android.util.Log
import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lessons_mark_item.view.*

class LessonsMarkItem(val lessonName: String) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.lessons_mark_item
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.lessonsNameTextView.text = lessonName
    }
}