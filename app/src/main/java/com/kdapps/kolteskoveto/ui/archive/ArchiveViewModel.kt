package com.kdapps.kolteskoveto.ui.archive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kdapps.kolteskoveto.data.ArchiveNode
import com.kdapps.kolteskoveto.data.ArchiveSum
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.data.UserDataRepository

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserDataRepository = UserDataRepository(application)

    var allArchiveNode  : LiveData<List<ArchiveNode>> private set
    var allArchiveSum   : LiveData<List<ArchiveSum>> private set

    init {
        allArchiveNode  = repository.getAllArchiveNode()
        allArchiveSum   = repository.getSumOfArchive()
    }

    private fun getArchivedNodesById(archiveId: Long) : List<SpendNode>{
        return repository.getArchivedNodesById(archiveId)
    }

    fun getArchivedNodeDescription(archiveId: Long) : String{
        var description = String()
        val nodes = getArchivedNodesById(archiveId)
        for(node in nodes){
            description += node.getShortString() + "\n"
        }
        return description
    }

    fun deleteArchive(item : ArchiveNode){
        repository.deleteArchiveNode(item)
    }

    fun unarchiveArchive(item: ArchiveNode){
        if(item.id != null){
            repository.unarchiveArchive(item.id!!)
        }
        repository.deleteArchiveNode(item)
    }
}