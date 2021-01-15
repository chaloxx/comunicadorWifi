package rectec.monitoreoapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import rectec.monitoreoapp.CreateProfile
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.R



//Manage data for alias
class AliasAdapter (private val id : String, context: Context, private val table : String ) :
    RecyclerView.Adapter<AliasAdapter.ViewHolder>(){
    private  val db = DataBaseHandler(context)
    private val newAlias = ArrayList<String>()
    private  val alias by lazy{
        if (!id.isEmpty()) db.recoverIdAlias(id,table)
        else ArrayList()
    }

    private val TAG = AliasAdapter::class.java.name



    //View that represent number + alias + trash image
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val tvNumber = itemView.findViewById<TextView>(R.id.tvNumber)
        val tvAlias = itemView.findViewById<TextView>(R.id.tvAlias)
        val imgTrash = itemView.findViewById<ImageView>(R.id.imgTrash)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.alias_item,parent,false)
        return  ViewHolder(v)
    }

    // Return the number of view to be created
    override fun getItemCount(): Int {
        return alias.size
    }


    //Create view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //view holder is create for first time
        val str = alias[position].split(CreateProfile.SEPARATOR)
        //set number and alias
        holder.tvNumber.text = str[0]
        holder.tvAlias.text = str[1]
        holder.imgTrash.setOnClickListener {
            deleteAlias(alias[position])
        }
    }

    fun addAlias(str:String){
        newAlias.add(str)
        alias.add(str)
        notifyDataSetChanged()
    }

    //Return list of new alias added (these arent in the database)
    fun recoverNewAlias() : ArrayList<String>{
        return newAlias
    }

    //If alias is in database so delete it from there
    fun deleteAlias(str:String){
        val idAlias = str.split(CreateProfile.SEPARATOR)[0]
        if(!alias.remove(str)){
            Log.e(TAG,"error to delete element:"+str)
            return

        }
        if(!newAlias.remove(str)){
            db.deleteAlias(idAlias,id,table)
        }

        notifyDataSetChanged()
    }


}