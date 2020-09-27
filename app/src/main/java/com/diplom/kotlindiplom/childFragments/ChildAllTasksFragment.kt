package com.diplom.kotlindiplom.childFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Task
import com.diplom.kotlindiplom.models.recyclerViewItems.TaskItem
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
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.get("title").toString()
        }
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
        //requireActivity().invalidateOptionsMenu()
        activity?.title = title

        tasksEmptyTextView?.isVisible = false
        var status: Int = -2
        if (title == "Невыполненные") status = -1;
        if (title == "На проверке") status = 0;
        if (title == "Выполненные") status = 1;
        val adapter = GroupAdapter<ViewHolder>()
        if (status != 1) {
            firebase.getFieldUserDatabase(
                firebase.uidUser!!,
                "parentUid",
                object : Callback<String> {
                    override fun onComplete(value: String) {
                        firebase.getTasksParentUid(value, status, object : Callback<List<Task>> {
                            override fun onComplete(value: List<Task>) {
                                if (value.isEmpty()){
                                    tasksEmptyTextView?.isVisible = true
                                    //taskRecyclerViewChild.isVisible = false
                                }
                                value.forEach {
                                    adapter.add(TaskItem(it))
                                }
                                taskRecyclerViewChild?.adapter = adapter
                                adapter.setOnItemClickListener { item, view ->
                                    val taskItem = item as TaskItem
                                    val bundle: Bundle = bundleOf()
                                    bundle.putString("title", "${taskItem.task.title}")
                                    bundle.putString("taskId", "${taskItem.task.taskId}")
                                    val navController = Navigation.findNavController(
                                        requireActivity(),
                                        R.id.navFragment
                                    )
                                    navController.navigate(R.id.action_childAllTasksFragment_to_childTaskContentFragment, bundle)
                                }
                            }

                        })
                    }
                })
        }else{
            firebase.getTasksChildUid(firebase.uidUser!!,status,object : Callback<List<Task>>{
                override fun onComplete(value: List<Task>) {
                    if (value.isEmpty()){
                        tasksEmptyTextView?.isVisible = true
                       // taskRecyclerViewChild.isVisible = false
                    }
                    value.forEach {
                        adapter.add(TaskItem(it))
                    }
                    taskRecyclerViewChild?.adapter = adapter
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

}
