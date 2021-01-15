package rectec.monitoreoapp.util
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import rectec.monitoreoapp.MessageService


class MqttClient(private val ms: MessageService) {
    private val TAG = MqttClient::class.java.name

    private var  mqttAndroidClient: MqttAndroidClient

    init {

        mqttAndroidClient = MqttAndroidClient(
            ms.applicationContext,
            Constants.MQTT_BROKER_URL,
            Constants.CLIENT_ID
        )

        connect()
        receiveMessages()
    }



    private fun connect() {
        try {
            val token = mqttAndroidClient.connect(getMqttConnection())
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(TAG, "connection success")
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Log.i(TAG, "connection failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }

    private fun getMqttConnection() : MqttConnectOptions{
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.setCleanSession(false)
        mqttConnectOptions.setAutomaticReconnect(true)
        return mqttConnectOptions

    }


    private fun getDisconnectedBufferOptions(): DisconnectedBufferOptions? {
        val disconnectedBufferOptions = DisconnectedBufferOptions()
        disconnectedBufferOptions.isBufferEnabled = true
        disconnectedBufferOptions.bufferSize = 100
        disconnectedBufferOptions.isPersistBuffer = false
        disconnectedBufferOptions.isDeleteOldestMessages = false
        return disconnectedBufferOptions
    }


    private fun receiveMessages() {
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                //connectionStatus = false
                // Give your callback on failure here
                Log.i(TAG, "connection lost")
            }

            override fun messageArrived(topic: String, msg: MqttMessage) {
                try {
                    val data = String(msg.payload, charset("UTF-8"))
                    // data is the desired received message
                    // Give your callback on message received here
                    Log.i(TAG, "message arrived: $data")
                    ms.processMessage(topic,data)

                } catch (e: Exception) {
                   Log.i(TAG, "error to read message")
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                  Log.i(TAG, "delivery complete")
            }
        })
    }







    fun subscribe(topic: String, qos: Int) {
        try {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Give your callback on Subscription here
                    //Log.i(TAG, "suscribed succesfully to topic $topic")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    //Log.i(TAG, "suscribed failed to topic $topic")
                    // Give your subscription failure callback here
                }
            })
        } catch (e: MqttException) {
            // Give your subscription failure callback here
            Log.i(TAG, "suscribed failed to topic $topic")
        }
    }



    fun unSubscribe(topic: String) {
        try {
            val unsubToken = mqttAndroidClient.unsubscribe(topic)
            unsubToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Give your callback on unsubscribing here
                    Log.i(TAG, "unsuscribed successfully to topic $topic")
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Give your callback on failure here
                }
            }
        } catch (e: MqttException) {
            // Give your callback on failure here
            Log.i(TAG, "unsuscribed failed to topic $topic")
        }
    }




    fun publish(topic: String, qos: Int, data: String) {
        val encodedPayload : ByteArray
        try {
            encodedPayload = data.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.qos = qos
            message.isRetained = false
            mqttAndroidClient.publish(topic, message)
        } catch (e: Exception) {
            Log.i(TAG,"Error to publish $data")
            e.printStackTrace()
            // Give Callback on error here
        } catch (e: MqttException) {
            Log.i(TAG,"Error to publish $data")
            e.printStackTrace()

        }
    }



    fun disconnect() {
        try {
            val disconToken = mqttAndroidClient.disconnect()
            disconToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    //connectionStatus = false
                    // Give Callback on disconnection here
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    // Give Callback on error here
                }
            }
        } catch (e: MqttException) {
            // Give Callback on error here
        }
    }


    fun subscribeClient(subtopic1: String, subtopic2: String){
        subscribe(subtopic1, 1)
        subscribe(subtopic2, 0)
    }

}

