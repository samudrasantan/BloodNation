package org.bloodnation.bloodnation.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.repository.MainRepo

class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    val donorInfo: LiveData<Donor> = MainRepo.donorInfo()

}