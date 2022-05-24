package com.kdapps.kolteskoveto.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.SettingsActivity
import com.kdapps.kolteskoveto.data.CategorySum
import com.kdapps.kolteskoveto.data.DaySum
import com.kdapps.kolteskoveto.data.MonthSum
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    private val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // Setting up number formatter
        moneyFormat.currency = Currency.getInstance("HUF")
        moneyFormat.isGroupingUsed = true
        moneyFormat.maximumFractionDigits = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        // Monthly limit module
        dashboardViewModel.monthlyLimit.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.ModuleMonthlyLimitContent1Value.text = moneyFormat.format(it)

                // it != null removed
                if (dashboardViewModel.thisMonthSpent.value != null) {
                    val limit = it.toDouble()
                    val progress = (dashboardViewModel.thisMonthSpent.value!!.toDouble() / limit) * 100
                    setProgressBarData(progress.toInt())
                }
            }
        }

        dashboardViewModel.thisMonthSpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisMonthSumValue.text = moneyFormat.format(it)
                binding.ModuleMonthlyLimitContent2Value.text = moneyFormat.format(it)

                // it != null removed
                if (dashboardViewModel.monthlyLimit.value != null) {
                    val limit = dashboardViewModel.monthlyLimit.value!!.toDouble()
                    val progress = (it / limit) * 100
                    setProgressBarData(progress.toInt())
                }

                if(binding.chipThisMonth.isChecked){
                    binding.chartCategory.centerText = getString(R.string.pie_center) + "\n" + moneyFormat.format(it)
                }

            }
        }

        dashboardViewModel.lastMonthSpent.observe(viewLifecycleOwner) {
            if (it != null) {

                if(binding.chipPreviousMonth.isChecked){
                    binding.chartCategory.centerText = getString(R.string.pie_center) + "\n" + moneyFormat.format(it)
                }

            }
        }



        // Quick stats module

        dashboardViewModel.thisDaySpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisDaySumValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.thisWeekSpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisWeekSumValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.thisMonthSpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisMonthSumValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.thisCycleSpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisCycleSumValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.thisYearSpent.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twThisYearSumValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.dailyAverage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twDailyAverageValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.weeklyAverage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twWeeklyAverageValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.monthlyAverage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twMonthlyAverageValue.text = moneyFormat.format(it)
            }
        }

        dashboardViewModel.annualAverage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.twAnnualAverageValue.text = moneyFormat.format(it)
            }
        }

        // Charts

        initCategoryChart()
        initMonthChart()
        initDayChart()

        dashboardViewModel.monthlySumByCategoryActual.observe(viewLifecycleOwner) {
            binding.chipThisMonth.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    if (it != null) {
                        setCategoryChartData(it)
                        if(dashboardViewModel.thisMonthSpent.value != null){
                            binding.chartCategory.centerText = getString(R.string.pie_center, moneyFormat.format(dashboardViewModel.thisMonthSpent.value))
                        }else{
                            binding.chartCategory.centerText = getString(R.string.no_data_pie_center)
                        }
                    }
                }
            }

            if(binding.chipThisMonth.isChecked){
                if (it != null) {
                    setCategoryChartData(it)
                    if(dashboardViewModel.thisMonthSpent.value != null) {
                        binding.chartCategory.centerText = getString(R.string.pie_center, moneyFormat.format(dashboardViewModel.thisMonthSpent.value))
                    }else{
                        binding.chartCategory.centerText = getString(R.string.no_data_pie_center)
                        binding.chipPreviousMonth.performClick()
                    }
                }
            }
        }

        dashboardViewModel.sumByMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                setMonthChartData(it)
            }
        }

        dashboardViewModel.sumOfWeekDays.observe(viewLifecycleOwner) {
            if (it != null) {
                setDayChartData(it)
            }
        }

        binding.chartPager.adapter = ChartAdapter()
        binding.chartPager.offscreenPageLimit = 2

        binding.btnPagerNext.setOnClickListener {
            val current = binding.chartPager.currentItem
            val max = binding.chartPager.childCount
            if(current+1 < max){
                binding.chartPager.setCurrentItem(current+1,true)
            }
        }

        binding.btnPagerPrevious.setOnClickListener {
            val current = binding.chartPager.currentItem
            if(current-1 >= 0){
                binding.chartPager.setCurrentItem(current-1,true)
            }
        }

        dashboardViewModel.monthlySumByCategoryPrevious.observe(viewLifecycleOwner) {
            binding.chipPreviousMonth.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    if (it != null) {
                        setCategoryChartData(it)
                        if(dashboardViewModel.lastMonthSpent.value != null){
                            binding.chartCategory.centerText = getString(R.string.pie_center, moneyFormat.format(dashboardViewModel.lastMonthSpent.value))
                        }else{
                            binding.chartCategory.centerText = getString(R.string.no_data_pie_center)
                        }
                    }
                }
            }

            if(binding.chipPreviousMonth.isChecked){
                if (it != null) {
                    setCategoryChartData(it)
                    if(dashboardViewModel.lastMonthSpent.value != null){
                        binding.chartCategory.centerText = getString(R.string.pie_center, moneyFormat.format(dashboardViewModel.lastMonthSpent.value))
                    }else{
                        binding.chartCategory.centerText = getString(R.string.no_data_pie_center)
                    }

                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as AppCompatActivity).setSupportActionBar(binding.topAppBar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard_menu_top, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setProgressBarData(progress: Int?){

        var color = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary,null)

        when {
            progress != null -> {
                binding.ModuleMonthlyLimitProgress.progress = progress

                if(progress > 100){
                    color = ResourcesCompat.getColor(resources, R.color.md_theme_dark_errorContainer, null)
                }
            }
            else -> {
                binding.ModuleMonthlyLimitProgress.progress = 0
            }
        }
        binding.ModuleMonthlyLimitProgress.setIndicatorColor(color)
    }

    private fun setCategoryChartData(list: List<CategorySum>){
        val entries = ArrayList<PieEntry>()

        for(item in list){
            when (item.main_category){
                SpendNode.CATEGORY_OTHER -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_other), moneyFormat.format(item.sum))))
                SpendNode.CATEGORY_FOOD -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_food), moneyFormat.format(item.sum))))
                SpendNode.CATEGORY_TRANSPORTATION -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_transportation), moneyFormat.format(item.sum))))
                SpendNode.CATEGORY_OCCASIONAL -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_occasional), moneyFormat.format(item.sum))))
                SpendNode.CATEGORY_LIVING -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_living), moneyFormat.format(item.sum))))
                SpendNode.CATEGORY_HOUSEHOLD -> entries.add(PieEntry(item.sum.toFloat(), getString(R.string.pie_label, getString(R.string.new_cat_household), moneyFormat.format(item.sum))))
            }
        }

        val dataSet = PieDataSet(entries, getString(R.string.category_title))
        dataSet.label = null
        dataSet.sliceSpace = 1F
        dataSet.colors = null

        val colors = resources.getIntArray(R.array.chart_color)

        for(color in colors){
            dataSet.addColor(color)
        }

        val data = PieData(dataSet)
        data.setValueTextSize(20F)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return moneyFormat.format(value.toInt())
            }
        })
        data.setDrawValues(false)
        data.setValueTextColor(ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null))
        binding.chartCategory.data = data
        binding.chartCategory.invalidate()

    }

    private fun setMonthChartData(list : List<MonthSum>){
        val entries = ArrayList<BarEntry>(12)

        for (i in 0..11){
            for(item in list){
                if(item.month == i){
                    entries.add(BarEntry(i.toFloat(), item.sum.toFloat()))
                    continue
                }
            }
            entries.add(BarEntry(i.toFloat(), 0F))
        }

        val dataSet = BarDataSet(entries, null)
        dataSet.color = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)

        val colors = resources.getIntArray(R.array.chart_color)

        for(color in colors){
            dataSet.addColor(color)
        }

        val barData = BarData(dataSet)
        barData.setDrawValues(false)

        binding.chartMonth.data = barData
        binding.chartMonth.invalidate()

    }

    private fun setDayChartData(list: List<DaySum>) {
        val entries = ArrayList<Entry>(7)

        val calendar    = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0..6){
            val entry = Entry(i.toFloat(), 0F)
            for(item in list){
                if(item.day == calendar.get(Calendar.DAY_OF_MONTH)){
                    entry.y = item.sum.toFloat()
                    continue
                }

            }
            entries.add(entry)
            calendar.add(Calendar.DATE, 1)
        }

        val dataSet = LineDataSet (entries,null)
        dataSet.color = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.lineWidth = 3F
        dataSet.color = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null))
        dataSet.setDrawCircleHole(false)
        dataSet.circleRadius = 5F


        val colors = resources.getIntArray(R.array.chart_color)

        for(color in colors){
            dataSet.addColor(color)
        }

        val lineData = LineData(dataSet)
        lineData.setDrawValues(true)
        lineData.setValueTextSize(15F)
        lineData.setValueTextColor(ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null))

        binding.chartDay.data = lineData
        binding.chartDay.invalidate()
    }

    // By Category this Month
    private fun initCategoryChart(){

        binding.chartCategory.setCenterTextSize(20F)
        binding.chartCategory.setCenterTextColor(ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null))
        binding.chartCategory.isDrawHoleEnabled = true
        binding.chartCategory.setHoleColor(Color.TRANSPARENT)
        binding.chartCategory.transparentCircleRadius = 0F
        binding.chartCategory.setEntryLabelColor(ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null))
        binding.chartCategory.setEntryLabelTextSize(20F)
        binding.chartCategory.isRotationEnabled = false
        binding.chartCategory.legend.orientation = Legend.LegendOrientation.VERTICAL
        binding.chartCategory.legend.isWordWrapEnabled = true
        binding.chartCategory.legend.form = Legend.LegendForm.CIRCLE
        binding.chartCategory.legend.yOffset = -45F
        binding.chartCategory.legend.textSize = 18F
        binding.chartCategory.legend.formSize = 18F
        binding.chartCategory.legend.textColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartCategory.extraBottomOffset = 100f
        binding.chartCategory.description.isEnabled = false
        binding.chartCategory.setDrawEntryLabels(false)
    }

    // Sum by Month this Year
    private fun initMonthChart(){

        val labelsMonths = resources.getStringArray(R.array.months_of_year_short).asList()

        binding.chartMonth.axisRight.isEnabled = false

        binding.chartMonth.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return labelsMonths[value.toInt()]
            }
        }

        binding.chartMonth.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${(value/1000).toInt()}K"
            }
        }

        binding.chartMonth.xAxis.labelCount = labelsMonths.size
        binding.chartMonth.xAxis.labelRotationAngle = 50F
        binding.chartMonth.xAxis.textSize = 15F
        binding.chartMonth.xAxis.textColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartMonth.xAxis.setDrawGridLines(false)
        binding.chartMonth.xAxis.setDrawAxisLine(false)
        binding.chartMonth.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chartMonth.axisLeft.setDrawGridLines(true)
        binding.chartMonth.axisLeft.gridColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartMonth.axisLeft.setDrawAxisLine(false)
        binding.chartMonth.axisLeft.textSize = 15F
        binding.chartMonth.axisLeft.textColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartMonth.axisRight.setDrawAxisLine(false)
        binding.chartMonth.legend.isEnabled = false
        binding.chartMonth.description.isEnabled = false
        binding.chartMonth.setDrawValueAboveBar(false)
        binding.chartDay.setPinchZoom(false)
        binding.chartDay.setScaleEnabled(false)
    }

    // Sum of Days this Week
    private fun initDayChart(){
        val labelsWeekdays = resources.getStringArray(R.array.days_of_week_short).asList()

        binding.chartDay.axisRight.isEnabled = false

        binding.chartDay.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return labelsWeekdays[value.toInt()]
            }
        }

        binding.chartDay.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${(value/1000).toInt()}K"
            }
        }

        binding.chartDay.xAxis.labelCount = labelsWeekdays.size
        binding.chartDay.xAxis.setAvoidFirstLastClipping(true)
        binding.chartDay.xAxis.labelRotationAngle = 50F
        binding.chartDay.xAxis.textSize = 15F
        binding.chartDay.xAxis.textColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartDay.xAxis.setDrawGridLines(false)
        binding.chartDay.xAxis.setDrawAxisLine(false)
        binding.chartDay.xAxis.position = XAxis.XAxisPosition.BOTTOM

        binding.chartDay.xAxis.axisMinimum = 0F
        binding.chartDay.xAxis.axisMaximum = 6F
        binding.chartDay.xAxis.granularity = 1F
        binding.chartDay.xAxis.mEntries = floatArrayOf(0F,1F,2F,3F,4F,5F,6F)

        binding.chartDay.axisLeft.setDrawGridLines(true)
        binding.chartDay.axisLeft.gridColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)
        binding.chartDay.axisLeft.setDrawAxisLine(false)
        binding.chartDay.axisLeft.textSize = 15F
        binding.chartDay.axisLeft.textColor = ResourcesCompat.getColor(resources, R.color.md_theme_dark_onPrimary, null)

        binding.chartDay.axisRight.setDrawAxisLine(false)

        binding.chartDay.legend.isEnabled = false

        binding.chartDay.description.isEnabled = false
        binding.chartDay.setPinchZoom(false)
        binding.chartDay.setScaleEnabled(false)
    }

}