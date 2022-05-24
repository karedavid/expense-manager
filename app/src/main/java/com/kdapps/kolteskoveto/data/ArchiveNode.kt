package com.kdapps.kolteskoveto.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ArchiveNodes")
data class ArchiveNode(

    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "date_from") var date_from: Long,
    @ColumnInfo(name = "date_to") var date_to: Long,

    )
