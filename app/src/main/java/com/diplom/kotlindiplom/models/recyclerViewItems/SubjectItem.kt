package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.subject_item.view.*

class SubjectItem(val subjectName:String,val subjectInFirebase:String): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameSubjectTextView.text = subjectName
    }

    override fun getLayout(): Int {
        return R.layout.subject_item
    }

}