package com.diplom.kotlindiplom.generalFragments.feedbackFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.MainActivity.FunctionUiSingleton.functionsUI
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Feedback
import com.diplom.kotlindiplom.models.recyclerViewItems.MessageItem
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feedback_details.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedbackDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedbackDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit  var feedbackId: String
    private var topic: String? = null
    private var countMessageUser  = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            feedbackId = it.getString("feedbackId","")
            topic = it.getString("topic")
        }
        requireActivity().title = topic
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback_details, container, false)
    }
    val adapter = GroupAdapter<ViewHolder>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMessageFromFeedback()
        messagesRecyclerView?.adapter = adapter
        sendMessageButton?.setOnClickListener {
            val textMessage = textMessageEditText?.text.toString()
            if (countMessageUser > 3){
                Toast.makeText(requireContext(),"Дождитесь, пока служба поддержки ответит Вам", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (textMessage.isNotEmpty()){
                firebase.sendMessageFeedback(feedbackId,textMessage,"user")
                textMessageEditText?.setText("")
            }
        }
    }
    private fun getMessageFromFeedback(){
        firebase.feedbackRef.child(feedbackId).child("messages").addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                firebase.getFeedback(feedbackId,object :Callback<Feedback>{
                    override fun onComplete(value: Feedback) {
                        adapter.clear()
                        countMessageUser = 1
                        messageFeedbackProgressBar?.isVisible = false
                        if (value.status == 1){
                            sendMessageButton?.isVisible = false
                            textMessageEditText?.isVisible = false
                            feedbackCloseTextView?.isVisible = true
                        }else{
                            sendMessageButton?.isVisible = true
                            textMessageEditText?.isVisible = true
                        }
                        value.messages.forEach {
                            if (it.author == "user"){
                                countMessageUser++
                            }else{
                                countMessageUser = 1
                            }
                            adapter.add(MessageItem(it.author,it.text,it.time))
                        }
                        messagesRecyclerView?.smoothScrollToPosition(messagesRecyclerView.adapter?.itemCount!!-1);
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedbackDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedbackDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}