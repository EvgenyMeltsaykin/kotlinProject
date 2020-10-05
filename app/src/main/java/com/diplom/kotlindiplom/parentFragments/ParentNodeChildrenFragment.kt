package com.diplom.kotlindiplom.parentFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.recyclerViewItems.ChildrenItem
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_parent_node_children, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Список детей"
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        emptyChildTextView?.isVisible = false
        updateRecyclerView(firebase,adapter)
        childrenRecyclerViewParent?.adapter = adapter
        addChildButton?.setOnClickListener {
            firebase.sendRequestChild(childIdTextInputParentMyProfile?.editText?.text.toString(),requireContext())
        }

        firebase.parentRef.child(firebase.uidUser!!).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.key == "acceptAnswer"){
                    if (p0.value.toString() == "1"){
                        updateRecyclerView(firebase,adapter)
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                return
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun updateRecyclerView(firebase:FunctionsFirebase,adapter:GroupAdapter<ViewHolder>){
        adapter.clear()
        firebase.getChildrenByParentUid(firebase.uidUser!!,object : Callback<List<Child>>{
            override fun onComplete(value: List<Child>) {
                emptyChildTextView?.isVisible = value.isEmpty()
                value.forEach {
                    adapter.add(ChildrenItem(it,object : ChildrenItem.OnClickDeleteButton{
                        override fun onClickDeleteButton(item: Item<ViewHolder>, child: Child) {
                            firebase.setFieldDatabaseUser(child.userUid,"parentUid","", object : Callback<Boolean>{
                                override fun onComplete(value: Boolean) {
                                    updateRecyclerView(firebase,adapter)
                                }
                            })
                        }
                    } ))
                }
                parentNodeChildrenProgressBar?.isVisible = false
            }

        })
    }
}