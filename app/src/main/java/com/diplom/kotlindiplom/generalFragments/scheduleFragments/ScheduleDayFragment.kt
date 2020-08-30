package com.diplom.kotlindiplom.generalFragments.scheduleFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.*
import com.diplom.kotlindiplom.models.recyclerViewItems.LessonItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_schedule_day.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleDayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!title.isNullOrBlank()) {
            activity?.title = title
        }
        messageTextView?.isVisible  =false
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        val day = activity?.title.toString().substringBefore(" ")
        val date = activity?.title.toString().substringAfter(day)
        if (date.isEmpty()){
            firebase.getFieldScheduleDay(firebase.uidUser!!,day.toLowerCase(),object : FirebaseCallback<String>{
                override fun onComplete(value: String) {
                    activity?.title = "${activity?.title} $value"
                }
            })
        }
        adapter.clear()
        firebase.getScheduleDay(firebase.uidUser!!, day.toLowerCase(),object : FirebaseCallback<List<Lesson>>{
            override fun onComplete(value: List<Lesson>) {
                var fl = true
                value.forEach {
                    if (it.name.isNotEmpty()){
                        adapter.add(LessonItem(it))
                        fl = false
                    }
                }
                if (fl)messageTextView?.isVisible  =true
                lessonsRecyclerView?.adapter = adapter
            }
        })

        adapter.setOnItemClickListener { item, view ->
            val lessonItem = item as LessonItem
            val bundle = bundleOf()
            bundle.putString("homework",lessonItem.lesson.homework)
            bundle.putString("lessonName",lessonItem.lesson.name)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_scheduleDayFragment_to_homeworkFragment,bundle)

        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScheduleDayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScheduleDayFragment()
                .apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}