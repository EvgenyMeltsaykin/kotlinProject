package com.diplom.kotlindiplom.generalFragments.scheduleFragments

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
import kotlinx.android.synthetic.main.fragment_choose_child_schedule.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChooseChildScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChooseChildScheduleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return inflater.inflate(R.layout.fragment_choose_child_schedule, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Выберите ребенка"
        progressBar?.isVisible = false
        updateRecyclerView()

        refreshButton?.setOnClickListener {
            updateChildFirebase()
        }
    }

    @ExperimentalStdlibApi
    fun updateChildFirebase() {
        val diary = Diary()
        Toast.makeText(
            requireContext(),
            "Подождите, идет загрузка списка детей",
            Toast.LENGTH_SHORT
        ).show()
        progressBar?.isVisible = true
        refreshButton?.isVisible = false
        val firebase = FunctionsFirebase()
        firebase.getFieldDiary(firebase.uidUser, "url", object : Callback<String> {
            override fun onComplete(value: String) {
                when (value) {
                    diary.elschool.url -> {
                        diary.elschool.writeChildrenDiaryInFirebase(object :
                            Callback<Boolean> {
                            override fun onComplete(value: Boolean) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (value) {
                                        updateRecyclerView()
                                        Toast.makeText(
                                            requireContext(), "Список детей успешно загружен",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            requireContext(), "Ошибка при загрузке",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                    progressBar?.isVisible = false
                                    refreshButton?.isVisible = true
                                }

                            }
                        })
                    }
                }
            }
        })
    }

    @ExperimentalStdlibApi
    fun updateRecyclerView() {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.clear()
        val diary = Diary()
        childScheduleRecyclerView?.isVisible = false
        diary.elschool.getChildrenFromFirebase(object : Callback<List<ChildForElschool>> {
            override fun onComplete(value: List<ChildForElschool>) {
                if (value.isNotEmpty()) {
                    value.forEach {
                        adapter.add(ChildDiaryItem(it))
                    }
                    childScheduleRecyclerView?.adapter = adapter
                    childScheduleRecyclerView?.isVisible = true
                } else {
                    updateChildFirebase()
                }

            }
        })
        adapter.setOnItemClickListener { item, view ->
            val childDiaryItem = item as ChildDiaryItem
            val bundle = bundleOf()
            val firebase = FunctionsFirebase()
            bundle.putString("id", childDiaryItem.child.id)
            firebase.getFieldDiary(
                firebase.uidUser,
                "idChild",
                object : Callback<String> {
                    override fun onComplete(idChild: String) {
                        bundle.putBoolean("updateSchedule",true)
                        if (idChild != childDiaryItem.child.id) {
                            bundle.putBoolean("updateWithoutCheck", true)
                        } else {
                            bundle.putBoolean("updateWithoutCheck", false)
                        }
                        firebase.setFieldDiary(
                            firebase.uidUser,
                            "idChild",
                            childDiaryItem.child.id
                        )
                        Navigation.findNavController(requireActivity(), R.id.navFragment).navigate(
                            R.id.action_chooseChildScheduleFragment_to_weekdayFragment,
                            bundle
                        )
                    }
                })
        }
    }
}