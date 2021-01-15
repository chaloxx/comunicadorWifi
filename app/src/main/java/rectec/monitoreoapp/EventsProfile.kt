package rectec.monitoreoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rectec.monitoreoapp.adapters.EventsRecyclerAdapter
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.util.IdProfile



//This fragment show logs por one profile in particular
class EventsProfile : Fragment() {
    private lateinit var idProfile : IdProfile
    private var recyclerView : RecyclerView? = null
    private lateinit var  deleteHistory : FloatingActionButton




    //If "message_arrived" is received so the database has changed
    inner class EventReceiver:BroadcastReceiver(){

        override fun onReceive(p0: Context?, p1: Intent?) {
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idProfile = it.getSerializable("idProfile") as IdProfile
        }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_events_profile, container, false)
        registerReceiver()
        recyclerView = v.findViewById(R.id.eventsRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(activity)

        val buttonVisibility = { b : Boolean ->
            deleteHistory.visibility = if (b)  View.VISIBLE
                                       else View.INVISIBLE

            v.findViewById<TextView>(R.id.tvNotEvents)
                .visibility = if(b) View.INVISIBLE
                              else View.VISIBLE
        }


        // Pass button visibilite as an accion in the adapter
        recyclerView?.adapter = EventsRecyclerAdapter(requireContext(),idProfile,buttonVisibility)

        DataBaseHandler(requireContext()).recoverProfile(idProfile)[5].let {profile ->
            v.findViewById<TextView>(R.id.tvId).text = profile
        }


        deleteHistory = v.findViewById(R.id.btDeleteHistory)
        deleteHistory.setOnClickListener {
                DataBaseHandler(requireContext()).deleteAllMessages(idProfile)
                recyclerView?.adapter?.notifyDataSetChanged()
            }

        return v
    }






    //Register broadcast receiver
    private fun registerReceiver(){
        val receiver  = EventReceiver()
        val filter  = IntentFilter("message_arrived")
        requireContext().registerReceiver(receiver,filter)
    }



}