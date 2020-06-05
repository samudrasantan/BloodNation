package org.bloodnation.bloodnation.repository

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.contact.PhoneBookEntity
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import org.bloodnation.bloodnation.number.PhoneNumberEntity
import org.bloodnation.bloodnation.model.Contacts
import org.bloodnation.bloodnation.model.ContactsLocal
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.main.UserUpdateListener
import org.bloodnation.bloodnation.model.PhoneContact
import org.bloodnation.bloodnation.authentication.ManualDonorInterFace
import org.bloodnation.bloodnation.history.History
import org.bloodnation.bloodnation.interfaces.NewDonorInterFace
import org.bloodnation.bloodnation.search.OnAdapterValueChanged
import org.bloodnation.bloodnation.settings.SettingsEntity
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object MainRepo {

    const val TAG = "bloodNation"
    private var LOG = ""
    private const val NAME = "MainRepo.kt"
    private var objectList = ArrayList<PhoneContact>()

    var job: CompletableJob? = null
    var log = ""

    //Table
    const val COLLECTION_DONORS = "donors"
    private const val DONORS_ID = "donor_id"
    const val DONORS_PHONE = "phone"
    private const val DONORS_BLOOD = "blood"
    private const val DONORS_NAME = "name"
    private const val DONORS_EAGERNESS = "eagerness"
    private const val DONORS_DATE = "date"
    private const val DONORS_EMAIL = "email"
    private const val DONORS_LOCATION = "location"


    const val COLLECTION_HISTORY = "history"


    var bloodGroup = ""

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val fireStore = FirebaseFirestore.getInstance()
    var donor: Donor? =
        Donor()


    //private val phoneContacts by lazy { ArrayList<PhoneContact>() }


    val contactList  by lazy {  ArrayList<ContactsLocal>() }
    val list by lazy { ArrayList<Contacts>() }



    fun addSettings(context: Context){
        val database = BnRoomDataBase.database(context)
        CoroutineScope(IO).launch {
            database.settingsDao().insert(SettingsEntity())
        }
    }

    fun updateSetting(context: Context, state: Int)
    {
        val database = BnRoomDataBase.database(context)
        CoroutineScope(IO).launch {
            database.settingsDao().updateTheme(state)
        }
    }

    fun getSettings(context: Context): LiveData<SettingsEntity>
    {
        val database = BnRoomDataBase.database(context)

        return object: LiveData<SettingsEntity>() {
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    database.settingsDao().getSettings().collect {
                        withContext(Main){
                            value = it
                        }

                    }
                }
            }

        }

    }

    fun updateDatabase(context: Context){

        val database = BnRoomDataBase.database(context)
        val startTime = System.currentTimeMillis()


        job = Job()

        val contactUri = ContactsContract.Contacts.CONTENT_URI
        val contactName = ContactsContract.Contacts.DISPLAY_NAME
        val contactHasPhoneNumber = ContactsContract.Contacts.HAS_PHONE_NUMBER
        val contactId = ContactsContract.Contacts._ID
        val contactProjection = arrayOf<String>(contactId, contactName, contactHasPhoneNumber)
        val contactSelection = "$contactHasPhoneNumber = 1"

        job?.let {job ->

            CoroutineScope(IO + job )
                .launch {
                    val contentCursor = context.contentResolver.query(contactUri, contactProjection, contactSelection, null, contactName )
                    contentCursor?.let {cursor ->

                        while(cursor.moveToNext())
                        {
                            val id = cursor.getString(0).toInt()
                            val name = cursor.getString(1)
                            findPhoneNumbers(context, database, id, name)
                        }//while
                        deleteDuplicates(context)
                    } //let
                    contentCursor?.close()
                    job.complete()

                    val endTime = System.currentTimeMillis()
                    Log.d(TAG, "updateDatabase run time ${(endTime - startTime)*.001} seconds")

                }//let

        }//let

    } //fun updateDatabase

    //called from updateDatabase
    private suspend fun findPhoneNumbers(context: Context, database: BnRoomDataBase, id: Int, name: String)
    {
        val commonUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val commonContactId = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        val commonPhoneNo = ContactsContract.CommonDataKinds.Phone.NUMBER

        val commonSelection = "$commonContactId=$id"
        val commonProjection = arrayOf<String>(commonPhoneNo)


        val contentCursor = context.contentResolver.query(commonUri, commonProjection, commonSelection, null, commonPhoneNo )

        contentCursor?.let {cursor ->
            if(cursor.count > 0){
                database.phoneBookDao().insert(
                    PhoneBookEntity(
                        id,
                        name,
                        "",
                        "",
                        0
                    )
                )
            }

            val temporaryList = ArrayList<String>()
            var temporaryBool = false
            while(cursor.moveToNext())
            {
                var phoneNumber = cursor.getString(0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    try{
                        val x =  PhoneNumberUtils.formatNumberToE164(phoneNumber, "BD")
                        if (x != null){
                            phoneNumber = PhoneNumberUtils.formatNumber(x, "BD")
                        }
                    }
                    catch (e: Exception)
                    {
                        Log.d(TAG, "$phoneNumber : $e ")
                    }

                if (temporaryList.size == 0)
                {
                    temporaryList.add(phoneNumber)
                    database.phoneNumberDao().insert(
                        PhoneNumberEntity(
                            phoneNumber,
                            id
                        )
                    )
                    searchBloodGroup(database, id, phoneNumber)

                } //if

                else {
                    temporaryList.forEach { tl->
                        if (PhoneNumberUtils.compare(tl, phoneNumber))
                        {
                            temporaryBool = true

                        } //if
                    } //for eacch

                    if (!temporaryBool) {
                        temporaryList.add(phoneNumber)
                        //Log.d(TAG, " : $phoneNumber")
                        database.phoneNumberDao().insert(
                            PhoneNumberEntity(
                                phoneNumber,
                                id
                            )
                        )
                        searchBloodGroup(database, id, phoneNumber)
                    }
                } //else
            }//while
        } //let
        contentCursor?.close()
    }// fun findPhoneNumbers

    //called from findPhoneNumbers
    private fun searchBloodGroup(database: BnRoomDataBase, id: Int, phone: String)
    {
        fireStore
            .collection(COLLECTION_DONORS)
            .whereEqualTo(DONORS_PHONE, phone)
            .get()
            .addOnSuccessListener {
                val document = it.documents

                if (document.size >0){

                    if(document.size>1)
                    {
                        //  Log.d(TAG, "Two Document in the database")
                    }



                    val bloodGroup = document[0].getString(DONORS_BLOOD).toString()
                    val donorId = document[0].getString(DONORS_ID).toString()
                    var priorityIndex = document[0].getLong(DONORS_EAGERNESS)
                    var location = document[0].getGeoPoint(DONORS_LOCATION)
                    CoroutineScope(IO).launch {
                        if (priorityIndex == null) {
                            priorityIndex = 0
                        }

                        if (location == null){
                            location = GeoPoint(0.00, 0.00)
                        }

                        database.phoneBookDao().update(id,bloodGroup, donorId, priorityIndex!!, location!!.latitude, location!!.longitude)
                        // Log.d(TAG, "$id: $bloodGroup")
                    }




                    //Log.d(TAG, "searchBlood : $phone : $bloodGroup")


                }//if
                else
                {
                    //Log.d(TAG, "No phone number found in database")
                }
            }
    } //fun searchBloodGroup

    //called from updateDatabase
    private fun deleteDuplicates(context: Context) {

        val job3 = Job()
        val database = BnRoomDataBase.database(context)
        job3.let { job ->
            CoroutineScope(IO + job3).launch {
                val list: List<PhoneBookWithNumbers> = database.phoneBookDao().getAll()
                list.forEach {

                    //Log.d(TAG, it.phoneBook.toString()+ " : "+ it.numberList.toString())

                    if(it.numberList.isEmpty())
                    {
                        // Log.d(TAG, it.phoneBook.contactName)
                        database.phoneBookDao().delete(it.phoneBook)
                    }
                }
                job.complete()
            }


        }
    }


    fun donorInfo(): LiveData<Donor>
    {
        return object: LiveData<Donor>()
        {
            override fun onActive() {
                super.onActive()
                fireStore
                    .collection(COLLECTION_DONORS)
                    .whereEqualTo(DONORS_ID, auth.currentUser?.uid)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        querySnapshot?.let {
                            if(it.documents.size>1)
                                Log.d(TAG, "Error: More Than One Donor ID")

                            if(it.documents.size>0){
                                value = it.documents[0].toObject(Donor::class.java)

                            }

                        }


                    }

            }

        }

    }


    fun updateBloodInfo(context: Context, index: Int, book: PhoneBookWithNumbers, listener: OnAdapterValueChanged){


            val database = BnRoomDataBase.database(context)
            book.numberList.forEach {
                fireStore.collection(COLLECTION_DONORS)
                    .whereEqualTo(DONORS_PHONE, it.phoneNumber)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        if (querySnapshot!=null && querySnapshot.count()>0 )
                        {
                            querySnapshot.documents.forEach {d->
                                CoroutineScope(IO).launch {
                                    val blood = d.getString("blood").toString()
                                    val id = d.getString("donor_id").toString()
                                    val eagerness = d.getLong("eagerness")?:0
                                    val latitude = d.getGeoPoint("location")?.latitude?:0.00
                                    val longitude = d.getGeoPoint("location")?.longitude?:0.00
                                    val phone = PhoneBookEntity(book.phoneBook.contactId, book.phoneBook.contactName, blood, id, eagerness.toInt(), latitude, longitude)
                                    val b = PhoneBookWithNumbers(phone,book.numberList)
                                    database.phoneBookDao().update(book.phoneBook.contactId, blood,id,eagerness, latitude, longitude )

                                    withContext(Main){
                                        listener.onValueChanged(index, b)
                                    }

                                }

                                //database.phoneBookDao().update(id,bloodGroup, donorId, priorityIndex!!, location!!.latitude, location!!.longitude)
                            }

                        }

                    }
            }
    }



    //check if donor is present
    fun returnDonor (listener: UserUpdateListener)
    {
        val startTime = System.currentTimeMillis()
        fireStore
            .collection(COLLECTION_DONORS)
            .whereEqualTo(DONORS_ID, auth.currentUser?.uid)
            .get()
            .addOnSuccessListener{
                val listQS = it.documents

                if( listQS.size > 0)
                {
                    donor = listQS[0].toObject(Donor::class.java)
                    bloodGroup = donor?.blood.toString()
                    LOG = "Check Point 3 : Profile Updated : ${donor?.name}"

                    if(listQS.size > 1)
                    {
                        LOG = "Check Point 3 : Duplicate User Profile"
                        // Log.d(TAG, LOG)
                    } //if

                    listener.updatedDonor(true, LOG )
                    LOG = ""
                }//if
                else {
                    LOG = "Check Point 3 :Profile Not Updated"
                    listener.updatedDonor(false, LOG)
                    LOG = ""
                }//else


            } //OnSuccessListener
            .addOnFailureListener{
                LOG = "Check Point 3 : $it"
                // Log.d(TAG, LOG)
                listener.updatedDonor(false, LOG)
            }//OnFailureListener

        val endTime = System.currentTimeMillis()
        Log.d(TAG, "returnDonor run time ${(endTime - startTime)*.001} seconds")
    } // fun returnDonor




    fun addHistory(time: Timestamp){
        auth.currentUser?.let {
            val history = History(it.uid, time)
            fireStore
                .collection(COLLECTION_HISTORY)
                .add(history)

        }

    }

    //call from AuthViewModel
    fun createUser(donor: Donor, listener: NewDonorInterFace)
    {
        fireStore
            .collection(COLLECTION_DONORS)
            .whereEqualTo(DONORS_ID, donor.donor_id)
            .get()
            .addOnSuccessListener {
                val data = it.documents
                if(data.size>0){
                    updateDonor(donor, listener, data[0].id)
                    Log.d(TAG, "update donor")
                    //Log.d(TAG, data[0].getString("donor_id").toString())
                }

                else
                {
                    Log.d(TAG, "create donor")
                    createProfile(donor, listener)

                }
            }
            .addOnFailureListener {
                log = "Check Point 4 : $it"
                listener.addedSuccessFully(false, log)
                log = ""
            }
    } //fun createUser


    fun phoneVerify(activity: Activity, donor: Donor, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(donor.phone, 60, TimeUnit.SECONDS,activity, callbacks)
        auth.currentUser?.let {
        }
    }


    fun nameUpdate(name: String){

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name).build()
        auth.currentUser?.updateProfile(profileUpdate)

    }

    fun phoneUpdate(p0: PhoneAuthCredential)
    {
        auth.currentUser?.updatePhoneNumber(p0)
    }


    private fun createProfile(donor: Donor, listener: NewDonorInterFace){
        fireStore
            .collection(COLLECTION_DONORS)
            .whereEqualTo("phone", donor.phone)
            .get()
            .addOnSuccessListener {
                val data = it.documents
                if (data.size>0)
                {
                    Log.d(TAG, "phone number Exists")

                    if(data[0].getString(DONORS_ID).toString() == "")
                    {

                        Log.d(TAG, "preExisting Phone Number")
                        createWithPreExistingPhone(donor, listener, data[0].id)

                    }

                    else
                    {
                        log = "An account is already registered under this phone number in the name of ${data[0].getString(
                            DONORS_NAME)}. Please Try a different number"
                        listener.addedSuccessFully(false, log)
                    }
                    //
                } //if

                else{
                    createNew(donor, listener)
                } //else
                //Log.d("bloodNation", "AuthViewModel.kt : Found")
            } //onSuccessLister
            .addOnFailureListener{
                log = "Check Point 4 : $it"
                listener.addedSuccessFully(false, log)
                log = ""
            } // OnFailureListener
    }

    private fun createWithPreExistingPhone(donor: Donor, listener: NewDonorInterFace, id: String){

        fireStore
            .collection(COLLECTION_DONORS)
            .document(id)
            .update(DONORS_ID, donor.donor_id, DONORS_NAME,donor.name, DONORS_BLOOD, donor.blood, DONORS_EAGERNESS, donor.eagerness, DONORS_DATE,donor.date, DONORS_EMAIL,  donor.email, DONORS_PHONE,  donor.phone, DONORS_LOCATION, donor.location)
            .addOnSuccessListener {
                log = "Check Point 4: Added with pre-exiting Phone Numebr"
                this.donor = donor
                listener.addedSuccessFully(true, log)
                log = ""
            } //onSuccessListener
            .addOnFailureListener {

                log = "Check Point 4 : $it"
                listener.addedSuccessFully(false, log)
                log = ""

            }//onFailure Listener
    }

    private fun createNew(donor: Donor, listener: NewDonorInterFace){

        fireStore.collection(COLLECTION_DONORS)
            .add(donor)
            .addOnSuccessListener {
                log = "Check Point 4 : Profile Created"
                listener.addedSuccessFully(true, log)
                MainRepo.donor = donor
            } //OnSuccessListener
            .addOnFailureListener{
                log = "Check Point 4 : $it"
                listener.addedSuccessFully(false, log)
                log = ""
            } //OnFailureListener
    }

    private fun updateDonor(donor: Donor, listener: NewDonorInterFace, id: String){

        fireStore
            .collection(COLLECTION_DONORS)
            .document(id)
            .update(DONORS_NAME,donor.name, DONORS_BLOOD, donor.blood, DONORS_EAGERNESS, donor.eagerness, DONORS_DATE,donor.date, DONORS_EMAIL,  donor.email, DONORS_PHONE,  donor.phone, DONORS_LOCATION, donor.location)
            .addOnSuccessListener {
                log = "Check Point 4: Updated Profile"
                this.donor = donor
                listener.addedSuccessFully(true, log)
                log = ""
            } //onSuccessListener
            .addOnFailureListener {

                log = "Check Point 4 : $it"
                listener.addedSuccessFully(false, log)
                log = ""

            }//onFailure Listener

    }


    fun getDonorProfile(context: Context, donorId: String) : LiveData<Donor> {
        return object : LiveData<Donor>() {
            override fun onActive() {
                super.onActive()



                fireStore
                    .collection(COLLECTION_DONORS)
                    .whereEqualTo(DONORS_ID, donorId)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                        if (querySnapshot != null && querySnapshot.count() > 0) {

                            if (querySnapshot.count() > 1) {
                                LOG = "Please, check $NAME : More Than one entry in database"
                                // Log.d(TAG, LOG)
                            } //if
                            value = querySnapshot.documents[0].toObject(Donor::class.java)

                        }

                    }
            }

        }
    }





    fun getAll(context: Context): LiveData<List<PhoneBookWithNumbers>>
    {
        val database = BnRoomDataBase.database(context)
        job = Job()


        return object: LiveData<List<PhoneBookWithNumbers>>(){
            override fun onActive() {
                super.onActive()
                //value = database.phoneBookDao().getAll()
                job?.let { job->
                    CoroutineScope(IO + job).launch{
                        val list: List<PhoneBookWithNumbers> = database.phoneBookDao().getAll()
                        withContext(Main){
                        value = list
                        }
                        job.complete()
                    }



                }//job?.let
            } //fun onActive()

    } //return

    }//fun getAll()

    fun getSelected(context: Context, code:Int): LiveData<List<PhoneBookWithNumbers>>
    {
        val database = BnRoomDataBase.database(context)
        job = Job()
        var bloodGroup = context.resources.getStringArray(R.array.blood_search)[code]


        return object: LiveData<List<PhoneBookWithNumbers>>(){
            override fun onActive() {
                super.onActive()
                //value = database.phoneBookDao().getAll()
                job?.let { job->
                    CoroutineScope(IO + job).launch{
                        val list: List<PhoneBookWithNumbers> = database.phoneBookDao().getSelected(bloodGroup)
                        withContext(Main){
                            value = list
                        }
                        job.complete()
                    }



                }//job?.let
            } //fun onActive()

        } //return

    }//fun getSelected()

    fun getSelectedName(context: Context, text:String): LiveData<List<PhoneBookWithNumbers>>
    {
        val database = BnRoomDataBase.database(context)
        job = Job()
        val text = "%$text%"


        return object: LiveData<List<PhoneBookWithNumbers>>(){
            override fun onActive() {
                super.onActive()
                //value = database.phoneBookDao().getAll()
                job?.let { job->
                    CoroutineScope(IO + job).launch{
                        val list: List<PhoneBookWithNumbers> = database.phoneBookDao().getSelectedName(text)
                        withContext(Main){
                            value = list
                        }
                        job.complete()
                    }



                }//job?.let
            } //fun onActive()

        } //return

    }//fun getSelected()

    fun contactsFromDatabase(context: Context) :LiveData<ArrayList<PhoneContact>>
    {

        job = Job()

        // val contactUri = ContactsContract.Contacts.CONTENT_URI
        // val contactName = ContactsContract.Contacts.DISPLAY_NAME
        // val contactHasPhoneNumber = ContactsContract.Contacts.HAS_PHONE_NUMBER
        // val contactId = ContactsContract.Contacts._ID
        //val contactProjection = arrayOf<String>(contactId, contactName, contactHasPhoneNumber)
        // val contactSelection = "$contactHasPhoneNumber = 1"


        val commonUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val commonContactId = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        val commonPhoneNo = ContactsContract.CommonDataKinds.Phone.NUMBER
        //val commonName = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME

        val commonSelection = "$commonContactId=?"
        val commonProjection = arrayOf<String>(commonPhoneNo)

        return object : LiveData<ArrayList<PhoneContact>>() {

            override fun onActive() {
                super.onActive()
                job?.let {job ->

                    CoroutineScope(IO + job )
                        .launch {
                            val phoneContacts : ArrayList<PhoneContact> = ArrayList<PhoneContact>()
                            objectList.forEach {
                                val contentCursor = context.contentResolver.query(commonUri, commonProjection, commonSelection, arrayOf(it.contactId), commonPhoneNo )

                                contentCursor?.let {cursor ->

                                    val temporaryList = ArrayList<String>()
                                    var temporaryBool = false
                                    while(cursor.moveToNext())
                                    {
                                        var phoneNumber = cursor.getString(0)

                                        if (temporaryList.size == 0)
                                        {
                                            temporaryList.add(phoneNumber)
                                            //Log.d(TAG, " : $phoneNumber")
                                        } //if

                                        else {
                                            temporaryList.forEach { tl->
                                                if (PhoneNumberUtils.compare(tl, phoneNumber))
                                                {
                                                    temporaryBool = true
                                                } //if
                                            } //for eacch

                                            if (!temporaryBool) {
                                                temporaryList.add(phoneNumber)
                                                //Log.d(TAG, " : $phoneNumber")
                                            }
                                        } //else
                                    }//while

                                    temporaryList.forEach {phone ->
                                        fireStore
                                            .collection(COLLECTION_DONORS)
                                            .whereEqualTo(DONORS_PHONE, phone)
                                            .get()
                                            .addOnSuccessListener {qSnap ->
                                                val document = qSnap.documents

                                                if (document.size >0){

                                                    if(document.size>1)
                                                    {
                                                    //    Log.d(TAG, "Two Document in the database")
                                                    }

                                                    phoneContacts.add(PhoneContact(it.contactId, it.contactName))
                                                }//if
                                                else
                                                {
                                                    //Log.d(TAG, "No phone number found in database")
                                                }
                                            }
                                    }//forEach


                                } //let
                                contentCursor?.close()

                            }//for each

                            withContext(Main){
                                value = phoneContacts
                                job.complete()
                            } //withContext
                        }//let

                }//let
            } //fun onActive()

        } //return



    }// fun contactsFromPhone

   // @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun contactsFromPhone(context: Context) :LiveData<ArrayList<PhoneContact>>
    {
        //Log.d(TAG, LOG)

        job = Job()

        val contactUri = ContactsContract.Contacts.CONTENT_URI
        val contactName = ContactsContract.Contacts.DISPLAY_NAME
        val contactHasPhoneNumber = ContactsContract.Contacts.HAS_PHONE_NUMBER
        val contactId = ContactsContract.Contacts._ID
        val contactProjection = arrayOf<String>(contactId, contactName, contactHasPhoneNumber)
        val contactSelection = "$contactHasPhoneNumber = 1"

        return object : LiveData<ArrayList<PhoneContact>>() {

            override fun onActive() {
                super.onActive()
                job?.let {job ->

                    CoroutineScope(IO + job )
                        .launch {

                            val contentCursor = context.contentResolver.query(contactUri, contactProjection, contactSelection, null, contactName )
                            contentCursor?.let {cursor ->
                                val phoneContacts : ArrayList<PhoneContact> = ArrayList<PhoneContact>()
                                while(cursor.moveToNext())
                                {
                                    phoneContacts.add(PhoneContact(cursor.getString(0), cursor.getString(1)))
                                    //Log.d(TAG, "${cursor.getString(0)}:: ${cursor.getString(1)} :: ${cursor.getString(2)}")
                                }//while

                                withContext(Main){
                                    value = phoneContacts
                                    objectList = phoneContacts
                                    job.complete()
                                }
                            } //let
                            contentCursor?.close()
                        }//let

                }//let
            } //fun onActive()

        } //return



    }// fun contactsFromPhone


    fun addManualDonor(pojo: PhoneBookWithNumbers, listener: ManualDonorInterFace)
    {
       val job2 = Job()
        job?.let{
            CoroutineScope(IO + job2).launch{
                pojo.numberList.forEach {number ->
                    checkDonorId(number, pojo.phoneBook, listener)
                }//foreach
            } // CoRoutineScope
            job2.complete()
        }//let

    } //fun addManualDonor


    private fun checkDonorId(number: PhoneNumberEntity, book: PhoneBookEntity, listener: ManualDonorInterFace)
    {
        fireStore
            .collection(COLLECTION_DONORS)
            .whereEqualTo(DONORS_PHONE, number.phoneNumber)
            .get()
            .addOnSuccessListener{ it ->
                val list = it.documents
                if (list.size>0) {
                    if (list.size > 1) {
                        Log.d(TAG, "More Than One Phone Number found in database")
                    } //if

                    val donorId = list[0].data?.get("donor_id")

                    if (donorId == "") {
                        val docId = list[0].id
                        updateFirebase(docId, number, book, listener)
                    }//if

                    else
                    {
                        //  Log.d(MainRepo.TAG, "$NAME Donor Has his own Porfile You Cann't change it")
                    }//else
                }
                else
                {
                    addFirebase(number, book, listener)
                } //else

            } //OnSuccessListener
            .addOnFailureListener {
                Log.d(TAG, it.toString())
            }

    }

    private fun updateFirebase(id: String, number: PhoneNumberEntity, book: PhoneBookEntity, listener: ManualDonorInterFace){
        fireStore
            .collection(COLLECTION_DONORS)
            .document(id)
            .update(DONORS_BLOOD, book.bloodGroup)
            .addOnSuccessListener {
                listener.manualDonor(Donor(blood = book.bloodGroup)
                )
            } //onSuccessListener
            .addOnFailureListener {
            }//onFailure Listener

    }

    private fun addFirebase(number: PhoneNumberEntity, book: PhoneBookEntity, listener: ManualDonorInterFace){

        fireStore.collection(COLLECTION_DONORS)
            .add(
                Donor(
                    blood = book.bloodGroup,
                    phone = number.phoneNumber
                )
            )
            .addOnSuccessListener {
                listener.manualDonor(
                    Donor(
                        blood = book.bloodGroup
                    )
                )
            } //OnSuccessListene
            .addOnFailureListener {
           //     Log.d(MainRepo.TAG, NAME+it)
            } //OnFailureListener
    }

    fun cancelJob(){
        job?.cancel()
    }

}