package com.diplom.kotlindiplom.childFragments.mySchedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import com.diplom.kotlindiplom.models.recyclerViewItems.LessonMyScheduleItem
import com.diplom.kotlindiplom.models.recyclerViewItems.LessonMySchedulePagerItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_my_schedule_day.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyScheduleDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyScheduleDayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var day: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            day = it.getString("day")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_schedule_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title =day?.capitalize()
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        firebase.getLessonMyScheduleOutFirebase(day!!,object :FirebaseCallback<List<Lesson>>{
            override fun onComplete(value: List<Lesson>) {
                var i = 0
                value.forEach {
                    if (it.name.isNotEmpty()){
                        adapter.add(LessonMyScheduleItem(it,i))
                        i++
                    }
                }
                lessonsRecyclerView?.adapter = adapter
            }
        })
        adapter.setOnItemClickListener { item, view ->
            val lessonItem = item as LessonMyScheduleItem
            val bundle = bundleOf()
            bundle.putString("lessonName",lessonItem.lesson.name)
            bundle.putString("lessonTime",lessonItem.lesson.time)
            bundle.putString("lessonCabinet",lessonItem.lesson.cabinet)
            bundle.putString("lessonNumber",lessonItem.number.toString())
            bundle.putString("lessonHomework",lessonItem.lesson.homework)
            bundle.putString("weekday",day)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_myScheduleDayFragment_to_detailLessonMyScheduleFragment,bundle)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyScheduleDayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyScheduleDayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}