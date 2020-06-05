package org.bloodnation.bloodnation.fragment.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_search.*

import org.bloodnation.bloodnation.R
import org.bloodnation.bloodnation.repository.MainRepo
import org.bloodnation.bloodnation.search.ContactRecyclerViewAdapter
import org.bloodnation.bloodnation.model.ContactsLocal

class SearchFragment : Fragment() {

    private lateinit var contactAdapter: ContactRecyclerViewAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_search, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var me = this


        searchRecyclerView.apply{
            layoutManager = LinearLayoutManager(requireContext())
            contactAdapter =
                ContactRecyclerViewAdapter()
            adapter = contactAdapter
        } //RecycleView


        searchBloodInput.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
               // Toast.makeText(requireContext(), "SearchFragment.kt : $parent", Toast.LENGTH_LONG).show()

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {

                //Toast.makeText(requireContext(), "SearchFragment.kt : $position", Toast.LENGTH_LONG).show()
               /* val newList = MainObject.contactList.filter {
                    it.blood == resources.getStringArray(R.array.blood_type)[position]
                }*/

                var newList : ArrayList<ContactsLocal> = ArrayList<ContactsLocal>()
                MainRepo.contactList.forEach{
                    if(it.blood == resources.getStringArray(R.array.blood_type)[position])
                        newList.add(it)
                }

                //Toast.makeText(requireContext(), "SearchFragment.kt : " +newList.size, Toast.LENGTH_LONG).show()

                //contactAdapter.submitData(newList, me)

                searchRecyclerView.swapAdapter(contactAdapter, false)

            }
        }
    }




}
