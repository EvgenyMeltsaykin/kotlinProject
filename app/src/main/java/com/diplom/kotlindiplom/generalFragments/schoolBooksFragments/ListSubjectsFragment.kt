package com.diplom.kotlindiplom.generalFragments.schoolBooksFragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.recyclerViewItems.SubjectItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_list_subjects.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListSchoolBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListSchoolBooksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var numberClass = ""
    private var param2: String? = null
    var subjects = mutableMapOf<String,String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numberClass = it.getString("numberClass","1")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_subjects, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GroupAdapter<ViewHolder>()
        activity?.title = "Выберите предмет"

        when(numberClass){
            "1" ->settingForFirstClass()
            "2" ->settingForSecondToFourthClass()
            "3" ->settingForSecondToFourthClass()
            "4" ->settingForSecondToFourthClass()
            "5" ->settingForFifthToEleventhClass()
            "6" ->settingForFifthToEleventhClass()
            "7" ->settingForFifthToEleventhClass()
            "8" ->settingForFifthToEleventhClass()
            "9" ->settingForFifthToEleventhClass()
            "10" ->settingForFifthToEleventhClass()
            "11" ->settingForFifthToEleventhClass()
        }
        subjects.forEach{ (subjectInFirebase, subjectName) ->
            adapter.add(SubjectItem(subjectName,subjectInFirebase))
        }
        subjectsRecyclerView?.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val itemSub = item as SubjectItem
            navigateNextFragment(itemSub.subjectInFirebase)
        }
    }
    fun settingForFifthToEleventhClass(){
        subjects["maths"] = "Алгебра"
        subjects["geometry"] = resources.getString(R.string.geometry)
        subjects["russianLanguage"] = resources.getString(R.string.russianLanguage)
        subjects["nativeLanguage"] = resources.getString(R.string.nativeLanguage)
        subjects["literature"] = resources.getString(R.string.literature)
        subjects["history"] = resources.getString(R.string.history)
        subjects["geography"] = resources.getString(R.string.geography)
        subjects["biology"] = resources.getString(R.string.biology)
        subjects["socialScience"] = resources.getString(R.string.socialScience)
        subjects["englishLanguage"] = resources.getString(R.string.englishLanguage)
        subjects["germanLanguage"] = resources.getString(R.string.germanLanguage)
        subjects["frenchLanguage"] = resources.getString(R.string.frenchLanguage)
        subjects["physics"] = resources.getString(R.string.physics)
        subjects["chemistry"] = resources.getString(R.string.chemistry)
        subjects["computerScience"] = resources.getString(R.string.computerScience)
    }
    fun settingForSecondToFourthClass(){
        settingForFirstClass()
        subjects["englishLanguage"] = resources.getString(R.string.englishLanguage)
        subjects["germanLanguage"] = resources.getString(R.string.germanLanguage)
        subjects["frenchLanguage"] = resources.getString(R.string.frenchLanguage)
    }
    fun settingForFirstClass() {
        subjects["maths"] = resources.getString(R.string.maths)
        subjects["russianLanguage"] = resources.getString(R.string.russianLanguage)
        subjects["nativeLanguage"] = resources.getString(R.string.nativeLanguage)
        subjects["surroundingWorld"] = resources.getString(R.string.surroundingWorld)
        subjects["literaryReading"] = resources.getString(R.string.literaryReading)

    }
    fun navigateNextFragment(subjectName:String){
        val bundle = bundleOf()
        bundle.putString("subjectName",subjectName)
        bundle.putString("numberClass",numberClass)
        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.schoolBooksFragment,bundle)
    }

}