package com.diplom.kotlindiplom.generalFragments.awardFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.MainActivity
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail_award.*
import kotlinx.android.synthetic.main.fragment_list_awards.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailAwardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailAwardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var awardId: String
    private lateinit var nameAward: String
    private lateinit var costAward: String
    private var status: Int = 1
    private lateinit var role:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            awardId = it.getString("awardId","")
            nameAward = it.getString("nameAward","")
            costAward = it.getString("costAward","")
            status = it.getInt("status",1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_award, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activityCallback = context as ActivityCallback
        role = activityCallback.getRoleUser().toString()
        if (role == "child") role = "children"
        else role = "parents"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Вознаграждение"
        childTakeAwardTextView?.isVisible = false
        val firebase = FunctionsFirebase()
        awardNameTextView?.text = "Вознаграждение: $nameAward"
        costAwardTxtView?.text = "Стоимость: $costAward"
        if (role == "children"){
            deleteButton?.isVisible = false
            if (status == 1){
                messageForChildTextView?.isVisible = true
                takeButton?.text = "Получено"
            }

        }
        if (role == "parents"){
            takeButton?.isVisible = false
            if (status != 0){
                deleteButton?.isVisible = false
            }
            if (status == 1){
                childTakeAwardTextView?.isVisible = true
            }

        }
        deleteButton?.setOnClickListener {
            Toast.makeText(requireContext(),"Успешно",Toast.LENGTH_SHORT).show()
            firebase.deleteAward(awardId)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailAwardFragment_to_listAwardsFragment)
        }
        takeButton?.setOnClickListener {
            if (takeButton?.text == "Забрать"){
                firebase.getFieldUserDatabase(
                    firebase.uidUser!!,
                    "point",
                    object : FirebaseCallback<String> {
                        override fun onComplete(value: String) {
                            if (value.toInt()>= costAward.toInt()){
                                Toast.makeText(requireContext(),"Успешно",Toast.LENGTH_SHORT).show()
                                firebase.setFieldAward(awardId,"status","1")
                                firebase.setFieldAward(awardId,"showNotification","1")
                                firebase.setFieldAward(awardId,"childUid",firebase.uidUser)
                                firebase.setFieldUserDatabase(firebase.uidUser!!,"point",value.toInt() - costAward.toInt())
                                Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailAwardFragment_to_listAwardsFragment)
                            }else{
                                Toast.makeText(requireContext(),"У Вас не хватает баллов",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            }else {
                Toast.makeText(requireContext(), "Успешно", Toast.LENGTH_SHORT).show()
                firebase.deleteAward(awardId)
                Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailAwardFragment_to_listAwardsFragment)
            }



        }

    }

}