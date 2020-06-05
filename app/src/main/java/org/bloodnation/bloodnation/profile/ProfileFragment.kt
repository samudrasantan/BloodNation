package org.bloodnation.bloodnation.profile


import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_search_contact.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.authentication.Donor

class ProfileFragment : Fragment(), FirebaseAuth.AuthStateListener
{


    private lateinit var model: ProfileViewModel
    private lateinit var navController: NavController


    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);

    } //fun onCreate

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View?
    {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    } //fun onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        model = ViewModelProvider(this)[ProfileViewModel::class.java]

        Picasso
            .with(context)
            .load(FirebaseAuth.getInstance().currentUser?.photoUrl)
            .resize(600,600)
            .centerCrop()
            .into(getView()?.findViewById<ImageView>(R.id.profilePicture))


        val observer = Observer<Donor>{
            nameField.text = it.name
            eagernessField.text = resources.getStringArray(R.array.donate_urgency)[it.eagerness.toInt()]
            bloodGroup.text = it.blood
        }

        model.donorInfo.observe(viewLifecycleOwner, observer)
    } //fun onViewCreated


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId) {

            R.string.settings ->
            {
                val action = ProfileFragmentDirections.actionProfileFragmentToSettings()
                navController.navigate(action)
            }

            R.string.edit_profile ->
            {
                val action = ProfileFragmentDirections.actionProfileFragmentToSignUpConfirmation()
                navController.navigate(action)
            }

            R.string.signOut -> {

                AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                    //control.navigate(R.id.action_searchContactFragment_to_mainFragment2)
                }

            }

        }

        return super.onOptionsItemSelected(item)
    } //fun onOptionsItemSelected

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser != null) {
            val action = ProfileFragmentDirections.actionProfileFragmentToLogInOrRegister()
            navController.navigate(action)
        }
    }

} //class ProfileFragment
