package com.actofit.vitalscan


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter


class UserDetailsActivity : AppCompatActivity(){

    private fun sendLogToServer(message: String) {

        Log.e("TAG", message)
        // Implement network operation to send message to your server
        runBlocking {
            val client = HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
            try {
                val response: String = client.post("https://20.124.20.8:5000/crashlogs") {
                    contentType(ContentType.Application.Json)
                    body = mapOf("crashlog" to message)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                client.close()
            }
        }
    }

    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }


    fun crashApp() {
        throw RuntimeException("Deliberate crash")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details)

        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            sendLogToServer(
                getStackTrace(e)
            )
        }
//        crashApp()
        val proceedButtonUserDetails = findViewById<Button>(R.id.proceed_button_user_details)
        val nameFieldUserDetails = findViewById<EditText>(R.id.editTextName)
        val heightFieldUserDetails = findViewById<EditText>(R.id.editTextHeight)
        val weightFieldUserDetails = findViewById<EditText>(R.id.editTextWeight)
        val ageFieldUserDetails = findViewById<EditText>(R.id.editTextAge)
        val pnoFieldUserDetails = findViewById<EditText>(R.id.editTextPno)
        val signOutButton = findViewById<Button>(R.id.signOutButton)

        proceedButtonUserDetails.apply {
            setOnClickListener {


                val name = nameFieldUserDetails.text.toString().trim()
                val height = heightFieldUserDetails.text.toString().trim()
                val weight = weightFieldUserDetails.text.toString().trim()
                val age = ageFieldUserDetails.text.toString().trim()
                val phoneNumber = pnoFieldUserDetails.text.toString().trim()

                if (height.isNotEmpty() && weight.isNotEmpty() && age.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    val instructionsIntent = Intent(applicationContext, Instructions::class.java)
                    instructionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    println("DOES THE PRINT WORK")

                    instructionsIntent.putExtra("name", name)
                    instructionsIntent.putExtra("height", height)
                    instructionsIntent.putExtra("weight", weight)
                    instructionsIntent.putExtra("age", age)
                    instructionsIntent.putExtra("pno", phoneNumber)

                    startActivity(instructionsIntent)
                } else {
                    Toast.makeText(applicationContext, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }

            }
        }

        signOutButton.apply {
            setOnClickListener {
                val fileToDelete = File(getExternalFilesDir("Downloads/loggedin.txt").toString())

                fileToDelete.delete()
                Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_SHORT).show()

                val loginIntent = Intent(applicationContext, LoginActivity::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(loginIntent)
            }
        }


    }

}