package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.message_to_item.view.*
import kotlinx.android.synthetic.main.message_from_item.view.*

class MessageItem(private val author:String, val text:String,val time:String): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (author == "user"){
            viewHolder.itemView.messageToTextView?.text = text
            viewHolder.itemView.timeMessageToTextView?.text = time
        }else{
            viewHolder.itemView.timeMessageFromTextView?.text = time
            viewHolder.itemView.messageFromTextView?.text = text
        }
    }

    override fun getLayout(): Int {
        return if (author == "user"){
            R.layout.message_to_item
        }else{
            R.layout.message_from_item
        }

    }

}