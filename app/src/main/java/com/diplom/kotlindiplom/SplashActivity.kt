package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.diplom.kotlindiplom.diaries.Diary
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*


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
                updateScheduleAndMark()
                firebase.getFieldUserDatabase(firebase.uidUser, "role", object : Callback<String> {
                    override fun onComplete(value: String) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            goToMainActivity(value)
                        }, 1500)
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
    @ExperimentalStdlibApi
    private fun updateScheduleAndMark() {
        val firebase = FunctionsFirebase()
        val fields = listOf("url", "idChild")
        firebase.getFieldsDiary(firebase.uidUser, fields, object : Callback<Map<String, String>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: Map<String, String>) {
                val idChild = value["idChild"]
                val urlDiary = value["url"]
                if (!idChild.isNullOrEmpty() && !urlDiary.isNullOrEmpty()) {
                    val diary = Diary()
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = System.currentTimeMillis()
                    val week = calendar.get(Calendar.WEEK_OF_YEAR)
                    val year = calendar.get(Calendar.YEAR)
                    firebase.setDateUpdateSсhedule(
                        calendar.get(Calendar.YEAR).toString(),
                        (calendar.get(Calendar.MONTH) + 1).toString(),
                        calendar.get(Calendar.DAY_OF_MONTH).toString(),
                        week
                    )
                    when (urlDiary) {
                        diary.elschool.url -> {
                            diary.elschool.getScheduleFromElschool(
                                year,
                                week,
                                idChild,
                                object : Callback<Boolean> {
                                    override fun onComplete(value: Boolean) {

                                    }
                                })
                        }
                    }
                }
            }
        })
    }

    private fun goToMainActivity(role: String) {
        intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("role", role)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
