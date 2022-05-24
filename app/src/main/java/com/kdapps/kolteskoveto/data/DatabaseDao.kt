package com.kdapps.kolteskoveto.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabaseDao {

    // Spend Node
    @Query("SELECT * FROM SpendNodes")
    fun getAllSpendNode(): LiveData<List<SpendNode>>

    @Query("SELECT * FROM SpendNodes WHERE NOT archived ORDER BY date desc")
    fun getAllActiveSpendNode(): LiveData<List<SpendNode>>

    @Query("SELECT * FROM SpendNodes WHERE archive_id = :archive_id")
    fun getArchivedSpendNode(archive_id : Long): List<SpendNode>

    @Query("SELECT * FROM SpendNodes WHERE (name LIKE :filter OR `desc`LIKE :filter OR place LIKE :filter OR secondary_category LIKE :filter) AND NOT archived ORDER BY date desc")
    fun getMatchingSpendNode(filter : String) : LiveData<List<SpendNode>>

    // Summarize
    @Query("SELECT SUM(amount) FROM SpendNodes WHERE year = :year AND month = :month AND day = :day")
    fun getSumOfDay(year: Int, month: Int, day: Int) :LiveData<Int>

    @Query("SELECT AVG(sum) FROM (SELECT SUM(amount) as sum FROM SpendNodes GROUP BY year,month,day) as inner_query")
    fun getSumOfAllDays(): LiveData<Int>

    @Query("SELECT SUM(amount) FROM SpendNodes WHERE year = :year AND month = :month")
    fun getSumOfMonth(year: Int, month: Int) :LiveData<Int>

    @Query("SELECT SUM(amount) FROM SpendNodes WHERE year = :year AND month = :month")
    fun forceGetSumOfMonth(year: Int, month: Int) :Int

    @Query("SELECT AVG(sum) FROM (SELECT SUM(amount) as sum FROM SpendNodes GROUP BY year, month) as inner_query")
    fun getSumOfAllMonths(): LiveData<Int>

    @Query("SELECT month, SUM(amount) as sum FROM SpendNodes GROUP BY year, month HAVING year = :year ORDER BY month")
    fun getSumOfAllMonths(year: Int): LiveData<List<MonthSum>>

    @Query("SELECT day, SUM(amount) as sum FROM SpendNodes WHERE year = :year AND week = :week GROUP BY year, week, day ORDER BY day")
    fun getSumOfWeekDays(year: Int, week: Int): LiveData<List<DaySum>>

    @Query("SELECT SUM(amount) FROM SpendNodes WHERE NOT archived")
    fun getSumOfCycle() : LiveData<Int>

    @Query("SELECT SUM(amount) FROM SpendNodes WHERE year = :year")
    fun getSumOfYear(year: Int) :LiveData<Int>

    @Query("SELECT AVG(sum) FROM (SELECT SUM(amount) as sum FROM SpendNodes GROUP BY year) as inner_query")
    fun getSumOfAllYears(): LiveData<Int>

    @Query("SELECT SUM(amount) FROM SpendNodes GROUP BY year, week HAVING year = :year AND week = :week")
    fun getSumOfWeek(year: Int, week: Int): LiveData<Int>

    @Query("SELECT AVG(sum) FROM (SELECT SUM(amount) as sum FROM SpendNodes GROUP BY year, week) as inner_query")
    fun getSumOfAllWeeks(): LiveData<Int>

    @Query("SELECT MIN(date) FROM SpendNodes WHERE NOT archived")
    fun getEarliestActiveSpendDate(): Long

    @Query("SELECT MAX(date) FROM SpendNodes WHERE NOT archived")
    fun getLatestActiveSpendDate(): Long

    @Insert
    fun insertSpendNode(spendNode: SpendNode): Long

    @Update
    fun updateSpendNode(spendNode: SpendNode)

    @Delete
    fun deleteSpendNode(spendNode: SpendNode)

    @Query("SELECT main_category, SUM(amount) as sum FROM SpendNodes GROUP BY year, month, main_category HAVING year = :year AND month = :month ORDER BY main_category")
    fun getSumOfAllCategoryThisMonth(year: Int, month: Int): LiveData<List<CategorySum>>

    // Archive Node
    @Query("SELECT * FROM ArchiveNodes")
    fun getAllArchiveNode(): LiveData<List<ArchiveNode>>

    @Query("UPDATE SpendNodes SET archived = 1, archive_id = :archive_id WHERE NOT archived")
    fun archiveAllSpendNode(archive_id: Long)

    @Query("SELECT archive_id, SUM(amount) as sum FROM SpendNodes WHERE archived GROUP BY archive_id")
    fun getSumOfArchive() : LiveData<List<ArchiveSum>>

    @Insert
    fun insertArchiveNode(archiveNode: ArchiveNode): Long

    @Query("UPDATE SpendNodes SET archived = 0, archive_id = null WHERE archived AND archive_id = :archiveId")
    fun unarchiveArchive(archiveId: Long)

    @Delete
    fun deleteArchiveNode(archiveNode: ArchiveNode)

    @Query("SELECT * FROM SpendNodes WHERE id = :itemId")
    fun getSpendNodeById(itemId: Long): SpendNode
}

data class CategorySum(
    var main_category: Int,
    var sum: Int
)

data class MonthSum(
    var month: Int,
    var sum: Int
)

data class DaySum(
    var day: Int,
    var sum: Int
)

data class ArchiveSum(
    var archive_id: Long,
    var sum : Int
)