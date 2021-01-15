package rectec.monitoreoapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


//Class to manage tabs who are called fragments
class PagerAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm) {
    private var  fragmentList:MutableList<Fragment> = mutableListOf()
    private  var fragmentTitles:MutableList<String> = mutableListOf()


    // Get fragment in position pos
    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    //Return number of fragments
    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitles.get(position)
    }


    fun addFragment(fg:Fragment,title:String){
        fragmentList.add(fg)
        fragmentTitles.add(title)
    }

}