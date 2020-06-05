package org.bloodnation.bloodnation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.settings.SettingsEntity

class MainActivityViewModel(application: Application): AndroidViewModel(application) {

    val context = application
    fun addSettings()= MainRepo.addSettings(context)
    fun updateSetting(state: Int) = MainRepo.updateSetting(context, state)
    val getSettings: LiveData<SettingsEntity> = MainRepo.getSettings(context)





}