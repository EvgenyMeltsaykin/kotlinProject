package com.diplom.kotlindiplom.generalFragments.scheduleFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.diplom.kotlindiplom.R
import kotlinx.android.synthetic.main.fragment_homework.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeworkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeworkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var homework: String? = null
    private var lessonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            homework = it.getString("homework")
            lessonName = it.getString("lessonName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_homework, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = lessonName
        homeworkTextView?.text = homework
    }

}