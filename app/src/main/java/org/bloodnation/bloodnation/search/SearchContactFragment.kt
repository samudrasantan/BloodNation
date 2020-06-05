package org.bloodnation.bloodnation.search

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_contact_items.view.*
import kotlinx.android.synthetic.main.fragment_search_contact.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import org.bloodnation.bloodnation.profile.ManualDonorAddForm
import org.bloodnation.bloodnation.interfaces.OnClickBloodGroupListener
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.authentication.ManualDonorInterFace
import org.bloodnation.bloodnation.history.HistoryAddDialog
import org.bloodnation.bloodnation.main.MainActivityViewModel
import org.bloodnation.bloodnation.profile.DonorProfile
import org.bloodnation.bloodnation.settings.SettingsEntity


class SearchContactFragment : Fragment(), OnClickBloodGroupListener,
    ManualDonorInterFace, FirebaseAuth.AuthStateListener, OnAdapterValueChanged
{
    private lateinit var contactAdapter: ContactRecyclerViewAdapter
    private lateinit var navController: NavController
    private lateinit var model: SearchViewModel
    private lateinit var mainViewModel: MainActivityViewModel

    private val readContacts = android.Manifest.permission.READ_CONTACTS
    private val writeContacts = android.Manifest.permission.WRITE_CONTACTS
    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    private var dialog = ManualDonorAddForm()
    private var historyDialog = HistoryAddDialog()
    private var profileDialog = DonorProfile()
    var position = 0

    private var t = MainRepo.TAG
    private var l = "SearchContactFragment.kt"

    private val requestCode = 4000



    var that = this



    override fun onStart() {
        super.onStart()
        MainRepo.auth.addAuthStateListener(this)
    }
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);

    } //fun onCreate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_search_contact, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(this)[SearchViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        checkPermission()



        combinedRV.apply{
            layoutManager = LinearLayoutManager(requireContext())
            contactAdapter =
                ContactRecyclerViewAdapter()
            adapter = contactAdapter
        } //RecycleView

        val observer = Observer<List<PhoneBookWithNumbers>> { list->

            list.forEachIndexed { index, book ->
                model.updateBloodInfo(book, index, this)
            }


            contactAdapter.submitData(list, that)
            combinedRV.swapAdapter(contactAdapter, false)
        }
        val settingObserver = Observer<SettingsEntity>{


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if(it.settingsTheme == 1)
                {
                    view.back1?.setBackgroundColor(resources.getColor(R.color.colorDarkBackground))
                    view.back2?.setBackgroundColor(resources.getColor(R.color.colorDarkLight))
                    view.listBloodGroup?.setTextColor(resources.getColor(R.color.colorLight1))
                    view.contactNames?.setTextColor(resources.getColor(R.color.colorLight1))
                    view.location?.setTextColor(resources.getColor(R.color.colorLight))
                    view.phoneNumberField?.setTextColor(resources.getColor(R.color.colorLight1))
                    view.contactsAdd?.setBackgroundColor(resources.getColor(R.color.colorLight1))
                    combinedSP.setBackgroundColor(resources.getColor(R.color.BlackPrimary))
                    combinedRV.setBackgroundColor(resources.getColor(R.color.BlackPrimary))

                }

            }
        }
        mainViewModel.getSettings.observe(viewLifecycleOwner, settingObserver)
        model.liveData.observe(viewLifecycleOwner, observer)

        navController = Navigation.findNavController(view)

        combinedSP.setSelection(resources.getStringArray(R.array.blood_search).indexOf(MainRepo.bloodGroup))
        combinedSP.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
        {
            var counter = 0;
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
                //Toast.makeText(requireContext(), "$t $parent", Toast.LENGTH_LONG).show()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                model.getType(position)
            }
        }

        bottomNavigation.setOnNavigationItemSelectedListener {

            when(it.itemId)
            {
                R.string.profile -> {
                    navController.navigate(R.id.action_searchContactFragment_to_profileFragment)

                }

                R.string.signOut -> {

                    AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                        //val action = SearchContactFragmentDirections.actionSearchContactFragmentToLogInOrRegister()
                        //navController.navigate(action)
                    }

                }

                R.string.add -> {
                    historyDialog.show(parentFragmentManager, "HISTORY DIALOG")
                }

                else -> {
                    Log.d(MainRepo.TAG, "")

                }
            }
            return@setOnNavigationItemSelectedListener false
        }

    } //fun onViewCreated

    private fun checkPermission(){

        val readPermission = ContextCompat.checkSelfPermission(requireContext(), readContacts)
        val writePermission = ContextCompat.checkSelfPermission(requireContext(), writeContacts)
        val permissionRationale1 = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), readContacts)
        val permissionRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), writeContacts)


        if ((readPermission != permissionGranted) or (writePermission != permissionGranted) ) {

            if (permissionRationale1 or permissionRationale2){
                Snackbar.make(requireView(), "Need Permission", Snackbar.LENGTH_INDEFINITE).setAction(
                    "Enable", View.OnClickListener()
                    {
                        requestPermissions(arrayOf(readContacts, writeContacts), requestCode)

                    }).show()
            }
            else{
                requestPermissions(arrayOf(readContacts, writeContacts), requestCode)
            }

        }
        else
        {

            model.updateDatabase()
        }

    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == requestCode){
            if((grantResults[0] == 0) or (grantResults[1] == 0))
            {
                model.updateDatabase()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    { inflater.inflate(R.menu.menu_layout,menu)

        val search = menu.findItem(R.id.search)
        val searchV = search.actionView as SearchView
        val listener = object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //Toast.makeText(requireContext(), newText, Toast.LENGTH_SHORT).show()
                model.getType( 10,  newText.toString())
                return false
            }

        }

        searchV.setOnQueryTextListener(listener)
    }//fun onCreateOptionsMenu


    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId) {

            R.string.manualMenu ->
            {
                if(bottomNavigation.visibility == View.GONE)
                    bottomNavigation.visibility = View.VISIBLE
                else
                    bottomNavigation.visibility = View.GONE
            }



            R.string.search ->
            {
                Toast.makeText(requireContext(), "search", Toast.LENGTH_SHORT).show()

            }

        }

        return super.onOptionsItemSelected(item)
    } //fun onOptionsItemSelected



    override fun onClickItem(contacts: PhoneBookWithNumbers, position: Int) {


        if(contacts.phoneBook.donorId == "")
        {
            dialog.contact.phoneBook.contactId = contacts.phoneBook.contactId
            dialog.contact.phoneBook.contactName = contacts.phoneBook.contactName
            dialog.contact.numberList = contacts.numberList
            dialog.contact.phoneBook.bloodGroup = contacts.phoneBook.bloodGroup
            dialog.listener = this
            dialog.show(parentFragmentManager, "manualDonorSubmitForm")
            this.position = position
            //Toast.makeText(requireContext(),"${contacts.name}", Toast.LENGTH_SHORT).show()
            //Log.d(MainRepo.TAG, position.toString())

        } //if

        else{
            val donorId = contacts.phoneBook.donorId
            val action = SearchContactFragmentDirections.actionSearchContactFragmentToDonorProfile(donorId)
            //val anim = FragmentNavigatorExtras(listBloodGroup to "blood")

            //val bundle = bundleOf("donor_id" to contacts.phoneBook.donorId)
            navController.navigate(action)

            //profileDialog.contact.name = "Donor Has no Profile"
            //profileDialog.contact.blood = "Please Invite"
            //profileDialog.show(parentFragmentManager, "manualDonorSubmitForm")

        } //else
    } //fun onClickItem

    override fun manualDonor(donor: Donor) {

      //  contactAdapter.notifyItemChanged(position, donor.blood)

    }


    override fun onDestroy() {
        super.onDestroy()
        model.cancelJob()
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        Log.d(MainRepo.TAG, "on auth"+p0.currentUser?.uid)

        if (p0.currentUser == null) {
            val action = SearchContactFragmentDirections.actionSearchContactFragmentToLogInOrRegister()
            navController.navigate(action)
        }
    }

    override fun onValueChanged(index: Int, book: PhoneBookWithNumbers) {
        contactAdapter.notifyItemChanged(index, book)
    }
} //class SearchContactFragment
