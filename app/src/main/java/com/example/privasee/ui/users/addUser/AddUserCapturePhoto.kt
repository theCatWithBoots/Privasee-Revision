package com.example.privasee.ui.users.addUser

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.example.privasee.R
import com.example.privasee.databinding.ActivityAddUserCapturePhotoBinding
import com.example.privasee.ui.monitor.Constants
import kotlinx.android.synthetic.main.activity_add_user_capture_photo.*
import kotlinx.android.synthetic.main.activity_capture_reference_image.*
import kotlinx.android.synthetic.main.capture_reference_instructions.*
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.android.synthetic.main.custom_dialog.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.StreamSupport


class AddUserCapturePhoto: AppCompatActivity() {

    private lateinit var binding: ActivityAddUserCapturePhotoBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
   // private lateinit var loadingDialog : LoadingDialog
    var counter = 1
    private lateinit var dialogBinding: View
    private lateinit var loadingDialog: Dialog
    var dialogCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddUserCapturePhotoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        outputDirectory = getOutputDirectory()
        outputDirectory = File(".$outputDirectory")
        cameraExecutor = Executors.newSingleThreadExecutor()


        binding.addUserCaptureButton.isEnabled = false
        if (allPermissionGranted()) {
            startCamera()
            binding.addUserCaptureButton.isEnabled = true
        } else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)

        binding.addUserCaptureButton.setOnClickListener {

            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            if((sp.getBoolean("isEnrolled", false))){ //if training data exists already
                val dir = File("$outputDirectory/face recognition")

                if (dir.isDirectory) {
                    val children = dir.list()
                    for (i in children.indices) {
                        File(dir, children[i]).delete()
                    }
                }

                val editor = sp.edit()
                editor.apply(){
                    putBoolean("isEnrolled", false)
                }.apply()
            }

            dialogCounter = 0
            dialogBinding = layoutInflater.inflate(R.layout.custom_dialog, null)
            loadingDialog = Dialog(this)
            loadingDialog.setContentView(dialogBinding)

            loadingDialog.setCancelable(false)
            loadingDialog.textView3.text = "Creating Training Data. Please Wait..."
            loadingDialog.progress_bar.progress = counter*5
            val progCounter = counter * 5
            loadingDialog.text_view_progress.text = "$progCounter%"
            loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog.show()

         //   val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sp.edit()
            editor.apply(){
                putBoolean("isThereAFace", false)
            }.apply()

            editor.apply(){
                putInt("loadingStopCounter", counter)
            }.apply()

            takePhoto()

        }
        binding.btnCameraPermission.setOnClickListener {
            checkForPermissions(android.Manifest.permission.CAMERA, "Camera", com.example.privasee.Constants.REQUEST_CODE_PERMISSIONS)
        }
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

    private fun goToSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        this.startActivity(intent)
    }

    private fun showDialog (permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app. If you deny this again, you will have to manually add permission via settings.")
            setTitle("Permission required")
            setPositiveButton("ok") { dialog, which ->
                ActivityCompat.requestPermissions(this@AddUserCapturePhoto, arrayOf(permission), requestCode)
                finish()
                startActivity(getIntent());
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun getOutputDirectory(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val pathSnapshot = "$outputDirectory/raw photo"
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
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val sp = PreferenceManager.getDefaultSharedPreferences(this@AddUserCapturePhoto)

                    if((sp.getBoolean("isThereAFace", false))){//if face is detected
                        faceDetection(photoFile.toString())
                    } else{
                        ifFaceExist(photoFile.toString())
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(
                        TAG,
                        "onError: ${exception.message}",
                        exception)
                }
            }
        )
    }

    private fun ifFaceExist(string: String){

        //start python
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        val py = Python.getInstance()

        //val imageFile = string
        //val bitmap = BitmapFactory.decodeFile(string)
       // val imageString = getStringImage(bitmap)
        val path = "empty"

        val pyobj = py.getModule("face_detection") //give name of python file
        val obj = pyobj.callAttr("main", string, path) //call main method
        val str = obj.toBoolean()


        if(!str){
            noFaceDetectedDialog()
        }else{

           // createDirectoryAndSaveFile(string)

            val file = File(string)

            val fullpath = File("$outputDirectory/login key/")
            if (!fullpath.exists()) {
                fullpath.mkdirs()
            }

            val newFile = File("$outputDirectory/login key/login_key.jpg")

            if (file.exists()) {
                val success = file.renameTo(newFile)
                if (success) {
                    val sp = PreferenceManager.getDefaultSharedPreferences(this)
                    val editor = sp.edit()

                    editor.apply() {
                        putString("ownerPic",newFile.toString() )
                    }.apply()

                    editor.apply(){
                        putBoolean("isThereAFace", true)
                    }.apply()

                    for(i in counter..20){
                        takePhoto()
                    }

                } else {
                    errorDialog("Failed to save login key")
                }
            } else {
                errorDialog("File does not exist")
            }




        }
    }

    private fun faceDetection(string: String){

        //start python
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        val py = Python.getInstance()

        //val imageFile = string
        //val bitmap = BitmapFactory.decodeFile(string)
        //val imageString = getStringImage(bitmap)
        val pathFd = "$outputDirectory/face recognition/"
        val fullpath = File(pathFd)

        if (!fullpath.exists()) {
            fullpath.mkdirs()
        }


        val pyobj = py.getModule("face_detection") //give name of python file
        val obj = pyobj.callAttr("main", string, pathFd) //call main method
        val str = obj.toBoolean()

       // errorDialog(str)

        //Toast.makeText(this, "$str", Toast.LENGTH_LONG).show()

       if(!str){
            if(dialogCounter == 0){
                noFaceDetectedDialog ()
                dialogCounter = 1
            }
        }
        else{
            counter++
            imageNumber.setText("$counter")

           val progCounter = counter * 5
           loadingDialog.progress_bar.progress = progCounter
           loadingDialog.text_view_progress.text = "$progCounter%"

            if(counter == 20){
                loadingDialog.dismiss()
                val sp = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sp.edit()

                editor.apply() {
                    putBoolean("isEnrolled",true )
                }.apply()


                this.finish()
            }
        }

    }
    private fun noFaceDetectedDialog (){
        loadingDialog.dismiss()

        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Please center your face inside the box.")
            setTitle("No face detected")
            setPositiveButton("ok") { dialog, which ->
               /* val intent = intent
                finish()
                startActivity(intent)*/
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun errorDialog (string: String){
        loadingDialog.dismiss()

        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("$string")
            setTitle("Error")
            setPositiveButton("ok") { dialog, which ->
                /* val intent = intent
                 finish()
                 startActivity(intent)*/
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun createDirectoryAndSaveFile(string: String) {

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()

        val bitmap = BitmapFactory.decodeFile(string)

        // var path = getOutputDirectory()
        val pathFd = "$outputDirectory/login key"
        val fullpath = File(pathFd)

        val imageStringSplit = string.substring(string.lastIndexOf("/")+1); //split file path, take last(file)
        val file = File("$pathFd", imageStringSplit)

        if(counter == 1) {
            editor.apply() {
                putString("ownerPic",file.toString() )
            }.apply()
        }

        if (!fullpath.exists()) {
            fullpath.mkdirs()
        }

        if (file.exists()) {
            file.delete()
        }

        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
          /*  val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sp.edit()

            editor.apply(){
                putInt("loadingStopCounter", ++counter)
            }.apply()

            if((sp.getInt("loadingStopCounter", 0)) == 20){
                loadingDialog.dismissDialog()
                this.finish()
            }
            counter++

            if(counter == 20){
                loadingDialog.dismissDialog()
                val sp = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sp.edit()

                editor.apply() {
                    putBoolean("isEnrolled",true )
                }.apply()


                this.finish()
            }*/

            /*   var v = sp.getInt("loadingStopCounter", 0).toString()
           // imageNumber.setText("$counter")

            //  Toast.makeText(this, "$v", Toast.LENGTH_SHORT).show()
            //faceRecognition(string)*/

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //Toast.makeText(this, "Can't be Saved", Toast.LENGTH_SHORT).show()
        }

    }
    private fun getStringImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

        //store in byte array
        val imageBytes = baos.toByteArray()
        //finally encode to string
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    // Start Front Camera Preview
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.captureView.surfaceProvider
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
                Log.d(TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((REQUEST_CODE_PERMISSION == 111) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            startCamera()
            binding.addUserCaptureButton.isEnabled = true
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        val dir = File("$outputDirectory/raw photo")

        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                File(dir, children[i]).delete()
            }
        }
        cameraExecutor.shutdown()
    }

    companion object {
        const val TAG = "cameraX"
        //const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSION = 111
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}
