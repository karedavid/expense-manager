package com.kdapps.kolteskoveto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kdapps.kolteskoveto.data.UserDataRepository

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserDataRepository = UserDataRepository(application)

    fun archiveAllSpendNode(name : String) {
        repository.archiveAllSpendNode(name)
    }

}