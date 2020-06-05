package org.bloodnation.bloodnation.authentication

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_sign_up_confirmation.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.interfaces.NewDonorInterFace
import java.text.SimpleDateFormat
import java.util.*


class SignUpConfirmation : Fragment(),
    NewDonorInterFace, FirebaseAuth.AuthStateListener
{
    //Variable Declarations
    private val request = 3000
    private var auth = MainRepo.auth
    private val user = auth.currentUser
    private lateinit var model: AuthViewModel
    private lateinit var navController: NavController
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var date = Timestamp(Date())
    var that = this
    var phoneNumber = ""
    var formatNumber = ""
    var log = ""

    private var donorName = ""
    private var donorEmail = ""

    lateinit var locationProvider: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var currentLocation: Location

    private val location1 = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val location2 = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val permissionGranted = PackageManager.PERMISSION_GRANTED



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
       = inflater.inflate(R.layout.fragment_sign_up_confirmation, container, false)

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(this)
        locationProvider.removeLocationUpdates(locationCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { super.onViewCreated(view, savedInstanceState)

        /*
       auth.currentUser?.let {
           it.providerData?.let {
               it.forEach {profile->
                   Log.d(MainRepo.TAG, "Sign-in provider: ${profile.providerId}")
                   Log.d(MainRepo.TAG, "uid: ${profile.uid}")
                   Log.d(MainRepo.TAG, "name: ${profile.displayName}")
                   Log.d(MainRepo.TAG, "email: ${profile.email}")
                   Log.d(MainRepo.TAG, "email varified: ${profile.isEmailVerified}")
                   Log.d(MainRepo.TAG, "phone numebr: ${profile.phoneNumber}")
                   Log.d(MainRepo.TAG, "photo url: ${profile.photoUrl}")
               }
           }
       }


        */

        val locationPermission1 = ActivityCompat.checkSelfPermission(requireActivity(), location1)
        val locationPermission2 = ActivityCompat.checkSelfPermission(requireActivity(), location2)
        val permissionRationale1 = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), location1)
        val permissionRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), location2)

        currentLocation = Location("")
        currentLocation.longitude = 0.00
        currentLocation.latitude = 0.00

        if ((locationPermission1 != permissionGranted) or (locationPermission2 != permissionGranted))
        {
            if (permissionRationale1 or permissionRationale2){

                Snackbar.make(requireView(), "Need Permission", Snackbar.LENGTH_INDEFINITE).setAction(
                    "Enable", View.OnClickListener()
                    {
                        requestPermissions(arrayOf(location1, location2), request)

                    }).show()

            }
            else
            {
                requestPermissions(arrayOf(location1, location2), request)
            }
        }

        else {
            locationSetUp()
        }

        model = ViewModelProvider(this)[AuthViewModel::class.java]
        navController = Navigation.findNavController(view)


        //setting up the image
        Picasso.with(context).load(user?.photoUrl)
            .into(upPhotoInput)


        donorName = user?.displayName.toString()
        donorEmail = user?.email.toString()
        phoneNumber = user?.phoneNumber.toString()
        formatNumber = createPhoneNumber(phoneNumber)


        // show field based on provider data

        MainRepo.auth.currentUser?.let {

            it.providerData.let {list->
                list.forEach {profile->

                    if(profile.providerId == "google.com")
                    {
                        upEmailInput.visibility = View.GONE

                    }

                    if(profile.providerId == "phone")
                    {
                        upPhoneInput.visibility = View.GONE
                    }
                }




            }
        }

        //setting up other Information

        upNameInput.editText?.setText(donorName)
        upEmailInput.editText?.setText(donorEmail)




        // set phone number

        upPhoneInput.editText?.setText(formatNumber)
        upPhoneInput.editText?.addTextChangedListener{

          phoneNumber = upPhoneInput.editText?.text.toString()
          formatNumber = createPhoneNumber(phoneNumber)
          Log.d(MainRepo.TAG, "phone :$phoneNumber")
          Log.d(MainRepo.TAG, "format :$formatNumber")
      }
        upPhoneInput.editText?.setOnFocusChangeListener { v, hasFocus ->

            if(hasFocus)
            {
                upPhoneInput.editText?.setText(phoneNumber)
                Log.d(MainRepo.TAG, phoneNumber)
            } //if

            else
            {
                upPhoneInput.editText?.setText(formatNumber)
                Log.d(MainRepo.TAG, formatNumber)
            } //else
        } //OnFocusChangeListener


        // blood type

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.blood_type))
        upBloodInput.editText?.inputType  = InputType.TYPE_NULL
        (upBloodInput.editText as AutoCompleteTextView).setAdapter(adapter)


        //priority
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_selectable_list_item, resources.getStringArray(R.array.donate_urgency))
        upEagerInput.editText?.inputType  = InputType.TYPE_NULL
        (upEagerInput.editText as AutoCompleteTextView).setAdapter(priorityAdapter)


        //date of birth

        upDateInput.editText?.inputType = InputType.TYPE_NULL

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                createDate(calendar.time)

            }, //OnDateSetListener
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))

        upDateInput.editText?.setOnClickListener {
            if(!datePicker.isShowing){
                datePicker.show()
            }


        } //OnClickListener



        //submit button
        upSubmit.setOnClickListener {
            donorRegister(it) } //OnClickListener


        val observer = Observer<Donor>{
            MainRepo.donor = it
            upNameInput.editText?.setText(it.name)
            upEmailInput.editText?.setText(it.email)
            upPhoneInput.editText?.setText(createPhoneNumber(it.phone))

            upBloodInput.editText?.setText(it.blood)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.blood_type))
            (upBloodInput.editText as AutoCompleteTextView).setAdapter(adapter)


            val eagerness = resources.getStringArray(R.array.donate_urgency)[it.eagerness.toInt()]
            upEagerInput.editText?.setText(eagerness)
            val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_selectable_list_item, resources.getStringArray(R.array.donate_urgency))
            (upEagerInput.editText as AutoCompleteTextView).setAdapter(priorityAdapter)


            val dt = it.date.toDate()
            createDate(dt)
        }
        model.donorInfo.observe(viewLifecycleOwner, observer)

    }//fun onViewCreated

    private fun createDate(x: Date)
    {

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val formattedDate = dateFormat.format(x)
        upDateInput.editText?.setText(formattedDate)
        date = Timestamp(x)

    } //fun createDate

    private fun locationSetUp(){

        locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationCallback = object: LocationCallback(){


            override fun onLocationAvailability(p0: LocationAvailability?) {
                super.onLocationAvailability(p0)
                p0?.let {

                    if( it.isLocationAvailable)
                    {
                        Toast.makeText(requireContext(),"Location is available", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        Toast.makeText(requireContext(),"Location is not available", Toast.LENGTH_SHORT).show()
                    }

                }//let
                0            }

            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                p0?.let{
                    Log.d(MainRepo.TAG, it.toString())
                }
            }

        }

        locationProvider.requestLocationUpdates(locationRequest, locationCallback, requireActivity().mainLooper)
        locationProvider.lastLocation
            .addOnSuccessListener {location->

                location?.let {




                currentLocation = it
                Toast.makeText(requireContext(),it.longitude.toString(), Toast.LENGTH_SHORT).show()
                locationProvider.removeLocationUpdates(locationCallback)
                }

            }
            .addOnFailureListener {e->

                    Toast.makeText(requireContext(),e.toString(), Toast.LENGTH_SHORT).show()

            }




    }

    private fun createPhoneNumber(phoneNumber: String):String
    {
        var phone = phoneNumber

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {

                phone = PhoneNumberUtils.formatNumberToE164(phoneNumber, "BD")
                phone = PhoneNumberUtils.formatNumber(phone, "BD")
                //Log.d(MainRepo.TAG, phone)
                upPhoneInput.error = null
            } //try

            catch (e: Exception) {
                upPhoneInput.error = "Warning: Not valid Phone Number"
                Log.d(MainRepo.TAG, e.toString())
                try {
                    phone = PhoneNumberUtils.formatNumber(phoneNumber, "BD")
                } catch (e: java.lang.Exception) {
                    Log.d(MainRepo.TAG, e.toString())
                }


            }
        }
        return phone;
    }




    private fun donorRegister(v: View)
    {

        val nameValue: String =  upNameInput.editText?.text.toString().trim()
        val emailValue: String = upEmailInput.editText?.text.toString().trim()
        val dayOfBirthValue: String = upDateInput.editText?.text.toString().trim()
        val bloodGroupValue: String = upBloodInput.editText?.text.toString().trim()
        val eagernessValue : String = upEagerInput.editText?.text.toString().trim()
        val indexNumber = resources.getStringArray(R.array.donate_urgency).indexOf(eagernessValue).toLong()

        val error = "Mandatory Field"

        if (nameValue.isEmpty()) {
            log = "Display name is Empty"
            Snackbar.make(v, log, Snackbar.LENGTH_SHORT).show();
            upNameInput.error = error
            return
        } //if

        if (bloodGroupValue.isEmpty()) {
            log = "Blood Group is Empty"
            Snackbar.make(v, log, Snackbar.LENGTH_SHORT).show();
            upBloodInput.error = error
            return
        } //if

        if (eagernessValue.isEmpty()) {
            log = "Eagerness Field is Empty"
            Snackbar.make(v, log, Snackbar.LENGTH_SHORT).show();
            upEagerInput.error = error
            return
        } //if

        if (dayOfBirthValue.isEmpty()) {
            log = "Date of Birth Field is Empty"
            Snackbar.make(v, log, Snackbar.LENGTH_SHORT).show();
            upDateInput.error = error
            return
        } //if



        val donor = Donor(
            MainRepo.auth.currentUser!!.uid,
            nameValue,
            bloodGroupValue,
            indexNumber,
            date,
            emailValue,
            formatNumber,
            GeoPoint(currentLocation.latitude, currentLocation.longitude)
        )


        val callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                MainRepo.phoneUpdate(p0)
                model.createUser(donor, that)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
               Toast.makeText(requireContext(), p0.toString(), Toast.LENGTH_LONG).show()
            }

        }

        auth.currentUser?.let {
            if(it.displayName != donor.name)
            {
                model.nameUpdate(donor.name)
                Log.d(MainRepo.TAG, "displayname updated"+donor.name)
            }

            if(it.email != donor.email)
            {
                /*
                //it.updateEmail(donor.email)

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("619474561972-nlpu0elu7uivot8pud76b8df1imhlf28.apps.googleusercontent.com")
                    .requestEmail()
                    .build()

                val credentials = GoogleAuthProvider.getCredential(, null)

                it.linkWithCredential(credentials)
                    .addOnSuccessListener { result ->
                        Log.d(MainRepo.TAG, "result : ${result.toString()}")
                    }
                    .addOnFailureListener {e ->
                        Log.d(MainRepo.TAG, "exeption : ${e.toString()}")
                    }
                Log.d(MainRepo.TAG, "email updated")

                 */

                Log.d(MainRepo.TAG, "will Implement in future")
            }
/*
            if(PhoneNumberUtils.compare(it.phoneNumber, donor.phone))
            {

                model.createUser(donor, this)

            }
            else
            {
                MainRepo.phoneVerify(requireActivity(), donor, callbacks)

            }

 */
            model.createUser(donor, this)

        }

    } //fun donorRegister()

    override fun addedSuccessFully(bool: Boolean, log:String)
    {
        Toast.makeText(requireContext(), log, Toast.LENGTH_SHORT).show()
        if(bool) {
            val action =
                SignUpConfirmationDirections.actionSignUpConfirmationToSearchContactFragment()
            navController.navigate(action)
        }
    } //fun addedSuccessFully

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_main, menu)
    }//fun onCreateOptionsMenu


    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

        when (item.itemId) {

            R.string.signOut ->
            {
                AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                   // val action = SearchContactFragmentDirections.actionSearchContactFragmentToMainFragment2()
                   // control.navigate(action)
                }
            }

        }

        return super.onOptionsItemSelected(item)
    } //fun onOptionsItemSelected

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser == null) {
            val action = SignUpConfirmationDirections.actionSignUpConfirmationToLogInOrRegister()
            navController.navigate(action)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == request){
            if((grantResults[0] == 0) or (grantResults[1] == 0))
            {
                locationSetUp()
            }
        }
    }

} //class SignUpConfirmation
