package com.diplom.kotlindiplom.models

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.diplom.kotlindiplom.childFragments.mySchedule.PagerMyScheduleFragment

class MyPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int {
        return 6
    }

    override fun getItem(position: Int): Fragment {
        Log.d("Tag","mypager = $position")
        return PagerMyScheduleFragment.newInstance(getPageTitle(position).toString(),position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position){
            0 -> "Понедельник"
            1 -> "Вторник"
            2 -> "Среда"
            3 -> "Четверг"
            4 -> "Пятница"
            else-> {
                return "Суббота"
            }
        }

    }
}