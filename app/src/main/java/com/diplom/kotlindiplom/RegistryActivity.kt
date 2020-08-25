package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    }


    private fun performRegistry() {
        val email = emailEditTextRegistry?.text.toString()
        val password = passwordEditTextRegistry?.text.toString()
        val username = usernameEditTextRegistry?.text.toString()
        val parentOrNot = intent.getBooleanExtra("parentOrNot", false)

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
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
                    Log.d("Tag","user = " + user.toString())
                    if (user != null){
                        sendVerificationEmail()
                    }else{
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

    fun sendVerificationEmail(){
        val user = FirebaseAuth.getInstance().currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    Log.d("Tag","emailSend")
                    if (task.isSuccessful){
                        Toast.makeText(applicationContext,"На почту отправлено письмо с подтверждением почты",Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@RegistryActivity,LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }else{
                        Toast.makeText(applicationContext,"При отправке сообщения на электронную почту произошла ошибка",Toast.LENGTH_SHORT).show()
                        overridePendingTransition(0, 0);
                        finish()
                        overridePendingTransition(0, 0);
                        startActivity(intent)
                    }
                }
            })
            .addOnFailureListener {
                Log.d("Tag",it.toString())
            }
    }

    private fun saveChildToFirebaseDatabase(username: String, email: String) {
        val firebase = FunctionsFirebase()

        var countChildren = 0
        val ref = firebase.childRef.child("${firebase.uidUser}")
        firebase.rolesRef.child("${firebase.uidUser}").setValue("child")
        val user = Child(firebase.uidUser!!, username, email)
        val refCount = FirebaseDatabase.getInstance().getReference("/users")
        refCount.keepSynced(true)
        refCount.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() == "countChildren") {
                        countChildren = it.value.toString().toInt() + 1
                        refCount.child("countChildren").setValue(countChildren)
                        ref.child("id").setValue(countChildren)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        ref.setValue(user)
            .addOnCompleteListener {
                Log.d(TAG, "Пользователь создан: ${firebase.uidUser}")
                //Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show()
                firebase.setFieldUserDatabase(firebase.uidUser!!, "role", "child")
                /*intent = Intent(
                    this,
                    MainActivity::class.java
                )
                intent.putExtra("role", "child")
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)*/
            }

    }

    private fun saveParentToFirebaseDatabase(username: String, email: String) {
        val firebase = FunctionsFirebase()
        val ref = FirebaseDatabase.getInstance().getReference("/users/parents/${firebase.uidUser}")
        firebase.rolesRef.child("${firebase.uidUser}").setValue("parent")
        val user = Parent(firebase.uidUser!!, username, email)
        ref.setValue(user)
            .addOnCompleteListener {
                Log.d(TAG, "Пользователь создан: ${firebase.uidUser}")
                //Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                firebase.setFieldUserDatabase(firebase.uidUser!!, "role", "parent")
                /*intent = Intent(this, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("role", "parent")
                startActivity(intent)*/
            }

    }
}

