package com.diplom.kotlindiplom.parent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.ChildrenItem
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_parent_node_children.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ParentNodeChildrenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ParentNodeChildrenFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle("Список детей")
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
        return inflater.inflate(R.layout.fragment_parent_node_children, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        updateRecyclerView(firebase,adapter)
        addChildButton.setOnClickListener {
            firebase.sendRequestChild(childIdEditTextParent,requireContext())
        }
    }
    private fun updateRecyclerView(firebase:FunctionsFirebase,adapter:GroupAdapter<ViewHolder>){
        adapter.clear()
        firebase.getChildrenByParentUid(firebase.uidUser!!,object : FirebaseCallback<List<Child>>{
            override fun onComplete(value: List<Child>) {
                value.forEach {
                    adapter.add(ChildrenItem(it,object : ChildrenItem.OnClickDeleteButton{
                        override fun onClickDeleteButton(item: Item<ViewHolder>, child: Child) {
                            firebase.setFieldDatabaseChild(child.childUid,"parentUid","")
                            updateRecyclerView(firebase,adapter)
                        }
                    } ))
                }
                childrenRecyclerViewParent.adapter = adapter
            }

        })
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ParentNodeChildrenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ParentNodeChildrenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}