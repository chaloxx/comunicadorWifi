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
import rectec.monitoreoapp.util.IdProfile


// Manage a list of profiles view in the profile tab

class ProfilesRecyclerAdapter (val context: Context, private val ivEditCallBack:((View, Int) -> Unit)) :
RecyclerView.Adapter<ProfilesRecyclerAdapter.ViewHolder>(){
    private  val db = DataBaseHandler(context)
    val idsProfiles : MutableList<IdProfile> = db.recoverIdsProfiles()

    //View that represent the profile
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val perfilName = itemView.findViewById<TextView>(R.id.perfilName)
        val teamId = itemView.findViewById<TextView>(R.id.teamId)
        val alarmModel = itemView.findViewById<TextView>(R.id.alarmModel)
        val createDate = itemView.findViewById<TextView>(R.id.createDate)
        val ivEditProfile = itemView.findViewById<ImageView>(R.id.ivEditProfile)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.profile_item,parent,false)
        return  ViewHolder(v)
    }

    // Return the number of view to be created
    override fun getItemCount(): Int {
        return db.howManyProfiles()
    }


    //Create view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //view holder is create for first time
        val values = db.recoverProfile(idsProfiles[position])

        //set perfil name and team id
        holder.perfilName.text = values[5]
        holder.teamId.text = values[2]

        //set model
        holder.alarmModel.text =  values[3]

        //set date
        holder.createDate.text = "${context.getString(R.string.str_date)} ${values[4]}"

        //set callback for edition
        holder.ivEditProfile.setOnClickListener { ivEditCallBack.invoke(it, position) }
    }
}