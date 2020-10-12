package com.diplom.kotlindiplom.childFragments

import android.net.Uri
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
import kotlinx.android.synthetic.main.fragment_child_tasks.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChildTasksFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChildTasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildTasksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_child_tasks, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Задания"
        firebase.getFieldUserDatabase(firebase.userUid,"parentUid",object :Callback<String>{
            override fun onComplete(value: String) {
                childTasksProgressBar?.isVisible = false
                if (value.isEmpty()){
                    emptyParentuidTextView?.isVisible = true
                    unfulfilledButtonChildTasks?.isVisible = false
                    completedButtonChildTasks?.isVisible = false
                    checkButtonChildTasks?.isVisible = false
                }else{
                    unfulfilledButtonChildTasks?.isVisible = true
                    completedButtonChildTasks?.isVisible = true
                    checkButtonChildTasks?.isVisible = true
                }
            }
        })
        unfulfilledButtonChildTasks?.setOnClickListener {
            navigateNextFragment("Невыполненные")
        }
        completedButtonChildTasks?.setOnClickListener {
            navigateNextFragment("Выполненные")
        }
        checkButtonChildTasks?.setOnClickListener {
            navigateNextFragment("На проверке")
        }
        additionalButtonChildTasks?.setOnClickListener {
            navigateNextFragment("Дополнительные")
        }
    }

    private fun navigateNextFragment(title:String){
        val bundle : Bundle = bundleOf()
        bundle.putString("title",title)
        Navigation.findNavController(requireActivity(), R.id.navFragment).navigate(R.id.action_childTasksFragment_to_childAllTasksFragment,bundle)
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
