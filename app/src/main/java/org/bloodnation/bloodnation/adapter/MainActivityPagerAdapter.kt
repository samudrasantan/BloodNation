package org.bloodnation.bloodnation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.bloodnation.bloodnation.fragment.tab.ManualFragment
import org.bloodnation.bloodnation.profile.ProfileFragment
import org.bloodnation.bloodnation.fragment.tab.SearchFragment


class MainActivityPagerAdapter( fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        when(position){

            0 -> return ProfileFragment()
            1 -> return SearchFragment()
            2 -> return ManualFragment()
        }
        return ManualFragment()
    }

}