package org.bloodnation.bloodnation.fragment.tab


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab.*
import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.adapter.MainActivityPagerAdapter


class TabFragment : Fragment()
{

    private lateinit var control: NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
       setHasOptionsMenu(true);

    } //fun onCreate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { super.onViewCreated(view, savedInstanceState)

        control = Navigation.findNavController(view)

        //Log.d("bloodNation", "TabFragment.kt, onViewCreated : ")



        val viewpager2 = tabViewPager
        viewpager2.adapter = MainActivityPagerAdapter(requireActivity())


        TabLayoutMediator(tabLayout, viewpager2,
            TabLayoutMediator.TabConfigurationStrategy
            { tab, position ->
                when (position) {
                    0 -> tab.text = "PROFILE"
                    1 -> tab.text = "SEARCH"
                    2 -> tab.text = "MANUAL"
                }
            }).attach() //Tab Layout
    } // fun onViewCreated

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)


    }  //fun onCreateOptionsMenu

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        Log.d("bloodNation", "TabFragment.kt + $item")

        when (item.itemId) {

            R.string.signOut -> {

                Log.d("bloodNation", "MainActivity")
                AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                    //control.navigate(R.id.action_tabFragment_to_mainFragment2)
                }
            }

            R.string.updateProfile ->{

               // control.navigate(R.id.action_tabFragment_to_signUpConfirmation)

            }

            //another when
        }
        return super.onOptionsItemSelected(item)
    } //fun onOptionsItemSelected

} //class TabFragment
