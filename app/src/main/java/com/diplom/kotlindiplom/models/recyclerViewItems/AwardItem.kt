package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.award_item.view.*

class AwardItem(val name:String, val cost:String,val awardId:String): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameAwardTextView.text = name
        viewHolder.itemView.costAwardTextView.text = cost
    }

    override fun getLayout(): Int {
        return R.layout.award_item
    }
}