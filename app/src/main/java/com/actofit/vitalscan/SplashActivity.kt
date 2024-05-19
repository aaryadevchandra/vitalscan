package com.actofit.vitalscan

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import java.io.File

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {


    private fun loggedInFileExists(): Boolean {
        val directory = File(getExternalFilesDir("Downloads").toString())
        val filenames = directory.listFiles()?.map { it.name } ?: emptyList()
        filenames.forEach { filename ->
            println(filename)
            if (filename == "loggedin.txt"){
                return true;
            }
        }
        return false;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val SPLASH_DISPLAY_LENGTH = 1000 // Splash screen delay time in milliseconds
        Handler().postDelayed({
            // After the set delay time, start your main activity

            FirebaseApp.initializeApp(this);

            if (loggedInFileExists()) {
                val mainIntent = Intent(
                    this@SplashActivity,
                    UserDetailsActivity::class.java
                )
                this@SplashActivity.startActivity(mainIntent)
            }
            else{
                val mainIntent = Intent(
                    this@SplashActivity,
                    LoginActivity::class.java
                )
                this@SplashActivity.startActivity(mainIntent)
            }



            finish() // Close the SplashActivity
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
