package com.diplom.kotlindiplom.generalFragments.teacherFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import kotlinx.android.synthetic.main.fragment_weekday.*
import kotlinx.android.synthetic.main.fragment_weekday_teacher.*
import kotlinx.android.synthetic.main.fragment_weekday_teacher.calendarView
import kotlinx.android.synthetic.main.fragment_weekday_teacher.dateTextView
import kotlinx.android.synthetic.main.fragment_weekday_teacher.fridayButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.mondayButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.openCalendarButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.progressBar
import kotlinx.android.synthetic.main.fragment_weekday_teacher.saturdayButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.thursdayButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.tuesdayButton
import kotlinx.android.synthetic.main.fragment_weekday_teacher.view.*
import kotlinx.android.synthetic.main.fragment_weekday_teacher.wednesdayButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WeekdayTeacherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekdayTeacherFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val calendar: Calendar = Calendar.getInstance()
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
        return inflater.inflate(R.layout.fragment_weekday_teacher, container, false)
    }
    private fun getDate(number:Int) : String{
        val cal = calendar
        cal[Calendar.DAY_OF_WEEK] = cal.getActualMinimum(Calendar.DAY_OF_WEEK) + number
        val nowDate = cal.time
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(nowDate)
    }
    private fun setupNameWeekday(){

        mondayButton?.text = "Понедельник " + getDate(1)
        tuesdayButton?.text = "Вторник " + getDate(2)
        wednesdayButton?.text = "Среда " + getDate(3)
        thursdayButton?.text= "Четверг " + getDate(4)
        fridayButton?.text= "Пятница " + getDate(5)
        saturdayButton?.text= "Суббота " + getDate(6)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Дни недели"
        val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
        dateTextView?.text = dateFormatter.format(calendar.time)
        calendarView?.isVisible = false
        progressBar?.isVisible = false
        setupNameWeekday()
        mondayButton?.setOnClickListener {
            openFragmentDay(mondayButton?.text.toString())
            updateTeacherSchedule("Понедельник",getDate(1))
        }
        tuesdayButton?.setOnClickListener {
            openFragmentDay(tuesdayButton?.text.toString())
            updateTeacherSchedule("Вторник",getDate(2))
        }
        wednesdayButton?.setOnClickListener {
            openFragmentDay(wednesdayButton?.text.toString())
            updateTeacherSchedule("Среда",getDate(3))
        }
        thursdayButton?.setOnClickListener {
            openFragmentDay(thursdayButton?.text.toString())
            updateTeacherSchedule("Четверг",getDate(4))
        }
        fridayButton?.setOnClickListener {
            openFragmentDay(fridayButton?.text.toString())
            updateTeacherSchedule("Пятница",getDate(5))
        }
        saturdayButton?.setOnClickListener {
            openFragmentDay(saturdayButton?.text.toString())
            updateTeacherSchedule("Суббота",getDate(6))
        }

        openCalendarButton?.setOnClickListener {
            if (!calendarView?.isVisible!!) {
                openCalendarButton?.text = "Закрыть календарь"
                calendarView?.isVisible = true
            } else {
                openCalendarButton?.text = "Открыть календарь"
                calendarView?.isVisible = false
            }
        }

        calendarView?.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dateTextView?.text = dateFormatter.format(calendar.time)
            setupNameWeekday()
            openCalendarButton?.text = "Открыть календарь"
            calendarView.isVisible = false
        }
    }
    private fun updateTeacherSchedule(day:String,date:String){
        val diary = Diary();
        firebase.getFieldDiary(firebase.userUid, "url", object :
            Callback<String> {
            override fun onComplete(value: String) {
                when (value) {
                    diary.elschool.url -> {
                        diary.elschool.getTeacherScheduleFromDiary(day.decapitalize(),date)
                    }
                }
            }
        })
    }
    private fun openFragmentDay(day: String) {
        val bundle: Bundle = bundleOf()
        bundle.putString("title", day.toLowerCase(Locale.ROOT))
        Navigation.findNavController(requireActivity(), R.id.navFragment)
            .navigate(R.id.action_weekdayTeacherFragment_to_dayLessonsTeachersFragment, bundle)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeekdayTeacherFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeekdayTeacherFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}