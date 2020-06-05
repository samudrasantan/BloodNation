package org.bloodnation.bloodnation.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.repository.MainRepo

class DonorProfileViewModel(application: Application): AndroidViewModel(application) {

    fun getDonorProfile(context: Context, donorId: String) : LiveData<Donor>
    {
        return MainRepo.getDonorProfile(context, donorId)
    }
}