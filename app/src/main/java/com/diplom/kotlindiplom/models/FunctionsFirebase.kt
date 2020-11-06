package com.diplom.kotlindiplom.models

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.diaries.Diary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import org.cryptonode.jncryptor.AES256JNCryptor
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt


open class FunctionsFirebase {
    val rootRef = FirebaseDatabase.getInstance().reference
    val diariesRef = rootRef.child("diaries")
    val secretKey = "applicatonFromDiplom"
    val userRef = rootRef.child("users")
    val userUid = FirebaseAuth.getInstance().uid.toString()
    val diaryRef = userRef.child(userUid).child("diary")
    val scheduleRef = diaryRef.child("schedule")
    val myScheduleRef = userRef.child(userUid).child("mySchedule")
    val markRef = diaryRef.child("marks")
    val feedbackRef = rootRef.child("feedback")
    fun importScheduleToMySchedule(firebaseCallBack: Callback<Boolean>) {
        getAllLessonsFromDiary(object : Callback<Map<String, List<Lesson>>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onComplete(value: Map<String, List<Lesson>>) {
                value.forEach { (weekday, lessons) ->
                    var i = 0
                    lessons.forEach { lessonInfo ->
                        addLessonMyScheduleInFirebase(weekday, i.toString(), lessonInfo)
                        i++
                    }
                }
                firebaseCallBack.onComplete(value.isNotEmpty())
            }
        })
    }

    private fun getAllLessonsFromDiary(firebaseCallBack: Callback<Map<String, List<Lesson>>>) {
        val lessons = mutableMapOf<String, List<Lesson>>()
        val listWeekday = listOf("понедельник", "вторник", "среда", "четверг", "пятница", "суббота")
        scheduleRef.keepSynced(true)
        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val weekday = it.key.toString()
                    listWeekday.forEach { listWeekdayElement ->
                        if (weekday == listWeekdayElement) {
                            val lessonsFromDay = mutableListOf<Lesson>()
                            it.children.forEach { fieldsLesson ->
                                if (fieldsLesson.key.toString() != "date") {
                                    lessonsFromDay.add(getLesson(fieldsLesson))
                                }
                            }
                            lessons[weekday] = lessonsFromDay
                        }
                    }
                }
                firebaseCallBack.onComplete(lessons)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun removeAllListener() {
        rootRef.removeEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    @ExperimentalStdlibApi
    fun updateSchedule() {
        val fields = listOf("url", "idChild")
        Log.d("Tag", "updateSchedule")
        getFieldsDiary(userUid, fields, object : Callback<Map<String, String>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onComplete(value: Map<String, String>) {
                val idChild = value["idChild"].toString()
                val urlDiary = value["url"].toString()
                if (idChild.isNotEmpty() && urlDiary.isNotEmpty()) {
                    val diary = Diary()
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = System.currentTimeMillis()
                    val week = calendar.get(Calendar.WEEK_OF_YEAR)
                    val year = calendar.get(Calendar.YEAR)
                    setDateUpdateSсhedule(
                        calendar.get(Calendar.YEAR).toString(),
                        (calendar.get(Calendar.MONTH) + 1).toString(),
                        calendar.get(Calendar.DAY_OF_MONTH).toString(),
                        week
                    )
                    when (urlDiary) {
                        diary.elschool.url -> {
                            diary.elschool.getScheduleFromElschool(
                                year,
                                week,
                                idChild,
                                object : Callback<Boolean> {
                                    override fun onComplete(value: Boolean) {

                                    }
                                })
                        }
                    }
                }
            }
        })
    }

    fun updateLessonMyScheduleInFirebase(
        weekday: String,
        numberLesson: String,
        lessonName: String,
        cabinet: String,
        homework: String,
        time: String,
        dateHomework: String
    ) {
        val ref = userRef.child(userUid).child("mySchedule").child(weekday).child(numberLesson)
        ref.child("dateHomework").setValue(dateHomework)
        ref.child("homework").setValue(homework)
        ref.child("cabinet").setValue(cabinet)
        ref.child("lessonName").setValue(lessonName)
        ref.child("time").setValue(time)
    }

    fun getFieldsLessonMyScheduleOutFirebase(infoLesson: DataSnapshot): Lesson {
        val lesson = Lesson()
        infoLesson.children.forEach {
            if (it.key.toString() == "lessonName") {
                lesson.lessonName = it.value.toString()
            }
            if (it.key.toString() == "homework") {
                lesson.homework = it.value.toString()
            }
            if (it.key.toString() == "cabinet") {
                lesson.cabinet = it.value.toString()
            }
            if (it.key.toString() == "time") {
                lesson.time = it.value.toString()
            }
            if (it.key.toString() == "dateHomework") {
                lesson.dateHomework = it.value.toString()
            }
        }
        return lesson
    }

    fun getLessonMyScheduleOutFirebase(
        weekday: String,
        firebaseCallBack: Callback<List<Lesson>>
    ) {
        val ref = myScheduleRef.child(weekday)
        val lessons = mutableListOf<Lesson>()
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var i = 0
                    snapshot.children.forEach { infoLesson ->
                        lessons.add(getFieldsLessonMyScheduleOutFirebase(infoLesson))
                        i++
                    }
                    firebaseCallBack.onComplete(lessons)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addLessonMyScheduleInFirebase(weekday: String, numberLesson: String, lesson: Lesson) {
        val ref = myScheduleRef.child(weekday).child(numberLesson)

        ref.child("homework").setValue("")
        ref.child("lessonName").setValue(lesson.lessonName)
        ref.child("cabinet").setValue(lesson.cabinet)
        ref.child("time").setValue(lesson.time)
    }

    fun getRoleByUid(callback: Callback<String>) {
        userRef.child(userUid).child("role")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onComplete(snapshot.value.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    fun clearAcceptRequest(childUid: String) {
        val ref = userRef.child(childUid)
        ref.child("acceptName").setValue("")
        ref.child("acceptUid").setValue("")
    }

    fun getNextIdChild(firebaseCallBack: Callback<Int>) {
        val ref = userRef.orderByChild("role").equalTo("child")
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var maxId = 0
                snapshot.children.forEach {
                    it.children.forEach { fieldChild ->
                        if (fieldChild.key.toString() == "id") {
                            if (fieldChild.value.toString().toInt() > maxId) {
                                maxId = fieldChild.value.toString().toInt()
                            }
                        }
                    }
                }
                firebaseCallBack.onComplete(maxId + 1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun downloadSchoolBook(book: SchoolBook, context: Context, activity: Activity) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(book.url)
        ref.downloadUrl.addOnSuccessListener { uri ->
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Загрузка началась", Toast.LENGTH_SHORT).show()
                    request.setDestinationInExternalPublicDir(
                        DIRECTORY_DOWNLOADS,
                        book.name + ".pdf"
                    )
                    downloadManager.enqueue(request)
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Ошибка при загрузке", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDetailsBook(bookSnap: DataSnapshot): SchoolBook {
        val schoolBook = SchoolBook()
        bookSnap.children.forEach { detail ->
            if (detail.key.toString() == "name") {
                schoolBook.name = detail.value.toString()
            }
            if (detail.key.toString() == "cover") {
                schoolBook.cover = detail.value.toString()
            }
            if (detail.key.toString() == "url") {
                schoolBook.url = detail.value.toString()
            }
        }
        return schoolBook
    }

    fun getSchoolBooks(
        numberClass: String?,
        subjectName: String?,
        firebaseCallBack: Callback<List<SchoolBook>>
    ) {
        val ref = rootRef.child("schoolBooks").child("${numberClass}class").child(subjectName!!)
        ref.keepSynced(true)
        val books = mutableListOf<SchoolBook>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach { book ->
                        books.add(getDetailsBook(book))
                    }
                }
                firebaseCallBack.onComplete(books)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getMarksLessonSemester(
        lessonName: String,
        firebaseCallBack: Callback<List<Int>>
    ) {
        val ref =
            userRef.child(userUid).child("diary")
                .child("marks").orderByChild("lessonName").equalTo(lessonName)
        ref.keepSynced(true)
        val marksSemester = mutableListOf<Int>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { lesson ->
                        lesson.children.forEach { semestr ->
                            semestr.children.forEach {
                                if (it.key.toString() == "middleMark") {
                                    marksSemester.add(
                                        it.value.toString().toFloat().roundToInt()
                                    )
                                }
                            }
                        }
                    }
                    firebaseCallBack.onComplete(marksSemester)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getFieldMarks(field: String, firebaseCallBack: Callback<String>) {

        val ref = userRef.child(userUid).child("diary").child("marks")
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getLessonsAndFinalMark(
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(userUid).child("diary")
            .child("marks")
        val lessons = mutableMapOf<String, String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { lesson ->
                        var lessonName = ""
                        val middleMarks = mutableListOf<Float>()
                        lesson.children.forEach { info ->
                            if (info.key.toString() == "lessonName") {
                                lessonName = info.value.toString()
                            }
                            info.children.forEach { semestr ->
                                if (semestr.key.toString() == "middleMark") {
                                    if (semestr.value.toString().toFloat() != 0f){
                                        middleMarks.add(semestr.value.toString().toFloat())
                                    }
                                }
                            }
                        }
                        var summa = 0f
                        middleMarks.forEach {
                            summa += it.roundToInt()
                        }
                        if (middleMarks.size != 0) {
                            summa /= middleMarks.size
                            if (summa.roundToInt() != 0) {
                                lessons[lessonName] = summa.roundToInt().toString()
                            }
                        }
                    }
                }
                firebaseCallBack.onComplete(lessons)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getLessonsAndMiddleMark(
        semesterNumber: String,
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(userUid).child("diary")
            .child("marks")
        val lessons = mutableMapOf<String, String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach { lesson ->
                        var lessonName = ""
                        lesson.children.forEach { info ->
                            if (info.key.toString() == "lessonName") {
                                lessonName = info.value.toString()
                                lesson.children.forEach { semestr ->
                                    if (semestr.key.toString() == "semestr$semesterNumber") {
                                        var mark = ""
                                        semestr.children.forEach { middleMark ->
                                            if (middleMark.key.toString() == "middleMark") {
                                                mark = middleMark.value.toString()
                                                lessons[lessonName] = mark
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    firebaseCallBack.onComplete(lessons)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                return
            }

        })
    }

    fun getMiddleMark(
        lessonName: String,
        semesterNumber: String,
        firebaseCallBack: Callback<String>
    ) {
        val ref = userRef.child(userUid).child("diary").child("marks").orderByChild("lessonName")
            .equalTo(lessonName)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { lesson ->
                        lesson.children.forEach { semestr ->
                            if (semestr.key.toString() == "semestr$semesterNumber") {
                                semestr.children.forEach { middleMark ->
                                    if (middleMark.key.toString() == "middleMark") {
                                        val mark = middleMark.value.toString()
                                        firebaseCallBack.onComplete(mark)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getDetailsMarks(
        lessonName: String,
        numberSemester: String,
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(userUid).child("diary").child("marks").orderByChild("lessonName")
            .equalTo(lessonName)
        val detailMarksMap = mutableMapOf<String, String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach { lesson ->
                        lesson.children.forEach { info ->
                            lesson.children.forEach { semestr ->
                                if (semestr.key.toString() == "semestr$numberSemester") {
                                    semestr.children.forEach { mark ->
                                        var date = ""
                                        var value = ""
                                        mark.children.forEach { detailMark ->
                                            if (detailMark.key.toString() == "date") {
                                                date =
                                                    detailMark.value.toString()
                                            }
                                            if (detailMark.key.toString() == "value") {
                                                value =
                                                    detailMark.value.toString()
                                            }
                                        }
                                        detailMarksMap[date] = value
                                    }
                                }
                            }
                        }
                    }
                    firebaseCallBack.onComplete(detailMarksMap)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getLesson(fieldsLesson: DataSnapshot): Lesson {
        val lesson = Lesson()
        lesson.lessonName = fieldsLesson.child("lessonName").value.toString()
        lesson.cabinet = fieldsLesson.child("cabinet").value.toString()
        lesson.time = fieldsLesson.child("time").value.toString()
        lesson.homework = fieldsLesson.child("homework").value.toString()
        lesson.form = fieldsLesson.child("form").value.toString()
        lesson.mark = fieldsLesson.child("mark").value.toString()
        return lesson
    }

    fun createDiary(urlDiary: String) {
        val diary = Diary()
        when (urlDiary) {
            diary.elschool.url -> diary.elschool.createDiary()
        }
    }

    fun deleteDiary() {
        getFieldDiary(userUid, "url", object : Callback<String> {
            override fun onComplete(value: String) {
                val diary = Diary()
                when (value) {
                    diary.elschool.url -> diary.elschool.deleteDiary()
                }
            }
        })
    }

    fun getRolesDiary(firebaseCallBack: Callback<List<RoleDiary>>) {
        val ref = diaryRef.child("roles")
        val roles = mutableListOf<RoleDiary>()
        ref.keepSynced(true)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { role ->
                    val roleDiary = role.getValue<RoleDiary>() ?: RoleDiary()
                    roles.add(roleDiary)
                }
                firebaseCallBack.onComplete(roles)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun setNewRoleDiary(count: Int, role: RoleDiary) {
        val ref = diaryRef.child("roles").child(count.toString())
        ref.setValue(role)
    }

    fun setFieldDiary(uid: String, field: String, value: Any) {
        val ref = userRef.child(uid).child("diary")
        ref.child(field).setValue(value)
    }
    fun getScheduleDay(uid: String, day: String, firebaseCallBack: Callback<List<Lesson>>) {
        val lessons = mutableListOf<Lesson>()
        val ref = userRef.child(uid).child("diary")
            .child("schedule").child(day)
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

    fun setLoginAndPasswordDiary(login: String, password: String) {
        val crypto = AES256JNCryptor()
        val cipherText =
            crypto.encryptData(password.toByteArray(), secretKey.toCharArray())
        val temp = Arrays.toString(cipherText)
        setFieldUserDatabase(userUid, "diary/login", login)
        setFieldUserDatabase(userUid, "diary/password", temp)


    }

    fun getPointChild(uid: String, firebaseCallBack: Callback<String>) {
        val ref = userRef.child(uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        if (it.key.toString() == "point") {
                            firebaseCallBack.onComplete(it.value.toString())
                            return
                        }
                    }
                }
            }
        })
    }

    fun getFieldUserDatabase(
        uid: String,
        field: String,
        firebaseCallBack: Callback<String>
    ) {
        val ref = userRef.child(uid)
        ref.keepSynced(true)
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

    fun getDateUpdateInSchedule(firebaseCallBack: Callback<LocalDate>) {
        val ref =
            userRef.child(userUid).child("diary").child("schedule")

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

    fun setDateUpdateSсhedule(year: String, month: String, day: String, weekUpdate: Int) {

        val ref = userRef.child(userUid).child("diary")
            .child("schedule")
        ref.child("year").setValue(year)
        ref.child("month").setValue(month)
        ref.child("day").setValue(day)
        ref.child("weekUpdate").setValue(weekUpdate)
    }

    fun setFieldSchedule(uid: String, field: String, value: Any) {
        val ref = userRef.child(userUid).child("diary")
            .child("schedule")
        ref.child(field).setValue(value)
    }

    fun getFieldScheduleDay(uid: String, day: String, firebaseCallBack: Callback<String>) {
        val ref =
            userRef.child(uid).child("diary")
                .child("schedule")
                .child(day)
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

    fun getFieldSchedule(uid: String, field: String, firebaseCallBack: Callback<String>) {
        val ref =
            userRef.child(uid).child("diary")
                .child("schedule")
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

    fun getFieldsUserDatabase(
        uid: String,
        fields: List<String>,
        firebaseCallBack: Callback<Map<String, String>>
    ) {

    }

    fun getFieldsDiary(
        uid: String,
        fields: List<String>,
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(uid).child("diary")
        val value = mutableMapOf<String, String>()
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        fields.forEach { field ->
                            if (it.key.toString() == field) {
                                value[field] = it.value.toString()
                            }
                        }

                    }
                }
                firebaseCallBack.onComplete(value)
            }
        })
    }

    fun getFieldDiary(uid: String, field: String, firebaseCallBack: Callback<String>) {
        val ref = userRef.child(uid).child("diary")
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
                        }
                    }
                }
                firebaseCallBack.onComplete(value)
            }
        })
    }

    fun setFieldDatabaseUser(
        uid: String,
        field: String,
        value: Any?,
        firebaseCallBack: Callback<Boolean>
    ) {
        val ref = userRef.child(uid)
        ref.keepSynced(true)
        ref.child(field).setValue(value)
        firebaseCallBack.onComplete(true)
    }

    fun setFieldUserDatabase(uid: String, field: String, value: Any?) {

        val ref = userRef.child(uid)
        ref.keepSynced(true)
        ref.child(field).setValue(value)

    }


    fun sendRequestChild(idChild: String, context: Context) {
        val ref = userRef.orderByChild("id").equalTo(idChild.toDouble())
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val childUid = child.key.toString()
                        getFieldUserDatabase(childUid, "parentUid", object : Callback<String> {
                            override fun onComplete(value: String) {
                                val parentUid = value
                                if (parentUid == userUid) {
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
                                    getFieldUserDatabase(
                                        childUid,
                                        "acceptUid",
                                        object : Callback<String> {
                                            override fun onComplete(value: String) {
                                                if (value.isEmpty()) {
                                                    getFieldUserDatabase(
                                                        userUid,
                                                        "username",
                                                        object : Callback<String> {
                                                            override fun onComplete(
                                                                value: String
                                                            ) {
                                                                setFieldUserDatabase(
                                                                    childUid,
                                                                    "acceptName",
                                                                    value
                                                                )
                                                                setFieldUserDatabase(
                                                                    childUid,
                                                                    "acceptUid",
                                                                    userUid
                                                                )
                                                            }
                                                        })
                                                    Toast.makeText(
                                                        context,
                                                        "Запрос отправлен",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Ожидание ответа на запрос",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        })
                                }
                            }
                        })
                    }
                } else {
                    Toast.makeText(context, "Id ребенка не был найден", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun getParent(parentUid: String?, firebaseCallBack: Callback<Parent>) {
        userRef.child(parentUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val parent = p0.getValue<Parent>() ?: Parent()
                    firebaseCallBack.onComplete(parent)
                }

            })
    }

    fun getChild(childUid: String, firebaseCallBack: Callback<Child>) {
        userRef.child(childUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val child = p0.getValue<Child>() ?: Child()
                    firebaseCallBack.onComplete(child)
                }

            })
    }

    fun getChildrenByParentUid(parentUid: String, firebaseCallBack: Callback<List<Child>>) {
        userRef.keepSynced(true)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val children: MutableList<Child> = mutableListOf()
                var child: Child
                p0.children.forEach {
                    child = it.getValue<Child>() ?: Child()
                    if (child.parentUid == parentUid) children.add(child)
                }
                firebaseCallBack.onComplete(children)
            }

        })
    }

    fun getDiaries(firebaseCallBack: Callback<List<String>>) {
        val diaries = mutableListOf("Электронного дневника нет")
        diariesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    diaries.add(it.value.toString())
                }
                firebaseCallBack.onComplete(diaries)
            }

        })
    }
}