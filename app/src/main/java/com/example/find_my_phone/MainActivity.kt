package com.example.find_my_phone

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.Manifest

class MainActivity : AppCompatActivity() {

    var TAG = "MainActivity"
//    var audioServ:MyServiceAudio ?= null

    lateinit var textView: TextView
    lateinit var onButton: Button
    lateinit var offButton: Button


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_RECORD_AUDIO = 1337
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        }


        textView = findViewById<TextView>(R.id.output)
        onButton = findViewById<Button>(R.id.button)
        offButton = findViewById<Button>(R.id.button2)

// set on-click listener
        onButton.setOnClickListener {
            foregroundStartService("Start")
            Toast.makeText(this@MainActivity, "FOREGROUND START.", Toast.LENGTH_SHORT).show()
        }
        offButton.setOnClickListener {
            foregroundStartService("Exit")
            Toast.makeText(this@MainActivity, "END.", Toast.LENGTH_SHORT).show()
        }
    }
}