package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.Callback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.*

class FunctionsFirebaseFeedback:FunctionsFirebase() {

    fun getCountOpenFeedback(callback: Callback<Int>) {
        val ref = feedbackRef.orderByChild("userUid").equalTo(userUid)
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                snapshot.children.forEach {
                    it.children.forEach {
                        if (it.key.toString() == "status") {
                            if (it.value.toString().toInt() == -1) {
                                count++;
                            }
                        }
                    }
                }
                callback.onComplete(count)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun sendMessageFeedback(feedbackId: String, textMessage: String, author: String) {
        val nowDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(nowDate)
        val messageFeedback = MessageFeedback(author, textMessage, formatter)
        val ref = feedbackRef.child(feedbackId).child("messages").push()
        messageFeedback.id = ref.key.toString()
        ref.setValue(messageFeedback)
    }

    private fun getAllMessagesInFeedback(detailsFeedback: DataSnapshot): MutableList<MessageFeedback> {
        val messages = mutableListOf<MessageFeedback>()
        detailsFeedback.children.forEach {
            val messageFeedback = it.getValue<MessageFeedback>()?: MessageFeedback()
            if (messageFeedback.text.isNotEmpty()) {
                messages.add(messageFeedback)
            }
        }
        return messages
    }

    fun getAllFieldsFeedback(detailsFeedback: DataSnapshot): Feedback {
        val feedback = Feedback()
        feedback.codeQuestion = detailsFeedback.child("codeQuestion").value.toString().toInt()
        feedback.status = detailsFeedback.child("status").value.toString().toInt()
        feedback.topic = detailsFeedback.child("topic").value.toString()
        feedback.id = detailsFeedback.child("id").value.toString()
        feedback.time = detailsFeedback.child("time").value.toString()
        feedback.userUid = detailsFeedback.child("userUid").value.toString()
        detailsFeedback.children.forEach { field ->
            when (field.key.toString()) {
                "messages" -> {
                    feedback.messages = getAllMessagesInFeedback(field)
                }
            }
        }
        return feedback
    }

    fun getFeedback(id: String, callback: Callback<Feedback>) {
        val ref = feedbackRef.orderByChild("id").equalTo(id)
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { feedbacks ->
                    val feedback = getAllFieldsFeedback(feedbacks)
                    feedback.messages.forEach {
                        if (it.author != "user" && it.readStatus != "1" && it.id.isNotEmpty()) {
                            feedbackRef.child(feedback.id).child("messages").child(it.id)
                                .child("readStatus").setValue("1")
                        }
                    }

                    callback.onComplete(feedback)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getListFeedback(callback: Callback<List<Feedback>>) {
        val ref = feedbackRef.orderByChild("userUid").equalTo(userUid)
        ref.keepSynced(true)
        val feedbacks = mutableListOf<Feedback>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { detailsFeedback ->
                    val feedback = getAllFieldsFeedback(detailsFeedback)
                    if (feedback.id.isNotEmpty()) {
                        feedbacks.add(getAllFieldsFeedback(detailsFeedback))
                    }
                }
                callback.onComplete(feedbacks)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addFeedback(codeQuestion: Int, topic: String, message: String) {
        val ref = feedbackRef.push()
        val nowDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(nowDate)
        ref.child("codeQuestion").setValue(codeQuestion)
        ref.child("topic").setValue(topic)
        ref.child("userUid").setValue(userUid)
        ref.child("status").setValue(-1)
        ref.child("time").setValue(formatter)
        ref.child("id").setValue(ref.key)
        val messageRef = ref.child("messages").push()
        messageRef.child("text").setValue(message)
        messageRef.child("author").setValue("user")
        messageRef.child("id").setValue(messageRef.key.toString())
        messageRef.child("time").setValue(formatter)
    }

}