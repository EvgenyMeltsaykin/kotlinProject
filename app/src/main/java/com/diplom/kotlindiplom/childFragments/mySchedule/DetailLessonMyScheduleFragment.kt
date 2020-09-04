package com.diplom.kotlindiplom.childFragments.mySchedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_detail_lesson_my_schedule.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailLessonMyScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailLessonMyScheduleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var lessonName: String
    private lateinit var lessonTime: String
    private lateinit var lessonCabinet: String
    private lateinit var lessonNumber: String
    private lateinit var lessonHomework: String
    private lateinit var dateHomework: String
    private lateinit var weekday: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lessonName = it.getString("lessonName","")
            lessonTime = it.getString("lessonTime","")
            lessonCabinet = it.getString("lessonCabinet","")
            lessonNumber = it.getString("lessonNumber","")
            lessonHomework = it.getString("lessonHomework","")
            dateHomework = it.getString("dateHomework","")
            weekday = it.getString("weekday","")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_lesson_my_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = lessonName

        lessonNameTextView?.text = "Предмет: $lessonName"
        lessonCabinetTextView?.text = "Кабинет: $lessonCabinet"
        lessonTimeTextView?.text = "Время: $lessonTime"
        dateHomeworkTextInput?.editText?.setText(dateHomework)
        if (lessonHomework.isNotEmpty()){
            homeworkTextInput.editText?.setText(lessonHomework)
        }
        saveLessonButton?.setOnClickListener {
            val firebase = FunctionsFirebase()
            val homework = homeworkTextInput?.editText?.text.toString()
            val dateHomework = dateHomeworkTextInput?.editText?.text.toString()
            firebase.updateLessonMyScheduleInFirebase(weekday,lessonNumber,lessonName,lessonCabinet, homework,lessonTime,dateHomework)
            Toast.makeText(requireContext(),"Домашняя работа сохранена",Toast.LENGTH_SHORT).show()
            Navigation.findNavController(requireActivity(),R.id.navFragment).popBackStack()
        }
    }

}