package com.diplom.kotlindiplom.generalFragments.markFragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_choose_semestr.*
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChooseSemestrElschoolFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChooseSemestrElschoolFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var idChild: String = ""
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idChild = it.getString("idChild","")
        }
        activity?.title = "Выберите"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_semestr, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = bundleOf()
        val firebase = FunctionsFirebase()
        progressBar.isVisible = false
        val firstSemestrButton = view.findViewById<Button>(R.id.firstSemestrButton)
        val secondSemestrButton = view.findViewById<Button>(R.id.secondSemestrButton)
        val thirdSemestrButton = view.findViewById<Button>(R.id.thirdSemestrButton)
        val refreshMarkButton = view.findViewById<Button>(R.id.refreshMarkButton)
        val dateUpdateTextView = view.findViewById<TextView>(R.id.dateUpdateTextView)
        firebase.getFieldMarks("dateUpdate",object :FirebaseCallback<String>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: String) {
                dateUpdateTextView.text = "Дата обновления: $value"
                if (value.isEmpty() || idChild.isNotEmpty()){
                    dateUpdateTextView.text = "Дата обновления: ${LocalDate.now()}"
                    refreshMarks()
                }
            }
        })

        firebase.getFieldDiary(firebase.uidUser!!,"semestrName",object :FirebaseCallback<String>{
            override fun onComplete(value: String) {
                firstSemestrButton?.text = "Первый $value"
                secondSemestrButton?.text = "Второй $value"
                thirdSemestrButton?.text = "Третий $value"
                activity?.title = "Выберите $value"
            }
        })

        refreshMarkButton.setOnClickListener {
            refreshMarks()
        }
        firstSemestrButton.setOnClickListener {
            bundle.putString("semestrNumber","1")
            navigateToLessons(requireActivity(),bundle)
        }
        secondSemestrButton.setOnClickListener {
            bundle.putString("semestrNumber","2")
            navigateToLessons(requireActivity(),bundle)
        }
        thirdSemestrButton.setOnClickListener {
            bundle.putString("semestrNumber","3")
            navigateToLessons(requireActivity(),bundle)
        }
    }
    @ExperimentalStdlibApi
    fun refreshMarks(){
        val firebase = FunctionsFirebase()
        val firstSemestrButton = view?.findViewById<Button>(R.id.firstSemestrButton)
        val secondSemestrButton = view?.findViewById<Button>(R.id.secondSemestrButton)
        val thirdSemestrButton = view?.findViewById<Button>(R.id.thirdSemestrButton)
        val refreshMarkButton = view?.findViewById<Button>(R.id.refreshMarkButton)

        val hideButtons = {
            refreshMarkButton?.isVisible = false
            firstSemestrButton?.isVisible = false
            secondSemestrButton?.isVisible = false
            thirdSemestrButton?.isVisible = false
        }
        val showButtons = {
            refreshMarkButton?.isVisible = true
            firstSemestrButton?.isVisible = true
            secondSemestrButton?.isVisible = true
            thirdSemestrButton?.isVisible = true
        }
        firebase.getFieldDiary(firebase.uidUser!!,"url",object :FirebaseCallback<String>{
            override fun onComplete(url: String) {
                firebase.getFieldDiary(firebase.uidUser!!,"idChild",object :FirebaseCallback<String>{
                    override fun onComplete(value: String) {
                        if (value != idChild) {
                            val diary = Diary()
                            when (url) {
                                diary.elschool.url -> {
                                    diary.elschool.getMarks(
                                        value,
                                        requireContext(),
                                        progressBar,
                                        hideButtons,
                                        showButtons
                                    )
                                }
                            }
                        }
                    }
                })

            }
        })
    }

    fun navigateToLessons(activity: Activity,bundle: Bundle){
        Navigation.findNavController(activity,R.id.navFragment).navigate(R.id.action_chooseSemestrElschoolFragment_to_lessonsMarkFragment,bundle)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChooseSemestrElschoolFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChooseSemestrElschoolFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}