package com.example.privasee

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.privasee.databinding.ActivityEnterPasswordBinding
import kotlinx.android.synthetic.main.activity_enter_password.*
import kotlinx.android.synthetic.main.fragment_monitor.*
import java.text.SimpleDateFormat
import java.util.*

class  GetPassword : AppCompatActivity(){

    private lateinit var binding: ActivityEnterPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        binding = ActivityEnterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPass.inputType = InputType.TYPE_CLASS_NUMBER
        getPass.transformationMethod = PasswordTransformationMethod.getInstance()

        buttonPass.setOnClickListener {
            val input: String = getPass.getText().toString()

            if (input != "") {
                if (input == getCurrentPassword()) {
                    //got to next screen

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    finish()

                } else {
                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Input Password", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getCurrentPassword(): String? {
        val calendar: Calendar = Calendar.getInstance()
        // calendar.time = Date()

        val time = calendar.timeInMillis
        val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
        val timeString = sdf.format(Date(time))

        return timeString.replace(":", "")
    }


}