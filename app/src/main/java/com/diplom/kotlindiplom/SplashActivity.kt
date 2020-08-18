package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        verifyUserIsLoggedIn()
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            val firebase = FunctionsFirebase()
            firebase.getRoleByUid(uid,object : FirebaseCallback<String>{
                override fun onComplete(value: String) {
                    if (value == "child"){
                        intent = Intent(applicationContext, MainActivity::class.java)
                        intent.putExtra("role", "child")
                    }
                    if (value == "parent"){
                        intent = Intent(applicationContext, MainActivity::class.java)
                        intent.putExtra("role", "parent")
                    }
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

            })
        }else{
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}