package com.diplom.kotlindiplom.generalFragments.feedbackFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Feedback
import com.diplom.kotlindiplom.models.recyclerViewItems.FeedbackItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_list_feedback.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFeedbackFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_feedback, container, false)
    }

    override fun onResume() {
        super.onResume()
        refreshListFeedback()
    }
    fun refreshListFeedback(){
        firebase.getListFeedback(object : Callback<List<Feedback>> {
            override fun onComplete(value: List<Feedback>) {
                listFeedbackProgressBar?.isVisible = false
                if (value.isEmpty()){
                    emptyFeedbackTextView?.isVisible  = true
                }
                adapter.clear()
                val feedbackId = mutableListOf<String>()
                var fl = true
                value.forEach {feedback->
                    feedbackId.forEach{
                        if (it ==  feedback.id){
                            fl = false
                        }
                    }
                    if (fl){
                        feedbackId.add(feedback.id)
                        adapter.add(FeedbackItem(feedback.topic,feedback.status,feedback.id))
                    }
                }
            }
        })
    }
    val adapter = GroupAdapter<ViewHolder>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().invalidateOptionsMenu()
        requireActivity().title = "Список вопросов"
        super.onViewCreated(view, savedInstanceState)
        listFeedbackRecyclerView?.adapter = adapter

        refreshListFeedback()
        adapter.setOnItemClickListener { item, view ->
            val bundle = bundleOf()
            val feedback = item as FeedbackItem
            bundle.putString("feedbackId",feedback.id)
            bundle.putString("topic",feedback.topic)
            Log.d("Tag",feedback.id.toString())
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listFeedbackFragment_to_feedbackDetailsFragment,bundle)
        }

        writeFeedbackButton?.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listFeedbackFragment_to_mailFragment)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFeedbackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}