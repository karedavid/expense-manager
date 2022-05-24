package com.kdapps.kolteskoveto.data

import android.app.*
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.kdapps.kolteskoveto.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs


class UserDataRepository(private val application: Application) {

    private val monthlyLimitKey = "monthly_limit"

    private var todayDate               : Calendar

    private var userDataDao             : DatabaseDao
    private var sharedPreferences       : SharedPreferences

    private var allSpendNode            : LiveData<List<SpendNode>>
    private var allActiveSpendNode      : LiveData<List<SpendNode>>
    private var allArchiveNode          : LiveData<List<ArchiveNode>>
    private val monthlyLimit            = MutableLiveData(0)

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener{sharedPreferences, key ->
        if(key.equals(monthlyLimitKey)){
            monthlyLimit.postValue(sharedPreferences.getString(key,"0")?.toInt())
        }
    }

    init {
        todayDate                       = Calendar.getInstance()
        todayDate.firstDayOfWeek        = Calendar.MONDAY

        // Initializing data sources
        userDataDao                     = UserDatabase.getDatabase(application).databaseDao()
        sharedPreferences               = PreferenceManager.getDefaultSharedPreferences(application)
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        allSpendNode                    = userDataDao.getAllSpendNode()
        allActiveSpendNode              = userDataDao.getAllActiveSpendNode()
        allArchiveNode                  = userDataDao.getAllArchiveNode()

        monthlyLimit.postValue(sharedPreferences.getString(monthlyLimitKey,"0")?.toInt())
    }

    // Query operations

    fun getAllSpendNode()               = allSpendNode
    fun getAllActiveSpendNode()         = allActiveSpendNode
    fun getMonthlyLimit()               = monthlyLimit
    fun getAllArchiveNode()             = allArchiveNode

    // Insert operations

    fun insertSpendNode(item: SpendNode){
        thread {
            val id = userDataDao.insertSpendNode(item)
            item.id = id
            checkForLimitReached()
        }
    }

    private fun checkForLimitReached() {
        todayDate = Calendar.getInstance()
        todayDate.firstDayOfWeek = Calendar.MONDAY

        val current = userDataDao.forceGetSumOfMonth(todayDate.get(Calendar.YEAR), todayDate.get(Calendar.MONTH))
        val limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(application)?.getString("monthly_limit", "0") ?: "0")

        if (current >= limit) {
            notifyLimitReached(abs(limit - current))
        }
    }

    fun insertArchiveNode(item: ArchiveNode){
        thread{
            userDataDao.insertArchiveNode(item)
        }
    }

    // Update operations

    fun updateSpendNode(item: SpendNode){
        thread{
            userDataDao.updateSpendNode(item)
            checkForLimitReached()
        }
    }

    // Delete operations

    fun deleteSpendNode(item: SpendNode){
        thread{
            userDataDao.deleteSpendNode(item)
        }
    }

    fun archiveAllSpendNode(name: String){
        thread {
            val minDate = userDataDao.getEarliestActiveSpendDate()
            val maxDate = userDataDao.getLatestActiveSpendDate()

            val archiveNode = ArchiveNode(null, name, minDate, maxDate)
            val archiveId = userDataDao.insertArchiveNode(archiveNode)
            userDataDao.archiveAllSpendNode(archiveId)
        }
    }

    fun deleteArchiveNode(item: ArchiveNode){
        thread{
            userDataDao.deleteArchiveNode(item)
        }
    }

    // Search

    fun searchSpendNode(filter : String) : LiveData<List<SpendNode>>{
        return userDataDao.getMatchingSpendNode("%$filter%")
    }

    fun getSumOfAllCategoryThisMonth(year: Int, month : Int) : LiveData<List<CategorySum>>{
        return userDataDao.getSumOfAllCategoryThisMonth(year, month)
    }

    fun getSumOfAllMonths(year: Int) : LiveData<List<MonthSum>>{
        return userDataDao.getSumOfAllMonths(year)
    }

    fun getSumOfWeekDays(year: Int, week: Int) : LiveData<List<DaySum>>{
        return userDataDao.getSumOfWeekDays(year, week)
    }

    fun getSumOfMonth(year: Int, month: Int) : LiveData<Int>{
        return userDataDao.getSumOfMonth(year,month)
    }

    fun getThisDaySpent(year: Int, month: Int, day: Int) : LiveData<Int>{
        return userDataDao.getSumOfDay(year, month, day)
    }

    fun getSumOfWeek(year: Int, week: Int) : LiveData<Int>{
        return userDataDao.getSumOfWeek(year, week)
    }

    fun getSumOfCycle() : LiveData<Int>{
        return userDataDao.getSumOfCycle()
    }

    fun getSumOfYear(year: Int) : LiveData<Int>{
        return userDataDao.getSumOfYear(year)
    }

    fun getDailyAverageEver() : LiveData<Int>{
        return userDataDao.getSumOfAllDays()
    }

    private fun notifyLimitReached(over: Int){

        // Appearance
        val CHANNEL_ID      = "monthly_limit"
        val TITLE           = application.resources.getString(R.string.alert_title)
        val MESSAGE         = application.resources.getString(R.string.alert_message, over)
        val CHANNEL_NAME    = application.resources.getString(R.string.alert_channel_name)
        val REQUEST_CODE    = 1

        // Behavior
        val intent      = Intent(application, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(application, 0, intent, PendingIntent.FLAG_MUTABLE)

        // Builder
        val notificationBuilder = NotificationCompat.Builder(application, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(TITLE)
            .setContentText(MESSAGE)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        // Manager
        val notificationManager = application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }

        // Fire
        notificationManager.notify(REQUEST_CODE, notificationBuilder.build())
    }

    fun getWeeklyAverageEver(): LiveData<Int> {
        return userDataDao.getSumOfAllWeeks()
    }

    fun getAnnualAverageEver(): LiveData<Int> {
        return userDataDao.getSumOfAllYears()
    }

    fun getArchivedNodesById(archiveId : Long) : List<SpendNode>{
        return userDataDao.getArchivedSpendNode(archiveId)
    }

    fun getMonthlyAverageEver() : LiveData<Int>{
        return userDataDao.getSumOfAllMonths()
    }

    fun unarchiveArchive(archiveId: Long){
        thread{
            userDataDao.unarchiveArchive(archiveId)
        }
    }

    fun getSumOfArchive() : LiveData<List<ArchiveSum>>{
        return userDataDao.getSumOfArchive()
    }

    fun getSpendNodeById(itemId: Long): SpendNode {
        return userDataDao.getSpendNodeById(itemId)
    }
}