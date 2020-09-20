package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class SplashActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.window.statusBarColor = resources.getColor(R.color.colorActionBarSplash)
        verifyUserIsLoggedIn()
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                val firebase = FunctionsFirebase()
                firebase.getRoleByUid(uid!!, object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        if (value == "child") {
                            intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("role", "child")
                        }
                        if (value == "parent") {
                            intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("role", "parent")
                        }
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }

                })
            } else {
                Toast.makeText(
                    applicationContext,
                    "Подтвердите электронную почту",
                    Toast.LENGTH_LONG
                ).show()
                FirebaseAuth.getInstance().signOut()
                intent = Intent(this, ChooseActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } else {
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
