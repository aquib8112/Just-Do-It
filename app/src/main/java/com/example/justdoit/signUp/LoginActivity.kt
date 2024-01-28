package com.example.justdoit.signUp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.justdoit.R
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.activity.HomeActivity
import com.example.justdoit.util.toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpActivity: TextView
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        signUpActivity = findViewById(R.id.sign_up_text)
        loginButton = findViewById(R.id.loginBT)
        email = findViewById(R.id.emailET)
        password = findViewById(R.id.passwordET)
        progressBar = findViewById(R.id.progress_bar)

        signUpActivity.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            if (email.text.isEmpty()) {
                email.error = "please fill the email"
            } else if (password.text.isEmpty()) {
                password.error = "please fill the password"
            } else {
                auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful && auth.currentUser!!.isEmailVerified) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val uid = auth.currentUser!!.uid
                                checkUserExistence(uid, this@LoginActivity)
                            }
                            toast("Authentication Successful.")
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            toast("Authentication failed.")
                        }
                    }
            }
        }

    }
}

private suspend fun checkUserExistence(uid: String, context: Context) {
    val db = Firebase.firestore
    if (FireStoreCO().userExist(uid) != null) {
        FireStoreCO().downloadUserData(context)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Welcome back! $uid", Toast.LENGTH_SHORT).show()
        }
    } else {
        val userMap = mapOf("uid" to uid)
        db.collection("users").add(userMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("checkUserExistence", "Error creating new user: $e")
            }
    }
}