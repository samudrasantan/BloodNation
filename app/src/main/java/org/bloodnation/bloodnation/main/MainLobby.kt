package org.bloodnation.bloodnation.main

import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.android.synthetic.main.fragment_test.imageView7
import kotlinx.android.synthetic.main.main_lobby.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.interfaces.ContactsListener
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.settings.SettingsEntity


class MainLobby : Fragment(), ContactsListener, FirebaseAuth.AuthStateListener,
    UserUpdateListener
{

    var t = MainRepo.TAG
    var that = this
    private val request = 2000
    private val auth = MainRepo.auth
    private lateinit var navController: NavController
    private lateinit var viewModel: MainLobbyViewModel
    private lateinit var mainViewModel: MainActivityViewModel
    private val permissionGranted = PackageManager.PERMISSION_GRANTED
    private val readContacts = android.Manifest.permission.READ_CONTACTS
    private val writeContacts = android.Manifest.permission.WRITE_CONTACTS




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.main_lobby, container, false)


    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }
    override fun onStop() {
        super.onStop()
        //(activity as AppCompatActivity).supportActionBar?.show()
        auth.removeAuthStateListener(this)
    }              // fun onStop


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(this)[MainLobbyViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
       // logoAnimation()
        checkPermission()

/*
        val observer = Observer<Donor>{

            if(it.donor_id == MainRepo.auth.currentUser?.uid) {
                val action = MainLobbyDirections.actionTestFragmentToSearchContactFragment()
                navController.navigate(action)
                Log.d(t, "donor observer ${it.name}")
            }

            else {
                val action = MainLobbyDirections.actionTestFragmentToSignUpConfirmation()
                navController.navigate(action)
            }
        }





        viewModel.donor.observe(viewLifecycleOwner, observer)

 */

        val observer = Observer<SettingsEntity>{

            if (it.settingsTheme == 1){
                back.background = resources.getDrawable(R.color.colorDarkBackground)
            }

        }
        mainViewModel.getSettings.observe(viewLifecycleOwner, observer)


    }

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
                        requestPermissions(arrayOf(readContacts, writeContacts), request)

                    }).show()
            }
            else{
                requestPermissions(arrayOf(readContacts, writeContacts), request)
            }

        }
        else
        {
            viewModel.updateDatabase()
            viewModel.returnDonor(this)
        }

    }

    private fun logoAnimation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val x = imageView7.drawable
            val avd: AnimatedVectorDrawable = x as AnimatedVectorDrawable
            avd.start()
        }
    }



    override fun contactsUpdated(bool: Boolean) {
        Log.d(MainRepo.TAG, ""+bool)
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser == null)  {
            val action = MainLobbyDirections.actionTestFragmentToMainFragment2()
            navController.navigate(action)

        }

    }


    override fun updatedDonor(bool: Boolean, log: String)
    {
        if(bool)
        {
            val action = MainLobbyDirections.actionTestFragmentToSearchContactFragment()
           navController.navigate(action)
        } //if

        else
        {
           val action = MainLobbyDirections.actionTestFragmentToSignUpConfirmation()
           navController.navigate(action)
        } // else

        //Toast.makeText(requireContext(), log, Toast.LENGTH_LONG).show()
    } //fun returnAuth

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

       viewModel.returnDonor(this)

        if(requestCode == request){
            if((grantResults[0] == 0) or (grantResults[1] == 0))
            {
                Log.d(MainRepo.TAG, "permission accepted")

                viewModel.updateDatabase()
            }

            else{
                Log.d(MainRepo.TAG, "permission denied")
            }
        }
    }
}
