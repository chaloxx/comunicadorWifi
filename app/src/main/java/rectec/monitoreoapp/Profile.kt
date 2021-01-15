package rectec.monitoreoapp



import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.telephony.SmsManager
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_profile.*
import rectec.monitoreoapp.util.*
import rectec.monitoreoapp.util.Constants.codes
import rectec.monitoreoapp.util.Constants.unNormalizedPartition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

//This Fragment initializes sub-fragments which are in charge of showing logs for
// each profile
class Profile : Fragment() {
    private lateinit var id : IdProfile
    private var TAG = Profile::class.java.name
    private lateinit var  tvStatus: TextView
    private lateinit var tvStatusInfo : TextView
    private lateinit var tvLastUpdate : TextView

    private lateinit var mService: MessageService
    private var mBound: Boolean = false


    private lateinit var btArmed : Button
    private lateinit var btDesarmed : Button
    private lateinit var btBypass : Button
    private lateinit var btDisablePanic : Button

    private lateinit var swipeRefresh : SwipeRefreshLayout
    private lateinit var profileView : View


    private val secsForTimeout : Long = 10

    //Determines if the status is in checked
    private var checkingStatus = false

    private  var armedRequest : Pair<Boolean,String>? = null

    private lateinit var db : DataBaseHandler


    private  var INIT = 19
    private var SONOROUS_ALARM = 20
    private var SILENT_ALARM = 21
    private var FIRE_ALARM = 22











    private val timeout by lazy{

        object : CountDownTimer(secsForTimeout * 1000, 1000) {

            private val unknownState = getString(R.string.unknown_state)

            override fun onTick(p0: Long) {
                //  Log.d("timer","remain:$p0")
            }

            override fun onFinish() {
                mService.quitSecondaryObserver(id.first+id.second)
                //Ready not arrived
                //toastMessage(errorToConnect)
                tvStatus.text = unknownState
                tvLastUpdate.text  = ""
                swipeRefresh.isRefreshing = false
                enabledArmed(false)
                enabledDisarmed(false)
            }
        }

    }


    //Connect with MessageService
            private val connection = object : ServiceConnection {

                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    val binder = service as MessageService.LocalBinder
                    mService = binder.getService()
                    mBound = true
                    subscribeProfile()
                    checkingStatus(profileView)
                    val panelCode = id.first
                    btDisablePanic.apply{
                        setOnClickListener {
                            mService.disablePanic(panelCode)
                        }

                        if(mService.isPanicActivated(panelCode)) visibility = View.VISIBLE

                    }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    // This is call as lazy because we dont know the name profile for now
    private val  userData : UserData by lazy {
        UserData(id,requireContext())
    }





    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(requireContext(), MessageService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(connection)
        mBound = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getSerializable("idProfile") as IdProfile
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        profileView = inflater.inflate(R.layout.fragment_profile, container, false)
        swipeRefresh = profileView.findViewById(R.id.swipeStatus)




        swipeRefresh.setOnRefreshListener{
            checkingStatus(profileView)
        }




        //Set function for deploy
        val btDeploy = profileView.findViewById<Button>(R.id.btDeploy)
        btDeploy.setOnClickListener {
            //Check the visibility of the grid and the bypass button
            val gridLayout = profileView.findViewById<GridLayout>(R.id.gridLayout)

            if(gridLayout.visibility == View.VISIBLE){
                gridLayout.visibility = View.GONE
                btBypass.visibility = View.GONE
                btDeploy.text = getString(R.string.str_btShow)
            }
            else{
                gridLayout.visibility = View.VISIBLE
                btBypass.visibility = View.VISIBLE
                btDeploy.text = getString(R.string.str_btHidden)
            }
        }

        btBypass = profileView.findViewById(R.id.btByPass)




        //Set callback for bypass
        btBypass.setOnClickListener {
            AlertDialogManager(requireContext()){ _,zone : String->
                publishMessage("${Constants.byPass}$zone")
            }.buildForByPass()
        }




        //Set the callbacks for buttons
        btArmed = profileView.findViewById(R.id.btArmado)
        btArmed.setOnClickListener{
            AlertDialogManager(requireContext()) { choice:Boolean,userCode:String ->
                armedRequest = Pair(choice,userCode)
                swipeRefresh.isRefreshing = true
                checkReady()
            }.buildForSelect()
        }


        btDesarmed = profileView.findViewById(R.id.btDesarmado)
        btDesarmed.setOnClickListener{
            AlertDialogManager(requireContext()) {_,userCode:String ->
                swipeRefresh.isRefreshing = true
                //Log.d("profile","${Constants.disarmed}${unNormalizedPartition(userData.partition)}$userCode")
                publishMessage("${Constants.disarmed}${unNormalizedPartition(userData.partition)}$userCode")
                //Log.d("timer","timer start in disarmed")
                timeout.start()
                enabledDisarmed(false)
                enabledArmed(false)
            }.buildForUserCode()
        }

        btDisablePanic = profileView.findViewById(R.id.btDisablePanic)



        profileView.findViewById<Button>(R.id.btSonorousPanic).setOnClickListener{
            //ALARMA SONORA
                sendSMS(SONOROUS_ALARM)
                //publishMessage(Constants.panic)
        }
        profileView.findViewById<Button>(R.id.btSilentPanic).setOnClickListener{
            //ALARMA SILENCIOSA
            sendSMS(SILENT_ALARM)
            publishMessage(Constants.panic)
        }
        profileView.findViewById<Button>(R.id.btFire).setOnClickListener{
            //ALARMA DE INCENDIO
            publishMessage(Constants.fire)
        }


        profileView.findViewById<Button>(R.id.btPgm1).setOnClickListener{
            //pgm1
            publishMessage(Constants.pgm1)
        }
        profileView.findViewById<Button>(R.id.btPgm2).setOnClickListener{
            //pgm2
            publishMessage(Constants.pgm2)
        }

        profileView.findViewById<TextView>(R.id.tvName).text = userData.nameProfile
        tvStatus = profileView.findViewById(R.id.tvStatus)
        tvStatusInfo = profileView.findViewById(R.id.tvStatusInfo)
        tvLastUpdate = profileView.findViewById(R.id.tvLastUpdate)

        db = DataBaseHandler(requireContext())

        checkPermission(INIT)


        profileView.findViewById<Button>(R.id.btFire).isEnabled = false

        return profileView
    }


    private fun publishMessage(msg:String){
        if(mBound){
            mService.publish(msg,"CNT/${userData.panelCode}/C")
        }
        else{
          //  Log.d(TAG,"publish-service off")
        }
    }

    private fun subscribeProfile(){
        if(mBound){
            //mService.subscribe("CNT/"+userData.panelCode+"/EA","CNT/"+userData.panelCode+"/ES")
            mService.addPrimaryObserver("${userData.panelCode}${userData.partition}",this)
        }
        else{
            //Log.d(TAG,"subscribe-service off")
        }
    }



    fun changeStatus(isArmed: Boolean, armedAway : Boolean){
        //Not listen secondary events
        mService.quitSecondaryObserver(userData.id)
        if(isArmed){
            if(armedAway){
                tvStatus.text = getString(R.string.event_away_armed)

            }else{
                tvStatus.text = getString(R.string.event_present_armed)
            }

            enabledDisarmed(true)
            enabledArmed(false)

        }

        else{

            tvStatus.text = getString(R.string.event_disarmed)
            enabledArmed(true)
            enabledDisarmed(false)

        }
        //Update time and date
        tvLastUpdate.text = requireContext().getString(R.string.event_last_update)+with(SimpleDateFormat("HH:mm:ss")){ format(Date()) }

    }



    fun messageArrived(msg:String) {

        val finishUpdate = {tag : String , armed : Boolean, armedAway : Boolean , clean : Boolean ->
            timeout.cancel()
            if(clean) tvStatusInfo.text = ""
            swipeRefresh.isRefreshing = false
            //Log.d("timer","timer cancel in $tag")
            changeStatus(armed, armedAway)
        }

        context.run {
            when (msg.substring(0, 4)) {
                //Ready or not ready
                codes["readyOrNotReady"] -> {
                    timeout.cancel()
                    //Log.d("timer", "timer cancel in ready")
                    if (msg.substring(8) == "1") { //if it is ready
                        if(checkingStatus){
                            checkingStatus = false
                            tvStatusInfo.text = getString(R.string.event_ready)
                            finishUpdate("disarmed",false,false,false)
                        }
                        else checkArmed()
                        mService.quitSecondaryObserver("${userData.panelCode}${userData.partition}")
                    }

                    else { // Not ready
                    }

                }

                codes["openZones"] -> {
                    checkingStatus = false
                    swipeRefresh
                    val aliasZones = msg.substring(0,4).split(",").map {idZone ->
                        db.recoverAlias(idZone,id.first+id.second,db.TABLE_ALIAS_ZONES)
                    }
                    val info = aliasZones.fold(""){acc,s -> "$acc,$s"}.substring(1)
                    tvStatusInfo.text = getString(R.string.event_open_zones)+info
                    mService.quitSecondaryObserver("${userData.panelCode}${userData.partition}")
                    enabledArmed(false)
                    enabledDisarmed(false)
                    swipeRefresh.isRefreshing = false
                }

                codes["isArmed"] -> {
                    finishUpdate("armed",true,msg.substring(6) == "001",true)
                    mService.quitSecondaryObserver("${userData.panelCode}${userData.partition}")

                }


                codes["armedPresent"] -> finishUpdate("armed present",true,false,true)

                codes["armedAway"] -> finishUpdate("armed away",true,true,true)

                codes["disarmed"] -> finishUpdate("disarmed",false,false,true)


                /*codes["armedParcial"] -> getString("armed parcial",)

                "E570" -> getString(R.string.event_parcial_armed)
                "E100" -> getString(R.string.event_help_alarm)
                "R100" -> getString(R.string.event_help_restored)
                "E110" -> getString(R.string.event_fire_alarm)
                "R110" -> getString(R.string.event_fire_restored)
                "E120" -> getString(R.string.event_panic_alarm)
                "R120" -> getString(R.string.event_panic_restored)


                else -> {}*/
            }

        }
    }


    private fun checkReady() {

        //Send armed and start a timeout messfor a response
        //Register broadcast only for my panel and partition
        mService.addSecondaryObserver(userData.id,this)
        publishMessage(Constants.ready)
        timeout.start()
       // Log.d("timer","timer start in ready")
        enabledArmed(false)
        enabledDisarmed(false)
    }

    private fun checkArmed(){
        val (armedAway,userCode) = armedRequest!!
        val partition = unNormalizedPartition(userData.partition)
        if (armedAway){
            publishMessage("${Constants.armedAway}$partition${userCode}")
        }
        else{
            publishMessage("${Constants.armedPres}$partition${userCode}")
        }
        timeout.start()
        //Log.d("timer","timer start in armed")
    }




    private fun toastMessage(msg:String){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
    }




    private fun enabledArmed(enabled:Boolean){
        btArmed?.isEnabled = enabled
        btArmed?.isClickable = enabled
        btByPass?.isEnabled = enabled
        btByPass?.isClickable = enabled
    }





    private fun enabledDisarmed(enabled:Boolean){
        btDesarmed?.isEnabled = enabled
        btDesarmed?.isClickable = enabled
    }



    private fun checkingStatus(v:View){
        v.findViewById<TextView>(R.id.tvStatus).setText(getString(R.string.str_checking_status))
        enabledArmed(false)
        enabledDisarmed(false)
        mService.addSecondaryObserver(id.first+id.second,this)
        checkingStatus = true
        swipeRefresh.isRefreshing = true
        publishMessage(Constants.ready)
        timeout.start()
    }





    private fun sendSMS(code : Int) {
        val event = when(code){
             SONOROUS_ALARM -> "SONORA"
             SILENT_ALARM -> "SILENCIOSA"
             FIRE_ALARM -> "DE INCENDIO"
            else -> ""
        }
        val buildMapURl = {location : Location -> "http://maps.google.com/maps?q=${location.latitude},${location.longitude}"}
        val strs = getString(R.string.sms_txt_msg).split("@")
        val buildSMS = {profile: String, location : Location -> "${strs[0]}$event${strs[1]}$profile${strs[2]}" + buildMapURl(location)}
        if (checkPermission(code)) {

            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener { location ->
                val smsManager = SmsManager.getDefault() as SmsManager
                val profile = db.recoverProfile(id)[5]
                db.recoverContact(Constants.fromIdToCode(id), Constants.typePanics[0])
                    .forEach { contact ->
                        smsManager.sendTextMessage(contact.second, null, buildSMS(profile,location), null, null)
                        Toast.makeText(requireContext(),"${getString(R.string.sms_txt_notified)} ${contact.first}",Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                when(requestCode){
                    SONOROUS_ALARM -> sendSMS(SONOROUS_ALARM)

                    SILENT_ALARM -> sendSMS(SILENT_ALARM)

                    FIRE_ALARM -> sendSMS(FIRE_ALARM)
                }
            }
    }





    //Check if there are permissions and request if not
    private fun checkPermission(code : Int) : Boolean{
        val permission1 = ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val permission2 = ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val permissionsToRequest = HashSet<String>()
        if (permission1 && permission2) return true
        if(!permission1) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if(!permission2) permissionsToRequest.add(Manifest.permission.SEND_SMS)
        requestPermissions(permissionsToRequest.toArray(arrayOfNulls<String>(permissionsToRequest.size)),code)
        return false
    }

    fun disableAlarm(){
        btDisablePanic.visibility = View.GONE
    }


    fun activatedAlarm(){
        btDisablePanic.visibility = View.VISIBLE
    }


}