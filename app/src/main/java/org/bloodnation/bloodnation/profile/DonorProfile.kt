package org.bloodnation.bloodnation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.donor_profile.*
import kotlinx.android.synthetic.main.donor_profile.view.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.authentication.Donor
import org.bloodnation.bloodnation.main.MainActivityViewModel
import org.bloodnation.bloodnation.messeging.ConversationWatch
import org.bloodnation.bloodnation.messeging.Conversations
import org.bloodnation.bloodnation.repository.MainRepo
import java.util.*


class DonorProfile : Fragment(), ConversationWatch
{
    private lateinit var viewModel : DonorProfileViewModel
    private val model: MainActivityViewModel by activityViewModels()
    private lateinit var navController: NavController





    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.donor_profile, container, false)









    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DonorProfileViewModel::class.java]
        navController =  Navigation.findNavController(view)
        val args : DonorProfileArgs by navArgs()
        val donorId = args.donorId


        val observer = Observer<Donor> {
            model.donorMessage.value = it

            profileNameField.text = it.name
            profileBloodField.text =it.blood
            progressBar2.progress = it.eagerness.toInt()
            priorityText.text = resources.getStringArray(R.array.donate_urgency)[it.eagerness.toInt()]
        }
        viewModel.getDonorProfile(requireContext(), donorId).observe(viewLifecycleOwner, observer)


        MainRepo.auth.currentUser?.let {user->

            view.profileMessegeButton.setOnClickListener {

                val idOne = user.uid
                val idTwo = model.liveDonorMessage.value?.donor_id.toString()

                val cOne = Conversations(idOne+idTwo, model.liveDonorMessage.value?.name?:"", user.uid, model.liveDonorMessage.value?.donor_id.toString(), Timestamp(Date()))
                val cTwo = Conversations(idTwo+idOne, user.displayName?:"", model.liveDonorMessage.value?.donor_id.toString(), user.uid, Timestamp(Date()))
                viewModel.findConversations(cOne, cTwo, this) }

        }

    }

    override fun conversationWatch(conversations: Conversations) {

        model.currentConversation.value = conversations
        val action = DonorProfileDirections.actionDonorProfileToMessegeRoom()
        navController.navigate(action)
    }


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
