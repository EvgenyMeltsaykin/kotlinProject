package com.diplom.kotlindiplom.generalFragments.sheduleFragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.FunctionsFirebase
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
    private var updateShedule: Boolean = true
    private var id: String = ""
    private var updateWithoutCheck: Boolean = false
    var selectedWeek = 0
    var selectedYear = 0
    val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Расписание"
        arguments?.let {
            updateShedule = it.getBoolean("updateShedule", true)
            id = it.getString("id", "")
            updateWithoutCheck = it.getBoolean("updateWithoutCheck", false)
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

        setupCalendar()
        progressBar.isVisible = false

        refreshSheduleButton.setOnClickListener {
            updateShedule(true, calendar)
        }
        mondayButton.setOnClickListener {
            openFragmentDay("Понедельник")
        }
        tuesdayButton.setOnClickListener {
            openFragmentDay("Вторник")
        }
        wednesdayButton.setOnClickListener {
            openFragmentDay("Среда")
        }
        thursdayButton.setOnClickListener {
            openFragmentDay("Четверг")
        }
        fridayButton.setOnClickListener {
            openFragmentDay("Пятница")
        }
        saturdayButton.setOnClickListener {
            openFragmentDay("Суббота")
        }

    }
    @ExperimentalStdlibApi
    private fun updateShedule(updateWithoutCheck: Boolean, calendar: Calendar) {
        val firebase = FunctionsFirebase()
        val mondayButton = view?.findViewById<Button>(R.id.mondayButton)
        val tuesdayButton = view?.findViewById<Button>(R.id.tuesdayButton)
        val wednesdayButton = view?.findViewById<Button>(R.id.wednesdayButton)
        val thursdayButton = view?.findViewById<Button>(R.id.thursdayButton)
        val fridayButton = view?.findViewById<Button>(R.id.fridayButton)
        val saturdayButton = view?.findViewById<Button>(R.id.saturdayButton)
        val openCalendarButton = view?.findViewById<Button>(R.id.openCalendarButton)
        val refreshSheduleButton = view?.findViewById<Button>(R.id.refreshSheduleButton)
        val dateTextView = view?.findViewById<TextView>(R.id.dateTextView)

        val showButtons = {
            mondayButton?.isVisible = true
            tuesdayButton?.isVisible = true
            wednesdayButton?.isVisible = true
            thursdayButton?.isVisible = true
            fridayButton?.isVisible = true
            saturdayButton?.isVisible = true
            openCalendarButton?.isVisible = true
            dateTextView?.isVisible = true
            refreshSheduleButton?.isVisible = true
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
            refreshSheduleButton?.isVisible = false
        }

        firebase.getFieldDiary(firebase.uidUser!!, "url", object : FirebaseCallback<String> {
            override fun onComplete(value: String) {
                if (updateShedule || updateWithoutCheck) {
                    firebase.getFieldShedule(
                        firebase.uidUser!!,
                        "weekUpdate",
                        object : FirebaseCallback<String> {
                            override fun onComplete(weekUpdate: String) {
                                if (selectedWeek != weekUpdate.toInt() || updateWithoutCheck) {
                                    diaryUrl = value
                                    when (value) {
                                        diary.elschool.url -> {
                                            diary.elschool.updateShedule(
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
                                    firebase.setFieldShedule(
                                        firebase.uidUser!!,
                                        "weekUpdate",
                                        selectedWeek
                                    )
                                    firebase.setDateUpdateShedule(
                                        calendar.get(Calendar.YEAR).toString(),
                                        (calendar.get(Calendar.MONTH) + 1).toString(),
                                        calendar.get(Calendar.DAY_OF_MONTH).toString()
                                    )
                                }
                            }
                        })
                } else {
                    updateShedule = true
                }

            }
        })
    }

    private fun openFragmentDay(day: String) {
        val bundle: Bundle = bundleOf()
        bundle.putString("title", day)
            Navigation.findNavController(requireActivity(), R.id.navFragment)
                .navigate(R.id.action_weekdayFragment_to_sheduleDayFragment, bundle)
    }


    @ExperimentalStdlibApi
    private fun setupCalendar() {
        calendarView.isVisible = false
        val selectedDate = calendarView.date
        val firebase = FunctionsFirebase()
        val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
        firebase.getDateUpdateInShedule(object : FirebaseCallback<LocalDate> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: LocalDate) {
                if (updateShedule) {
                    calendar.timeInMillis = selectedDate
                } else {
                    calendar.set(value.year, value.monthValue - 1, value.dayOfMonth)
                    calendarView.date = calendar.timeInMillis
                }
                selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                selectedYear = calendar.get(Calendar.YEAR)
                dateTextView.text = dateFormatter.format(calendar.time)
            }
        })
        openCalendarButton.setOnClickListener {
            if (!calendarView.isVisible) {
                openCalendarButton.text = "Закрыть календарь"
                calendarView.isVisible = true
            } else {
                openCalendarButton.text = "Открыть календарь"
                calendarView.isVisible = false
            }
        }
        calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dateTextView.text = dateFormatter.format(calendar.time)
            openCalendarButton.text = "Открыть календарь"
            calendarView.isVisible = false
            selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            selectedYear = calendar.get(Calendar.YEAR)

            updateShedule(updateWithoutCheck, calendar)
        }
        updateShedule(updateWithoutCheck, calendar)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeekdayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeekdayFragment()
                .apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}