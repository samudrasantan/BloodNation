package org.bloodnation.bloodnation.authentication


import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_log_in_or_register.*
import kotlinx.android.synthetic.main.main_lobby.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.main.MainActivity
import org.bloodnation.bloodnation.main.MainActivityViewModel
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.settings.SettingsEntity


class LogInOrRegister : Fragment(), FirebaseAuth.AuthStateListener
{

    //Variable Declaration
    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainActivityViewModel
    private val auth = MainRepo.auth
    var l = "LogInOrRegister.kt"
    var requestCode = 1000


    //Functions
    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }             // fun onStart
    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(this)
    }              // fun onStop


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View?
            = inflater.inflate(R.layout.log_in_or_register, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        mainViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        fmLogInButton.setOnClickListener{ handleLoginRegisterButton(it)}



        val observer = Observer<SettingsEntity>{

            Log.d(MainRepo.TAG, "theme "+it.settingsTheme.toString())
            if (it.settingsTheme == 1){
                back.background = resources.getDrawable(R.color.colorDarkLight)
                fmLogInText.setTextColor(resources.getColor(R.color.colorPrimary))
                fmLogInButton.setTextColor(resources.getColor(R.color.colorLight1))
            }

        }
        mainViewModel.getSettings.observe(viewLifecycleOwner, observer)
    } //fun onViewCreated

    private fun handleLoginRegisterButton(view: View)
    {
        val providers = arrayListOf<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTosAndPrivacyPolicyUrls("https://google.com", "https://google.com") //we need to update it later
            .setLogo(R.drawable.bn_fill)
            .setTheme(R.style.whiteTheme)
            .build()

        startActivityForResult(intent, requestCode)

    } //fun handleLoginRegisterButton


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        // callbackManager.onActivityResult(requestCode, resultCode, data)

        if(requestCode == requestCode)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                //we can have either new user or old user, so we need to check that
                if(auth.currentUser?.metadata?.creationTimestamp ?: "" == auth.currentUser?.metadata?.lastSignInTimestamp ?: "")
                    l = "Welcome to Blood Nation"
                else
                    l = "Glad to have you back"

                Toast.makeText(context, l, Toast.LENGTH_SHORT).show()
                startActivity(Intent(context, MainActivity::class.java))

            } // if result code

            else
            {
                if(IdpResponse.fromResultIntent(data) == null)
                {
                    l = "PLEASE COME BACK AGAIN"
                }

                else
                {
                    l = "Network Error   :: "+IdpResponse.fromResultIntent(data)?.error ?: ""

                }

                Toast.makeText(requireContext(), l, Toast.LENGTH_SHORT).show()
            }

        } // if(requestCode == 1000)
        else
        {
            l = "Check Point 2  : LogInOrRegister.kt : Request Code Problem"
            Toast.makeText(requireContext(), l, Toast.LENGTH_SHORT).show()
        } //else
    } //fun onActivityResult

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser != null) {
            val action =
                LogInOrRegisterDirections.actionLogInOrRegisterToMainFragment2()
            navController.navigate(action)
        }
    }

} //class LogInOrRegister
