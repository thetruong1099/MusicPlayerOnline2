package com.example.musicplayeronline2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.ui.activity.SearchActivity
import com.example.musicplayeronline2.ui.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_rank.*


class RankFragment : Fragment() {

    private val adapterViewPagers by lazy {
        ViewPagerAdapter(
            childFragmentManager,
            lifecycle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewpager()

        startSearchActivity()
    }

    private fun setViewpager() {
        var listFragment: MutableList<Fragment> = mutableListOf(
            MusicVietnameseFragment(),
            MusicUSUKFragment()
        )

        adapterViewPagers.setFragment(listFragment)

        view_pager_top_100.apply {
            adapter = adapterViewPagers
        }

        tabLayout.addTab(tabLayout.newTab().setText("Vietnam"))
        tabLayout.addTab(tabLayout.newTab().setText("US - UK"))

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager_top_100.setCurrentItem(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        view_pager_top_100.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }

    private fun startSearchActivity() {
        btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }


}