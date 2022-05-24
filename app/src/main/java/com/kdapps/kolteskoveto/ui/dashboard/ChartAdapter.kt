package com.kdapps.kolteskoveto.ui.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.kdapps.kolteskoveto.R

class ChartAdapter : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return when (position){
            0 ->    container.findViewById(R.id.chartCategoryContainer)
            1 ->    container.findViewById(R.id.chartMonthContainer)
            else -> container.findViewById(R.id.chartDayContainer)
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
}