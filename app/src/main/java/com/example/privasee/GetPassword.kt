package com.example.privasee

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.privasee.databinding.ActivityEnterPasswordBinding
import com.example.privasee.ui.monitor.Constants
import com.example.privasee.ui.users.addUser.AddUserCapturePhoto
import com.example.privasee.ui.users.addUser.AddUserCapturePhoto.Companion.REQUEST_CODE_PERMISSION
import com.example.privasee.ui.users.addUser.CaptureReferenceImage
import kotlinx.android.synthetic.main.activity_enter_password.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class  GetPassword : AppCompatActivity(){

    private lateinit var binding: ActivityEnterPasswordBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var loadingDialog : LoadingDialogFaceMatching

    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        binding = ActivityEnterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if(!(sp.getBoolean("isEnrolled", false))){ //if training data exists already

            val intent = Intent(this@GetPassword, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.faceUnlock.isEnabled = false
        if (allPermissionGranted()) {
            startCamera()
            binding.faceUnlock.isEnabled = true

        } else
            ActivityCompat.requestPermissions(this,
                CaptureReferenceImage.REQUIRED_PERMISSIONS,
                CaptureReferenceImage.REQUEST_CODE_PERMISSION
            )

        faceUnlock.setOnClickListener {
            binding.faceUnlock.isEnabled = false
            loadingDialog = LoadingDialogFaceMatching(this)
            loadingDialog.startLoadingDialog()
            takePhoto()
        }

    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val outputDirectory = getOutputDirectory()
        val pathSnapshot = "$outputDirectory/face unlock"
        val fullpath = File(pathSnapshot)

        if (!fullpath.exists()) {
            fullpath.mkdirs()
        }

        val fileName = SimpleDateFormat(
            Constants.FILE_NAME_FORMAT,
            Locale.getDefault())
            .format(System
                .currentTimeMillis()) + ".jpg"

        val photoFile = File(
            "$fullpath",fileName)

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    ifFaceExist(photoFile.toString())

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(
                        CaptureReferenceImage.TAG,
                        "onError: ${exception.message}",
                        exception)
                }
            }
        )
    }
    private fun ifFaceExist(string: String){

        val bitmap = BitmapFactory.decodeFile(string)
        captureFace.visibility = View.INVISIBLE;
        seeCapturedFace.setImageBitmap(bitmap)

        //start python
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        val py = Python.getInstance()

        //val imageString = getStringImage(bitmap)

        val pyobj = py.getModule("face_detection") //give name of python file
        val obj = pyobj.callAttr("main", string, "") //call main method
        val str = obj.toString()

        if(str == "False"){
            erroDialog("No face detected", "Please center your face inside the box.")
        }else{

            CoroutineScope(Dispatchers.IO).launch {
                val outputDirectory = getOutputDirectory()
                val pathFd = "$outputDirectory/login key/login_key.jpg"

                if (!Python.isStarted()) Python.start(AndroidPlatform(this@GetPassword))
                val py = Python.getInstance()
                val pyobj = py.getModule("recognition") //give name of python file
                val obj = pyobj.callAttr("android_kotlin", pathFd, string) //call main method
                val strPass = obj.toString()
               val longPass = strPass.replace("%", "").toDouble()

              //  seeIfMatched.setText(strPass)


                withContext(Dispatchers.Main) {
                  //  seeIfMatched.text = obj.toString()
                    loadingDialog.dismissDialog()

                    if(longPass > 95){
                        val file = File(string)
                        if (file.exists()) {
                            file.delete()
                        }

                        Toast.makeText(this@GetPassword, "Threshold: $longPass", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@GetPassword, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }else{
                        erroDialog("Not Matched", "Please try again. Threshold $longPass")
                      //  Toast.makeText(this@GetPassword, "Face not Matched!", Toast.LENGTH_SHORT).show()
                        binding.faceUnlock.isEnabled = true
                    }

                }
            }
        }

    }

    private fun erroDialog (title:String, message:String){

        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage(message)
            setTitle(title)
            builder.setCancelable(false)
            setPositiveButton("ok") { _, _ ->
                if(title == "Not Matched"){
                    captureFace.visibility = View.VISIBLE
                    seeCapturedFace.setImageBitmap(null)
                }
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.captureFace.surfaceProvider
                    )
                }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e:Exception) {
                Log.d(CaptureReferenceImage.TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun getStringImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

        //store in byte array
        val imageBytes = baos.toByteArray()
        //finally encode to string
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun imageReader(root: File): ArrayList<File> {
        val a: ArrayList < File > = ArrayList()
        if (root.exists()) {
            val files = root.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    if (files[i].name.endsWith(".jpg")) {
                        a.add(files[i])
                    }
                }
            }
        }
        return a
    }

    private fun getCurrentPassword(): String? {
        val calendar: Calendar = Calendar.getInstance()
        // calendar.time = Date()

        val time = calendar.timeInMillis
        val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
        val timeString = sdf.format(Date(time))

        return timeString.replace(":", "")
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int){ //if not granted, it asks for permission
        when {

            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
            shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode) //explains why permission is needed after they rejected it the first time

            else -> {
                goToSettings()
            }
        }
    }

    private fun showDialog (permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app. If you deny this again, you will have to manually add permission via settings.")
            setTitle("Permission required")
            setPositiveButton("ok") { dialog, which ->
                ActivityCompat.requestPermissions(this@GetPassword, arrayOf(permission), requestCode)
                finish()
                startActivity(getIntent());
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun goToSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        this.startActivity(intent)
    }

    private fun allPermissionGranted() = CaptureReferenceImage.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((REQUEST_CODE_PERMISSION == 111) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            startCamera()
            binding.faceUnlock.isEnabled = true
        }
    }

}