package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_weekday.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val loginProgressBar = findViewById<ProgressBar>(R.id.loginProgressBar)
        val backregistryTextviewLogin = findViewById<TextView>(R.id.backregistryTextviewLogin)
        val loginButtonLogin = findViewById<Button>(R.id.loginButtonLogin)
        loginProgressBar.isVisible = false
        val parentOrNot = intent.getBooleanExtra("parentOrNot",false)
        backregistryTextviewLogin.setOnClickListener {
            intent = Intent(this,RegistryActivity::class.java)
            intent.putExtra("parentOrNot",parentOrNot)
            startActivity(intent)
        }

        loginButtonLogin.setOnClickListener {
            val email = emailEdittextLogin.text.toString()
            val password = passwordEdittextLogin.text.toString()
            loginButtonLogin.isVisible = false
            backregistryTextviewLogin.isVisible = false
            loginProgressBar.isVisible = true
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        return@addOnCompleteListener
                    }
                    val firebase = FunctionsFirebase()
                    firebase.getRoleByUid(firebase.uidUser!!,object : FirebaseCallback<String>{
                        override fun onComplete(value: String) {
                            loginButtonLogin.isVisible = true
                            backregistryTextviewLogin.isVisible = true
                            loginProgressBar.isVisible = false
                            Toast.makeText(this@LoginActivity,"Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                            intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("role","$value")
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }
                    })

                }
                .addOnFailureListener{
                    loginButtonLogin.isVisible = true
                    backregistryTextviewLogin.isVisible = true
                    loginProgressBar.isVisible = false
                    Toast.makeText(this,"Ошибка при входе: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
}
