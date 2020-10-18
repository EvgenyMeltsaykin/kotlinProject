package com.diplom.kotlindiplom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import com.diplom.kotlindiplom.models.Parent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authorization.*


class AuthorizationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthorizationActivity"
    }

    private lateinit var firebase:FunctionsFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
        registryProgressBar?.isVisible = false
        registryButtonRegistry?.setOnClickListener {
            hideButtons()
            performRegistry()
        }

        loginButtonRegistry?.setOnClickListener {
            hideButtons()
            loginApplication()
        }
        usernameTextInputRegistry?.editText?.doAfterTextChanged {
            usernameTextInputRegistry?.error = null
        }
        emailTextInputRegistry?.editText?.doAfterTextChanged {
            emailTextInputRegistry?.error = null
        }
        passwordTextInputRegistry?.editText?.doAfterTextChanged {
            passwordTextInputRegistry?.error = null
        }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        var fl = true
        if (email.isEmpty()) {
            emailTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (password.isEmpty()) {
            passwordTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        return fl
    }

    private fun loginApplication() {
        val email = emailTextInputRegistry?.editText?.text.toString()
        val password = passwordTextInputRegistry?.editText?.text.toString()
        if (!validateLogin(email, password)) {
            showButtons()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    showButtons()
                    return@addOnCompleteListener
                }
                val user = FirebaseAuth.getInstance().currentUser
                if (user!!.isEmailVerified) {
                    firebase = FunctionsFirebase()
                    firebase.getFieldUserDatabase(
                        firebase.userUid,
                        "role",
                        object : Callback<String> {
                            override fun onComplete(value: String) {
                                val functionUI = FunctionsUI()
                                val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                                val editor = prefs.edit()
                                editor.putString(functionUI.APP_PREFERENCES_ROLE,value).apply()
                                intent =
                                    Intent(applicationContext, MainActivity::class.java)
                                //intent.putExtra("role", value)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                Toast.makeText(
                                    applicationContext,
                                    "Вход выполнен успешно!",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    showButtons()
                }


            }
            .addOnFailureListener {
                showButtons()
                Toast.makeText(this, "Ошибка при входе: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showButtons() {
        usernameTextInputRegistry?.editText?.isEnabled = true
        emailTextInputRegistry?.editText?.isEnabled = true
        passwordTextInputRegistry?.editText?.isEnabled = true
        registryButtonRegistry?.isVisible = true
        loginButtonRegistry?.isVisible = true
        registryProgressBar?.isVisible = false
    }

    private fun hideButtons() {
        usernameTextInputRegistry?.editText?.isEnabled = false
        emailTextInputRegistry?.editText?.isEnabled = false
        passwordTextInputRegistry?.editText?.isEnabled = false
        registryButtonRegistry?.isVisible = false
        loginButtonRegistry?.isVisible = false
        registryProgressBar?.isVisible = true
    }

    private fun validateRegistry(username: String, email: String, password: String): Boolean {
        var fl = true
        if (username.isEmpty()) {
            usernameTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (email.isEmpty()) {
            emailTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (password.isEmpty()) {
            passwordTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        return fl

    }

    private fun performRegistry() {
        val email = emailTextInputRegistry?.editText?.text.toString()
        val password = passwordTextInputRegistry?.editText?.text.toString()
        val username = usernameTextInputRegistry?.editText?.text.toString()
        val parentOrNot = intent.getBooleanExtra("parentOrNot", false)


        if (!validateRegistry(username, email, password)) {
            showButtons()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                if (!parentOrNot) saveChildToFirebaseDatabase(username, email)
                else saveParentToFirebaseDatabase(username, email)
                val mAuthListener = FirebaseAuth.AuthStateListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        sendVerificationEmail()
                    } else {
                        FirebaseAuth.getInstance().signOut()
                    }
                }
                mAuthListener.onAuthStateChanged(FirebaseAuth.getInstance())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка регистрации: ${it.message}", Toast.LENGTH_LONG).show()
                showButtons()
            }

    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "На email ${emailTextInputRegistry.editText?.text.toString()} отправлено письмо. Перейдите по ссылке в письме для подтверждения своего email.",
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(applicationContext, ChooseActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "При отправке сообщения на электронную почту произошла ошибка",
                        Toast.LENGTH_LONG
                    ).show()
                    overridePendingTransition(0, 0);
                    finish()
                    overridePendingTransition(0, 0);
                    startActivity(intent)
                }
            }
    }

    private fun setMySchedule(){
        val weekday = listOf(
            "понедельник",
            "вторник",
            "среда",
            "четверг",
            "пятница",
            "суббота"
        )
        weekday.forEach {
            for (i in 0..6) {
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/cabinet",
                    ""
                )
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/dateHomework",
                    ""
                )
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/homework",
                    ""
                )
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/number",
                    i
                )
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/time",
                    ""
                )
                firebase.setFieldUserDatabase(
                    firebase.userUid,
                    "mySchedule/$it/$i/lessonName",
                    ""
                )
            }
        }
    }

    private fun saveChildToFirebaseDatabase(username: String, email: String) {
        firebase = FunctionsFirebase()
        val ref = firebase.userRef.child(firebase.userUid)

        val refCount = firebase.userRef
        refCount.keepSynced(true)
        firebase.getNextIdChild(object : Callback<Int> {
            override fun onComplete(value: Int) {
                val user = Child(firebase.userUid, username, email, id = value)
                ref.setValue(user)
                setMySchedule()
            }
        })
    }

    private fun saveParentToFirebaseDatabase(username: String, email: String) {
        firebase = FunctionsFirebase()
        val ref = firebase.userRef.child(firebase.userUid)
        //firebase.rolesRef.child("${firebase.uidUser}").setValue("parent")
        val user = Parent(firebase.userUid, username, email)
        ref.setValue(user)
    }
}

