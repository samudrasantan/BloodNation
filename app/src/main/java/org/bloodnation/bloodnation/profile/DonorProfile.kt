package org.bloodnation.bloodnation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.donor_profile.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.authentication.Donor


class DonorProfile : Fragment()
{
    private lateinit var model : DonorProfileViewModel

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.donor_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(this)[DonorProfileViewModel::class.java]

        val args : DonorProfileArgs by navArgs()
       val donorId = args.donorId


        val observer = Observer<Donor> {
            profileNameField.text = it.name
            profileBloodField.text =it.blood
            progressBar2.progress = it.eagerness.toInt()
            priorityText.text = resources.getStringArray(R.array.donate_urgency)[it.eagerness.toInt()]
        }
        model.getDonorProfile(requireContext(), donorId).observe(viewLifecycleOwner, observer)
    }


    /*
    val contact by lazy {
        ContactsLocal()
    } //lazy
    */

/*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = MaterialAlertDialogBuilder(
                it,
                R.style.ThemeOverlay_MaterialComponents_Dialog
            )
            val view = it.layoutInflater.inflate(R.layout.donor_profile, null)
            view.profileNameField.text = contact.name
            view.profileBloodField.text = contact.blood
            view.profileClose.setOnClickListener {

                this.dismiss()
            }

            builder.setView(view)
            builder.create()

        } //let
            ?: throw IllegalStateException("ManualDonorAddForm.kt")

    }

    
*/

} // class ManualMenuFragment
