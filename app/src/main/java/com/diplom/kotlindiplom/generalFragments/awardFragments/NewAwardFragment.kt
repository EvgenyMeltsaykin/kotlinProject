package com.diplom.kotlindiplom.generalFragments.awardFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_new_award.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewAwardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewAwardFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_new_award, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Новое вознаграждение"

        addNewAwardButton?.setOnClickListener {
            val nameAward = nameAwardEditText?.text.toString()
            val costAward = costEditText?.text.toString()
            if (nameAward.isEmpty() || costAward.isEmpty()){
                Toast.makeText(requireContext(),"Заполните все поля",Toast.LENGTH_SHORT).show()
            }else{
                val firebase = FunctionsFirebase()
                firebase.addAwardInFirebase(nameAward,costAward)
                Toast.makeText(requireContext(),"Вознаграждение добавлено",Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_newAwardFragment_to_listAwardsFragment)
            }

        }
    }
}