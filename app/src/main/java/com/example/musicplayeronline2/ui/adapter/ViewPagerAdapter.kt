package com.example.musicplayeronline2.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    private var listFragment: MutableList<Fragment> = mutableListOf()

    override fun getItemCount(): Int = listFragment.size

    override fun createFragment(position: Int): Fragment = listFragment[position]

    fun setFragment(listFragment: MutableList<Fragment>) {
        this.listFragment = listFragment
        notifyDataSetChanged()
    }
}