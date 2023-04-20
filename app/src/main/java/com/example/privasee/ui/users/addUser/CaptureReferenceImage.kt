package com.example.privasee.ui.users.addUser

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.example.privasee.databinding.ActivityCaptureReferenceImageBinding
import com.example.privasee.ui.monitor.Constants
import kotlinx.android.synthetic.main.activity_capture_reference_image.*
import kotlinx.android.synthetic.main.capture_reference_instructions.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CaptureReferenceImage: AppCompatActivity() {

    private lateinit var binding: ActivityCaptureReferenceImageBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var loadingDialog : LoadingDialog
    var counter = 1
    var dialogCounter = 0
    private lateinit var vibrator: Vibrator
    private val mTextViewCountDown: TextView? = null
    private val mButtonStartPause: Button? = null
    private val mButtonReset: Button? = null

    private var mCountDownTimer: CountDownTimer? = null

    private var mTimerRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCaptureReferenceImageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        outputDirectory = getOutputDirectory()
        outputDirectory = File(".$outputDirectory")
        cameraExecutor = Executors.newSingleThreadExecutor()


        binding.addUserCaptureButton.isEnabled = false
        if (allPermissionGranted()) {
            startCamera()
            binding.addUserCaptureButton.isEnabled = true


            //}

        } else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)


        binding.addUserCaptureButton.setOnClickListener {

            when (counter) {
                1 -> {
                    val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)

                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinding)

                    myDialog.setCancelable(false)
                    myDialog.imgInstruction.setImageResource(R.drawable.front)
                    myDialog.instruction.text = "Position your face in the front."
                    //myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    facePosition.text = "Face Position: Front"

                    myDialog.btnOk.setOnClickListener {
                        myDialog.dismiss()
                        startTimer()
                    }
                }
                2 ->   {
                    val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)
                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinding)
                    myDialog.setCancelable(false)

                        myDialog.imgInstruction.setImageResource(R.drawable.down)
                        myDialog.instruction.text =  "Slightly tilt your face to the Downward."
                        facePosition.text = "Face Position: Downward"

                    myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    myDialog.btnOk.setOnClickListener {
                        myDialog.dismiss()
                        startTimer()
                    }
                }
                3 -> {
                    val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)
                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinding)
                    myDialog.setCancelable(false)

                        myDialog.imgInstruction.setImageResource(R.drawable.up)
                        myDialog.instruction.text =  "Slightly tilt your face to the Upward."
                        facePosition.text = "Face Position: Upward"

                    //myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    myDialog.btnOk.setOnClickListener {
                        myDialog.dismiss()
                        startTimer()
                    }
                }
                4 -> {
                    val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)
                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinding)
                    myDialog.setCancelable(false)

                        myDialog.imgInstruction.setImageResource(R.drawable.left)
                        myDialog.instruction.text = "Slightly tilt your face to the Left."
                        facePosition.text = "Face Position: Left"

                    //myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    myDialog.btnOk.setOnClickListener {
                        myDialog.dismiss()
                        startTimer()
                    }
                }
                5 ->   {
                    val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)
                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinding)
                    myDialog.setCancelable(false)

                        myDialog.imgInstruction.setImageResource(R.drawable.right)
                        myDialog.instruction.text = "Slightly tilt your face to the Right."
                        facePosition.text = "Face Position: Right"

                    //myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    myDialog.btnOk.setOnClickListener {
                        myDialog.dismiss()
                        startTimer()
                    }
                }
                else -> {
                    //
                }
            }


        }

        binding.btnCameraPermission.setOnClickListener {
            checkForPermissions(android.Manifest.permission.CAMERA, "Camera", com.example.privasee.Constants.REQUEST_CODE_PERMISSIONS)
        }
    }


    private fun startTimer() {
        var mTimeLeftInMillis: Long = 5000

        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateCountDownText(mTimeLeftInMillis)
            }

            override fun onFinish() {
                mTimerRunning = false
                takePhoto()
            }
        }.start()
        mTimerRunning = true

    }

    private fun updateCountDownText(mTimeLeftInMillis: Long) {
      //  val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60
        val timeLeftFormatted =
            java.lang.String.format(Locale.getDefault(), "%2d", seconds)
        captureTimer!!.text = timeLeftFormatted
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
                ActivityCompat.requestPermissions(this@CaptureReferenceImage, arrayOf(permission), requestCode)
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

                    ifFaceExist(photoFile.toString())

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
        val bitmap = BitmapFactory.decodeFile(string)
        val imageString = getStringImage(bitmap)

        val pyobj = py.getModule("face_detection") //give name of python file
        val obj = pyobj.callAttr("main", imageString) //call main method
        val str = obj.toString()


        if(str == "No face detected"){
            noFaceDetectedDialog()
        }else{

            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sp.edit()
            editor.apply(){
                putBoolean("isThereAFace", true)
            }.apply()

            //convert it to byte array
            val bitmap = BitmapFactory.decodeFile(string)

            createDirectoryAndSaveFile(bitmap, string)

        }
    }

    private fun noFaceDetectedDialog (){

        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Please center your face inside the box.")
            setTitle("No face detected")
            setPositiveButton("ok") { _, _ ->
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun createDirectoryAndSaveFile(bitmap: Bitmap, string: String) {

        val pathFd = "$outputDirectory/reference face"
        val fullpath = File(pathFd)

        val imageStringSplit = string.substring(string.lastIndexOf("/")+1); //split file path, take last(file)
        val file = File("$pathFd", imageStringSplit)

        if (!fullpath.exists()) {
            fullpath.mkdirs()
        }

        if (file.exists()) {
            file.delete()
        }

        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

            counter++

            if(counter == 6){
                val sp = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sp.edit()

                editor.apply() {
                    putBoolean("isEnrolled",true )
                }.apply()

                this.finish()
            }

            captureTimer!!.text = "✔"

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(500)
                }
            }

            out.flush()
            out.close()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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
