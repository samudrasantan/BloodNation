package org.bloodnation.bloodnation.authentication



import org.bloodnation.bloodnation.repository.MainRepo
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import org.bloodnation.bloodnation.interfaces.NewDonorInterFace


class AuthViewModel(application: Application) : AndroidViewModel(application)
{

    var fireStore = MainRepo.fireStore
    var l = "AuthViewModel"
    private val repo = MainRepo
    val donorInfo: LiveData<Donor> = repo.donorInfo()
    //called from SignUpConfirmation
    fun createUser(donor: Donor, listener: NewDonorInterFace)
    {
        repo.createUser(donor, listener)
    } //fun createUser

    fun addManualPhoneNumber(donor: Donor, listener: ManualDonorInterFace)
    {
        fireStore
            .collection("donors")
            .whereEqualTo("phone", donor.phone)
            .get()
            .addOnSuccessListener { it ->

                val list = it.documents
                if (list.size>0)
                {
                    val donorId = list[0].data?.get("donor_id")
                    if(donorId == ""){
                        fireStore
                            .collection("donors")
                            .document(list[0].id)
                            .update("blood", donor.blood)
                            .addOnSuccessListener {
                                MainRepo.contactList.filter {
                                    it.phone == donor.phone
                                }.map {
                                    it.blood = donor.blood
                                }
                                listener.manualDonor(donor)
                            } //onSuccessListener
                            .addOnFailureListener {

                                Log.d(MainRepo.TAG, l+it)

                            }//onFailure Listener

                    }//if

                    else{
                        Log.d(MainRepo.TAG, "$l Donor Has his own Porfile You Cann't change it")
                    }//else
                } //if
                else
                {
                    fireStore.collection("donors")
                        .add(donor)
                        .addOnSuccessListener {
                            MainRepo.contactList.filter {
                                it.phone == donor.phone
                            }.map {
                                it.blood = donor.blood
                            }
                            listener.manualDonor(donor)
                        } //OnSuccessListene
                        .addOnFailureListener {
                            Log.d(MainRepo.TAG, l+it)
                        } //OnFailureListener
                } //else

            } //OnSuccessListener
            .addOnFailureListener {
                Log.d(MainRepo.TAG, l + it)
            } //OnFailureListener

    } //fun addManualPhoneNumber


    fun nameUpdate(name: String) = repo.nameUpdate(name)

} //class AuthViewModel


