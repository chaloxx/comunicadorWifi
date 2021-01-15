package rectec.monitoreoapp.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import rectec.monitoreoapp.R
import rectec.monitoreoapp.adapters.BypassZoneAdapter



//Differents alerts dialog (for select type of armed, for input user code or for select zones for bypassing)
class AlertDialogManager(val context: Context, val action: (Boolean, String) -> Unit){
    val dialogBuilder = AlertDialog.Builder(context)
    private var choice : Boolean = false
    private var userCode : String = ""
    private val KEY_PASS = "KEY_PASS"


    fun buildForLogin(){
        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_login,null)
        dialogBuilder.setView(mView)
        val dialog = dialogBuilder.create()
        val etPass = mView.findViewById<EditText>(R.id.etLogin)
        val preferences = context.getSharedPreferences(context.getString(R.string.pass_file),Context.MODE_PRIVATE)
        val passSaved =  preferences.getString(KEY_PASS,null)
        if (passSaved == null) etPass.hint = context.getString(R.string.str_enter_pass)
        mView.findViewById<Button>(R.id.btAcceptPass).setOnClickListener {
            val passInput = etPass.text.toString()
            // If there isnt a previus pass save the pass input

            if (passSaved == null){
                preferences.edit().putString(KEY_PASS,passInput).apply()
                dialog.dismiss()
                action(false,"")
            }
            else{
                // Check if the pass is right
                if (passSaved.equals(passInput)) {
                    action(false,"")
                    dialog.dismiss()
                }
                else{
                    etPass.text.clear()
                    Toast.makeText(context, context.getString(R.string.str_error_pass),Toast.LENGTH_SHORT).show()

                }
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }


    fun buildForSelect(){
        windowForSelect()
    }


    fun buildForUserCode(){
        windowForUserCode()
    }

    fun buildForByPass(){
        windowForBypass()
    }

    private fun windowForSelect() {

        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_select_armed,null)
        dialogBuilder.setView(mView)
        val dialog = dialogBuilder.create()
        mView.findViewById<Button>(R.id.armedPresent).setOnClickListener{
            dialog.dismiss()
            choice = false
            windowForUserCode()


        }
        mView.findViewById<Button>(R.id.armedAway).setOnClickListener {
            dialog.dismiss()
            choice = true
            windowForUserCode()

        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    private fun windowForUserCode() {

        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_enter_user_code,null)
        dialogBuilder.setView(mView)
        val dialog = dialogBuilder.create()
        val etUserCode = mView.findViewById<EditText>(R.id.etUserCode)
        mView.findViewById<Button>(R.id.btConfirmUserCode).setOnClickListener{
            dialog.dismiss()
            userCode = etUserCode.getText().toString()
            action(choice,userCode)

        }
        mView.findViewById<Button>(R.id.btCancelUserCode).setOnClickListener {
            dialog.dismiss()
        }


        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }


    private fun windowForBypass(){
        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_bypass_zone,null)
        val gridLayout = mView.findViewById<GridView>(R.id.bypassGridLayout)
        gridLayout.adapter = BypassZoneAdapter(context)
        dialogBuilder.setView(mView)
        val dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)

        mView.findViewById<Button>(R.id.btSendByPass)?.setOnClickListener {
            (gridLayout.adapter as BypassZoneAdapter).recoverZones().forEach {zone ->
                action(false,zone.toString())
            }
            dialog.dismiss()

        }

        dialog.show()
    }


}