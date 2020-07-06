package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.task_row.view.*

class TaskItem(val task:Task): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.task_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameTaskTextViewParent.setText(task.title)
        viewHolder.itemView.costTaskTextViewParent.setText("Стоимость " + task.cost.toString())
        viewHolder.itemView.dateTaskTextViewParent.setText(task.time)
    }

}