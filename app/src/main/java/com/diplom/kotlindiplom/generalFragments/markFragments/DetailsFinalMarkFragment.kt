package com.diplom.kotlindiplom.generalFragments.markFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_details_final_mark.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailsFinalMarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailsFinalMarkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var role: String
    private var lessonName: String? = null
    private var yearMark: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lessonName = it.getString("lessonName")
            yearMark = it.getString("yearMark")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details_final_mark, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activityCallback = context as ActivityCallback
        role = activityCallback.getRoleUser().toString()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        lessonNameTextView?.text = "Предмет: $lessonName"
        yearMarkTextView?.text = "Годовая оценка: $yearMark"

        val firebase = FunctionsFirebase()
        val detailMark = mutableListOf<String>()
        firebase.getFieldDiary(firebase.uidUser!!,"semestrName",object :FirebaseCallback<String>{
            override fun onComplete(semestrName: String) {
                firebase.getMarksLessonSemestr(role,lessonName!!,object :FirebaseCallback<List<Int>>{
                    override fun onComplete(value: List<Int>) {
                        var i = 0
                        value.forEach {
                            i++
                            detailMark.add("${semestrName.capitalize()} $i. Оценка:$it")
                        }
                        val adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,detailMark)
                        detailFinalMarkListView?.adapter = adapter
                        detailFinalMarkListView?.setOnItemClickListener { adapterView, view, position, id ->
                            val bundle = bundleOf()
                            bundle.putString("semestrNumber",(position+1).toString())
                            bundle.putString("lessonName",lessonName)
                            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailsFinalMarkFragment2_to_detailsMarksFragment,bundle)
                        }
                    }
                })
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
         * @return A new instance of fragment DetailsFinalMarkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailsFinalMarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}