package org.bloodnation.bloodnation.fragment.tab




import android.view.View
import android.view.ViewGroup
import org.bloodnation.bloodnation.R
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.bloodnation.bloodnation.search.ContactRecyclerViewAdapter
import org.bloodnation.bloodnation.profile.ManualViewModel
import kotlinx.android.synthetic.main.fragment_manual.*
import org.bloodnation.bloodnation.authentication.AuthViewModel
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.os.Bundle
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.profile.ManualDonorAddForm
import org.bloodnation.bloodnation.authentication.ManualDonorInterFace


class ManualFragment : Fragment(),
    ManualDonorInterFace
{
    lateinit var model: ManualViewModel
    lateinit var model2: AuthViewModel
    private lateinit var contactAdapter: ContactRecyclerViewAdapter
    private var dialog =
        ManualDonorAddForm()
    var position = 0

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_manual, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(this)[ManualViewModel::class.java]
        model2 = ViewModelProvider(this)[AuthViewModel::class.java]

        rvmanualContactList.apply{
            layoutManager = LinearLayoutManager(requireContext())
            contactAdapter =
                ContactRecyclerViewAdapter()
            adapter = contactAdapter
        } //RecycleView

        //contactAdapter.submitData(MainRepo.contactList, this)

/*
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                1000
            )

            //model.populateContacts(requireContext())

        }
        else {

            //model.populateContacts(requireContext())

        }

*/

        //Live Data
/*
        var observer = Observer<List<Contacts>>{ list->

            contactAdapter.submitData(list, this)

            Log.d("bloodNation", "Observer Fired:: Search Fragment"+list[0].contactName)

        }

        model.liveData().observe(viewLifecycleOwner, observer)

*/


/*
            MaterialAlertDialogBuilder(view.context, R.style.ThemeOverlay_MaterialComponents_Dialog)
                .setTitle(R.string.addDonor)
                .setView(layoutInflater.inflate(R.layout.form_manual_add, null))
                .apply {
                    setPositiveButton(R.string.update, DialogInterface.OnClickListener()
                    {dialog, which ->
                        //do something if clicked Submit
                    }


                    )

                }
                .create().show()

*/
        model2.addManualPhoneNumber(Donor(),this)

    } //fun onViewCreated





    override fun manualDonor(donor: Donor) {
       // Toast.makeText(requireContext()," : + ${donor.blood}", Toast.LENGTH_SHORT).show()

        contactAdapter.notifyItemChanged(position, donor.blood)

    }

} //class ManualFragment
