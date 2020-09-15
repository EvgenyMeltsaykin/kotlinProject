package com.diplom.kotlindiplom.generalFragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.BaseFragment
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.childFragments.RequestParentFragment
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_main.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MainFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private lateinit var role:String
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val activityCallback = context as ActivityCallback
        role = activityCallback.getRoleUser()!!
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (role == "child"){
            return inflater.inflate(R.layout.fragment_main, container, false)
        }
        if (role == "parent"){
            return inflater.inflate(R.layout.fragment_login_diary, container, false)
        }
        return null
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Главная"
        if (role == "child"){
            setupChildFragment()
        }
    }
    fun setupChildFragment(){
        val firebase = FunctionsFirebase()
        firebase.getFieldUserDatabase(firebase.uidUser!!,"acceptName",object :FirebaseCallback<String>{
            override fun onComplete(parentName: String) {
                if (parentName.isNotEmpty()){
                    val requestParentFragment = RequestParentFragment()
                    val bundle = bundleOf()
                    bundle.putString("parentName",parentName)
                    requestParentFragment.arguments = bundle
                    requestParentFragment.show(requireActivity().supportFragmentManager,"requestParentFragment")
                }

            }
        })
        mondayMainButton?.setOnClickListener {
            navigateToDay("понедельник")
        }
        tuesdayMainButton?.setOnClickListener {
            navigateToDay("вторник")
        }
        wednesdayMainButton?.setOnClickListener {
            navigateToDay("среда")
        }
        thursdayMainButton?.setOnClickListener {
            navigateToDay("четверг")
        }
        fridayMainButton?.setOnClickListener {
            navigateToDay("пятница")
        }
        saturdayMainButton?.setOnClickListener {
            navigateToDay("суббота")
        }
    }
    fun navigateToDay(day : String){
        val bundle = bundleOf()
        bundle.putString("day",day)
        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_mainFragment_to_myScheduleDayFragment,bundle)
    }

}
