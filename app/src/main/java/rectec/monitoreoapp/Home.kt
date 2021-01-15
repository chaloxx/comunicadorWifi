package rectec.monitoreoapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import rectec.monitoreoapp.adapters.PagerAdapter
import rectec.monitoreoapp.util.DataBaseHandler

class Home : Fragment() {
    private var homeView : View? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        homeView =  inflater.inflate(R.layout.fragment_home, container, false)
        updateView()
        return homeView
    }


    fun updateView(){
        //recover database
        val db = DataBaseHandler(requireContext())
        val profiles = db.recoverIdsProfiles()
        if (profiles.size > 0) homeView?.findViewById<TextView>(R.id.notExistProfiles)?.visibility = View.INVISIBLE
        //create a pagerAdapter and create a fragment for each row
        val pagerAdapter = PagerAdapter(childFragmentManager).apply {
            profiles.forEach{
                //pass the name as argument
                addFragment(
                    Profile().apply {
                        arguments = Bundle().apply {
                            putSerializable("idProfile",it)
                        }
                    },
                    "Profile")
            }
        }
        homeView?.findViewById<ViewPager>(R.id.homeViewPager)?.adapter =  pagerAdapter

    }




}