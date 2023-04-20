package com.example.privasee.ui.users.addUser

import android.app.Activity
import android.app.AlertDialog
import com.example.privasee.R

class LoadingDialog internal constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null

    fun startLoadingDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog!!.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog!!.show()
    }

    fun dismissDialog() {
        dialog!!.dismiss()
    }
}