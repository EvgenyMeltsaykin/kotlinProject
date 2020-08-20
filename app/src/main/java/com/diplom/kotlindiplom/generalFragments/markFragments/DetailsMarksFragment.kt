package com.diplom.kotlindiplom.generalFragments.markFragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_details_marks.*
import org.decimal4j.util.DoubleRounder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailsMarksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailsMarksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var semestrNumber: String =""
    private var lessonName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            semestrNumber = it.getString("semestrNumber","Ошибка при загрузке")
            lessonName = it.getString("lessonName","Ошибка при загрузке")
        }
        activity?.title ="Оценки"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details_marks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebase = FunctionsFirebase()
        semestrNumberTextView.text = "Семестр: $semestrNumber"
        lessonNameTextView.text = "Предмет: $lessonName"
        firebase.getDetailsMarks(lessonName,semestrNumber,object : FirebaseCallback<Map<String,String>>{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onComplete(value: Map<String, String>) {
                Log.d("Tag",value.toString())
                val middleMark = getMiddleMark(value)
                middleMarkTextView.text = "Средний балл: ${DoubleRounder.round(middleMark.toDouble(),2)}"
                val detailMark = mutableListOf<String>()
                value.forEach { s, s1 ->
                    detailMark.add("Дата урока:$s.Оценка: $s1")
                }
                val adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,detailMark)
                dateMarkListView.adapter = adapter
            }
        })


    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun getMiddleMark(detailsMarks : Map<String,String>) : Float{
        var sum = 0f
        var countMark = 0
        detailsMarks.forEach { s, s1 ->
            if (s1.isDigitsOnly()){
                sum+=s1.toInt()
                countMark++
            }

        }
        return sum/countMark
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailsMarksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailsMarksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}