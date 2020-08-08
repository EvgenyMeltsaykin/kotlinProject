package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.diplom.kotlindiplom.child.ChildMainActivity
import com.diplom.kotlindiplom.database.ChildParentDatabase
import com.diplom.kotlindiplom.database.DBChild
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.Parent
import com.diplom.kotlindiplom.parent.ParentMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_registry.*
import kotlinx.coroutines.*

class RegistryActivity : AppCompatActivity() {

    companion object {
        const val TAG = "RegistryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        alreadyRegistryRextviewRegistry.setOnClickListener {
            val parentOtNot = intent.getBooleanExtra("parentOrNot", false)
            intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("parentOrNot", parentOtNot)
            startActivity(intent)
        }

        registryButtonRegistry.setOnClickListener {
            performRegistry()
        }

    }

    private fun performRegistry() {
        val email = emailEdittextRegistry.text.toString()
        val password = passwordEdittextRegistry.text.toString()
        val username = usernameEditеextRegistry.text.toString()
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
                saveUserToFirebaseDatabase(username, email, parentOrNot)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка регистрации: ${it.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun saveUserToFirebaseDatabase(username: String, email: String, parentOrNot: Boolean) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        if (!parentOrNot) {
            var countChildren = 0
            val ref = FirebaseDatabase.getInstance().getReference("/users/children/$uid")
            val refRole = FirebaseDatabase.getInstance().getReference("roles/")
            refRole.child("$uid").setValue("child")
            val user = Child(uid, username, email)
            val refCount = FirebaseDatabase.getInstance().getReference("/users")
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
                    Log.d(TAG, "Пользователь создан: $uid")
                    Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show()
                    intent = Intent(
                        this,
                        ChildMainActivity::class.java
                    )
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    GlobalScope.launch(Dispatchers.IO) {
                        val child = DBChild(username,email,id = countChildren)
                        applicationContext.let {
                            ChildParentDatabase(it).getChildParentDao().addChild(child)
                        }

                    }
                    startActivity(intent)

                }
        }
        if (parentOrNot){
            val ref = FirebaseDatabase.getInstance().getReference("/users/parents/$uid")
            val refRole = FirebaseDatabase.getInstance().getReference("roles/")
            refRole.child("$uid").setValue("parent")
            val user = Parent(uid, username, email)
            ref.setValue(user)
                .addOnCompleteListener {
                    Log.d(TAG, "Пользователь создан: $uid")
                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                    intent = Intent(this, ParentMainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
        }
    }

}

