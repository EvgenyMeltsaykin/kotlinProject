package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
        emailTextInput?.editText?.doAfterTextChanged {
            emailTextInput?.error = null
        }
        passwordTextInput?.editText?.doAfterTextChanged {
            passwordTextInput?.error = null
        }
        loginButtonLogin?.setOnClickListener {
            val email = emailTextInput?.editText?.text.toString()
            val password = passwordTextInput?.editText?.text.toString()
            if(!validateLogin(email, password)){
                return@setOnClickListener
            }
            loginButtonLogin?.isVisible = false
            backregistryTextViewLogin?.isVisible = false
            loginProgressBar?.isVisible = true
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        return@addOnCompleteListener
                    }
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user!!.isEmailVerified){
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
                    }else{
                        Toast.makeText(applicationContext,"Подтвердите электронную почту",Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        loginButtonLogin?.isVisible = true
                        backregistryTextViewLogin?.isVisible = true
                        loginProgressBar?.isVisible = false
                    }


                }
                .addOnFailureListener{
                    loginButtonLogin?.isVisible = true
                    backregistryTextViewLogin?.isVisible = true
                    loginProgressBar?.isVisible = false
                    Toast.makeText(this,"Ошибка при входе: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun validateLogin(email:String,password:String):Boolean{
        var fl = true
        if (email.isEmpty()){
            emailTextInput?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (password.isEmpty()){
            passwordTextInput?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        return fl
    }
}
