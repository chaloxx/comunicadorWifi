package rectec.monitoreoapp.edits

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rectec.monitoreoapp.R
import rectec.monitoreoapp.adapters.ContactsAdapter
import rectec.monitoreoapp.util.Constants
import rectec.monitoreoapp.util.Constants.fromIdToCode
import rectec.monitoreoapp.util.Constants.panic
import rectec.monitoreoapp.util.Constants.typePanics
import rectec.monitoreoapp.util.Contact
import rectec.monitoreoapp.util.IdProfile


//Fragment for edit panics (sonorous,silent and fire)
class EditPanics : AppCompatActivity() {
    private lateinit var idProfile : IdProfile
    //One list of contacts for each type of panic
    private lateinit var panicsAdapters : List<ContactsAdapter>
    private val ADD_CONTACT_SONOROUS = 0
    private val ADD_CONTACT_SILENT = 1
    private val ADD_CONTACT_FIRE = 2



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_panics)

        supportActionBar?.title = getString(R.string.activity_title_edit_panics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        idProfile  = intent.getSerializableExtra("idProfile") as IdProfile



        //Initialize adapters
        panicsAdapters = typePanics.map { panic ->
            ContactsAdapter(fromIdToCode(idProfile),this,panic).apply {
                for (c in intent.getSerializableExtra(panic) as ArrayList<Contact>) {
                    addContact(c)
                }
            }
        }


        //Initialize contact list views
        findViewById<RecyclerView>(R.id.listContacts1).run{
            adapter = panicsAdapters[0]
            layoutManager = LinearLayoutManager(context)
        }


        findViewById<RecyclerView>(R.id.listContacts2).run{
            adapter = panicsAdapters[1]
            layoutManager = LinearLayoutManager(context)
        }


        findViewById<RecyclerView>(R.id.listContacts3).run{
            adapter = panicsAdapters[2]
            layoutManager = LinearLayoutManager(context)
        }

        //Initialize buttons
        findViewById<FloatingActionButton>(R.id.addContact).setOnClickListener{
            addContact(ADD_CONTACT_SONOROUS)
        }


        findViewById<FloatingActionButton>(R.id.addContact2).setOnClickListener{
            addContact(ADD_CONTACT_SILENT)
        }

        findViewById<FloatingActionButton>(R.id.addContact3).setOnClickListener{
            addContact(ADD_CONTACT_FIRE)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_panic_configuration,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
                //Recover name and phone number of contact selected
                val  contactData : Uri? = data?.getData()
                val cursor = contentResolver.query(contactData!!, null, null, null, null);
                cursor?.moveToFirst();
                val hasPhone = cursor?.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                cursor?.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))?.let {contactId ->

                    var number = ""
                    if (hasPhone.equals("1")) {
                        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = " + contactId, null, null);
                        while (phones!!.moveToNext()) {
                            number = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("[-() ]", "");
                        }
                        phones.close()
                        val contactName = cursor?.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME) )
                        panicsAdapters[requestCode].addContact(Pair(contactName,number))
                        panicsAdapters[requestCode].notifyDataSetChanged()

                        //Do something with number
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_contact_without_number), Toast.LENGTH_LONG).show();
                    }

                    cursor?.close();

                }

        }
    }






    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.savePanic){

            //Return data to create profile activity
            Intent()?.apply {
                putExtra(typePanics[0], panicsAdapters[0].recoverContacts())
                putExtra(typePanics[1], panicsAdapters[1].recoverContacts())
                putExtra(typePanics[2], panicsAdapters[2].recoverContacts())
                setResult(Activity.RESULT_OK, this)
            }
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    //Start activity for select contacts
    @RequiresApi(Build.VERSION_CODES.M)
    private fun addContact(code: Int)  {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI).let {intent ->
                startActivityForResult(intent,code)

            }

        else requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),code)

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Codes serve to identifier the action when the permission is request
        when(requestCode){
            ADD_CONTACT_SONOROUS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 addContact(ADD_CONTACT_SONOROUS)
            }

            ADD_CONTACT_SILENT -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                addContact(ADD_CONTACT_SILENT)
            }

            ADD_CONTACT_FIRE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                addContact(ADD_CONTACT_FIRE)
            }
        }
    }







}