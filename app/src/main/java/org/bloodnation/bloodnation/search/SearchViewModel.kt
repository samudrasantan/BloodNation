package org.bloodnation.bloodnation.search

import android.app.Application
import androidx.lifecycle.*
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import org.bloodnation.bloodnation.model.Contacts
import org.bloodnation.bloodnation.repository.MainRepo

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    var contactList: MutableLiveData<List<Contacts>> = MutableLiveData()
    val repo = MainRepo
    val context = application
    private var text = ""

    fun updateDatabase() = repo.updateDatabase(context)

    private  val type: MutableLiveData<Int> = MutableLiveData()
    val liveData: LiveData<List<PhoneBookWithNumbers>> = Transformations
        .switchMap(type){

            when(it)
            {
                0 -> MainRepo.getAll(context)
                10-> MainRepo.getSelectedName(context, text)
                else -> MainRepo.getSelected(context, it)
            }

        }

    fun getType(newType:Int, newText: String = ""){
        type.value = newType
        this.text = newText
    }

    fun cancelJob(){
        MainRepo.cancelJob()
    }

    fun updateBloodInfo(book: PhoneBookWithNumbers, position: Int, listener:OnAdapterValueChanged) = repo.updateBloodInfo(context, position, book, listener)


}