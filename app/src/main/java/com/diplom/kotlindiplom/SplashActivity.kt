package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.google.firebase.auth.FirebaseAuth


class SplashActivity : AppCompatActivity() {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.window.statusBarColor = resources.getColor(R.color.colorActionBarSplash)

        verifyUserIsLoggedIn()
    }

    @ExperimentalStdlibApi
    private fun verifyUserIsLoggedIn() {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                val firebase = FunctionsFirebase()
                Log.d("Tag", "begin")
                firebase.updateSchedule()
                firebase.getFieldUserDatabase(firebase.userUid, "role", object : Callback<String> {
                    override fun onComplete(value: String) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            goToMainActivity(value)
                        }, 2000)
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
    private fun goToMainActivity(role: String) {
        intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("role", role)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
