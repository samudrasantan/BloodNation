package org.bloodnation.bloodnation.search


import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_contact_items.view.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers
import org.bloodnation.bloodnation.interfaces.OnClickBloodGroupListener
import org.bloodnation.bloodnation.repository.MainRepo
import kotlin.math.round

class ContactRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private var contactsList :List<PhoneBookWithNumbers> = ArrayList()
    private lateinit var listener : OnClickBloodGroupListener
    private  var myPosition : Location = Location("")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            =
        ContactsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.activity_contact_items,
                parent,
                false
            )
        )


    override fun getItemCount(): Int
            = contactsList.size



    override fun onBindViewHolder( holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>)
    {

      if(payloads.isNotEmpty())
          when(holder)
          {
              is ContactsViewHolder -> holder.bindBlood(payloads[0] as PhoneBookWithNumbers, myPosition)
          } //when

        else
          super.onBindViewHolder(holder, position, payloads)


    } //fun onBindViewHolder



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {

        when(holder){
            is ContactsViewHolder ->{
                holder.bind(contactsList[position], listener, position, myPosition)

            }

        }

    } //fun onBindViewHolder



    fun submitData (contactsList :List<PhoneBookWithNumbers>, listener: OnClickBloodGroupListener)
    {
        this.contactsList =  contactsList
        this.listener = listener
        if ((MainRepo.donor?.location?.latitude != 0.00) and (MainRepo.donor?.location?.longitude != 0.00) )
        {
            myPosition.latitude = MainRepo.donor?.location?.latitude!!
            myPosition.longitude = MainRepo.donor?.location?.longitude!!
        }

    } //fun submitData



    class ContactsViewHolder(
        contactView: View
    ) : RecyclerView.ViewHolder(contactView)
    {

        private var contactName: TextView = contactView.contactNames
        private var bloodGroup: TextView = contactView.listBloodGroup
        private var addButton: ImageView = contactView.contactsAdd
        private var priorityText: TextView = contactView.phoneNumberField
        private var priorityBar: ProgressBar = contactView.progressBar2
        private var location: TextView = contactView.location


        fun bind(contact : PhoneBookWithNumbers, listener: OnClickBloodGroupListener, i:Int, myLocation: Location)
        {
            Log.d("now", contact.phoneBook.contactName)
            contactName.text = contact.phoneBook.contactName
            bloodGroup.text = contact.phoneBook.bloodGroup
            val position : Location = Location("")
            position.latitude = contact.phoneBook.latitude
            position.longitude = contact.phoneBook.longitude

            location.text =  if ((position.latitude != 0.00) and (position.longitude != 0.00) ) { round(myLocation.distanceTo(position)*.001).toString()+" km"} else {""}

            priorityText.text = (if (contact.phoneBook.priority == 2) {"Ready"} else if(contact.phoneBook.priority == 1){"Friends Only"} else {"Not Ready"}).toString()
            priorityBar.progress = contact.phoneBook.priority
            addButton.setOnClickListener {
                listener.onClickItem(contact, i)
            } //OnClickListener
        } //fun bind

        fun bindBlood(contact : PhoneBookWithNumbers, myLocation: Location){
            contactName.text = contact.phoneBook.contactName
            bloodGroup.text = contact.phoneBook.bloodGroup
            val position : Location = Location("")
            position.latitude = contact.phoneBook.latitude
            position.longitude = contact.phoneBook.longitude

            location.text =  if ((position.latitude != 0.00) and (position.longitude != 0.00) ) { round(myLocation.distanceTo(position)*.001).toString()+" km"} else {""}

            priorityText.text = (if (contact.phoneBook.priority == 2) {"Ready"} else if(contact.phoneBook.priority == 1){"Friends Only"} else {"Not Ready"}).toString()
            priorityBar.progress = contact.phoneBook.priority
        } //fun bindBlood
    }

} //lass ContactRecyclerViewAdapter

