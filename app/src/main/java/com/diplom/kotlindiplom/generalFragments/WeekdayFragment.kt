package com.diplom.kotlindiplom.generalFragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_weekday.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DateFormat
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
    private var param1: String? = null
    private var param2: String? = null
    var urlDiary: String = ""
    var role : String = ""
    var selectedWeek = 0
    var selectedYear = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Дни недели"
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
        return inflater.inflate(R.layout.fragment_weekday, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        updateShedule()


        val firebase = FunctionsFirebase()
        firebase.getFieldDiary(firebase.uidUser!!,"url",object : FirebaseCallback<String>{
            override fun onComplete(value: String) {
                diaryTextView.text = "Электронный дневник: ${value}"
            }
        })
        deleteDiaryButton.setOnClickListener {
            firebase.setFieldDatabase(firebase.uidUser!!,"diary/login","")
            firebase.setFieldDatabase(firebase.uidUser!!,"diary/password","")
            firebase.setFieldDatabase(firebase.uidUser!!,"diary/url","")
            firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule","")
            firebase.getRoleByUid(firebase.uidUser!!,object : FirebaseCallback<String>{
                override fun onComplete(value: String) {
                    if(value == "child"){
                        Navigation.findNavController(requireActivity(), R.id.navFragmentChild)
                            .navigate(R.id.action_weekdayFragment_to_weekdayWithoutDiaryFragment)
                    }else{
                        Navigation.findNavController(requireActivity(), R.id.navFragmentParent)
                            .navigate(R.id.action_weekdayFragment_to_weekdayWithoutDiaryFragment)
                    }
                }
            })
        }

    }

    private fun updateShedule(){
        val diary = Diary()
        val firebase = FunctionsFirebase()
        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule","")
        GlobalScope.launch(Dispatchers.IO){
            diary.elschool.getShedule(selectedYear,selectedWeek,object : FirebaseCallback<MutableMap<String, List<String>>>{
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: MutableMap<String, List<String>>) {
                    var i = 0
                    value.forEach { s, list ->
                        i = 0
                        list.forEach {
                            i++
                            firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$s/lesson$i/lessonName",it)
                        }

                    }

                }
            })
        }
    }

    private fun setupCalendar(){
        calendarView.isVisible = false

        val selectedDate = calendarView.date
        val calendar = Calendar.getInstance()
        selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        selectedYear = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = selectedDate
        val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
        dateTextView.text = dateFormatter.format(calendar.time)

        openCalendarButton.setOnClickListener {
            if (!calendarView.isVisible ){
                openCalendarButton.text = "Закрыть календарь"
                calendarView.isVisible = true
            }else{
                openCalendarButton.text = "Открыть календарь"
                calendarView.isVisible = false
            }
        }
        calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            calendar.set(year,month,dayOfMonth)
            dateTextView.text = dateFormatter.format(calendar.time)
            openCalendarButton.text = "Открыть календарь"
            calendarView.isVisible  = false
            selectedWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            selectedYear = calendar.get(Calendar.YEAR)
            updateShedule()

        }
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