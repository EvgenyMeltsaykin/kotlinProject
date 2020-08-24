package com.diplom.kotlindiplom.childFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Task
import kotlinx.android.synthetic.main.fragment_child_task_content.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChildTaskContentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildTaskContentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.get("title").toString()
            activity?.title = title
            taskId = it.get("taskId").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_child_task_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = title
        val navController = Navigation.findNavController(requireActivity(), R.id.navFragment)
        val firebase = FunctionsFirebase()
        firebase.getTask(taskId.toString(), object : FirebaseCallback<Task> {
            override fun onComplete(value: Task) {
                titleContentTaskTextViewChild.setText(value.title)
                costContentTaskTextViewChild.setText("Стоимость: " + value.cost.toString())
                descriptionTaskContentTextViewChild.setText(value.description)
                if (value.status == -1) {
                    statusContentTaskTextViewChild.setText("Не выполнено")
                    sendTaskButtonChild.isVisible = true
                }
                if (value.status == 0) {
                    statusContentTaskTextViewChild.setText("На проверке")
                }
                if (value.status == 1) statusContentTaskTextViewChild.setText("Выполнено")
            }
        })
        sendTaskButtonChild.setOnClickListener {
            firebase.setFieldDatabaseTask(taskId.toString(),"status",0)
            firebase.setFieldDatabaseTask(taskId.toString(),"showNotification",0)
            firebase.setFieldDatabaseTask(taskId.toString(),"childUid",firebase.uidUser!!)
            Toast.makeText(requireContext(),"Задание отправлено на проверку", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            requireActivity().setTitle("Невыполненные")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChildTaskContentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChildTaskContentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
