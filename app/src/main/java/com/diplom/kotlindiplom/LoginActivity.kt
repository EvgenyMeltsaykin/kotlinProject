package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.diplom.kotlindiplom.child.ChildMainActivity
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.diplom.kotlindiplom.parent.ParentMainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val parentOrNot = intent.getBooleanExtra("parentOrNot",false)
        backregistryTextviewLogin.setOnClickListener {
            intent = Intent(this,RegistryActivity::class.java)
            intent.putExtra("parentOrNot",parentOrNot)
            startActivity(intent)
        }

        loginButtonLogin.setOnClickListener {
            val email = emailEdittextLogin.text.toString()
            val password = passwordEdittextLogin.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        return@addOnCompleteListener
                    }
                    Toast.makeText(this,"Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                    if (parentOrNot){
                        intent = Intent(this, ParentMainActivity::class.java)
                    }else{
                        intent = Intent(this,
                            ChildMainActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                }
                .addOnFailureListener{
                    Toast.makeText(this,"Ошибка при входе: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
}
