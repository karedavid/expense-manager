package com.kdapps.kolteskoveto.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kdapps.kolteskoveto.data.*
import java.util.*


class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    //Today date
    private val todayDate           = Calendar.getInstance()
    private val lastMonthDate       = Calendar.getInstance()

    // Repository
    private val repository          : UserDataRepository

    // Monthly limit module
    var monthlyLimit                : LiveData<Int>                 private set
    var thisMonthSpent              : LiveData<Int>                 private set
    var lastMonthSpent              : LiveData<Int>                 private set

    // Quick stats module
    var thisDaySpent                : LiveData<Int>                 private set
    var thisWeekSpent               : LiveData<Int>                 private set
    var thisCycleSpent              : LiveData<Int>                 private set
    var thisYearSpent               : LiveData<Int>                 private set

    var dailyAverage                : LiveData<Int>                 private set
    var weeklyAverage               : LiveData<Int>                 private set
    var monthlyAverage              : LiveData<Int>                 private set
    var annualAverage               : LiveData<Int>                 private set

    // Charts
    var monthlySumByCategoryActual  : LiveData<List<CategorySum>>   private set
    var monthlySumByCategoryPrevious: LiveData<List<CategorySum>>   private set
    var sumByMonth                  : LiveData<List<MonthSum>>      private set
    var sumOfWeekDays               : LiveData<List<DaySum>>        private set

    init{
        // Setting up dates
        todayDate.firstDayOfWeek    = Calendar.MONDAY
        lastMonthDate.firstDayOfWeek= Calendar.MONDAY
        lastMonthDate.add(Calendar.MONTH, -1)

        val year                    = todayDate.get(Calendar.YEAR)
        val month                   = todayDate.get(Calendar.MONTH)
        val day                     = todayDate.get(Calendar.DAY_OF_MONTH)
        val week                    = todayDate.get(Calendar.WEEK_OF_YEAR)

        val lastMonthYear           = lastMonthDate.get(Calendar.YEAR)
        val lastMonthMonth          = lastMonthDate.get(Calendar.MONTH)

        //Initializing repository
        repository                  = UserDataRepository(application)

        //Init
        monthlyLimit                = repository.getMonthlyLimit()
        thisMonthSpent              = repository.getSumOfMonth(year, month)
        thisDaySpent                = repository.getThisDaySpent(year, month, day)
        thisWeekSpent               = repository.getSumOfWeek(year, week)
        thisCycleSpent              = repository.getSumOfCycle()
        thisYearSpent               = repository.getSumOfYear(year)
        lastMonthSpent              = repository.getSumOfMonth(lastMonthYear, lastMonthMonth)

        dailyAverage                = repository.getDailyAverageEver()
        weeklyAverage               = repository.getWeeklyAverageEver()
        monthlyAverage              = repository.getMonthlyAverageEver()
        annualAverage               = repository.getAnnualAverageEver()

        //Charts
        sumOfWeekDays               = repository.getSumOfWeekDays(year, week)
        sumByMonth                  = repository.getSumOfAllMonths(year)
        monthlySumByCategoryActual  = setMonthlySumByCategory()
        monthlySumByCategoryPrevious= setMonthlySumByCategory(lastMonthYear, lastMonthMonth)

    }

    private fun setMonthlySumByCategory(year: Int? = null, month: Int? = null): LiveData<List<CategorySum>> {

        var queryYear   = todayDate.get(Calendar.YEAR)
        var queryMonth  = todayDate.get(Calendar.MONTH)

        year?.let{queryYear = year}
        month?.let{queryMonth = month}

        return repository.getSumOfAllCategoryThisMonth(queryYear, queryMonth)
    }

}