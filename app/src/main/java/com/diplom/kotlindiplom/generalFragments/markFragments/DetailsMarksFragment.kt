package com.diplom.kotlindiplom.generalFragments.markFragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_details_marks.*

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
        //requireActivity().invalidateOptionsMenu()
        activity?.title ="Оценки"
        val detailMark = mutableListOf<String>()
        semestrNumberTextView?.isVisible = false
        lessonNameTextView?.isVisible = false
        middleMarkTextView?.isVisible = false

        firebase.getFieldDiary(firebase.uidUser!!,"semestrName",object : Callback<String>{
            override fun onComplete(value: String) {
                semestrNumberTextView?.text = "${value.capitalize()}: $semestrNumber"
            }
        })
        firebase.getMiddleMark(lessonName,semestrNumber,object :Callback<String>{
            override fun onComplete(value: String) {
                middleMarkTextView?.text = "Средний балл: $value"
            }
        })
        lessonNameTextView?.text = "Предмет: $lessonName"
        firebase.getDetailsMarks(lessonName,semestrNumber,object : Callback<Map<String,String>>{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onComplete(value: Map<String, String>) {
                value.forEach { (s, s1) ->
                    if (s.isNotEmpty())
                        detailMark.add("Дата урока:$s.Оценка: $s1")
                }
                val adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,detailMark)
                dateMarkListView?.adapter = adapter
                detailsMarkProgressBar?.isVisible = false
                semestrNumberTextView?.isVisible = true
                lessonNameTextView?.isVisible = true
                middleMarkTextView?.isVisible = true
            }
        })
        plotGraphButton?.setOnClickListener {
            val bundle = bundleOf()
            bundle.putString("lessonName",lessonName)
            bundle.putString("semestrNumber",semestrNumber)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailsMarksFragment_to_graphMarksFragment,bundle)
        }
    }
}