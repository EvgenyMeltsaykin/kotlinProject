package com.diplom.kotlindiplom.models

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.diplom.kotlindiplom.FirebaseCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.header.*
import org.cryptonode.jncryptor.AES256JNCryptor
import java.time.LocalDate
import java.util.*


class FunctionsFirebase {
    val rootRef = FirebaseDatabase.getInstance().getReference()
    val diariesRef = rootRef.child("diaries")
    val secretKey = "applicatonFromDiplom"
    val childRef = rootRef.child("users").child("children")
    val parentRef = rootRef.child("users").child("parents")
    val taskRef = rootRef.child("tasks")
    val rolesRef = rootRef.child("roles")
    val uidUser = FirebaseAuth.getInstance().uid
    fun clearAcceptRequest() {
        val ref = rootRef.child("users").child("children").child("$uidUser")
        ref.child("acceptName").setValue("")
        ref.child("acceptUid").setValue("")
    }

    fun getLesson(fieldsLesson: DataSnapshot): Lesson {
        var lesson = Lesson()

        fieldsLesson.children.forEach {
            if (it.key.toString() == "lessonName") {
                lesson.name = it.value.toString()
            }
            if (it.key.toString() == "cabinet") {
                lesson.cabinet = it.value.toString()
            }
            if (it.key.toString() == "time") {
                lesson.time = it.value.toString()
            }
            if (it.key.toString() == "homework") {
                lesson.homework = it.value.toString()
            }
            if (it.key.toString() == "form") {
                lesson.form = it.value.toString()
            }
            if (it.key.toString() == "mark") {
                lesson.mark = it.value.toString()
            }
        }
        return lesson
    }

    fun createDiary(){
        setFieldDatabase(uidUser!!, "diary/login", "")
        setFieldDatabase(uidUser!!, "diary/password", "")
        setFieldDatabase(uidUser!!, "diary/url", "")
        setFieldDatabase(uidUser!!, "diary/shedule/weekUpdate", 0)
    }
    fun deleteDiary() {
        setFieldDatabase(uidUser!!, "diary/login", "")
        setFieldDatabase(uidUser!!, "diary/password", "")
        setFieldDatabase(uidUser!!, "diary/url", "")
        setFieldDatabase(uidUser!!, "diary/shedule", "")
        setFieldDatabase(uidUser!!, "diary/shedule/weekUpdate", 0)
    }

    fun getSheduleDay(uid: String, day: String, firebaseCallBack: FirebaseCallback<List<Lesson>>) {
        val lessons = mutableListOf<Lesson>()
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!).child("diary")
                    .child("shedule").child(day)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach {
                            val lesson = getLesson(it)
                            lessons.add(lesson)
                        }
                        firebaseCallBack.onComplete(lessons)
                    }

                })

            }
        })

    }

    fun setLoginAndPasswordDiary(login:String, password:String){
        val cryptor = AES256JNCryptor()
        val cipherText = cryptor.encryptData(password.toByteArray(),secretKey.toCharArray())
        var temp = Arrays.toString(cipherText)
        setFieldDatabase(uidUser!!,"diary/login",login)
        setFieldDatabase(uidUser!!,"diary/password",temp)


    }
    fun getFieldDatabase(uid: String, field: String, firebaseCallBack: FirebaseCallback<Any>) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role: String = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!)
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
        })
    }

    fun getDateUpdateInShedule(firebaseCallBack: FirebaseCallback<LocalDate>) {
        getRoleByUid(uidUser!!, object : FirebaseCallback<String> {
            override fun onComplete(value: String) {
                var role = ""
                if (value == "child") role = "children"
                else role = "parents"
                val ref = rootRef.child("users").child(role).child(uidUser).child("diary").child("shedule")

                var year: Int = 1
                var month: Int = 1
                var day: Int = 1

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == "year") {
                                    year = it.value.toString().toInt()
                                }
                                if (it.key.toString() == "month") {
                                    month = it.value.toString().toInt()
                                }
                                if (it.key.toString() == "day") {
                                    day = it.value.toString().toInt()
                                }
                            }
                            val date = LocalDate.of(year, month, day)
                            firebaseCallBack.onComplete(date)
                        }
                    }
                })
            }
        })
    }
    fun setDateUpdateShedule(year:String,month:String,day:String){
        getRoleByUid(uidUser!!, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role: String = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!).child("diary")
                    .child("shedule")
                ref.child("year").setValue(year)
                ref.child("month").setValue(month)
                ref.child("day").setValue(day)
            }
        })
    }
    fun setFieldShedule(uid: String, field: String, value: Any) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role: String = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!).child("diary")
                    .child("shedule")
                ref.child(field).setValue(value)
            }
        })
    }
    fun getFieldSheduleDay(uid: String, day: String,firebaseCallBack: FirebaseCallback<String>) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"
                val ref =
                    rootRef.child("users").child(role).child(uid).child("diary").child("shedule").child(day)
                var value = ""
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == "date") {
                                    value = it.value.toString()
                                    firebaseCallBack.onComplete(value)
                                }
                            }
                        }
                    }

                })
            }
        })
    }
    fun getFieldShedule(uid: String, field: String, firebaseCallBack: FirebaseCallback<String>) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"
                val ref =
                    rootRef.child("users").child(role).child(uid).child("diary").child("shedule")
                var value = ""
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == field) {
                                    value = it.value.toString()
                                    firebaseCallBack.onComplete(value)
                                }
                            }
                        }
                    }

                })
            }
        })
    }
    fun getFieldDiary(uid: String, field: String, firebaseCallBack: FirebaseCallback<String>) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uid).child("diary")
                var value = ""
                ref.keepSynced(true)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == field) {
                                    value = it.value.toString()
                                    firebaseCallBack.onComplete(value)
                                }
                            }
                        }
                    }
                })
            }
        })
    }

    @ExperimentalStdlibApi
    fun getLoginAndPasswordAndUrlDiary(
        uid: String,
        firebaseCallBack: FirebaseCallback<Map<String, String>>
    ) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!).child("diary")
                var value = mutableMapOf<String, String>()

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == "login") {
                                    value["login"] = it.value.toString()
                                }
                                if (it.key.toString() == "password") {
                                    if (it.value.toString().isNotEmpty()) {
                                        val temp = it.value.toString()
                                        val byteValues: List<String> =
                                            temp.substring(1, temp.length - 1).split(",")
                                        val bytes = ByteArray(byteValues.size)

                                        run {
                                            var i = 0
                                            val len = bytes.size
                                            while (i < len) {
                                                bytes[i] = byteValues[i].trim { it <= ' ' }.toByte()
                                                i++
                                            }
                                        }
                                        val cryptor = AES256JNCryptor()
                                        val decrypt =
                                            cryptor.decryptData(bytes, secretKey.toCharArray())
                                        value["password"] = decrypt.decodeToString()
                                    }else{
                                        value["password"] = ""
                                    }
                                }
                                if (it.key.toString() == "url") {
                                    value["url"] = it.value.toString()
                                }

                            }
                            firebaseCallBack.onComplete(value)
                        }
                    }
                })
            }
        })
    }

    fun getFieldDiaryWithRole(
        uid: String,
        field: String,
        firebaseCallBack: FirebaseCallback<List<String>>
    ) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role: String = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!).child("diary")
                val value = mutableListOf<String>()

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            p0.children.forEach {
                                if (it.key.toString() == field) {
                                    value.add(it.value.toString())
                                    value.add(answer)
                                    firebaseCallBack.onComplete(value)
                                }
                            }
                        }
                    }
                })
            }
        })
    }

    fun setFieldDatabase(uid: String, field: String, value: Any) {
        getRoleByUid(uid, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role: String = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = rootRef.child("users").child(role).child(uidUser!!)
                ref.child("$field").setValue(value)

            }
        })
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

    fun setFieldDatabaseChild(childUid: String, field: String, value: Any) {
        val ref = childRef.child("${childUid}")
        ref.child("$field").setValue(value)
    }

    fun setFieldDatabaseParent(parentUid: String, field: String, value: String) {
        val ref = parentRef.child("${parentUid}")
        ref.child("$field").setValue(value)
    }

    fun setFieldDatabaseTask(taskId: String, field: String, value: Any) {
        val ref = taskRef.child("${taskId}")
        ref.child("$field").setValue(value)

    }

    fun getFieldDatabaseChild(
        childUid: String,
        field: String,
        firebaseCallBack: FirebaseCallback<String>
    ) {
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

    fun getFieldDatabaseParent(
        parentUid: String,
        field: String,
        firebaseCallBack: FirebaseCallback<String>
    ) {
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

    fun getFieldDatabaseTask(
        taskId: String,
        field: String,
        firebaseCallBack: FirebaseCallback<String>
    ) {
        val ref = taskRef.child("${taskId}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        Log.d("TAG", it.key.toString())
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }
        })
    }

    fun addPointChild(childUid: String, point: Int) {
        val ref = childRef.child("$childUid")
        Log.d("TAG", childUid)
        getFieldDatabaseChild(childUid, "point", object : FirebaseCallback<String> {
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
                        getFieldDatabaseChild(
                            childUid,
                            "parentUid",
                            object : FirebaseCallback<String> {
                                override fun onComplete(value: String) {
                                    val parentUid = value
                                    if (parentUid == uidUser) {
                                        Toast.makeText(
                                            context,
                                            "Ребенок уже привязан к Вам",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return
                                    }
                                    if (parentUid.isNotEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Данный пользователь уже привязан к родителю. Вы можете сбросить привязку через приложение ребенка",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        val firebase = FunctionsFirebase()
                                        firebase.setFieldDatabaseChild(childUid, "acceptName", "")
                                        firebase.setFieldDatabaseChild(childUid, "acceptUid", "")
                                        firebase.getFieldDatabaseParent(
                                            firebase.uidUser!!,
                                            "username",
                                            object : FirebaseCallback<String> {
                                                override fun onComplete(value: String) {
                                                    firebase.setFieldDatabaseChild(
                                                        childUid,
                                                        "acceptName",
                                                        value
                                                    )
                                                    firebase.setFieldDatabaseChild(
                                                        childUid,
                                                        "acceptUid",
                                                        firebase.uidUser!!
                                                    )
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


        getFieldDatabase(uidUser!!,"profileImageName", object : FirebaseCallback<Any>{
            override fun onComplete(value: Any) {
                if (value.toString().isNotEmpty()){
                    val deleteRef = FirebaseStorage.getInstance().getReference("/image/$value")
                    deleteRef.delete()
                }


            }
        })
        fileRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("TAG", "Upload image successfully: ${it.metadata?.path}")
                fileRef.downloadUrl.addOnSuccessListener {
                    it.toString()
                    //Сохранение в firebase
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$role/$uid")
                    ref.child("profileImageUrl").setValue(it.toString())
                    ref.child("profileImageName").setValue(filename)

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

    fun addNewTaskDatabase(
        context: Context,
        title: String,
        description: String,
        cost: Int,
        parentUid: String,
        time: String
    ) {
        val uid = UUID.randomUUID().toString()
        taskRef.child("$uid").setValue(Task(uid, title, description, cost, parentUid, time))
            .addOnCompleteListener {
                Toast.makeText(context, "Задание успешно добавлено", Toast.LENGTH_SHORT).show()
            }
    }

    fun getParent(parentUid: String?, firebaseCallBack: FirebaseCallback<Parent>) {
        parentRef.child("$parentUid").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val parent = getAllFieldsParent(p0)
                firebaseCallBack.onComplete(parent)
            }

        })
    }

    fun getAllFieldsParent(p0: DataSnapshot): Parent {
        var parent = Parent("", "", "")
        p0.children.forEach {
            if (it.key.toString() == "acceptAnswer") {
                parent.acceptAnswer = it.value.toString()
            }

            if (it.key.toString() == "city") {
                parent.city = it.value.toString()
            }
            if (it.key.toString() == "cityId") {
                parent.cityId = it.value.toString().toInt()
            }
            if (it.key.toString() == "email") {
                parent.email = it.value.toString()
            }
            if (it.key.toString() == "profileImageUrl") {
                parent.profileImageUrl = it.value.toString()
            }
            if (it.key.toString() == "parentUid") {
                parent.parentUid = it.value.toString()
            }
            if (it.key.toString() == "username") {
                parent.username = it.value.toString()
            }
        }
        return parent
    }

    fun getChild(childUid: String?, firebaseCallBack: FirebaseCallback<Child>) {
        childRef.child("$childUid").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val child = getAllFieldsChild(p0)
                firebaseCallBack.onComplete(child)
            }

        })
    }

    fun getAllFieldsChild(p0: DataSnapshot): Child {
        var child = Child("", "", "")
        p0.children.forEach {
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

    fun getTask(taskId: String, firebaseCallBack: FirebaseCallback<Task>) {
        val ref = taskRef.child("$taskId")
        var task = Task("", "", "", 0, "", "")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                task = getAllFieldsTask(p0)
                Log.d("TAG", task.childUid)
                firebaseCallBack.onComplete(task)
            }

        })
    }

    fun getAllFieldsTask(p0: DataSnapshot): Task {
        var task = Task("", "", "", 0, "", "")

        p0.children.forEach {
            if (it.key.toString() == "cost") {
                task.cost = it.value.toString().toInt()
            }
            if (it.key.toString() == "description") {
                task.description = it.value.toString()
            }
            if (it.key.toString() == "parentUid") {
                task.parentUid = it.value.toString()
            }
            if (it.key.toString() == "status") {
                task.status = it.value.toString().toInt()
            }
            if (it.key.toString() == "taskId") {
                task.taskId = it.value.toString()
            }
            if (it.key.toString() == "time") {
                task.time = it.value.toString()
            }
            if (it.key.toString() == "title") {
                task.title = it.value.toString()
            }
            if (it.key.toString() == "childUid") {
                task.childUid = it.value.toString()
            }
            if (it.key.toString() == "showNotification") {
                task.showNotification = it.value.toString().toInt()
            }
        }
        return task
    }

    fun getTasksParentUid(
        parentUid: String,
        status: Int,
        firebaseCallBack: FirebaseCallback<List<Task>>
    ) {
        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var tasks: MutableList<Task> = mutableListOf()
                var parentUidInFirebase: String = ""
                var statusInFirebase: Int = -2
                for (p1 in p0.children) {
                    for (p2 in p1.children) {
                        if (p2.key.toString() == "parentUid") {
                            parentUidInFirebase = p2.value.toString()
                        }
                        if (p2.key.toString() == "status") {
                            statusInFirebase = p2.value.toString().toInt()
                        }
                    }
                    if (parentUidInFirebase == parentUid && statusInFirebase == status) {
                        tasks.add(getAllFieldsTask(p1))
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }

        })
    }

    fun getTasksChildUid(
        childUid: String,
        status: Int,
        firebaseCallBack: FirebaseCallback<List<Task>>
    ) {
        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var tasks: MutableList<Task> = mutableListOf()
                var childUidInFirebase: String = ""
                var statusInFirebase: Int = -2
                for (p1 in p0.children) {
                    for (p2 in p1.children) {
                        if (p2.key.toString() == "childUid") {
                            childUidInFirebase = p2.value.toString()
                        }
                        if (p2.key.toString() == "status") {
                            statusInFirebase = p2.value.toString().toInt()
                        }
                    }
                    if (childUidInFirebase == childUid && statusInFirebase == status) {
                        tasks.add(getAllFieldsTask(p1))
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }
        })
    }

    fun getChildrenByParentUid(parentUid: String, firebaseCallBack: FirebaseCallback<List<Child>>) {
        childRef.keepSynced(true)
        childRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val children: MutableList<Child> = mutableListOf()
                var child: Child
                p0.children.forEach {

                    child = getAllFieldsChild(it)
                    if (child.parentUid == parentUid) children.add(child)
                }
                firebaseCallBack.onComplete(children)
            }

        })
    }

    fun getRoleByUid(uid: String, firebaseCallBack: FirebaseCallback<String>) {
        rolesRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                firebaseCallBack.onComplete(p0.value.toString())
            }

        })
    }

    fun getDiaries(firebaseCallBack: FirebaseCallback<List<String>>) {
        val diaries = mutableListOf("Электронного дневника нет")
        diariesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    diaries.add("${it.value}")
                    //diaries.add("${it.key} (${it.value})" )
                }
                firebaseCallBack.onComplete(diaries)
            }

        })
    }

    fun getFieldDiaryChild(
        childUid: String,
        field: String,
        firebaseCallBack: FirebaseCallback<String>
    ) {
        val ref = childRef.child("${childUid}").child("diary")
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

    fun getFieldDiaryParent(
        parentUid: String,
        field: String,
        firebaseCallBack: FirebaseCallback<String>
    ) {
        val ref = parentRef.child("${parentUid}").child("diary")
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
}