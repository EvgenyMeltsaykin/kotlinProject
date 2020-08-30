package com.diplom.kotlindiplom.generalFragments

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
import com.diplom.kotlindiplom.models.FunctionsUI
import kotlinx.android.synthetic.main.fragment_diary.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Электронный дневник"
        val firebase = FunctionsFirebase()
        firebase.getFieldUserDatabase(firebase.uidUser!!,"role",object : FirebaseCallback<String>{
            override fun onComplete(value: String) {
                scheduleButton?.setOnClickListener {
                    if (value == "child"){
                        val bundle = bundleOf()
                        bundle.putBoolean("updateSchedule",true)
                        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_diaryFragment_to_weekdayFragment,bundle)
                    }
                    if (value == "parent"){
                        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_diaryFragment_to_chooseChildScheduleFragment)
                    }

                }
                marksButton?.setOnClickListener {
                    if (value == "child"){
                        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_diaryFragment_to_chooseSemestrElschoolFragment)
                    }
                    if (value == "parent"){
                        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_diaryFragment_to_chooseChildMarkFragment)
                    }
                }
            }
        })


        firebase.getFieldDiary(firebase.uidUser!!,"url",object : FirebaseCallback<String> {
            override fun onComplete(value: String) {
                diaryTextView?.text = value
            }
        })
        deleteDiaryButton?.setOnClickListener {
            firebase.deleteDiary()
            val bundle = bundleOf()
            bundle.putBoolean("deletedDiary",true)
            Navigation.findNavController(requireActivity(), R.id.navFragment)
                .navigate(R.id.action_diaryFragment_to_loginDiaryFragment,bundle)
        }
    }

}