package com.diplom.kotlindiplom.parentFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation

import com.diplom.kotlindiplom.R
import kotlinx.android.synthetic.main.fragment_parent_tasks.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ParentTasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ParentTasksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Задания"
        val navController = Navigation.findNavController(
            requireActivity(),
            R.id.navFragment
        )
        unfulfilledButtonParentTasks?.setOnClickListener {
            val bundle : Bundle = bundleOf()
            bundle.putString("title","Невыполненные")
            navController.navigate(R.id.parentAllTasksFragment,bundle)
        }
        checkButtonParentTasks?.setOnClickListener {
            val bundle : Bundle = bundleOf()
            bundle.putString("title","На проверке")
            navController.navigate(R.id.parentAllTasksFragment,bundle)
        }
        completedButtonParentTasks?.setOnClickListener {
            val bundle : Bundle = bundleOf()
            bundle.putString("title","Выполненные")
            navController.navigate(R.id.parentAllTasksFragment,bundle)
        }
        addButtonParentTasks?.setOnClickListener {
            val bundle : Bundle = bundleOf()
            bundle.putString("title","Новое задание")
            navController.navigate(R.id.parentNewTaskFragment,bundle)
        }
    }

}
