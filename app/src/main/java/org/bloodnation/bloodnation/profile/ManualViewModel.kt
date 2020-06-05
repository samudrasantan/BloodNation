package org.bloodnation.bloodnation.profile


import android.app.Application
import android.util.Log
import android.content.Context
import androidx.lifecycle.ViewModel
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.bloodnation.bloodnation.model.Contacts
import org.bloodnation.bloodnation.model.ContactsLocal
import org.bloodnation.bloodnation.repository.MainRepo
import androidx.lifecycle.MutableLiveData
import org.bloodnation.bloodnation.authentication.ManualDonorInterFace
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import org.bloodnation.bloodnation.interfaces.ContactsListener
import java.util.*

class ManualViewModel(application: Application): AndroidViewModel(application)
{

    val context = application


    fun updateBlood(pojo: PhoneBookWithNumbers, listener: ManualDonorInterFace){
        MainRepo.addManualDonor(pojo, listener)
    }

} //class ManualViewModel


