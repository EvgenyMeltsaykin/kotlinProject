package com.diplom.kotlindiplom.childFragments.mySchedule

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import kotlinx.android.synthetic.main.fragment_add_lesson.*
import kotlinx.android.synthetic.main.fragment_detail_lesson_my_schedule.*
import java.lang.ClassCastException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddLessonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddLessonFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var day: String = ""

    private var numberLesson: String? = null
    private var lessonName: String? = null
    private var time: String? = null
    private var cabinet: String? = null
    private var indexTab: Int = 0
    private lateinit var roleUser: String

    interface OnInputListener{
        fun sendInput(indexTab:Int)
    }
    private lateinit var onInputListener : OnInputListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            day = it.getString("day","")
            numberLesson = it.getString("numberLesson")
            lessonName = it.getString("lessonName")?.capitalize()
            time = it.getString("time")
            cabinet = it.getString("cabinet")
            indexTab = it.getInt("indexTab",0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_lesson, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onInputListener = targetFragment as OnInputListener
        val activityCallback = context as ActivityCallback
        roleUser = activityCallback.getRoleUser().toString()

    }
    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().invalidateOptionsMenu()
        lessonNameTextInput?.editText?.doAfterTextChanged {
            lessonNameTextInput?.error = null
        }
        cabinetTextInput.editText?.doAfterTextChanged {
            cabinetTextInput?.error = null
        }
        deleteLessonButton?.isVisible = false
        if (!lessonName.isNullOrBlank()){
            deleteLessonButton?.isVisible = true
            lessonNameTextInput.editText?.setText(lessonName)
            val timeBegin = time?.substringBefore("-")
            val timeEnd = time?.substringAfter("-")
            lessonBeginTimeTextView?.text = timeBegin
            lessonEndTimeTextView?.text = timeEnd
            cabinetTextInput.editText?.setText(cabinet)
            addLessonButton.text = "Сохранить"
        }
        lessonBeginTimeTextView?.setOnClickListener {
            openDatePicker(lessonBeginTimeTextView)
        }
        lessonEndTimeTextView?.setOnClickListener {
            openDatePicker(lessonEndTimeTextView)
        }

        addLessonButton?.setOnClickListener {
            val lessonName = lessonNameTextInput?.editText?.text.toString().capitalize()
            val time = lessonBeginTimeTextView?.text.toString() + "-" + lessonEndTimeTextView?.text.toString()
            val cabinet = cabinetTextInput.editText?.text.toString()
            var fl = true
            if (!validateLessonName(lessonName)) fl = false
            if (!validateCabinet(cabinet)) fl = false
            if (fl){
                val lesson = Lesson(lessonName,"",time,cabinet,"","")
                firebase.addLessonMyScheduleInFirebase(day,numberLesson!!,lesson)
                onInputListener.sendInput(indexTab)
                dismiss()
            }
        }
        deleteLessonButton?.setOnClickListener {
            val lesson = Lesson()
            firebase.addLessonMyScheduleInFirebase(day,numberLesson!!,lesson)
            onInputListener.sendInput(indexTab)
            dismiss()
        }

    }
    fun validateLessonName(lessonName : String):Boolean{
        if (lessonName.isEmpty()) {
            lessonNameTextInput?.error = resources.getString(R.string.messageEmptyField)
            return false
        }else{
            lessonNameTextInput?.error = null
            return true
        }
    }
    fun validateCabinet(cabinet : String):Boolean{
        if (cabinet.isEmpty()) {
            cabinetTextInput?.error = resources.getString(R.string.messageEmptyField)
            return false
        }else{
            cabinetTextInput?.error = null
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openDatePicker(textView: TextView){
        val hourTextView = textView.text.toString().substringBefore(":").toInt()
        val minuteTextView = textView.text.toString().substringAfter(":").toInt()
        val timepickerdialog = TimePickerDialog(
            requireContext(),
            { p0, hourOfDay, minute ->
                var time = LocalTime.of(hourOfDay,minute)
                textView.text = time.toString()
                var tempMinute = minute+45
                var tempHour = hourOfDay
                if (tempMinute>=60){
                    tempMinute-=60
                    tempHour+=1
                }
                time = LocalTime.of(tempHour,tempMinute)
                lessonEndTimeTextView?.text = time.toString()
            },
            hourTextView,
            minuteTextView,
            true
        )
        timepickerdialog.show()
    }

}