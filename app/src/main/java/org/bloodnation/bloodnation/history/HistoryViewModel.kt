package org.bloodnation.bloodnation.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import org.bloodnation.bloodnation.repository.MainRepo

class HistoryViewModel(application: Application): AndroidViewModel(application) {


    fun addHistory(date: Timestamp) = MainRepo.addHistory(date)

}