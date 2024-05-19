package com.actofit.vitalscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


@SuppressLint("CustomSplashScreen")
class Instructions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions)


        val proceedButton = findViewById<Button>(R.id.proceed_button)
        proceedButton.apply {
            setOnClickListener {
                val mainIntent = Intent(applicationContext, MainActivity::class.java)
                val userDetailsName = intent.getStringExtra("name")
                val userDetailsHeight = intent.getStringExtra("height")
                val userDetailsWeight = intent.getStringExtra("weight")
                val userDetailsAge = intent.getStringExtra("age")
                val userDetailsPno = intent.getStringExtra("pno")

                mainIntent.putExtra("name", userDetailsName)
                mainIntent.putExtra("height", userDetailsHeight)
                mainIntent.putExtra("weight", userDetailsWeight)
                mainIntent.putExtra("age", userDetailsAge)
                mainIntent.putExtra("pno", userDetailsPno)


                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(mainIntent)
            }
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
                ),
                101
            )

            return
        }

    }
}
