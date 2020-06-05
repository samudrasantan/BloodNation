package org.bloodnation.bloodnation.settings

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_settings.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.main.MainActivityViewModel

class Settings : Fragment() {

    private lateinit var model: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val observer = Observer<SettingsEntity>{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (it.settingsTheme == 1) {
                    settingsBackground.background = resources.getDrawable(R.color.BlackPrimary)
                    settingsHeading.setTextColor(resources.getColor(R.color.colorLight1))
                    darkModeSwitch.setTextColor(resources.getColor(R.color.colorLight1))
                    darkModeSwitch.isChecked = true
                } else {
                    settingsBackground.background = resources.getDrawable(R.color.colorLight1)
                    settingsHeading.setTextColor(resources.getColor(R.color.BlackPrimary))
                    darkModeSwitch.setTextColor(resources.getColor(R.color.BlackPrimary))

                }

            }

            else
            {
                darkModeSwitch.visibility = View.GONE
            }
        }
        model.getSettings.observe(viewLifecycleOwner, observer)

        darkModeSwitch.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->

            if (b)
            {
                model.updateSetting(1)
            }

            else
            {
                model.updateSetting(0)
            }

        }
    }

}
