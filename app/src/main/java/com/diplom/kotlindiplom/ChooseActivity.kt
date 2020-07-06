package com.diplom.kotlindiplom

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.child.ChildMainActivity
import com.diplom.kotlindiplom.parent.ParentMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_choose.*

class ChooseActivity : AppCompatActivity() {
    var parent = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            chooseTextviewChoose.text = "Выполняется вход"
            childButtonChoose.isVisible = false
            parentButtonChoose.isVisible = false
            val ref = FirebaseDatabase.getInstance().getReference("")
            ref.child("roles").child("$uid").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val role = p0.getValue()
                        if (role == "parent") {
                            parent = true
                            Log.d("Test", "parent")
                        }
                        if (role == "child") {
                            Log.d("Test", "child")
                            parent = false
                        }
                        checkRoleUser(parent)
                    }

                }

            )
        }
        childButtonChoose.setOnClickListener {
            intent = Intent(this, RegistryActivity::class.java)
            intent.putExtra("parentOrNot", false)
            startActivity(intent)
        }

        parentButtonChoose.setOnClickListener {
            intent = Intent(this, RegistryActivity::class.java)
            intent.putExtra("parentOrNot", true)
            startActivity(intent)
        }
    }

    fun checkRoleUser(parent:Boolean){
        if (parent) {
            intent = Intent(this, ParentMainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }else{
            intent = Intent(this, ChildMainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
