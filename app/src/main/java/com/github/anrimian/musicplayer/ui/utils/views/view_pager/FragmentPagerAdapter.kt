package com.github.anrimian.musicplayer.ui.utils.views.view_pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentPagerAdapter(
    fragment: Fragment,
    private val fragments: List<FragmentBuilder>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position].creator()

    override fun getItemId(position: Int): Long {
        return fragments[position].id
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragments.any { fragmentBuilder -> fragmentBuilder.id == itemId }
    }
}

class FragmentBuilder(val id: Long, val creator: () -> Fragment)