package com.kdapps.kolteskoveto.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.*

@Entity(tableName = "SpendNodes")
data class SpendNode(

    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)   var id: Long? = null,
    @ColumnInfo(name = "archive_id")                            var archive_id: Long? = null,
    @ColumnInfo(name = "name")                                  var name: String,
    @ColumnInfo(name = "main_category")                         var main_category: Int,
    @ColumnInfo(name = "secondary_category")                    var secondary_category: String?,
    @ColumnInfo(name = "amount")                                var amount: Int,
    @ColumnInfo(name = "date")                                  var date: Long,
    @ColumnInfo(name = "year")                                  var year: Int,
    @ColumnInfo(name = "month")                                 var month: Int,
    @ColumnInfo(name = "day")                                   var day: Int,
    @ColumnInfo(name = "week")                                  var week: Int,
    @ColumnInfo(name = "paid")                                  var paid: Boolean,
    @ColumnInfo(name = "desc")                                  var desc: String?,
    @ColumnInfo(name = "place")                                 var place: String?,
    @ColumnInfo(name = "archived")                              var archived: Boolean

){
    companion object{
        const val CATEGORY_OTHER      = 0
        const val CATEGORY_FOOD       = 1
        const val CATEGORY_HOUSEHOLD  = 2
        const val CATEGORY_LIVING     = 3
        const val CATEGORY_OCCASIONAL = 4
        const val CATEGORY_TRANSPORTATION = 5
    }

    fun getString() :String{
        return "$name $amount\n$year.${month+1}.$day\n$desc\n$place\n"
    }

    fun getShortString() :String {
        val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

        // Setting up number formatter
        moneyFormat.currency = Currency.getInstance("HUF")
        moneyFormat.isGroupingUsed = true
        moneyFormat.maximumFractionDigits = 0

        return "${moneyFormat.format(amount)} - $name"

    }
}


