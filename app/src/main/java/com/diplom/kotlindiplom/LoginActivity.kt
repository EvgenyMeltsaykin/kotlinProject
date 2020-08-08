package com.diplom.kotlindiplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.diplom.kotlindiplom.child.ChildMainActivity
import com.diplom.kotlindiplom.database.ChildParentDatabase
import com.diplom.kotlindiplom.database.DBChild
import com.diplom.kotlindiplom.database.DBParent
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.diplom.kotlindiplom.parent.ParentMainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val parentOrNot = intent.getBooleanExtra("parentOrNot",false)
        backregistryTextviewLogin.setOnClickListener {
            intent = Intent(this,RegistryActivity::class.java)
            intent.putExtra("parentOrNot",parentOrNot)
            startActivity(intent)
        }

        loginButtonLogin.setOnClickListener {
            val email = emailEdittextLogin.text.toString()
            val password = passwordEdittextLogin.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        return@addOnCompleteListener
                    }
                    Toast.makeText(this,"Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                    val firebase = FunctionsFirebase()
                    if (parentOrNot){
                        firebase.getParent(firebase.uidUser, object: FirebaseCallback<Parent>{
                            override fun onComplete(value: Parent) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    applicationContext.let {
                                        val updateParent = DBParent(value.username,value.city,value.email)
                                        val parent = ChildParentDatabase(it).getChildParentDao().getAllParent()
                                        if (parent.size == 0){
                                            ChildParentDatabase(it).getChildParentDao().addParent(updateParent)
                                        }else {
                                            parent.forEach {
                                                updateParent.uid = it.uid
                                            }
                                            ChildParentDatabase(it).getChildParentDao()
                                                .updateParent(updateParent)
                                        }
                                    }
                                }
                            }
                        })
                        intent = Intent(this, ParentMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }else{
                        firebase.getChild(firebase.uidUser, object: FirebaseCallback<Child>{
                            override fun onComplete(value: Child) {

                                GlobalScope.launch(Dispatchers.IO) {
                                    val updateChild = DBChild(value.username,value.email,value.point,value.city,value.educationalInstitution,value.id)
                                    applicationContext.let { it ->
                                        val child = ChildParentDatabase(it).getChildParentDao().getAllChild()
                                        if (child.isEmpty()){
                                            ChildParentDatabase(it).getChildParentDao().addChild(updateChild)
                                        }else {
                                            child.forEach {
                                                updateChild.uid = it.uid
                                            }
                                            ChildParentDatabase(it).getChildParentDao()
                                                .updateChild(updateChild)
                                        }
                                    }
                                }
                            }
                        })
                        intent = Intent(this,
                            ChildMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }

                }
                .addOnFailureListener{
                    Toast.makeText(this,"Ошибка при входе: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
}
