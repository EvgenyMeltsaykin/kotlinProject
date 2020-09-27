package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registry.*


class RegistryActivity : AppCompatActivity() {

    companion object {
        const val TAG = "RegistryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        registryProgressBar?.isVisible = false
        alreadyRegistryTextViewRegistry?.setOnClickListener {
            val parentOrNot = intent.getBooleanExtra("parentOrNot", false)
            intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("parentOrNot", parentOrNot)
            startActivity(intent)
        }
        registryButtonRegistry?.setOnClickListener {
            registryButtonRegistry?.isVisible = false
            alreadyRegistryTextViewRegistry?.isVisible = false
            registryProgressBar?.isVisible = true
            performRegistry()
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

    private fun validateRegistry(username:String, email:String,password:String):Boolean{
        var fl = true
        if (username.isEmpty()){
            usernameTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (email.isEmpty()){
            emailTextInputRegistry?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (password.isEmpty()){
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


        if (!validateRegistry(username,email,password)){
            registryButtonRegistry?.isVisible = true
            alreadyRegistryTextViewRegistry?.isVisible = true
            registryProgressBar?.isVisible = false
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(TAG, "email:$email")
                Log.d(TAG, "password:$password")
                if (!parentOrNot) saveChildToFirebaseDatabase(username, email)
                else saveParentToFirebaseDatabase(username, email)
                val mAuthListener = FirebaseAuth.AuthStateListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d("Tag", "user = " + user.toString())
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
                registryButtonRegistry?.isVisible = true
                alreadyRegistryTextViewRegistry?.isVisible = true
                registryProgressBar.isVisible = false
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
                    val intent = Intent(this@RegistryActivity, ChooseActivity::class.java)
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

    private fun saveChildToFirebaseDatabase(username: String, email: String) {
        val firebase = FunctionsFirebase()
        val ref = firebase.childRef.child("${firebase.uidUser}")
        firebase.rolesRef.child("${firebase.uidUser}").setValue("child")
        val user = Child(firebase.uidUser!!, username, email)
        val refCount = FirebaseDatabase.getInstance().getReference("/users")
        refCount.keepSynced(true)
        ref.setValue(user)
        firebase.setFieldUserDatabase(firebase.uidUser!!, "role", "child")
        val weekday = listOf<String>("понедельник","вторник","среда","четверг","пятница","суббота")
        weekday.forEach {
            for(i in 0..6){
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/cabinet", "")
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/dateHomework", "")
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/homework", "")
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/number", i)
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/time", "")
                firebase.setFieldUserDatabase(firebase.uidUser, "mySchedule/$it/$i/lessonName", "")
            }
        }
        firebase.getCountChildren(object :Callback<String>{
            override fun onComplete(value: String) {
                val countChildren = value.toInt() + 1
                firebase.setFieldUserDatabase(firebase.uidUser,"id",countChildren)
            }
        })
    }
    private fun saveParentToFirebaseDatabase(username: String, email: String) {
        val firebase = FunctionsFirebase()
        val ref = firebase.parentRef.child(firebase.uidUser!!)
        firebase.rolesRef.child("${firebase.uidUser}").setValue("parent")
        val user = Parent(firebase.uidUser!!, username, email)
        ref.setValue(user)
        firebase.setFieldUserDatabase(firebase.uidUser!!, "role", "parent")
    }
}

