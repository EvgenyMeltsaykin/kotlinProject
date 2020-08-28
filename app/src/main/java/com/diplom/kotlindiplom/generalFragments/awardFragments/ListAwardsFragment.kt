package com.diplom.kotlindiplom.generalFragments.awardFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.recyclerViewItems.AwardItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_list_awards.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListAwardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListAwardsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var role : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activityCallback = context as ActivityCallback
        role = activityCallback.getRoleUser().toString()
        if (role == "child") role = "children"
        else role = "parents"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_awards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Вознаграждение"
        emptyListAwardTextView?.isVisible = false
        if (role == "children"){
            addAwardButton?.isVisible = false
        }
        addAwardButton?.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listAwardsFragment_to_newAwardFragment)
        }

        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        firebase.getFieldUserDatabase(firebase.uidUser!!,"parentUid",object :FirebaseCallback<String>{
            override fun onComplete(value: String) {
                firebase.getAwardOutFirebaseWithParentUid(value,object :FirebaseCallback<Map<String,String>>{
                    override fun onComplete(value: Map<String, String>) {
                        listAwardProgressBar?.isVisible = false
                        if (value.isEmpty()){
                            if (role == "children"){
                                emptyListAwardTextView?.text = "Родители ещё не добавили вознаграждения"
                            }
                            if (role == "parents"){
                                emptyListAwardTextView?.text = "Чтобы у ребенка была дополнительная мотивация делать задания, добавляйте ему вознаграждение. Это очень просто! Просто нажмите на кнопку добавить"
                            }
                            emptyListAwardTextView?.isVisible = true
                            return
                        }else{
                            val nameAward = value["name"]
                            val costAward = value["cost"]
                            val awardId = value["awardId"]
                            adapter.add(AwardItem(nameAward!!,costAward!!,awardId!!))
                            listAwardRecyclerView?.adapter = adapter
                        }
                    }
                })
            }
        })
        adapter.setOnItemClickListener { item , view ->
            val bundle = bundleOf()
            val f = item as AwardItem
            bundle.putString("awardId",f.awardId)
            bundle.putString("nameAward",f.name)
            bundle.putString("costAward",f.cost)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listAwardsFragment_to_detailAwardFragment,bundle)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListAwardsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListAwardsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}