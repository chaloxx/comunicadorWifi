package rectec.monitoreoapp.edits

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import rectec.monitoreoapp.R
import rectec.monitoreoapp.util.DataBaseHandler


// This activity serve for assign alias to a number of user
class EditUser : AppCompatActivity() {
    val tvEditNumber by lazy {findViewById<TextView>(R.id.tvEditNumber)}
    val tvEditUser by lazy {findViewById<TextView>(R.id.tvEditAlias)}

    val etEditUser by lazy {findViewById<EditText>(R.id.etEditNumber)}
    val etEditNumber by lazy {findViewById<EditText>(R.id.etEditAlias)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_zone_user)
        supportActionBar?.title = getString(R.string.str_title_edit_user)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        tvEditNumber.text = getString(R.string.str_tvUserNumber)
        tvEditUser.text = getString(R.string.str_tvUserAlias)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_profile_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.saveProfile){
            val user = etEditUser.text.toString()
            val number = etEditNumber.text.toString()

            //Neither the user or the number must be empty
            if(user.isEmpty() || number.isEmpty()){
                Toast.makeText(this,getString(R.string.err_empty_field_user),Toast.LENGTH_SHORT)
            }

            else {
                //Return result to createProfile
                Intent()?.apply {
                    putExtra("user", user)
                    putExtra("number", number)
                    setResult(Activity.RESULT_OK, this)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}