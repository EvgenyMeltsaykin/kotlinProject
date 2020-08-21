package com.diplom.kotlindiplom.generalFragments.markFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.LessonsMarkItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lessons_mark_item.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LessonsMarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LessonsMarkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var semestrNumber: String = "1"
    private var semestrName: String? = null
    var role = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            semestrNumber = it.getString("semestrNumber", "1")
        }
        activity?.title = "Предметы"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lessons_mark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressBar = view.findViewById<ProgressBar>(R.id.lessonsMarkProgressBar)
        val adapter = GroupAdapter<ViewHolder>()
        adapter.clear()
        val firebase = FunctionsFirebase()
        val lessonsMarkRecyclerView = view.findViewById<RecyclerView>(R.id.lessonsMarkRecyclerView)
        firebase.getLessonsFromMark(role,object : FirebaseCallback<List<String>> {
            override fun onComplete(value: List<String>) {
                value.forEach {
                    adapter.add(LessonsMarkItem(it))
                }
                lessonsMarkRecyclerView.adapter = adapter
                progressBar.isVisible = false

            }
        })
        adapter.setOnItemClickListener { item, view ->
            val bundle = bundleOf()
            val lessonName = view.lessonsNameTextView.text.toString()
            bundle.putString("lessonName", lessonName)
            bundle.putString("semestrNumber", semestrNumber)
            Navigation.findNavController(requireActivity(), R.id.navFragment).navigate(
                R.id.action_lessonsMarkFragment_to_detailsMarksFragment,
                bundle
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val activityCallback = context as ActivityCallback
        role = activityCallback.getRole()
        if (role == "child") role = "children"
        else role = "parents"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LessonsMarkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LessonsMarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}