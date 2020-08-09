package com.diplom.kotlindiplom

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.child.ChildMainActivity
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.parent.ParentMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_choose.*

class ChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
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
}
