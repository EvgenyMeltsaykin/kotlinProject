package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginProgressBar?.isVisible = false
        val parentOrNot = intent.getBooleanExtra("parentOrNot",false)
        backregistryTextViewLogin?.setOnClickListener {
            intent = Intent(this,RegistryActivity::class.java)
            intent.putExtra("parentOrNot",parentOrNot)
            startActivity(intent)
        }

        loginButtonLogin?.setOnClickListener {
            val email = emailEditTextLogin?.text.toString()
            val password = passwordEditTextLogin?.text.toString()
            loginButtonLogin?.isVisible = false
            backregistryTextViewLogin?.isVisible = false
            loginProgressBar?.isVisible = true
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        return@addOnCompleteListener
                    }
                    val firebase = FunctionsFirebase()
                    firebase.getRoleByUid(firebase.uidUser!!,object : FirebaseCallback<String>{
                        override fun onComplete(value: String) {
                            Toast.makeText(this@LoginActivity,"Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                            intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("role","$value")
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }
                    })

                }
                .addOnFailureListener{
                    loginButtonLogin?.isVisible = true
                    backregistryTextViewLogin?.isVisible = true
                    loginProgressBar?.isVisible = false
                    Toast.makeText(this,"Ошибка при входе: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
}
