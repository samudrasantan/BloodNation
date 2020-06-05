package org.bloodnation.bloodnation.main



import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.databinding.ActivityMainBinding
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.settings.SettingsEntity


class MainActivity : AppCompatActivity()
{

    //Variable Declaration
    private lateinit var binding: ActivityMainBinding
    lateinit var model: MainActivityViewModel

    var appTheme = R.style.AppTheme

    //Functions
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?)
    { super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(actionBarMain)




        model = ViewModelProvider(this)[MainActivityViewModel::class.java]
        model.addSettings()
        val observer = Observer<SettingsEntity> {
            //Log.d(MainRepo.TAG, it.settingsTheme.toString())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if (it.settingsTheme == 1)
                {
                    actionBarMain.background =  resources.getDrawable (R.color.BlackPrimary)
                    window.statusBarColor = resources.getColor(R.color.BlackPrimary)
                }

                else
                {
                    actionBarMain.background =  resources.getDrawable (R.color.colorPrimary)
                    window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
                }
            }


        }
        model.getSettings.observe(this, observer)








    } //fun onCreate

/*
    override fun getTheme(): Resources.Theme {
        val theme =super.getTheme()
        theme.applyStyle(appTheme, true)
        Log.d(MainRepo.TAG, "theme called"+appTheme)
        return theme
    }

 */


}  //class MainActivity

// For future Reference this is how to generate hash number for facebook, apply in onCreate methods

/*

        //Generate Hash for Facebook
        try {
            val info = packageManager.getPackageInfo(
                "org.bloodnation.bloodnation",
                PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
*/