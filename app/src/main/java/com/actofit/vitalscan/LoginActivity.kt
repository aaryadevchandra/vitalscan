package com.actofit.vitalscan

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.io.OutputStreamWriter

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var mAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance()
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)

        signUpButton.setOnClickListener {


            if ( emailField.text.isNotEmpty() && passwordField.text.isNotEmpty()){
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                createAccount(email, password)
            }
            else{
                Toast.makeText(this, "Fill all fields and signup", Toast.LENGTH_SHORT).show()
            }

        }

        loginButton.setOnClickListener {

            if ( emailField.text.isNotEmpty() && passwordField.text.isNotEmpty()){
                // Add login functionality here
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                loginAccount(email, password);
            }
            else{
                Toast.makeText(this, "Please fill all fields and login", Toast.LENGTH_SHORT).show()
            }


        }
    }

    fun writeLoggedInFile() {
//        Toast.makeText(this, getExternalFilesDir("Downloads/loggedin.txt").toString(), Toast.LENGTH_SHORT).show()
        try {
            this.openFileOutput(getExternalFilesDir("Downloads/loggedin.txt").toString(), MODE_PRIVATE).use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loginAccount(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success, update UI with the signed-in user's information
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Redirect to another activity or update the UI


                    // create logged in file
                    writeLoggedInFile()


                    val userDetailsIntent = Intent(applicationContext, UserDetailsActivity::class.java)
                    userDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(userDetailsIntent)
                } else {
                    // If login fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun createAccount(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    Toast.makeText(this, "Signup successful.", Toast.LENGTH_SHORT).show()

                    // create logged in file
                    writeLoggedInFile()

                    // Proceed to next activity or main application screen

                    val userDetailsIntent = Intent(applicationContext, UserDetailsActivity::class.java)
                    userDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(userDetailsIntent)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}

