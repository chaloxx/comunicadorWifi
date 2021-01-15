package rectec.monitoreoapp.util


typealias IdProfile = Pair<String,String>
typealias Partition = String
typealias Contact = Pair<String,String>


//La app debe interactuar con los siguientes tópicos:CNT/PPPP/EA,CNT/PPPP/ES,CNT/PPPP/C (PPPP son 4 dígitos del número de panel)
//Tópicos EA: Eventos prioritarios, mensajes E o R (alarma activada o no) ejemplo E 130 01 002 (zona 2 partición 1)
//Tópicos ES: Eventos secundarios , eventos S
object Constants {
    //LOS TOPICOS DE PUB Y SUBS TIENEN EL FOTMATO EEE/PPPP/AA
    //EEE: la empresa, suponemos que no va a cambiar
    //PPPP: IDENTIFICADOR DE PANEL ESTO VA A TENER QUE SER SETEADO POR EL USUARIO CUANDO CREEEL PERFIL, Y VA A TENER QUE PROVEERLO LA BASE DE DATOS
    //AA: acción, esto depende de qué botón o qué quiera hacer el usuario, así que esto seguro que no es problema de Pablo
    //PARA REALIZAR UN DESARMADO, DEBE PASARSE POR EJEMPLO EL COMANDO X11234, EL FORMATO SERÍA XPCCCC
    //P: TIENE QUE ESTAR  POR DEFECTO EN 1, EN CASO DE SETEARSE SIN PARTICIÓN, Y SI SE SETEA ALGUNA EN CREAR PERFIL, DEBE DE PROVEERSE
    //CCCC: DEBE DE PROVEERSE DE LA BASE DE DATOS. CADA USUARIO DEL PANEL DEBE TENER UN CÓDIGO PROPIO PARA EL ARMADO Y DESARMADO
    //const val MQTT_BROKER_URL = "tcp://mitre1017.dyndns.tv:18831"
    const val MQTT_BROKER_URL = "tcp://190.230.169.42:18831"
    //const val MQTT_BROKER_URL = "tcp://192.168.0.13:1883"
    const val CLIENT_ID = "MartinAPP"
    const val TAG = "MQTT_APP"
    const val armedPres = "S"
    const val armedAway = "W"
    const val disarmed = "X"
    const val panic = "P"
    const val fire = "F"
    const val help = "A"
    const val pgm1 = "M1"
    const val pgm2 = "M2"
    const val byPass = "I"
    const val ready = "R1"

    val codes = mapOf(
        "disarmed" to "E400",
        "armedPresent" to "R400",
        "armedAway" to "R441",
        "armedParcial" to "R456",
        "byPassedZones" to "E570",
        "fire" to "E110",
        "fireRestored" to "R110",
        "readyOrNotReady" to "S999",
        "isArmed" to "S666",
        "openZones" to "S888",
        "sonorousPanic" to "E123",
        "sonorousPanicRestored" to "R123",
        "silentPanic" to "E122",
        "silentPanicRestored" to "R122"
    )


    val sharedEvents =
        arrayOf(codes["silentPanic"]!!,
                codes["silentPanicRestored"]!!,
                codes["fire"]!!,
                codes["fireRestored"]!!,
                codes["sonorousPanic"]!!,
                codes["sonorousPanicRestored"]!!)


    val typePanics = arrayOf("sonorousPanic","silentPanic","fire")



    fun fromIdToCode(id: IdProfile) : String{ return id.first+ normalizedPartition(id.second)}


    fun normalizedPartition(partition:String) : String {
        var normPart = partition
        while(normPart.length<2) normPart =  "0$normPart"
        return normPart
    }


    fun unNormalizedPartition(partition:String) : String{
        return partition.substring(1)
    }




}