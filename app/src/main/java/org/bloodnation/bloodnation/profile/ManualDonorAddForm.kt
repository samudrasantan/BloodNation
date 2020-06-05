package org.bloodnation.bloodnation.profile

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.form_manual_add.view.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.authentication.ManualDonorInterFace
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import java.util.*

class ManualDonorAddForm : DialogFragment()
{
    val contact by lazy {
        PhoneBookWithNumbers()
    } //lazy


    lateinit var listener : ManualDonorInterFace



    private lateinit var model : ManualViewModel

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
        {
            model = ViewModelProvider(this)[ManualViewModel::class.java]
            return activity?.let {

                val builder = MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_MaterialComponents_Dialog)
                val view = it.layoutInflater.inflate(R.layout.form_manual_add, null)
                view.manualName.text = contact.phoneBook.contactName
                val list: ArrayList<String> = ArrayList<String>(0)

                contact.numberList.forEach{ p->
                        list.add(p.phoneNumber)
                }
                view.manualPhone.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.blood_type))
                view.manualBlood.editText?.inputType = InputType.TYPE_NULL
                view.manualBlood.editText?.setText(contact.phoneBook.bloodGroup)
                (view.manualBlood.editText as AutoCompleteTextView).setAdapter(adapter)

                builder.setView(view)
                    .setPositiveButton(R.string.Submit){ dialog, id ->
                        contact.phoneBook.bloodGroup = view.manualBlood.editText?.text.toString()
                        model.updateBlood(contact, listener)
                        //model.addManualPhoneNumber(Donor(phone = view.manualPhoneAddField.text.toString(), blood = view.manualBloodGroupAddField.selectedItem.toString()), listener)
                    }
                    .setNegativeButton(R.string.fui_cancel){ dialog, id -> // Toast.makeText(requireContext(), "User Cancelled the operaton$id", Toast.LENGTH_SHORT).show()
                    }

                builder.create()

            } //let
                ?: throw IllegalStateException("ManualDonorAddForm.kt")
        } //fun onCreateDialog

        fun setListener(tag: String, listener: ManualDonorInterFace){
            this.listener = listener
        }
    } //class ManualDonorAddForm