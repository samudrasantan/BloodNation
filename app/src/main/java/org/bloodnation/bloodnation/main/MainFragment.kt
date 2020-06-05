package org.bloodnation.bloodnation.main


import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_test.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.repository.MainRepo


class MainFragment : Fragment(), FirebaseAuth.AuthStateListener
{

    private var l = "MainFragment.kt"
    private var t = MainRepo.TAG


    private val auth: FirebaseAuth = MainRepo.auth
    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }             // fun onStart
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }            // fun onResume
    override fun onStop() {
        super.onStop()
        //(activity as AppCompatActivity).supportActionBar?.show()
        auth.removeAuthStateListener(this)
    }              // fun onStop

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
      //  logoAnimation()
        //Log.d(TAG, log)
    } //fun onViewCreated

    private fun logoAnimation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val x = imageView7.drawable
            val avd: AnimatedVectorDrawable = x as AnimatedVectorDrawable
            avd.start()
        }
    }


    override fun onAuthStateChanged(p0: FirebaseAuth)
    {
        //Log.d(MainRepo.TAG, ": "+p0.currentUser?.uid.toString())
        if (p0.currentUser == null)
        {
            l = "Check Point 1: Unauthorized"
            Log.d(MainRepo.TAG, l)
            //Toast.makeText(requireContext(), l, Toast.LENGTH_SHORT).show()


            val action = MainFragmentDirections.actionMainFragment2ToLogInOrRegister()
            navController.navigate(action)
        } //if
        else
        {
            l = "Check Point 1. Authorized"
            Log.d(MainRepo.TAG, l)
            //Toast.makeText(requireContext(), l, Toast.LENGTH_SHORT).show()


            val action = MainFragmentDirections.actionMainFragment2ToTestFragment()
            navController.navigate(action)
        } //else

    } // fun onAuthStateChanged

} //class MainFragment
