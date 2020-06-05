package org.bloodnation.bloodnation.history

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_sign_up_confirmation.*
import kotlinx.android.synthetic.main.history_add_dialog.*
import kotlinx.android.synthetic.main.history_add_dialog.view.*
import org.bloodnation.bloodnation.R
import java.text.SimpleDateFormat
import java.util.*

class HistoryAddDialog: DialogFragment() {

    private  var date = Timestamp(Date())
    private lateinit var model: HistoryViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        model = ViewModelProvider(this)[HistoryViewModel::class.java]
        return activity?.let {

            val builder = MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_MaterialComponents_Dialog)
            val view = it.layoutInflater.inflate(R.layout.history_add_dialog, null)
            view.dialogDateInput.editText?.inputType  = InputType.TYPE_NULL
            createDate(view, Date())
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { v, year, month, dayOfMonth ->

                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    createDate(view, calendar.time)

                }, //OnDateSetListener
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))

            view.dialogDateInput.editText?.setOnClickListener {

                datePicker.show()
            }

            builder.setView(view)
                .setPositiveButton(R.string.Submit){ dialogInterface: DialogInterface, i: Int ->
                    model.addHistory(date)
                }
                .setNegativeButton(R.string.fui_cancel){ dialogInterface: DialogInterface, i: Int ->


                }
            builder.create()

        } ?: throw IllegalStateException("ManualDonorAddForm.kt")
    }

    private fun createDate(view: View, x: Date)
    {

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val formattedDate = dateFormat.format(x)
        view.dialogDateInput.editText?.setText(formattedDate)
        date = Timestamp(x)

    } //fun createDate
}