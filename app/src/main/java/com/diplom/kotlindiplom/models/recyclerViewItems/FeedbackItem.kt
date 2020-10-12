package com.diplom.kotlindiplom.models.recyclerViewItems

import com.diplom.kotlindiplom.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feedback_item.view.*

class FeedbackItem(val topic:String,val status:Int,val id:String):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.topicFeedbackTextView.text = topic
        when(status){
            -1->{
                viewHolder.itemView.statusFeedbackTextView.text = "Открыто"
            }
            0->{
                viewHolder.itemView.statusFeedbackTextView.text = "В обработке"
            }
            1->{
                viewHolder.itemView.statusFeedbackTextView.text = "Закрыто"
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.feedback_item
    }
}