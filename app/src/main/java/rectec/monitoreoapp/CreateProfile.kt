package rectec.monitoreoapp

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_create_profile.*
import rectec.monitoreoapp.adapters.AliasAdapter
import rectec.monitoreoapp.edits.EditPanics
import rectec.monitoreoapp.edits.EditUser
import rectec.monitoreoapp.edits.EditZone
import rectec.monitoreoapp.util.Constants.fromIdToCode
import rectec.monitoreoapp.util.Constants.typePanics
import rectec.monitoreoapp.util.Contact
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.util.IdProfile



//This activity serve either for create a profile or edit
class CreateProfile : AppCompatActivity()  {

    // links
    private val etLocation by lazy {findViewById<EditText>(R.id.etLocation)}
    private val etPanelCode by lazy { findViewById<EditText>(R.id.etPanelCode) }
    private val etTeamId by lazy { findViewById<EditText>(R.id.etTeamId) }
    private val alarmSpinner by lazy { findViewById<Spinner>(R.id.alarmSpinner) }
    private val btDeleteProfile by lazy { findViewById<Button>(R.id.bt_delete_profile) }
    private  val partitionSpinner by lazy { findViewById<Spinner>(R.id.partitionSpinner) }
    private val switchNotifications by lazy {findViewById<Switch>(R.id.switchNotifications)}
    private val etPgm1 by lazy {findViewById<EditText>(R.id.etPgm1)}
    private val etPgm2 by lazy {findViewById<EditText>(R.id.etPgm2)}
    private lateinit var zoneAdapter : AliasAdapter
    private lateinit var userAdapter : AliasAdapter
    private var panicContacts = arrayOf(ArrayList<Contact>(),ArrayList<Contact>(),ArrayList<Contact>())
    private var idProfile = Pair("","")



    companion object {
        val SEPARATOR = "    "
    }

    private val  USER_REQUEST = 2
    private val  ZONE_REQUEST = 3
    private val  EDIT_PANIC = 4

    //database handler
    private val db = DataBaseHandler(this@CreateProfile)


    //modes of activity:
    // createMode = true => mode of create profiles
    // editMode = false => mode of edit profiles

    private var editMode = true



    private lateinit var mService: MessageService
    private var mBound: Boolean = false




    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MessageService.LocalBinder
            mService = binder.getService()
            mBound = true

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, MessageService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)


        //Set spinners
        val partitions = resources.getStringArray(R.array.partitionsArray)
        val models = resources.getStringArray(R.array.alarmModels)
        partitionSpinner.adapter = ArrayAdapter(this@CreateProfile,R.layout.support_simple_spinner_dropdown_item,partitions)
        alarmSpinner.adapter = ArrayAdapter(this@CreateProfile,R.layout.support_simple_spinner_dropdown_item,models)




        supportActionBar?.title = "Crear Pérfil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when(intent.getIntExtra("intention",0)){
            19 ->  {
                editMode = true
                val rowNumber = intent.getIntExtra("rowId",-1)
                val idsProfiles = db.recoverIdsProfiles()
                idProfile = idsProfiles[rowNumber]
                btDeleteProfile.setOnClickListener {
                    deleteProfile(idProfile)
                }
                restoreState(idProfile)
            }

            else ->{
                editMode = false
                builtLists(Pair("",""))
            }

        }




        findViewById<TextView>(R.id.editPanics).setOnClickListener {
            Intent(this@CreateProfile, EditPanics::class.java)?.let{i ->
                i.putExtra("idProfile",idProfile)
                i.putExtra(typePanics[0],panicContacts[0])
                i.putExtra(typePanics[1],panicContacts[1])
                i.putExtra(typePanics[2],panicContacts[2])
                startActivityForResult(i,EDIT_PANIC)
            }
        }

        findViewById<FloatingActionButton>(R.id.addZones).setOnClickListener {
            Intent(this, EditZone::class.java).run {
                startActivityForResult(this,ZONE_REQUEST)
            }
        }


        findViewById<FloatingActionButton>(R.id.addUsers).setOnClickListener {
            Intent(this, EditUser::class.java).run {
                startActivityForResult(this,USER_REQUEST)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_profile_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.saveProfile){
                    if (editMode){
                        //the profile already exist, only we must update it
                        updateData()
                    }
                    else{
                        // the profile doesnt exist, we must create it

                        saveData()

                    }

                    Intent().run{
                        putExtra("createdProfile",Pair(etPanelCode.text.toString(),partitionSpinner.selectedItem.toString()))
                        setResult(Activity.RESULT_OK,this)
                        finish()
                    }

        }
        return super.onOptionsItemSelected(item)
    }



    private fun saveData() {
        val panelCode = etPanelCode.text.toString()
        val partition = partitionSpinner.selectedItem.toString()
        if(mBound){
            mService.subscribe(panelCode)
        }
        val (pgm1,pgm2) = Pair(etPgm1.text.toString(),etPgm2.text.toString())
        db.insertProfile(etLocation.text.toString(),panelCode,etTeamId.text.toString(),alarmSpinner.selectedItem.toString(),partition,switchNotifications.isChecked,pgm1,pgm2)
        saveAliasAndContacts(Pair(etPanelCode.text.toString(),partitionSpinner.selectedItem.toString()))
    }


    private fun updateData(){
        val id = Pair(etPanelCode.text.toString(),partitionSpinner.selectedItem.toString())
        db.updateProfile(id,etPanelCode.text.toString())
        saveAliasAndContacts(id)
    }

    private fun saveAliasAndContacts(id : IdProfile){
        db.insertAlias(fromIdToCode(id),zoneAdapter.recoverNewAlias(),db.TABLE_ALIAS_ZONES)
        db.insertAlias(fromIdToCode(id),userAdapter.recoverNewAlias(),db.TABLE_ALIAS_USERS)
        db.insertContacts(fromIdToCode(id),panicContacts[0], typePanics[0])
        db.insertContacts(fromIdToCode(id),panicContacts[1], typePanics[1])
        db.insertContacts(fromIdToCode(id),panicContacts[2], typePanics[2])
    }


    private fun restoreState(id:IdProfile){
        //the delete button must be visible
        btDeleteProfile.visibility = View.VISIBLE
        val values = db.recoverProfile(id)
        etLocation.setText(values[5])
        etPanelCode.setText(values[0])
        etTeamId.setText(values[2])
        val models = resources.getStringArray(R.array.alarmModels)
        alarmSpinner.setSelection(models.indexOf(values[3]))
        builtLists(id)
    }


    private fun deleteProfile(id: IdProfile){

       if(mBound){
           mService.unsuscribedProfile(id.first)
       }


        db.deleteProfile(id)

        Intent().run{
            putExtra("deletedProfile",id )
            setResult(Activity.RESULT_OK,this)
            finish()
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){

            when(requestCode){
                ZONE_REQUEST -> {
                    val zone = data?.getStringExtra("zone")
                    val number = data?.getStringExtra("number")
                    val alias = "$number$SEPARATOR$zone"
                    zoneAdapter.addAlias(alias)
                }

                USER_REQUEST -> {
                    val user = data?.getStringExtra("user")
                    val number = data?.getStringExtra("number")
                    val alias = "$number$SEPARATOR$user"
                    userAdapter.addAlias(alias)
                }

                EDIT_PANIC -> {
                    panicContacts[0] = data?.getSerializableExtra(typePanics[0]) as ArrayList<Contact>
                    panicContacts[1] = data?.getSerializableExtra(typePanics[1]) as ArrayList<Contact>
                    panicContacts[2] = data?.getSerializableExtra(typePanics[2]) as ArrayList<Contact>
                }
            }

        }


    }


    private fun builtLists(id:IdProfile){
        val id = id.first+id.second
        zoneAdapter = AliasAdapter(id,this,db.TABLE_ALIAS_ZONES)
        findViewById<RecyclerView>(R.id.listZones).run{
            layoutManager = LinearLayoutManager(context)
            adapter = zoneAdapter
        }


        userAdapter = AliasAdapter(id,this,db.TABLE_ALIAS_USERS)
        findViewById<RecyclerView>(R.id.listUsers).run{
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }



    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }








}