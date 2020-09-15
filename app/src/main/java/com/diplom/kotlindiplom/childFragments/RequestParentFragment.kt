package com.diplom.kotlindiplom.childFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.accept_parent.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RequestParentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestParentFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var parentName: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parentName = it.getString("parentName")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog?.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.accept_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firebase = FunctionsFirebase()
        invitationTextView?.text = "Пользователь ${parentName} запрашивает привязку аккаунта"
        rejectButton?.setOnClickListener {
            dismiss()
            firebase.getFieldUserDatabase(
                firebase.uidUser!!,
                "acceptUid",
                object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        firebase.setFieldUserDatabase(value, "acceptAnswer", "0")
                        firebase.clearAcceptRequest()
                    }
                })

        }

        acceptButton?.setOnClickListener {
            dismiss()
            firebase.getFieldUserDatabase(
                firebase.uidUser!!,
                "acceptUid",
                object : FirebaseCallback<String> {
                    override fun onComplete(parentUid: String) {
                        Log.d("TAG", "$parentUid")
                        firebase.setFieldUserDatabase(
                            firebase.uidUser,
                            "parentUid",
                            parentUid
                        )
                        firebase.setFieldUserDatabase(
                            parentUid,
                            "acceptAnswer",
                            "1"
                        )
                        firebase.clearAcceptRequest()
                    }
                })

        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RequestParentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RequestParentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}