package org.bloodnation.bloodnation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.repository.MainRepo

class MainLobbyViewModel(application: Application):AndroidViewModel(application)
{

    private val repo = MainRepo
    val context = application
    fun updateDatabase() = repo.updateDatabase(context)
    fun returnDonor(listener: UserUpdateListener) = repo.returnDonor(listener)

    val donor: LiveData<Donor> = repo.donorInfo()
}