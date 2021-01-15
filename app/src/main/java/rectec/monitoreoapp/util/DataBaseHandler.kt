package rectec.monitoreoapp.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import rectec.monitoreoapp.CreateProfile
import rectec.monitoreoapp.R
import rectec.monitoreoapp.util.Constants.fromIdToCode
import rectec.monitoreoapp.util.Constants.normalizedPartition
import rectec.monitoreoapp.util.Constants.typePanics
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


val DATABASE_NAME = "MONITOREO"


typealias SqlResult = Array<String>




//Class to manage sqlite
class DataBaseHandler(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    private val  writableDB : SQLiteDatabase by lazy { this.writableDatabase }
    private val  readableDB : SQLiteDatabase by lazy { this.readableDatabase }

    val TABLE_PROFILE = "profile"
    val TABLE_MESSAGES = "messageService"
    val TABLE_ALIAS_USERS = "aliasUsers"
    val TABLE_ALIAS_ZONES = "aliasZones"
    val TABLE_CONTACTS = "tablePanics"

    val TAG = DataBaseHandler::class.java.name

    val columnNames : HashMap<String, Array<String>> = hashMapOf(
        TABLE_PROFILE to arrayOf("panelCode","partition", "teamdId", "modelAlarm", "date", "name","notifications","pgm1","pgm2"),
        TABLE_MESSAGES to arrayOf("msg","adressed","date","time"),
        TABLE_ALIAS_USERS to arrayOf("id", "propertyName", "alias"),
        TABLE_ALIAS_ZONES to arrayOf("id", "propertyName", "alias"),
        TABLE_CONTACTS to arrayOf("idProperty","idContact","phoneNumber","typePanic")
    )





    override fun onCreate(db: SQLiteDatabase?) {
        columnNames[TABLE_PROFILE]?.let{
            val tableProfile =  "CREATE TABLE " + TABLE_PROFILE +" ("+
                    "${it[0]} VARCHAR(256) ,"+
                    "${it[1]} VARCHAR(256) NOT NULL," +
                    "${it[2]} VARCHAR(256) NOT NULL, " +
                    "${it[3]} INTEGER NOT NULL," +
                    "${it[4]} VARCHAR(256) NOT NULL, " +
                    "${it[5]} VARCHAR(256)," +
                    "${it[6]} INTEGER NOT NULL," +
                    "${it[7]} VARCHAR(256)," +
                    "${it[8]} VARCHAR(256)," +
                    "PRIMARY KEY(${it[0]},${it[1]}))"
                    db?.execSQL(tableProfile)
        }


        columnNames[TABLE_MESSAGES]?.let {
            val tableMessage = "CREATE TABLE $TABLE_MESSAGES ("+
                                      "${it[0]} VARCHAR(256),"+
                                      "${it[1]} VARCHAR(256),"+
                                      "${it[2]} VARCHAR(256),"+
                                      "${it[3]} VARCHAR(256))"
                                      db?.execSQL(tableMessage)
        }


        columnNames[TABLE_ALIAS_USERS]?.let {
            val tableUserAlias = "CREATE TABLE $TABLE_ALIAS_USERS ("+
                                     "${it[0]} VARCHAR(256),"+
                                     "${it[1]} VARCHAR(256),"+
                                     "${it[2]} VARCHAR(256),"+
                                     "PRIMARY KEY(${it[0]},${it[1]}))"
                                     db?.execSQL(tableUserAlias)
        }


        columnNames[TABLE_ALIAS_ZONES]?.let {
            val tableZoneAlias = "CREATE TABLE $TABLE_ALIAS_ZONES ("+
                    "${it[0]} VARCHAR(256),"+
                    "${it[1]} VARCHAR(256),"+
                    "${it[2]} VARCHAR(256),"+
                    "PRIMARY KEY(${it[0]},${it[1]}))"
            db?.execSQL(tableZoneAlias)
        }


        columnNames[TABLE_CONTACTS]?.let{
            val tableContacts = "CREATE TABLE $TABLE_CONTACTS (" +
                    "${it[0]} VARCHAR(256)," +
                    "${it[1]} VARCHAR(256)," +
                    "${it[2]} VARCHAR(256)," +
                    "${it[3]} VARCHAR(256)," +
                    "PRIMARY KEY(${it[0]},${it[1]}))"
            db?.execSQL(tableContacts)

        }



        

    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(newVersion>oldVersion){
           // Log.w( TAG, "Updating database from version $oldVersion to " + "$newVersion .Existing data will be lost." )
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
            onCreate(db);
        }
    }


    fun insertProfile(
        name: String,
        panelCode: String,
        teamId: String,
        modelAlarm: String,
        partition: String,
        notifications:Boolean,
        pgm1 : String,
        pgm2 : String
    ){
        val cv = ContentValues()
        val currentDate =  with(SimpleDateFormat("dd/M/yyyy")){format(Date())}
        var partitionNorm = normalizedPartition(partition)
        columnNames[TABLE_PROFILE]?.let{

            cv.put(it[0], panelCode)
            cv.put(it[1], partitionNorm)
            cv.put(it[2], teamId)
            cv.put(it[3], modelAlarm)
            cv.put(it[4], currentDate)
            cv.put(it[5], name)
            cv.put(it[6], if(notifications) 1 else 0)
            cv.put(it[7], pgm1)
            cv.put(it[8], pgm2)
            
        }
        

        // insert into database
        if(writableDB.insert(TABLE_PROFILE, null, cv)<0){
            Log.e(TAG, "error to insert values " + partitionNorm)
        }

    }


    // A profile is identify by the pair panelCode + partition
    // recover information about a determined profile
    fun recoverProfile(id: IdProfile):Array<String>{
        val (panelCode,partition) = id
        val colPC = columnNames[TABLE_PROFILE]!![0]
        val colP = columnNames[TABLE_PROFILE]!![1]
        val cursor=readableDB.query(
            TABLE_PROFILE,
            null,
            "$colPC=? AND $colP=?",
            arrayOf(panelCode,normalizedPartition(partition)),
            null,
            null,
            null,
            null
        )
        if(!cursor.moveToFirst()){
            //Log.e(TAG, "error to recover:" + panelCode + partition)
        }
        //return results in array
        return Array(columnNames[TABLE_PROFILE]!!.size){
           // Log.d("database","recover:${cursor.getString(it)}")
            cursor.getString(it)

        }

    }

    // Recover amount of profiles
    fun howManyProfiles():Int{
        val cursor = readableDB.query(
            TABLE_PROFILE,
            arrayOf("COUNT(*)"),
            null,
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        return cursor.getInt(0)
    }




    //Delete profile of database
    fun deleteProfile(id: IdProfile){
        val (panelCode,partition) = id
        val colPC = columnNames[TABLE_PROFILE]!![0]
        val colP = columnNames[TABLE_PROFILE]!![1]
        columnNames[TABLE_PROFILE]?.let {
            if(writableDB.delete(
                    TABLE_PROFILE,
                    "$colPC=? AND $colP=?",
                    arrayOf(panelCode,partition)
                ) < 0){
               // Log.e(TAG, "error to delete: " +panelCode+partition)
            }

        }

        deleteAllAlias(fromIdToCode(id),TABLE_ALIAS_USERS)
        deleteAllAlias(fromIdToCode(id),TABLE_ALIAS_ZONES)
        deleteAllMessages(id)
        typePanics.forEach {tp -> deleteAllContact(fromIdToCode(id),tp)  }



    }


    //Update profile in the database
    fun updateProfile(id:IdProfile, panelKey: String):Boolean{
        val (panelCode,partition) = id
        val colPC = columnNames[TABLE_PROFILE]!![0]
        val colP = columnNames[TABLE_PROFILE]!![1]
        val cv = ContentValues().apply{
            columnNames[TABLE_PROFILE]?.let {
                put(it[1], panelKey)
            }

        }
        return writableDB.update(
            TABLE_PROFILE, cv, "$colPC=? AND $colP=?", arrayOf(
                panelCode,partition)
        ) < 0
    }


    //Recover all profiles id in table
    fun recoverIdsProfiles(): MutableList<Pair<String,String>> {
        val colPC = columnNames[TABLE_PROFILE]!![0]
        val colP = columnNames[TABLE_PROFILE]!![1]
        return readableDB.query(TABLE_PROFILE, arrayOf(colPC,colP), null, null, null, null, null, null).let { cursor ->
            cursor.moveToFirst()
            MutableList(cursor.count) {
                if (it > 0) cursor.moveToNext()
                Pair(cursor.getString(0),cursor.getString(1))
            }
        }
    }
    
    fun insertMessage(id: String,msg: String){
        val currentDate = with(SimpleDateFormat("dd/M/yyyy")){ format(Date()) }
        val currentTime = with(SimpleDateFormat("HH:mm:ss")){ format(Date()) }
        val cv = ContentValues()
        columnNames[TABLE_MESSAGES]?.let{
                cv.put(it[0], msg)
                cv.put(it[1], id)
                cv.put(it[2], currentDate)
                cv.put(it[3], currentTime)

                // insert into database
                if(writableDB.insert(TABLE_MESSAGES, null, cv)<0){
                  //  Log.e(TAG, "error to insert values")
                }
        }

    }


    private fun recoverPartitionsWithPanelCode(panelCode: String) : Array<Pair<Partition,Boolean>>{
        val fieldPC = columnNames[TABLE_PROFILE]!![0]
        val fieldP = columnNames[TABLE_PROFILE]!![1]
        val fieldNotification = columnNames[TABLE_PROFILE]!![6]
        return readableDB.query(
            TABLE_PROFILE,
            arrayOf(fieldP,fieldNotification),
            "$fieldPC=?",
            arrayOf(panelCode), null, null, null, null
        ).let { cursor ->
            cursor.moveToFirst()
            Array(cursor.count){
                if (it > 0) cursor.moveToNext()
                Pair(cursor.getString(0),cursor.getInt(1) > 0)
            }
        }



    }



    fun howManyMessagesForId(id:IdProfile):Int{
        val idStr = id.first+id.second
        val fieldAdd = columnNames[TABLE_MESSAGES]!![1]
        val cursor = readableDB.query(
            TABLE_MESSAGES,
            arrayOf("COUNT(*)"),
            "$fieldAdd=?",
            arrayOf(idStr),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        return cursor.getInt(0)
    }


    fun recoverMessagesForId(id : IdProfile): Array<SqlResult> {
        val idStr = id.first+id.second
        return columnNames[TABLE_MESSAGES]!!.let {colNames ->
            val cursor = readableDB.query(
                TABLE_MESSAGES,
                null,
                "${colNames[1]}=?",
                arrayOf(idStr),
                null,
                null,
                "${colNames[2]},${colNames[3]} DESC",
                null
            )

            cursor.moveToFirst()
            Array(cursor.count){row ->
                if(row > 0) cursor.moveToNext()
                Array(colNames.size){i ->
                    cursor.getString(i)
                }
            }
        }
    }


    fun insertUserAlias(id: String, propertyName: String, alias: String){
        val cv = ContentValues()
        columnNames[TABLE_ALIAS_USERS]?.let{
            cv.put(it[0], id)
            cv.put(it[1], propertyName)
            cv.put(it[2], alias)
            if(writableDB.insert(TABLE_MESSAGES, null, cv)<0){
                //Log.e(TAG, "error to insert values")
            }
        }
    }


    fun insertAlias(propertyName: String, alias: ArrayList<String>,table: String){
        val cv = ContentValues()
        columnNames[table]?.let{ colNames ->
            writableDB.beginTransaction()
                try {
                    alias.forEach {alias ->
                        val split = alias.split(CreateProfile.SEPARATOR)
                        cv.put(colNames[0],normalizedAliasId(split[0],table))
                        cv.put(colNames[1],propertyName)
                        cv.put(colNames[2],split[1])

                        if(writableDB.insert(table, null, cv)<0){
                            //Log.e(TAG, "error to insert: $alias")
                        }

                    }
                    writableDB.setTransactionSuccessful()
                } finally {
                    writableDB.endTransaction()
                }
            }
    }


    private fun normalizedAliasId(id : String,table : String) : String{
        var str = id
        when(table){
            TABLE_ALIAS_ZONES -> while(str.length < 2) str = "0$str"
            TABLE_ALIAS_USERS -> while(str.length < 3) str = "0$str"
            else -> ""
        }
        return  str
    }




    fun recoverIdAlias(property: String,table: String) : ArrayList<String>{
        return ArrayList<String>().apply {
            columnNames[table]?.let{ colNames ->
                readableDB.query(table, arrayOf(colNames[0], colNames[2]), "${colNames[1]}=?", arrayOf(property), null, null, null, null)?.run{
                    if(moveToFirst()){
                        do{ add("${getString(0)}${CreateProfile.SEPARATOR}${getString(1)}") }while(moveToNext())
                    }
                }
            }
        }
    }



    fun deleteAlias(id:String,property : String,table:String) {
        columnNames[table]?.let{colNames->
            if(writableDB.delete(table, "${colNames[0]}=? AND ${colNames[1]}=?", arrayOf(id,property)) < 0){
               // Log.e(TAG, "error to delete alias " + id)
            }
        }
    }


    fun deleteAllAlias(property : String,table: String) {
        columnNames[table]?.let{colNames->
            if(writableDB.delete(table, "${colNames[1]}=?", arrayOf(property)) < 0){
                //Log.e(TAG, "error to delete all aliases")
            }
        }
    }




    fun recoverPushNotification(panelCode: String, partition: String) : Boolean?{
        return columnNames[TABLE_PROFILE]!!.let { colNames ->
            readableDB.query(TABLE_PROFILE, arrayOf(colNames[6]), "${colNames[0]}=? AND ${colNames[1]}=? ", arrayOf(panelCode,partition), null, null, null, null)?.let{
                    cursor ->
                     if(!cursor.moveToFirst()){
                          Log.e(TAG, "error to recover:" + panelCode + partition)
                     }
                      //return push notification boolean
                     cursor.getInt(6) > 0
            }
        }
    }







    // Check in the database if alias exist if not return id
    fun recoverAlias(idAlias:String,profile:String,table:String) : String {
        return columnNames[table]!!.let { colNames ->
            readableDB.query(table, arrayOf(colNames[2]), "${colNames[0]}=?  AND ${colNames[2]}=?", arrayOf(idAlias,profile), null, null, null, null)?.let{cursor ->
                if(cursor.moveToFirst()){
                    cursor.getString(0)
                } else{
                    idAlias
                }
            }
        }!!
    }


    //Insert message in the database , recover what are the partitions that must be notifies
    // and determines if the user want to be notified
    fun processMessageInDB(panelCode: String,partition:String,msg:String) : ArrayList<Pair<Partition,Boolean>>{

        return ArrayList<Pair<Partition,Boolean>>().apply{
            val eventCode = msg.substring(0,4)
            // if the event is shared, recover info of all partitions for the panel
            if (eventCode in Constants.sharedEvents){
                recoverPartitionsWithPanelCode(panelCode).map { p ->
                    //Only save msg in the database if it is not a secondary event
                    if(msg.substring(0,1) != "S")insertMessage(panelCode + p,msg)
                    add(p)
                }
            }
            else{
                if(msg.substring(0,1) != "S")insertMessage(panelCode+partition,msg)
                recoverPushNotification(panelCode,partition)?.let {res -> add(Pair(partition,res)) }
            }
        }
    }





/*
    fun createLog(event:String,id: String,profile:String) : String{
        return when(event){
            context.getString(R.string.event_disarmed) -> event
            context.getString(R.string.event_away_armed) -> event
            context.getString(R.string.event_present_armed) -> event
            context.getString(R.string.event_parcial_armed) -> event
            context.getString(R.string.event_parcial_armed) -> event
            context.getString(R.string.event_help_alarm) -> event
            context.getString(R.string.event_help_restored) -> event
            context.getString(R.string.event_fire_alarm) -> event
            context.getString(R.string.event_fire_restored) -> event
            context.getString(R.string.event_sonoround_alarm) -> event
            context.getString(R.string.event_panic_restored) -> event
            else -> context.getString(R.string.event_unrecognized)
        }

    }
*/


    fun deleteAllMessages(id:IdProfile){
        val fieldAdd = columnNames[TABLE_MESSAGES]!![1]
        if(writableDB.delete(
                TABLE_MESSAGES,
                "$fieldAdd=?",
                arrayOf(fromIdToCode(id))
            ) < 0){
                //Log.e(TAG, "error to delete messages for id: " + id)
        }
    }





    fun insertContacts(id: String, contacts: ArrayList<Contact>,typePanic: String){
        val cv = ContentValues()
        columnNames[TABLE_CONTACTS]?.let{ colNames ->
            writableDB.beginTransaction()
            try {
                contacts.forEach {contact ->
                    cv.put(colNames[0],id)
                    cv.put(colNames[1], contact.first)
                    cv.put(colNames[2], contact.second)
                    cv.put(colNames[3], typePanic)

                    if(writableDB.insert(TABLE_CONTACTS, null, cv)<0){
                        //Log.e(TAG, "error to insert contact with id"+contact.first)
                    }

                }
                writableDB.setTransactionSuccessful()
            } finally {
                writableDB.endTransaction()
            }
        }
    }


    fun deleteContact(id: String,contact : String,typePanic: String) {
        columnNames[TABLE_CONTACTS]?.let{colNames->
            if(writableDB.delete(TABLE_CONTACTS, "${colNames[0]}=? AND ${colNames[1]}=? AND ${colNames[3]}=?", arrayOf(id,contact,typePanic)) < 0){
                //Log.e(TAG, "error to delete")
            }
        }
    }


    //Recover contact list to make a panic difusion
    fun recoverContact(id : String,typePanic: String) : ArrayList<Contact> {
        return ArrayList<Contact>().apply{
            columnNames[TABLE_CONTACTS]!!.let { colNames ->
                readableDB.query(
                    TABLE_CONTACTS,
                    arrayOf(colNames[1], colNames[2]),
                    "${colNames[0]}=?  AND ${colNames[3]}=?",
                    arrayOf(id, typePanic),
                    null,
                    null,
                    null,
                    null
                ).let { cursor ->
                    if (cursor.moveToFirst()) {
                        repeat(cursor.count){index ->
                            if (index > 0) cursor.moveToNext()
                            add(Pair(cursor.getString(0), cursor.getString(1)))
                        }
                    }
                }
            }
        }
    }


    fun deleteAllContact(id: String,typePanic: String) {
        columnNames[TABLE_CONTACTS]?.let{colNames->
            if(writableDB.delete(TABLE_CONTACTS, "${colNames[0]}=? AND ${colNames[3]}=?", arrayOf(id,typePanic)) < 0){
            }
        }
    }



}