package com.diplom.kotlindiplom.models

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.diplom.kotlindiplom.FirebaseCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.header.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class FunctionsFirebase {
    val rootRef = FirebaseDatabase.getInstance().getReference()
    val childRef = rootRef.child("users").child("children")
    val parentRef = rootRef.child("users").child("parents")
    val taskRef = rootRef.child("tasks")
    val uidUser  = FirebaseAuth.getInstance().uid
    fun clearAcceptRequest(){
        val ref = rootRef.child("users").child("children").child("$uidUser")
        ref.child("acceptName").setValue("")
        ref.child("acceptUid").setValue("")
    }
    fun searchIdChild(p0: DataSnapshot, id: String): String {
        for (p1: DataSnapshot in p0.children) {
            for (p2: DataSnapshot in p1.children) {
                if (p2.key == "id") {
                    if (p2.value.toString() == id) {
                        return p1.key.toString()
                    }
                }
            }
        }
        return ""
    }
    fun setFieldDatabaseChild(childUid: String, field: String, value:Any){
        val ref = childRef.child("${childUid}")
        ref.child("$field").setValue(value)
    }
    fun setFieldDatabaseParent(parentUid: String, field: String, value:String){
        val ref =parentRef.child("${parentUid}")
        ref.child("$field").setValue(value)
    }
    fun setFieldDatabaseTask(taskId:String, field:String,value : Any){
        val ref =taskRef.child("${taskId}")
        ref.child("$field").setValue(value)

    }
    fun getFieldDatabaseChild(childUid: String, field: String,firebaseCallBack: FirebaseCallback<String>) {
        val ref = childRef.child("${childUid}")
        var value = ""

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }
        })
    }
    fun getFieldDatabaseParent(parentUid: String, field: String,firebaseCallBack: FirebaseCallback<String>){
        val ref = parentRef.child("${parentUid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }
        })
    }
    fun getFieldDatabaseTask(taskId: String,field: String,firebaseCallBack: FirebaseCallback<String>){
        val ref = taskRef.child("${taskId}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        Log.d("TAG",it.key.toString())
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }
        })
    }
    fun addPointChild(childUid: String,point:Int){
        val ref = childRef.child("$childUid")
        Log.d("TAG",childUid)
        getFieldDatabaseChild(childUid,"point",object :FirebaseCallback<String>{
            override fun onComplete(value: String) {
                ref.child("point").setValue(value.toInt() + point)
            }
        })
    }
    fun sendRequestChild(editTextId: EditText, context: Context) {
        val childRef = rootRef.child("users").child("children")
        childRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
               if (p0.exists()) {
                    val childUid = searchIdChild(p0, editTextId.text.toString())
                    if (childUid.isNotEmpty()) {
                        getFieldDatabaseChild(childUid, "parentUid",object : FirebaseCallback<String> {
                            override fun onComplete(value: String) {
                                val parentUid = value
                                if (parentUid.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Данный пользователь уже привязан к родителю. Вы можете сбросить привязку через приложение ребенка",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    val firebase = FunctionsFirebase()
                                    firebase.getFieldDatabaseParent(firebase.uidUser!!,"username",object : FirebaseCallback<String>{
                                        override fun onComplete(value: String) {
                                            firebase.setFieldDatabaseChild(childUid,"acceptName",value)
                                            firebase.setFieldDatabaseChild(childUid,"acceptUid",firebase.uidUser!!)
                                        }
                                    })
                                    Toast.makeText(
                                        context,
                                        "Запрос отправлен",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }
                            }
                        })
                    } else {
                        Toast.makeText(
                            context,
                            "Id ребенка не был найден",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
    fun uploadImageToFirebase(selectedPhotoUri: Uri?, activity: Activity, role: String) {
        Log.d("TAG", "$selectedPhotoUri")
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val fileRef = FirebaseStorage.getInstance().getReference("/image/$filename")

        fileRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("TAG", "Upload image successfully: ${it.metadata?.path}")
                fileRef.downloadUrl.addOnSuccessListener {
                    it.toString()
                    //Сохранение в firebase
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$role/$uid")
                    ref.child("profileImageUrl").setValue(it.toString())
                    Glide.with(activity).load(it.toString())
                        .into(activity.photoImageviewDrawer)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    activity,
                    "Ошибка при загрузке изображения: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    fun addNewTaskDatabase(context: Context,title:String, description:String, cost: Int, parentUid: String,time:String){
        val uid = UUID.randomUUID().toString()
        taskRef.child("$uid").setValue(Task(uid,title,description,cost,parentUid,time)).addOnCompleteListener {
            Toast.makeText(context,"Задание успешно добавлено", Toast.LENGTH_SHORT).show()
        }
    }
    fun getChild(childUid: String?,firebaseCallBack: FirebaseCallback<Child>){
        childRef.child("$childUid").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val child = getAllFieldsChild(p0)
                firebaseCallBack.onComplete(child)
            }

        })
    }
    fun getAllFieldsChild (p0: DataSnapshot) : Child {
        var child = Child("","","")
        p0.children.forEach{
            if (it.key.toString() == "acceptName") {
                child.acceptName = it.value.toString()
            }
            if (it.key.toString() == "acceptUid") {
                child.acceptUid = it.value.toString()
            }
            if (it.key.toString() == "childUid") {
                child.childUid = it.value.toString()
            }
            if (it.key.toString() == "city") {
                child.city = it.value.toString()
            }
            if (it.key.toString() == "cityId") {
                child.cityId = it.value.toString().toInt()
            }
            if (it.key.toString() == "educationalInstitution") {
                child.educationalInstitution = it.value.toString()
            }
            if (it.key.toString() == "educationalInstitutionId") {
                child.educationalInstitutionId = it.value.toString().toInt()
            }
            if (it.key.toString() == "email") {
                child.email = it.value.toString()
            }
            if (it.key.toString() == "id") {
                child.id = it.value.toString().toInt()
            }
            if (it.key.toString() == "parentUid") {
                child.parentUid = it.value.toString()
            }
            if (it.key.toString() == "point") {
                child.point = it.value.toString().toInt()
            }
            if (it.key.toString() == "profileImageUrl") {
                child.profileImageUrl = it.value.toString()
            }
            if (it.key.toString() == "username") {
                child.username = it.value.toString()
            }
        }
        return child
    }
    fun getTask(taskId:String, firebaseCallBack: FirebaseCallback<Task>){
        val ref = taskRef.child("$taskId")
        var task = Task("","","",0,"","")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                task = getTask(p0)
                Log.d("TAG",task.childUid)
                firebaseCallBack.onComplete(task)
            }

        })
    }
    fun getTask(p0: DataSnapshot) : Task{
        var task =  Task("","","",0,"","")

        p0.children.forEach{
            if (it.key.toString() == "cost"){
                task.cost = it.value.toString().toInt()
            }
            if (it.key.toString() == "description"){
                task.description = it.value.toString()
            }
            if (it.key.toString() == "parentUid"){
                task.parentUid = it.value.toString()
            }
            if (it.key.toString() == "status"){
                task.status = it.value.toString().toInt()
            }
            if (it.key.toString() == "taskId"){
                task.taskId = it.value.toString()
            }
            if (it.key.toString() == "time"){
                task.time = it.value.toString()
            }
            if (it.key.toString() == "title"){
                task.title = it.value.toString()
            }
            if (it.key.toString() == "childUid"){
                task.childUid = it.value.toString()
            }
            if (it.key.toString() == "showNotification"){
                task.showNotification = it.value.toString().toInt()
            }
        }
        return  task
    }
    fun getTasksParentUid(parentUid:String, status:Int, firebaseCallBack: FirebaseCallback<List<Task>>){
        taskRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var tasks: MutableList<Task> = mutableListOf()
                var parentUidInFirebase : String =""
                var statusInFirebase : Int = -2
                for (p1 in p0.children){
                    for (p2 in p1.children){
                        if (p2.key.toString() == "parentUid"){
                            parentUidInFirebase = p2.value.toString()
                        }
                        if (p2.key.toString() == "status"){
                            statusInFirebase = p2.value.toString().toInt()
                        }
                    }
                    if (parentUidInFirebase == parentUid && statusInFirebase == status){
                        tasks.add(getTask(p1))
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }

        })
    }
    fun getTasksChildUid(childUid:String, status:Int, firebaseCallBack: FirebaseCallback<List<Task>>){
        taskRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var tasks: MutableList<Task> = mutableListOf()
                var childUidInFirebase : String =""
                var statusInFirebase : Int = -2
                for (p1 in p0.children){
                    for (p2 in p1.children){
                        if (p2.key.toString() == "childUid"){
                            childUidInFirebase = p2.value.toString()
                        }
                        if (p2.key.toString() == "status"){
                            statusInFirebase = p2.value.toString().toInt()
                        }
                    }
                    if (childUidInFirebase == childUid && statusInFirebase == status){
                        tasks.add(getTask(p1))
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }
        })
    }
}