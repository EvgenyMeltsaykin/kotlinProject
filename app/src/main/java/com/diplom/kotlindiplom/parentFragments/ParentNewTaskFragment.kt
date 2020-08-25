package com.diplom.kotlindiplom.parentFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation

import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_parent_new_task.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewTaskFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.get("title").toString()
            activity?.setTitle(param1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_new_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        val navController = Navigation.findNavController(requireActivity(),R.id.navFragment)
        val firebase = FunctionsFirebase()
        val time = Calendar.getInstance().time
        val currentTime = time.toLocaleString()
        addTaskButtonParent.setOnClickListener {
            if (titleTaskEditTextParent?.text?.isEmpty()!!){
                Toast.makeText(requireContext(),"Заполните поле название",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (costTaskEditTextParent?.text?.isEmpty()!!){
                Toast.makeText(requireContext(),"Заполните поле стоимость",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            firebase.addNewTaskDatabase(
                requireContext(),
                titleTaskEditTextParent?.text.toString(),
                descriptionTaskEditTextParent?.text.toString(),
                costTaskEditTextParent?.text.toString().toInt(),
                firebase.uidUser.toString(),
                currentTime.toString()
            )
            navController.popBackStack()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewTaskFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewTaskFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
