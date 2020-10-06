package com.diplom.kotlindiplom.generalFragments.markFragments

import android.app.Activity
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
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_choose_semestr.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

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
    private var finalGrades = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idChild = it.getString("idChild", "")
            finalGrades = it.getBoolean("finalGrades", false)
        }
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
        //requireActivity().invalidateOptionsMenu()
        activity?.title = "Выберите"
        val bundle = bundleOf()
        progressBar?.isVisible = false
        firebase.getFieldMarks("dateUpdate", object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: String) {
                dateUpdateTextView?.text = "Дата обновления: $value"
                if (value.isEmpty() || idChild.isNotEmpty()) {
                    refreshMarks()
                }
            }
        })

        firebase.getFieldDiary(
            firebase.uidUser!!,
            "semestrName",
            object : Callback<String> {
                override fun onComplete(value: String) {
                    firstSemestrButton?.text = "Первый $value"
                    secondSemestrButton?.text = "Второй $value"
                    thirdSemestrButton?.text = "Третий $value"
                    activity?.title = "Выберите $value"
                }
            })

        refreshMarkButton?.setOnClickListener {
            refreshMarks()
        }
        firstSemestrButton?.setOnClickListener {
            bundle.putString("semestrNumber", "1")
            navigateToLessons(requireActivity(), bundle)
        }
        secondSemestrButton?.setOnClickListener {
            bundle.putString("semestrNumber", "2")
            navigateToLessons(requireActivity(), bundle)
        }
        thirdSemestrButton?.setOnClickListener {
            bundle.putString("semestrNumber", "3")
            navigateToLessons(requireActivity(), bundle)
        }
        yearsMarksButton?.setOnClickListener {
            bundle.putString("semestrNumber", "0")
            bundle.putBoolean("finalGrades",true)
            navigateToLessons(requireActivity(), bundle)
        }

    }

    @ExperimentalStdlibApi
    fun refreshMarks() {
        val nowDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(nowDate)
        dateUpdateTextView?.text = "Дата обновления: $formatter"
        val hideButtons = {
            refreshMarkButton?.isVisible = false
            firstSemestrButton?.isVisible = false
            secondSemestrButton?.isVisible = false
            thirdSemestrButton?.isVisible = false
            yearsMarksButton?.isVisible = false
        }
        val showButtons = {
            refreshMarkButton?.isVisible = true
            firstSemestrButton?.isVisible = true
            secondSemestrButton?.isVisible = true
            thirdSemestrButton?.isVisible = true
            yearsMarksButton?.isVisible = true
        }
        firebase.getFieldDiary(firebase.uidUser!!, "url", object : Callback<String> {
            override fun onComplete(url: String) {
                firebase.getFieldDiary(
                    firebase.uidUser!!,
                    "idChild",
                    object : Callback<String> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onComplete(valueIdChild: String) {
                            if (valueIdChild != idChild) {
                                val diary = Diary()
                                when (url) {
                                    diary.elschool.url -> {
                                        diary.elschool.getMarks(
                                            valueIdChild,
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

    fun navigateToLessons(activity: Activity, bundle: Bundle) {
        Navigation.findNavController(activity, R.id.navFragment)
            .navigate(R.id.action_chooseSemestrElschoolFragment_to_lessonsMarkFragment, bundle)
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