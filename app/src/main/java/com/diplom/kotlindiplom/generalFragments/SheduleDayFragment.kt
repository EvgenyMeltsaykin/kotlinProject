package com.diplom.kotlindiplom.generalFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_shedule_day.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SheduleDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SheduleDayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    var role = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.get("title").toString()
        }
        activity?.title = title
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
        return inflater.inflate(R.layout.fragment_shedule_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageTextView.isVisible  =false
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()

        firebase.getFieldSheduleDay(firebase.uidUser!!,"${title.toString().toLowerCase()}",object : FirebaseCallback<String>{
            override fun onComplete(value: String) {
                activity?.title = "${activity?.title} $value"
            }
        })
        adapter.clear()
        firebase.getSheduleDay(firebase.uidUser!!, title.toString().toLowerCase(Locale.ROOT),object : FirebaseCallback<List<Lesson>>{
            override fun onComplete(value: List<Lesson>) {
                var fl = true
                value.forEach {
                    if (it.name.isNotEmpty()){
                        adapter.add(LessonItem(it))
                        fl = false
                    }
                }
                if (fl)messageTextView.isVisible  =true
                lessonsRecyclerView.adapter = adapter
            }
        })

        adapter.setOnItemClickListener { item, view ->
            val lessonItem = item as Lesson
            val bundle = bundleOf()
            bundle.putString("homework",lessonItem.homework)
            if(role == "child"){
                Navigation.findNavController(requireActivity(),R.id.navFragmentChild).navigate(R.id.homeworkFragment)
            }
            if (role == "parent"){
                Navigation.findNavController(requireActivity(),R.id.navFragmentParent).navigate(R.id.homeworkFragment)
            }
        }




    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SheduleDayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SheduleDayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}