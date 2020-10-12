package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choose.*

class ChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        childButtonChoose?.setOnClickListener {
            intent = Intent(this, AuthorizationActivity::class.java)
            intent.putExtra("parentOrNot", false)
            startActivity(intent)
        }

        parentButtonChoose?.setOnClickListener {
            intent = Intent(this, AuthorizationActivity::class.java)
            intent.putExtra("parentOrNot", true)
            startActivity(intent)
        }
    }
}
