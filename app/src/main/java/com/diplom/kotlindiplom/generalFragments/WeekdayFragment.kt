package com.diplom.kotlindiplom.generalFragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
    private var param2: String? = null
    var role: String = ""
    var selectedWeek = 0
    var selectedYear = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Дни недели"
        arguments?.let {
            updateShedule = it.getBoolean("updateShedule", true)
        }
        val firebase = FunctionsFirebase()

        firebase.getRoleByUid(firebase.uidUser!!, object : FirebaseCallback<String> {
            override fun onComplete(value: String) {
                role = value
            }
        })
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
        val firebase = FunctionsFirebase()
        deleteDiaryButton.setOnClickListener {
            firebase.deleteDiary()
            if (role == "child") {
                Navigation.findNavController(requireActivity(), R.id.navFragment)
                    .navigate(R.id.action_weekdayFragment_to_weekdayWithoutDiaryFragment)
            if(role == "parent")
                Navigation.findNavController(requireActivity(), R.id.navFragment)
                    .navigate(R.id.action_weekdayFragment_to_weekdayWithoutDiaryFragment)
            }
        }
        progressBar.isVisible = false
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
    private fun updateShedule(calendar: Calendar) {
        val firebase = FunctionsFirebase()
        val showButtons = {
            mondayButton.isVisible = true
            tuesdayButton.isVisible = true
            mondayButton.isVisible = true
            wednesdayButton.isVisible = true
            thursdayButton.isVisible = true
            fridayButton.isVisible = true
            saturdayButton.isVisible = true
            deleteDiaryButton.isVisible = true
            openCalendarButton.isVisible = true
            dateTextView.isVisible = true
        }
        val hideButtons ={
            mondayButton.isVisible = false
            tuesdayButton.isVisible = false
            mondayButton.isVisible = false
            wednesdayButton.isVisible = false
            thursdayButton.isVisible = false
            fridayButton.isVisible = false
            saturdayButton.isVisible = false
            deleteDiaryButton.isVisible = false
            openCalendarButton.isVisible = false
            dateTextView.isVisible = false
        }
        firebase.getFieldDiary(firebase.uidUser!!, "url", object : FirebaseCallback<String> {
            override fun onComplete(value: String) {
                diaryTextView.text = value
                if (updateShedule) {
                    firebase.getFieldShedule(
                        firebase.uidUser!!,
                        "weekUpdate",
                        object : FirebaseCallback<String> {
                            override fun onComplete(answer: String) {
                                if (selectedWeek != answer.toInt()) {
                                    diaryUrl = value
                                    when (value) {
                                        diary.elschool.url -> {
                                            diary.elschool.updateShedule(
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
                                        (calendar.get(Calendar.MONTH)+1).toString(),
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
        val calendar = Calendar.getInstance()
        val firebase = FunctionsFirebase()
        val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
        firebase.getDateUpdateInShedule(object : FirebaseCallback<LocalDate> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: LocalDate) {
                if (updateShedule) {
                    calendar.timeInMillis = selectedDate
                }else{
                    calendar.set(value.year,value.monthValue-1,value.dayOfMonth)
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
            diaryUrl = diaryTextView.text.toString()
            updateShedule(calendar)
        }
        updateShedule(calendar)
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
            WeekdayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}