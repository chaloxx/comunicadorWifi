package rectec.monitoreoapp.edits

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import rectec.monitoreoapp.R
import rectec.monitoreoapp.util.DataBaseHandler

// Activity for assign alias to number of zone
class EditZone : AppCompatActivity() {
    val etEditNumber by lazy {findViewById<TextView>(R.id.etEditNumber)}
    val etEditZone by lazy {findViewById<TextView>(R.id.etEditAlias)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_zone_user)
        supportActionBar?.title = getString(R.string.str_title_edit_zone)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        findViewById<TextView>(R.id.tvEditNumber).text = getString(R.string.str_tvZoneNumber)
        findViewById<TextView>(R.id.tvEditAlias).text = getString(R.string.str_tvZoneAlias)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_profile_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.saveProfile){
            val zone = etEditZone.text.toString()
            val number = etEditNumber.text.toString()

            //Neither the zone or the number must be empty
            if(zone.isEmpty() || number.isEmpty()){
                Toast.makeText(this,getString(R.string.err_empty_field_user), Toast.LENGTH_SHORT)
            }

            else{
                //Return result to createProfile
                Intent()?.apply{
                    putExtra("zone",zone)
                    putExtra("number",number)
                    setResult(Activity.RESULT_OK,this)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}