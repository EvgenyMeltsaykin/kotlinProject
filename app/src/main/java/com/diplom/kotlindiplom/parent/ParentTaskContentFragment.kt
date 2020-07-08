package com.diplom.kotlindiplom.parent

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
import kotlinx.android.synthetic.main.fragment_parent_task_content.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ParentTaskContentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ParentTaskContentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.get("title").toString()
            activity?.setTitle(title)
            taskId = it.get("taskId").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_task_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()

        val navController = Navigation.findNavController(requireActivity(),R.id.navFragmentParent)
        val firebase = FunctionsFirebase()
        firebase.getTask(taskId.toString(), object: FirebaseCallback<Task>{
            override fun onComplete(value: Task) {
                titleTaskContentTextViewParent.setText(value.title)
                costTaskContentTextViewParent.setText("Стоимость: " + value.cost.toString())
                descriptionTaskContentTextViewParent.setText(value.description)
                if (value.status == -1) statusContentTastTextViewParent.setText("Не выполнено")
                if (value.status == 0){
                    statusContentTastTextViewParent.setText("На проверке")
                    acceptContentTaskButtonParent.isVisible = true
                    rejectContentTaskButtonParent.isVisible = true
                }
                if (value.status == 1) statusContentTastTextViewParent.setText("Выполнено")
            }
        })
        acceptContentTaskButtonParent.setOnClickListener {
            firebase.setFieldDatabaseTask(taskId.toString(),"status",1)
            firebase.setFieldDatabaseTask(taskId.toString(),"showNotification",0)
            Toast.makeText(requireContext(),"Задание принято",Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            requireActivity().setTitle("На проверке")
            firebase.getTask(taskId.toString(), object : FirebaseCallback<Task>{
                override fun onComplete(value: Task) {
                    Log.d("TAG",value.childUid)
                    firebase.addPointChild(value.childUid,value.cost)
                }

            })

        }
        rejectContentTaskButtonParent.setOnClickListener {
            firebase.setFieldDatabaseTask(taskId.toString(),"status",-1)
            firebase.setFieldDatabaseTask(taskId.toString(),"showNotification",2)
            firebase.setFieldDatabaseTask(taskId.toString(),"childUid","")
            Toast.makeText(requireContext(),"Задание не принято",Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            requireActivity().setTitle("На проверке")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ParentTaskContentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ParentTaskContentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
