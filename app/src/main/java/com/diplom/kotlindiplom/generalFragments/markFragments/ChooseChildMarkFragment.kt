package com.diplom.kotlindiplom.generalFragments.markFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.recyclerViewItems.ChildDiaryItem
import com.diplom.kotlindiplom.models.ChildForElschool
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_choose_child_mark.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChooseChildMarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChooseChildMarkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var finalGrades = false
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            finalGrades = it.getBoolean("finalGrades",false)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_child_mark, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        progressBar?.isVisible= false
        activity?.title = "Выберите ребенка"
        updateRecyclerView()

        refreshButton?.setOnClickListener {
            updateChildFirebase()
        }

    }
    @ExperimentalStdlibApi
    fun updateChildFirebase(){
        val diary = Diary()
        Toast.makeText(requireContext(),"Подождите, идет загрузка списка детей",Toast.LENGTH_SHORT).show()
        progressBar?.isVisible = true
        refreshButton?.isVisible = false
        val firebase = FunctionsFirebase()
        firebase.getFieldDiary(firebase.uidUser!!,"url",object :Callback<String> {
            override fun onComplete(value: String) {
                when (value) {
                    diary.elschool.url -> {
                        diary.elschool.writeChildrenDiaryInFirebase(object :
                            Callback<Boolean> {
                            override fun onComplete(value: Boolean) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (value){
                                        updateRecyclerView()
                                        progressBar?.isVisible = false
                                        refreshButton?.isVisible = true
                                        Toast.makeText(requireContext(),"Список детей успешно загружен",Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(requireContext(),"Ошибка при загрузке",Toast.LENGTH_SHORT).show()
                                        progressBar?.isVisible = false
                                        refreshButton?.isVisible = true
                                    }
                                }

                            }
                        })
                    }
                }
            }
        })
    }
    @ExperimentalStdlibApi
    fun updateRecyclerView(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.clear()
        childMarkRecyclerView?.isVisible = false
        val diary = Diary()
        diary.elschool.getChildrenFromFirebase(object : Callback<List<ChildForElschool>>{
            override fun onComplete(value: List<ChildForElschool>) {
                if (value.isNotEmpty()){
                    value.forEach{
                        adapter.add(ChildDiaryItem(it))
                    }
                    childMarkRecyclerView?.adapter = adapter
                    childMarkRecyclerView?.isVisible = true
                }else{
                    updateChildFirebase()
                }

            }
        })
        adapter.setOnItemClickListener { item, view ->
            val firebase = FunctionsFirebase()
            val childDiaryItem = item as ChildDiaryItem
            val bundle = bundleOf()
            bundle.putString("id","${childDiaryItem.child.id}")
            bundle.putBoolean("finalGrades",finalGrades)
            firebase.setFieldDiary(firebase.uidUser!!,"idChild",childDiaryItem.child.id)
            Navigation.findNavController(requireActivity(), R.id.navFragment).navigate(R.id.action_chooseChildMarkFragment_to_chooseSemestrElschoolFragment,bundle)

        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChoodeChildMarkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChooseChildMarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

