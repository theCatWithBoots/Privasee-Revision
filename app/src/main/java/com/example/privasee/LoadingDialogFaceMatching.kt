package com.example.privasee

import android.app.Activity
import android.app.AlertDialog
import com.example.privasee.R

class LoadingDialogFaceMatching internal constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null

    fun startLoadingDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog_face_matching, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog!!.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog!!.show()
    }

    fun dismissDialog() {
        dialog!!.dismiss()
    }
}