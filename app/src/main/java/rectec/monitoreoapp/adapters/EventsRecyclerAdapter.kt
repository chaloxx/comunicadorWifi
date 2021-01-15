package rectec.monitoreoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.R
import rectec.monitoreoapp.util.Constants.codes
import rectec.monitoreoapp.util.Constants.fromIdToCode
import rectec.monitoreoapp.util.IdProfile
import rectec.monitoreoapp.util.SqlResult


/*Manage a list of profiles view in the profile tab*/


//Manage data for events
class EventsRecyclerAdapter (private val context: Context,private val id : IdProfile, private val accion : (Boolean) -> Unit) :
    RecyclerView.Adapter<EventsRecyclerAdapter.ViewHolder>(){
    private  val db = DataBaseHandler(context)
    private var rows : Array<SqlResult>? = null

    //View that represent the profile
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val tvMsg = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvArrived = itemView.findViewById<TextView>(R.id.tvArrived)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.message_item,parent,false)
        return  ViewHolder(v)
    }

    // Return the number of view to be created
    override fun getItemCount(): Int {
        rows = db.recoverMessagesForId(id)
        val ctos = db.howManyMessagesForId(id)
        accion(ctos > 0)
        return ctos
    }


    //Create view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //view holder is create for first time
        rows!!.get(position)!!.let{values ->
            //set adressed,date and message
            holder.tvArrived.text = values[2] +"@"+ values[3]
            holder.tvMsg.text = parseMsg(values[0])
        }
        if(position+1 == rows!!.size) rows = null
    }



    fun parseMsg(msg:String) : String{

       return  when(msg.substring(0,4)){
            codes["disarmed"] -> {
                val user = db.recoverAlias(msg.substring(6), fromIdToCode(id),db.TABLE_ALIAS_USERS)
                "${context.getString(R.string.log_disarmed)} $user"
            }

            codes["armedPresent"] -> {
                val user = db.recoverAlias(msg.substring(6), fromIdToCode(id),db.TABLE_ALIAS_USERS)
                "${context.getString(R.string.log_armed_present)} $user"
            }

            codes["armedAway"] -> {
                val user = db.recoverAlias(msg.substring(6), fromIdToCode(id),db.TABLE_ALIAS_USERS)
                "${context.getString(R.string.log_armed_away)} $user"
            }


            codes["byPassedZones"] -> {
                val zone = db.recoverAlias(msg.substring(6), fromIdToCode(id),db.TABLE_ALIAS_ZONES)
                "${context.getString(R.string.log_by_pass_zone)} $zone"
            }

            codes["armedParcial"] -> {
                "${context.getString(R.string.log_armed_parcial)}"
            }


            codes["openZones"] -> {
                val aliasZones = msg.substring(0,4).split(",").map {idZone ->
                    db.recoverAlias(idZone,id.first+id.second,db.TABLE_ALIAS_ZONES)
                }
                val info = aliasZones.fold(""){acc,s -> "$acc,$s"}.substring(1)
               "${context.getString(R.string.log_open_zones)} $info"
            }

             codes["sonorousPanic"] -> {
                 context.getString(R.string.log_sonorous_panic)
             }

             codes["silentPanic"] -> {
               context.getString(R.string.log_silent_panic)
             }

            codes["fire"] -> {
               context.getString(R.string.log_fire)
            }







           else -> ""
       }


    }






}

