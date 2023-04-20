package com.example.privasee.ui.controlAccess

import androidx.appcompat.app.AlertDialog


class LockScreen(private val activity: MyForegroundServices){

     fun showDialog (){
        val builder = AlertDialog.Builder(activity)

        builder.apply {
            setMessage("This device will now be locked.")
            setTitle("Time ran out")
            setPositiveButton("ok") { dialog, which ->
                ControlAccessFragmentScreenTimeLimit.devicePolicyManager!!.lockNow()
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}