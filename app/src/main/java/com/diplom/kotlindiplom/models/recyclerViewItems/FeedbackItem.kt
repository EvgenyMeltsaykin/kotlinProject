package com.diplom.kotlindiplom.models.recyclerViewItems

import androidx.core.view.isVisible
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Feedback
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feedback_item.view.*

class FeedbackItem(val feedback: Feedback):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.topicFeedbackTextView.text = feedback.topic
        feedback.messages.forEach {
            if (it.readStatus == "0"){
                viewHolder.itemView.noReadMessageImageView?.isVisible = true
            }
        }
        when(feedback.status){
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