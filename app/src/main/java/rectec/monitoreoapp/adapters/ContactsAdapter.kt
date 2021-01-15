package rectec.monitoreoapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.R
import rectec.monitoreoapp.util.Contact



// Manage data of contacts (which are selected in panics)
class ContactsAdapter (private val id : String, context: Context, private val typePanic : String ) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>(){
    private  val db = DataBaseHandler(context)
    private val newContacts = ArrayList<Contact>()
    private  val contacts  by lazy{
        if (!id.isEmpty())  db.recoverContact(id,typePanic)
        else ArrayList()
    }

    private val TAG = ContactsAdapter::class.java.name



    //View that represent the contact added
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val tvContact = itemView.findViewById<TextView>(R.id.tvContact)
        val imgTrash = itemView.findViewById<ImageView>(R.id.imgTrash)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        return  ViewHolder(v)
    }

    // Return the number of view to be created
    override fun getItemCount(): Int {
        return contacts.size
    }


    //Create a view for contact in position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //view holder is create for first time
        val contact = contacts[position]
        //set number and alias
        holder.tvContact.text = contact.first
        holder.imgTrash.setOnClickListener {
            deleteContacts(contact)
        }
    }

    // New contact added
    fun addContact(contact:Contact){
        if(!contacts.contains(contact)){
            newContacts.add(contact)
            contacts.add(contact)
            notifyDataSetChanged()
        }
    }

    // Return new contacts (these arent in the database)
    fun recoverContacts() : ArrayList<Contact>{
        return newContacts
    }

    //Delete one contact
    fun deleteContacts(contact:Contact){
        if(!contacts.remove(contact)){
            Log.e(TAG,"error to delete contact:"+contact.first)
            return

        }
        if(!newContacts.remove(contact)){
            db.deleteContact(id,contact.first,typePanic)
        }

        notifyDataSetChanged()
    }


}