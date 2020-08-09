package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.child_row.view.*

class ChildrenItem(val child:Child, val deleteListener: OnClickDeleteButton?): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.child_row
    }

    interface OnClickDeleteButton{
        fun onClickDeleteButton(item: Item<ViewHolder>,child:Child)
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameChildTextView.text = child.username
        viewHolder.itemView.childIdTextView.text = "id:" + child.id.toString()
        viewHolder.itemView.deleteButton.setOnClickListener {
            deleteListener?.onClickDeleteButton(this@ChildrenItem,child)
        }
    }

}