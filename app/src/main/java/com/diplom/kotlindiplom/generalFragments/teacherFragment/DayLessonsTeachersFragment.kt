package com.diplom.kotlindiplom.generalFragments.teacherFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.elschool.SchoolSubjectElschool
import com.diplom.kotlindiplom.models.recyclerViewItems.SchoolSubjectElschoolItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_day_lessons_teachers.*
import java.security.acl.Group

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DayLessonsTeachersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DayLessonsTeachersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var day: String? = null
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            day = it.getString("day")
            title = it.getString("title")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_lessons_teachers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = title
        val diary = Diary()
        val adapter = GroupAdapter<ViewHolder>()
        lessonsRecyclerView?.adapter = adapter
        firebase.getFieldDiary(firebase.userUid,"url",object :Callback<String>{
            override fun onComplete(value: String) {
                when(value){
                    diary.elschool.url->{
                        diary.elschool.getTeacherLessonsDay(day!!,object :Callback<List<SchoolSubjectElschool>>{
                            override fun onComplete(value: List<SchoolSubjectElschool>) {
                                if (value.isEmpty()){
                                    messageTextView?.isVisible = true
                                }
                                scheduleDayProgressBar?.isVisible = false
                                value.forEach {
                                    adapter.add(SchoolSubjectElschoolItem(it))
                                }
                            }
                        })
                    }
                }
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
         * @return A new instance of fragment DayLessonsTeachersFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DayLessonsTeachersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}