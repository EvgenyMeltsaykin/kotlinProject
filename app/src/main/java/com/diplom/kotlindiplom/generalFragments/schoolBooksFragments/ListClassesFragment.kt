package com.diplom.kotlindiplom.generalFragments.schoolBooksFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.R
import kotlinx.android.synthetic.main.fragment_list_classes.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListClassesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListClassesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        activity?.title = "Выберите класс"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_classes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstClassButton.setOnClickListener {
            navigateNextFragment(1)
        }
        secondClassButton.setOnClickListener {
            navigateNextFragment(2)
        }
        thirdClassButton.setOnClickListener {
            navigateNextFragment(3)
        }
        fourethClassButton.setOnClickListener {
            navigateNextFragment(4)
        }
        fifthClassButton.setOnClickListener {
            navigateNextFragment(5)
        }
        sixthClassButton.setOnClickListener {
            navigateNextFragment(6)
        }
        seventhClassButton.setOnClickListener {
            navigateNextFragment(7)
        }
        eighthClassButton.setOnClickListener {
            navigateNextFragment(8)
        }
        ninthClassButton.setOnClickListener {
            navigateNextFragment(9)
        }
        tenthClassButton.setOnClickListener {
            navigateNextFragment(10)
        }
        eleventhClassButton.setOnClickListener {
            navigateNextFragment(11)
        }
    }

    fun navigateNextFragment(numberClass:Int){
        val bundle = bundleOf()
        bundle.putString("numberClass",numberClass.toString())
        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listClassesFragment_to_listSubjectsFragment,bundle)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListClassesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListClassesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}