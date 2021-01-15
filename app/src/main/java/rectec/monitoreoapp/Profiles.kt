package rectec.monitoreoapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rectec.monitoreoapp.adapters.ProfilesRecyclerAdapter
import rectec.monitoreoapp.util.IdProfile



const val LAUNCH_CREATE_PROFILE = 18
const val LAUNCH_EDIT_PROFILE = 19

class Profiles(private val callback: Notifier) : Fragment() {

    private var recyclerView : RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_profiles, container, false)
        recyclerView = v.findViewById(R.id.profilesRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(activity)


        //callback for edit button
        val callBackForEdit = { v:View,p:Int ->
            val intent = Intent(context,CreateProfile::class.java)
            intent.putExtra("rowId",p)
            intent.putExtra("intention", LAUNCH_EDIT_PROFILE)
            startActivityForResult(intent, LAUNCH_EDIT_PROFILE)
        }

        recyclerView?.adapter = ProfilesRecyclerAdapter(requireContext(),callBackForEdit)



        //Add profile when click has ocurred on floating button
        v.findViewById<FloatingActionButton>(R.id.btAddProfile).setOnClickListener {
            val intent = Intent(context,CreateProfile::class.java)
            intent.putExtra("intention", LAUNCH_CREATE_PROFILE)
            startActivityForResult(intent, LAUNCH_CREATE_PROFILE)
        }


        return v

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == LAUNCH_CREATE_PROFILE || requestCode == LAUNCH_EDIT_PROFILE){

                val recyclerAdapter = recyclerView?.adapter as ProfilesRecyclerAdapter

                //when a profile is delete we must delete the profile name to the recycler
                //when a profile is create we must add the profile name to the recycler
                data?.run{
                    if(hasExtra("deletedProfile")){
                        getSerializableExtra("deletedProfile")?.let{
                            recyclerAdapter.idsProfiles.remove(it)
                        }

                    }

                    if(hasExtra("createdProfile")){

                        getSerializableExtra("createdProfile")?.let {
                            recyclerAdapter.idsProfiles.add(it as IdProfile)
                        }
                    }

                    callback.notifyToTheFragments()
                    recyclerAdapter.notifyDataSetChanged()
                }


            }


        }

    }


    interface Notifier{
        fun notifyToTheFragments()
    }
}