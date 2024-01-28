package com.example.justdoit.signUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.justdoit.R
import com.example.justdoit.util.toast
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var loginActivity: TextView
    private lateinit var signUpButton: Button
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var passwordCheck: EditText
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        loginActivity = findViewById(R.id.login_text)
        signUpButton = findViewById(R.id.sign_up_BT)
        name = findViewById(R.id.nameEt)
        email = findViewById(R.id.sign_up_emailET)
        password = findViewById(R.id.sign_up_passwordET)
        passwordCheck = findViewById(R.id.sign_up_password_checkET)

        loginActivity.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }

        signUpButton.setOnClickListener {
            if (name.text.isNullOrEmpty()) {
                name.error = "please fill the email"
            } else if (email.text.isNullOrEmpty()) {
                email.error = "please fill the email"
            } else if (password.text.isNullOrEmpty()) {
                password.error = "please fill the password"
            } else if (passwordCheck.text.isNullOrEmpty()) {
                passwordCheck.error = "please fill the password"
            } else if (passwordCheck.text.toString() != password.text.toString()) {
                passwordCheck.error = "password doesn't match"
            } else {
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                                toast("Authentication Successful.Kindly Verify Your Email.")
                            }
                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            toast("Authentication failed.")
                        }
                    }
            }
        }
    }
}