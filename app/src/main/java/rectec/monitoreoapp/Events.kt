package rectec.monitoreoapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import rectec.monitoreoapp.adapters.PagerAdapter
import rectec.monitoreoapp.util.DataBaseHandler

class Events : Fragment() {
    private var eventsView:View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        eventsView = inflater.inflate(R.layout.fragment_events, container, false)
        updateView()
        return  eventsView
    }


    fun updateView(){
        //recover database
        val db = DataBaseHandler(requireContext())
        val profiles = db.recoverIdsProfiles()
        //create a pagerAdapter and create a fragment for each row
        val pagerAdapter = PagerAdapter(childFragmentManager).apply {
            profiles.forEach{
                //pass the name as argument
                addFragment(
                    EventsProfile().apply {
                        arguments = Bundle().apply {
                            putSerializable("idProfile",it)
                        }
                    },
                    "Profile")
            }
        }
        eventsView?.findViewById<ViewPager>(R.id.eventsViewPager)?.adapter =  pagerAdapter

    }




}