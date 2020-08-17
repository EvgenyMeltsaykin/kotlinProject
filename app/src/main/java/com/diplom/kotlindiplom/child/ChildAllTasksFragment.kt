package com.diplom.kotlindiplom.child

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Task
import com.diplom.kotlindiplom.models.TaskItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_child_all_tasks.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChildAllTasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildAllTasksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.get("title").toString()
        }
        activity?.setTitle(param1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_child_all_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        var status: Int = -2
        if (param1 == "Невыполненные") status = -1;
        if (param1 == "На проверке") status = 0;
        if (param1 == "Выполненные") status = 1;

        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        if (status != 1) {
            firebase.getFieldUserDatabase(
                firebase.uidUser!!,
                "parentUid",
                object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        firebase.getTasksParentUid(value, status, object : FirebaseCallback<List<Task>> {
                            override fun onComplete(value: List<Task>) {
                                value.forEach {
                                    adapter.add(TaskItem(it))
                                }
                                taskRecyclerViewChild.adapter = adapter
                                adapter.setOnItemClickListener { item, view ->
                                    val taskItem = item as TaskItem
                                    val bundle: Bundle = bundleOf()
                                    bundle.putString("title", "${taskItem.task.title}")
                                    bundle.putString("taskId", "${taskItem.task.taskId}")
                                    val navController = Navigation.findNavController(
                                        requireActivity(),
                                        R.id.navFragment
                                    )
                                    navController.navigate(R.id.childTaskContentFragment, bundle)
                                }
                            }

                        })
                    }
                })
        }else{
            firebase.getTasksChildUid(firebase.uidUser!!,status,object : FirebaseCallback<List<Task>>{
                override fun onComplete(value: List<Task>) {
                    value.forEach {
                        adapter.add(TaskItem(it))
                    }
                    taskRecyclerViewChild.adapter = adapter
                    adapter.setOnItemClickListener { item, view ->
                        val taskItem = item as TaskItem
                        val bundle: Bundle = bundleOf()
                        bundle.putString("title", "${taskItem.task.title}")
                        bundle.putString("taskId", "${taskItem.task.taskId}")
                        val navController = Navigation.findNavController(
                            requireActivity(),
                            R.id.navFragment
                        )
                        navController.navigate(R.id.childTaskContentFragment, bundle)
                    }
                }
            })
        }
    }

    companion object {
        /**
         * Use this factory method to create a nw instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChildAllTasksFragment.
         */
        val TAG = ChildAllTasksFragment::class.java.simpleName

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChildAllTasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
