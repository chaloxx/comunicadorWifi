package rectec.monitoreoapp.util

import android.content.Context


// Container for profile data
class UserData(idP:IdProfile,
               context:Context
               ) {

    val nameProfile: String
    val panelCode: String
    val partition:String
    val id : String

    init {

        DataBaseHandler(context).run {
            recoverProfile(idP).let {
                //User alias
                nameProfile = it[5]
                //Panel code
                panelCode = it[0]
                //Partition
                partition= it[1]
                // Id in the database
                id = it[0]+it[1]
            }
        }
    }


}