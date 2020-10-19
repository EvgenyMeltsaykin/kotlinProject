package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.elschool.SchoolSubjectElschool
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.school_subject_elschool_item.view.*

class SchoolSubjectElschoolItem(val lesson:SchoolSubjectElschool): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.lessonNameTextView.text = "${lesson.DisciplineName} ${lesson.DepartmentName}"
        viewHolder.itemView.timeTextView.text = "${lesson.StartTime} - ${lesson.EndTime}"
    }

    override fun getLayout(): Int {
        return R.layout.school_subject_elschool_item
    }
}