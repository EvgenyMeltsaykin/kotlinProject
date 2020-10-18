package com.diplom.kotlindiplom.generalFragments

import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.RoleDiary
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.android.synthetic.main.fragment_login_diary.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryFragment : Fragment(),
    AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters

    private lateinit var login: String
    private lateinit var urlDiary: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            login = it.getString("login","")
            urlDiary = it.getString("urlDiary","")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().invalidateOptionsMenu()


        val fm = requireActivity().supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
        activity?.title = "Электронный дневник"
        firebase.getFieldDiary(
            firebase.userUid,
            "roleDiary",
            object : Callback<String> {
                override fun onComplete(value: String) {
                    setupSpinner(value)
                    scheduleButton?.setOnClickListener {
                        if (value == "child") {
                            val bundle = bundleOf()
                            Navigation.findNavController(requireActivity(), R.id.navFragment)
                                .navigate(
                                    R.id.action_diaryFragment_to_weekdayFragment,
                                    bundle
                                )
                        }
                        if (value == "parent") {
                            Navigation.findNavController(requireActivity(), R.id.navFragment)
                                .navigate(R.id.action_diaryFragment_to_chooseChildScheduleFragment)
                        }

                    }
                    marksButton?.setOnClickListener {
                        if (value == "child") {
                            Navigation.findNavController(requireActivity(), R.id.navFragment)
                                .navigate(R.id.action_diaryFragment_to_chooseSemestrElschoolFragment)
                        }
                        if (value == "parent") {
                            Navigation.findNavController(requireActivity(), R.id.navFragment)
                                .navigate(R.id.action_diaryFragment_to_chooseChildMarkFragment)
                        }
                    }
                }
            })
        loginDiaryTextView?.text = login
        diaryTextView?.text = urlDiary
        if (urlDiary.isEmpty()){
            firebase.getFieldDiary(firebase.userUid, "url", object : Callback<String> {
                override fun onComplete(value: String) {
                    diaryTextView?.text = value
                }
            })
        }
        deleteDiaryButton?.setOnClickListener {
            firebase.deleteDiary()
            val bundle = bundleOf()
            bundle.putBoolean("deletedDiary", true)
            Navigation.findNavController(requireActivity(), R.id.navFragment)
                .navigate(R.id.action_diaryFragment_to_loginDiaryFragment, bundle)
        }
    }

    private fun setupSpinner(roleDatabase:String){
        var arrayAdapter: ArrayAdapter<String>?

        firebase.getRolesDiary(object :Callback<List<RoleDiary>>{
            override fun onComplete(value: List<RoleDiary>) {
                val listRole = mutableListOf<String>()
                var role = ""
                value.forEach {
                    if (it.roleInDatabase == roleDatabase){
                        role = it.name
                    }
                }
                listRole.add(role)
                value.forEach {
                    if (role != it.name){
                        listRole.add(it.name)
                    }
                }
                arrayAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listRole)
                arrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                roleDiarySpinner?.adapter = arrayAdapter
            }

        })
        roleDiarySpinner?.onItemSelectedListener = this
    }
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent?.getItemAtPosition(position) as String
        firebase.getRolesDiary(object :Callback<List<RoleDiary>>{
            override fun onComplete(value: List<RoleDiary>) {
                value.forEach {
                    if (it.name == item){
                        firebase.setFieldDiary(firebase.userUid,"roleDiary",it.roleInDatabase)
                        return
                    }
                }
            }
        })
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}