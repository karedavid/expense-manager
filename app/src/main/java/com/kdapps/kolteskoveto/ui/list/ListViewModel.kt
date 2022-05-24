package com.kdapps.kolteskoveto.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.data.UserDataRepository

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserDataRepository = UserDataRepository(application)

    var allActiveSpendNode  : LiveData<List<SpendNode>> private set
    private var allSpendNode        : LiveData<List<SpendNode>>

    var filteredSpendNode   : LiveData<List<SpendNode>> private set
    var filterQuery         = MutableLiveData<String>()

    init{

        allActiveSpendNode  = repository.getAllActiveSpendNode()
        allSpendNode        = repository.getAllSpendNode()

        filterQuery.value   = ""
        filteredSpendNode   = Transformations.switchMap(filterQuery){
            repository.searchSpendNode(it)
        }
        allActiveSpendNode.value?.get(1)
    }

    fun updateSpendNode(item: SpendNode){
        repository.updateSpendNode(item)
    }

    fun deleteSpendNode(item: SpendNode){
        repository.deleteSpendNode(item)
    }
}