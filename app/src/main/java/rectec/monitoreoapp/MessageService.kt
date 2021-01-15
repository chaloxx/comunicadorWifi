package rectec.monitoreoapp

//import android.util.Log

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import rectec.monitoreoapp.util.Constants
import rectec.monitoreoapp.util.Constants.codes
import rectec.monitoreoapp.util.DataBaseHandler
import rectec.monitoreoapp.util.MqttClient


//This class is the intermediate between the paho client and the home screen
// every time a message arrived the correspondent profile is notify
// a notification consist of an event and a alias of the profile
// Example: Event in profile Pepe -> Alert of fire



class MessageService() : Service() {


    private lateinit var pahoMqttClient : MqttClient
    private lateinit var db : DataBaseHandler
    private val observers = mapOf(
        "PRIMARY" to mutableMapOf<String, Profile>(),
        "SECONDARY" to mutableMapOf()
    )


    val TAG = MessageService::class.java.name
    val CHANNEL_ID = "101"
    var NOTIFICATION_ID  = 0

    private lateinit var wakeLock: WakeLock


    val panics = mutableMapOf<String, MediaPlayer>()


    inner class LocalBinder : Binder() {
        // Return this instance of MessageService so clients can call public methods
        fun getService(): MessageService = this@MessageService
    }





    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "service started")
        return START_STICKY
    }


    override fun onCreate() {
        Log.i(TAG, "service created")

        createNotificationChannel()

        //Make that service run in foreground
        startForeground(1001, createNotification());


        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        //Asegurar que el cpu se continué ejecutando
        wakeLock.acquire()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, Intent(this, HideNotificationService::class.java))
        }

        pahoMqttClient = MqttClient(this)
        db = DataBaseHandler(this)
    }


    override fun onBind(p0: Intent?): IBinder? {
        return LocalBinder()
    }


    fun processMessage(topic: String, msg: String){

        Log.i(TAG, "llego:$msg topic:$topic")
        val (eventCode, partition) = parseMsg(msg)
        val panelCode = topic.split("/")[1]
        var pushNotification = false

        //For the shared events, if a profile want to receive a push notification
        // so everyone on the panel will receive a push notification
        db.processMessageInDB(panelCode, partition, msg).forEach { (p,push) ->
            pushNotification = pushNotification || push
            notifyObserver(panelCode + p, msg)
        }
        if(pushNotification) setPushNotification(eventCode, partition, panelCode)

        //Activated sonorous alarm
        if (eventCode == codes["sonorousPanic"]) activateSonorousPanic(panelCode)

        //Notify that message arrives for updates
        sendBroadcast(Intent("message_arrived"))
    }



    fun setPushNotification(eventCode: String, partition: String, panelCode: String) {
        val (str, mustBeNotified) = codeToPair(eventCode)
        if (!mustBeNotified) return
        //Define title depending on the type of event
        val title = if(eventCode in Constants.sharedEvents) "${getString(R.string.title_panel)}  $panelCode"

                    else {
                            val profile = db.recoverProfile(Pair(panelCode, partition))?.get(5)
                            "${getString(R.string.title_profile)} $profile"
                     }


        var mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(str)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val resultIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        NOTIFICATION_ID += (NOTIFICATION_ID+1) %10
    }

    fun notifyObserver(id: String, msg: String){
        // the califier identify the priority
        val type : String = if(msg.substring(0, 1) == "S") "SECONDARY"
                            else "PRIMARY"
        observers[type]!![id]?.messageArrived(msg)

    }

    fun addPrimaryObserver(id: String, p: Profile){
        observers["PRIMARY"]!!.put(id, p)
    }
    fun addSecondaryObserver(id: String, p: Profile){
        observers["SECONDARY"]!!.put(id, p)
    }

    fun quitPrimaryObserver(id: String){
        observers["PRIMARY"]!!.remove(id)
    }

    fun quitSecondaryObserver(id: String){
        observers["SECONDARY"]!!.remove(id)
    }






    fun publish(msg: String, topic: String){
        pahoMqttClient.publish(topic, 0, msg)
    }

    fun subscribe(panelCode: String){
        val topic1 = "CNT/$panelCode/EA"
        val topic2 = "CNT/$panelCode/ES"
        pahoMqttClient.subscribeClient(topic1, topic2)
    }




    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MESSEGES"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID.toString(), name, importance).apply {
                description = "NADA"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    //Parse  the event code and return the corresponding message and if it
    // must be notified as push notification
    private fun codeToPair(code: String) : Pair<String, Boolean>{
        return when(code){
            codes["disarmed"] -> Pair(getString(R.string.event_disarmed), true)
            codes["armedPresent"] -> Pair(getString(R.string.event_present_armed), true)
            codes["armedAway"] -> Pair(getString(R.string.event_away_armed), true)
            codes["armedParcial"] -> Pair(getString(R.string.event_parcial_armed), true)
            codes["byPassedZones"] -> Pair("", false)
            codes["sonorousAlarm"] -> Pair(getString(R.string.event_help_alarm), true)
            codes["sonorousAlarmRestored"] -> Pair("", false)
            codes["fire"] -> Pair(getString(R.string.event_fire_alarm), true)
            codes["fireRestored"] -> Pair("", false)
            codes["readyOrNotReady"] -> Pair("",false)
            codes["isArmed"] -> Pair("",false)
            codes["openZones"] -> Pair("",false)
            codes["sonorousPanic"] -> Pair(getString(R.string.event_sonorous_panic), true)
            codes["sonorousPanicRestored"] -> Pair("", false)
            codes["silentPanic"] -> Pair(getString(R.string.event_silent_panic), true)
            codes["silentPanicRestored"] -> Pair("", false)
            else -> Pair(getString(R.string.event_unrecognized), true)
        }
    }


    private fun parseMsg(msg: String) : Pair<String, String>{
        return Pair(msg.substring(0, 4), msg.substring(4, 6))

    }





    fun unsuscribedProfile(panelCode: String){
        pahoMqttClient.unSubscribe("CNT/$panelCode/EA")
        pahoMqttClient.unSubscribe("CNT/$panelCode/ES")
    }



    //Play sonorous panic in panel code
    private fun activateSonorousPanic(panelCode: String){
        val mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.start()
        mediaPlayer.setVolume(0.5f, 0.5f)
        panics.put(panelCode, mediaPlayer)
        //Notify to all observers of this panel that the panic is ringing
        observers["PRIMARY"]!!.forEach {
            if (it.key.substring(0, 4) == panelCode) it.value.activatedAlarm()
        }
    }


    //That is call by the panel for disable the panic
    fun disablePanic(panelCode: String){
        val mediaPlayer = panics.remove(panelCode)
        mediaPlayer!!.stop()
        observers["PRIMARY"]!!.forEach {
            if (it.key.substring(0, 4) == panelCode) it.value.disableAlarm()
        }

    }


    //Predicated
    fun isPanicActivated(panelCode: String) : Boolean{
        return panics.containsKey(panelCode)
    }

    //Create notification for the service
    private fun createNotification(): Notification? {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.service)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
        val intent = Intent(this, MainActivity::class.java)
            builder
                .setTicker("Servicio corriendo").color = ContextCompat.getColor(this, R.color.primaryDark)
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
        return builder.build()
    }


    inner class HideNotificationService : Service() {
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            startForeground(NOTIFICATION_ID, createNotification())
            stopForeground(true)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
    }


    override fun onDestroy() {
        stopForeground(true)
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release()
        }

    }

}