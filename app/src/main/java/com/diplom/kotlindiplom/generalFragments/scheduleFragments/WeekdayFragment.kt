package com.diplom.kotlindiplom.generalFragments.scheduleFragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import kotlinx.android.synthetic.main.fragment_weekday.*
import java.text.DateFormat
import java.time.LocalDate
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WeekdayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekdayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var id: String = ""
    var selectedWeek = 0
    var selectedYear = 0
    val calendar: Calendar = Calendar.getInstance()
    private var updateSchedule = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            updateSchedule = it.getBoolean("updateSchedule", false)
            id = it.getString("id", "")
            //updateWithoutCheck = it.getBoolean("updateWithoutCheck", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weekday, container, false)
    }

    val diary = Diary()
    var diaryUrl = ""
    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().invalidateOptionsMenu()
        activity?.title = "Расписание"
        setupCalendar()
        progressBar?.isVisible = false
        changeColorTodayWeekdayButton()
        refreshScheduleButton?.setOnClickListener {
            updateSchedule(true)
            calendarView?.isVisible = false
        }
        mondayButton?.setOnClickListener {
            openFragmentDay("Понедельник")
        }
        tuesdayButton?.setOnClickListener {
            openFragmentDay("Вторник")
        }
        wednesdayButton?.setOnClickListener {
            openFragmentDay("Среда")
        }
        thursdayButton?.setOnClickListener {
            openFragmentDay("Четверг")
        }
        fridayButton?.setOnClickListener {
            openFragmentDay("Пятница")
        }
        saturdayButton?.setOnClickListener {
            openFragmentDay("Суббота")
        }

    }
    private fun setResourceOnButton(){
        mondayButton?.setBackgroundResource(R.drawable.simple_button)
        tuesdayButton?.setBackgroundResource(R.drawable.simple_button)
        wednesdayButton?.setBackgroundResource(R.drawable.simple_button)
        thursdayButton?.setBackgroundResource(R.drawable.simple_button)
        fridayButton?.setBackgroundResource(R.drawable.simple_button)
        saturdayButton?.setBackgroundResource(R.drawable.simple_button)
    }
    private fun changeColorTodayWeekdayButton(){
        setResourceOnButton()
        when(calendar.get(Calendar.DAY_OF_WEEK) - 1){
            1 ->{
                mondayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
            2 ->{
                tuesdayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
            3 ->{
                wednesdayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
            4 ->{
                thursdayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
            5 ->{
                fridayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
            6 ->{
                saturdayButton?.setBackgroundResource(R.drawable.rounded_today_weekday_button)
            }
        }
    }
    @ExperimentalStdlibApi
    private fun updateSchedule(updateWithoutCheck: Boolean) {
        val showButtons = {
            mondayButton?.isVisible = true
            tuesdayButton?.isVisible = true
            wednesdayButton?.isVisible = true
            thursdayButton?.isVisible = true
            fridayButton?.isVisible = true
            saturdayButton?.isVisible = true
            openCalendarButton?.isVisible = true
            dateTextView?.isVisible = true
            refreshScheduleButton?.isVisible = true
        }
        val hideButtons ={
            mondayButton?.isVisible = false
            tuesdayButton?.isVisible = false
            wednesdayButton?.isVisible = false
            thursdayButton?.isVisible = false
            fridayButton?.isVisible = false
            saturdayButton?.isVisible = false
            openCalendarButton?.isVisible = false
            dateTextView?.isVisible = false
            refreshScheduleButton?.isVisible = false
        }

        firebase.getFieldDiary(firebase.userUid, "url", object : Callback<String> {
            override fun onComplete(value: String) {
                firebase.getFieldSchedule(
                    firebase.userUid,
                    "weekUpdate",
                    object : Callback<String> {
                        override fun onComplete(weekUpdate: String) {
                            if (selectedWeek != weekUpdate.toInt() || updateWithoutCheck) {
                                diaryUrl = value
                                when (value) {
                                    diary.elschool.url -> {
                                        diary.elschool.updateSchedule(
                                            id,
                                            selectedYear,
                                            selectedWeek,
                                            requireContext(),
                                            progressBar,
                                            hideButtons,
                                            showButtons
                                        )
                                    }
                                }
                                firebase.setDateUpdateSсhedule(
                                    calendar.get(Calendar.YEAR).toString(),
                                    (calendar.get(Calendar.MONTH) + 1).toString(),
                                    calendar.get(Calendar.DAY_OF_MONTH).toString(),
                                    selectedWeek
                                )
                            }
                        }
                    })
            }
        })
    }

    private fun openFragmentDay(day: String) {
        val bundle: Bundle = bundleOf()
        bundle.putString("title", day)
            Navigation.findNavController(requireActivity(), R.id.navFragment)
                .navigate(R.id.action_weekdayFragment_to_scheduleDayFragment, bundle)
    }


    @ExperimentalStdlibApi
    private fun setupCalendar() {
        calendarView?.isVisible = false
        //val selectedDate = calendarView?.date
        val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
        firebase.getDateUpdateInSchedule(object : Callback<LocalDate> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: LocalDate) {
                if (updateSchedule) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    updateSchedule(true)
                } else {
                    calendar.set(value.year, value.monthValue - 1, value.dayOfMonth)
                    calendarView?.date = calendar.timeInMillis
                }
                selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                selectedYear = calendar.get(Calendar.YEAR)
                dateTextView?.text = dateFormatter.format(calendar.time)
            }
        })
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
            openCalendarButton?.text = "Открыть календарь"
            calendarView.isVisible = false
            selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            selectedYear = calendar.get(Calendar.YEAR)
            changeColorTodayWeekdayButton()
            updateSchedule(false)
        }
       //updateSchedule(updateWithoutCheck, calendar)
    }
}