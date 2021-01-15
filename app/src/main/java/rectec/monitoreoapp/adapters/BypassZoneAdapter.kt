package rectec.monitoreoapp.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import rectec.monitoreoapp.R


// Manage zones for bypassing
class BypassZoneAdapter(private val c: Context) : BaseAdapter(){
    private var layoutInflater: LayoutInflater? = null
    private var zones = ArrayList<Int>()

    override fun getCount(): Int {
        //Total number of zones
        return 64
    }

    //Not mean anything
    override fun getItem(p0: Int): Any {
        TODO("Not yet implemented")
    }

    //Not mean anything
    override fun getItemId(p0: Int): Long {
        return 0
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View? {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.bypass_item, null)
        }
        convertView?.findViewById<TextView>(R.id.itemBypass)?.apply {
            val v  = position+1
            text = v.toString()
            //Check if the zone is selected
            setOnClickListener {
                //if it is selected so unselected
                if (zones.contains(v)){
                    zones.remove(v)
                    setBackgroundColor(context.resources.getColor(R.color.byPassItem_unselected,null))
                }
                else{
                    // Higlight zones selected
                    zones.add(v)
                    setBackgroundColor(context.resources.getColor(R.color.byPassItem_selected,null))
                }
            }
        }


        return convertView
    }


    //Return new zones (these arent in database)
    fun recoverZones(): ArrayList<Int>{
        return zones
    }


}