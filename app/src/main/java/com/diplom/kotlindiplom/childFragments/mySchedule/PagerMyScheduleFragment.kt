package com.diplom.kotlindiplom.childFragments.mySchedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Lesson
import com.diplom.kotlindiplom.models.MyPagerAdapter
import com.diplom.kotlindiplom.models.recyclerViewItems.LessonMySchedulePagerItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_pager_my_schedule.*
import kotlin.properties.Delegates

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PagerMyScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PagerMyScheduleFragment : Fragment() , AddLessonFragment.OnInputListener {
    // TODO: Rename and change types of parameters
    override fun sendInput(indexTab:Int) {
        val bundle = bundleOf()
        bundle.putInt("indexTab",indexTab)
        Log.d("Tag",indexTab.toString())
        Navigation.findNavController(requireActivity(),R.id.navFragment).popBackStack()
        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.editScheduleFragment,bundle)
    }
    private lateinit var weekday: String
    private var indexTab by Delegates.notNull<Int>()
    private lateinit var fragmentAdapter: MyPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            weekday = it.getString("weekday","")
            indexTab = it.getInt("indexTab",0)
        }
        fragmentAdapter = MyPagerAdapter(childFragmentManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pager_my_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GroupAdapter<ViewHolder>()
        firebase.getLessonMyScheduleOutFirebase(weekday.toLowerCase(),object : Callback<List<Lesson>> {
            override fun onComplete(value: List<Lesson>) {
                var i = 0
                value.forEach {
                    adapter.add(LessonMySchedulePagerItem(it, i))
                    i++
                }
                lessonsRecyclerViewPagerMySchedule?.adapter = adapter
                pagerMyScheduleProgressBar?.isVisible = false
            }
        })

        adapter.setOnItemClickListener { item, view ->
            val itemAdapter = item as LessonMySchedulePagerItem
            val addLessonFragment = AddLessonFragment()
            val bundle =  bundleOf()
            if (itemAdapter.lesson.lessonName.isEmpty()){
                bundle.putString("day",weekday.toLowerCase())
                bundle.putString("numberLesson",item.number.toString())
            }else{
                bundle.putString("day",weekday.toLowerCase())
                bundle.putString("numberLesson",item.number.toString())
                bundle.putString("lessonName",item.lesson.lessonName)
                bundle.putString("time",item.lesson.time)
                bundle.putString("cabinet",item.lesson.cabinet)

            }
            bundle.putInt("indexTab",indexTab)
            /*Log.d("Tag",this.fragmentManager.toString())
            Log.d("Tag",
            Log.d("Tag",this.parentFragmentManager.toString())
            Log.d("Tag",requireActivity().supportFragmentManager.toString())*/
            addLessonFragment.setTargetFragment(this,1)
            addLessonFragment.arguments = bundle
            addLessonFragment.show(this.parentFragmentManager, "addLessonFragment")
        }
    }
    companion object {
        @JvmStatic
        fun newInstance(weekday: String,indexTab:Int) =
            PagerMyScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString("weekday", weekday)
                    putInt("indexTab", indexTab)
                }
            }
    }
}