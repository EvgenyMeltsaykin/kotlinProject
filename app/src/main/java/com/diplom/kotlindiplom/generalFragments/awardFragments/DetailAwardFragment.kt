package com.diplom.kotlindiplom.generalFragments.awardFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.fragment_detail_award.*

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
    private var param2: String? = null
    private lateinit var role:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            awardId = it.getString("awardId","")
            nameAward = it.getString("nameAward","")
            costAward = it.getString("costAward","")
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

        awardNameTextView?.text = "Вознаграждение: $nameAward"
        costAwardTxtView?.text = "Стоимость: $costAward"
        if (role == "children"){
            deleteButton?.isVisible = false
        }
        if (role == "parent"){
            takeButton?.isVisible = false
        }
        deleteButton?.setOnClickListener {
            Toast.makeText(requireContext(),"Успешно",Toast.LENGTH_SHORT).show()
            val firebase = FunctionsFirebase()
            firebase.deleteAward(awardId)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailAwardFragment_to_listAwardsFragment)
        }
        takeButton?.setOnClickListener {
            Toast.makeText(requireContext(),"Успешно",Toast.LENGTH_SHORT).show()
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_detailAwardFragment_to_listAwardsFragment)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailAwardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailAwardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}