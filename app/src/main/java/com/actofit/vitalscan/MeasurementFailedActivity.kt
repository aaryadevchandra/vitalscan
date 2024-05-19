package com.actofit.vitalscan

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MeasurementFailedActivity : AppCompatActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val userDetailsIntent = Intent(this, UserDetailsActivity::class.java)
        userDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(userDetailsIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.no_support)
    }
}