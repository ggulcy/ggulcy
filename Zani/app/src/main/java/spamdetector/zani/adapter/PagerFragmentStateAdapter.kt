package spamdetector.zani.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerFragmentStateAdapter(fragmentActivity: FragmentActivity, val array : ArrayList<Fragment>): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return array[position]
    }
}