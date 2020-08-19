package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.child_diary_elschool_item.view.*

class ChildDiaryItem(val child : ChildForElschool) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return  R.layout.child_diary_elschool_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.childNameTextView.text = child.name
    }

}