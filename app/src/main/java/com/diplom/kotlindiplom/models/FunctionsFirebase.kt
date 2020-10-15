package com.diplom.kotlindiplom.models

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_choose_semestr.*
import org.cryptonode.jncryptor.AES256JNCryptor
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt


class FunctionsFirebase {
    val rootRef = FirebaseDatabase.getInstance().reference
    val diariesRef = rootRef.child("diaries")
    val secretKey = "applicatonFromDiplom"
    val userRef = rootRef.child("users")
    val taskRef = rootRef.child("tasks")
    val awardsRef = rootRef.child("awards")
    val userUid = FirebaseAuth.getInstance().uid.toString()
    val diaryRef = userRef.child(userUid).child("diary")
    val scheduleRef = diaryRef.child("schedule")
    val myScheduleRef = userRef.child(userUid).child("mySchedule")
    val feedbackRef = rootRef.child("feedback")

    fun getCountOpenFeedback(callback: Callback<Int>){
        val ref = feedbackRef.orderByChild("userUid").equalTo(userUid)
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                snapshot.children.forEach {
                    it.children.forEach{
                        if (it.key.toString() == "status"){
                            if (it.value.toString().toInt() == -1){
                                count++;
                            }
                        }
                    }
                }
                callback.onComplete(count)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun sendMessageFeedback(feedbackId:String, textMessage:String, author:String){
        val nowDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(nowDate)
        val messageFeedback = MessageFeedback(author,textMessage,formatter)
        val ref = feedbackRef.child(feedbackId).child("messages").push()
        messageFeedback.id = ref.key.toString()
        ref.setValue(messageFeedback)
    }
    private fun getAllMessagesInFeedback(detailsFeedback: DataSnapshot): MutableList<MessageFeedback> {
        val messages = mutableListOf<MessageFeedback>()
        detailsFeedback.children.forEach {
            val messageFeedback = MessageFeedback()
            it.children.forEach { detailMessage ->
                val value = detailMessage.value.toString()
                when (detailMessage.key.toString()) {
                    "id"->{
                        messageFeedback.id = value
                    }
                    "author" -> {
                        messageFeedback.author = value
                    }
                    "text" -> {
                        messageFeedback.text = value
                    }
                    "time" ->{
                        messageFeedback.time = value
                    }
                    "readStatus"->{
                        messageFeedback.readStatus = value
                    }
                }
            }
            if (messageFeedback.text.isNotEmpty()) {
                messages.add(messageFeedback)
            }
        }
        return messages
    }

    fun getAllFieldsFeedback(detailsFeedback: DataSnapshot): Feedback {
        val feedback = Feedback()
        detailsFeedback.children.forEach { field ->
            val value = field.value.toString()
            when (field.key.toString()) {
                "codeQuestion" -> {
                    feedback.codeQuestion = value.toInt()
                }
                "status" -> {
                    feedback.status = value.toInt()
                }
                "topic" -> {
                    feedback.topic = value
                }
                "messages" -> {
                    feedback.messages = getAllMessagesInFeedback(field)
                }
                "id" -> {
                    feedback.id = value
                }
                "time"->{
                    feedback.time = value
                }
                "userUid"->{
                    feedback.userUid = value
                }
            }
        }
        return feedback
    }

    fun getFeedback(id: String, callback: Callback<Feedback>) {
        val ref = feedbackRef.orderByChild("id").equalTo(id)
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { feedbacks ->
                    val feedback  = getAllFieldsFeedback(feedbacks)
                    feedback.messages.forEach {
                        if (it.author != "user" && it.readStatus != "1" && it.id.isNotEmpty()){
                            feedbackRef.child(feedback.id).child("messages").child(it.id).child("readStatus").setValue("1")
                        }
                    }

                    callback.onComplete(feedback)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getListFeedback(callback: Callback<List<Feedback>>) {
        val ref = feedbackRef.orderByChild("userUid").equalTo(userUid)
        ref.keepSynced(true)
        val feedbacks = mutableListOf<Feedback>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { detailsFeedback ->
                    val feedback = getAllFieldsFeedback(detailsFeedback)
                    if (feedback.id.isNotEmpty()) {
                        feedbacks.add(getAllFieldsFeedback(detailsFeedback))
                    }
                }
                callback.onComplete(feedbacks)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addFeedback(codeQuestion: Int, topic: String, message: String) {
        val ref = feedbackRef.push()
        val nowDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(nowDate)
        ref.child("codeQuestion").setValue(codeQuestion)
        ref.child("topic").setValue(topic)
        ref.child("userUid").setValue(userUid)
        ref.child("status").setValue(-1)
        ref.child("time").setValue(formatter)
        ref.child("id").setValue(ref.key)
        val messageRef = ref.child("messages").push()
        messageRef.child("text").setValue(message)
        messageRef.child("author").setValue("user")
        messageRef.child("id").setValue(messageRef.key.toString())
        messageRef.child("time").setValue(formatter)
    }

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
                Log.d("Tag", idChild + " " + urlDiary)
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
                lesson.name = it.value.toString()
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
        ref.child("lessonName").setValue(lesson.name)
        ref.child("cabinet").setValue(lesson.cabinet)
        ref.child("time").setValue(lesson.time)
    }

    fun deleteAward(awardId: String) {
        val ref = rootRef.child("awards").child(awardId)
        ref.removeValue()
    }

    fun setFieldAward(awardId: String, field: String, value: Any) {
        val ref = rootRef.child("awards").child(awardId)
        ref.child(field).setValue(value)
    }

    fun getAllFieldAward(award: DataSnapshot): Award {
        val awardField = Award()
        award.children.forEach {
            if (it.key.toString() == "awardId") {
                awardField.awardId = it.value.toString()
            }
            if (it.key.toString() == "cost") {
                awardField.cost = it.value.toString()
            }
            if (it.key.toString() == "name") {
                awardField.name = it.value.toString()
            }
            if (it.key.toString() == "parentUid") {
                awardField.parentUid = it.value.toString()
            }
            if (it.key.toString() == "status") {
                awardField.status = it.value.toString().toInt()
            }
            if (it.key.toString() == "showNotification") {
                awardField.showNotification = it.value.toString().toInt()
            }
            if (it.key.toString() == "childUid") {
                awardField.childUid = it.value.toString()
            }
        }
        return awardField
    }

    fun getAwardOutFirebaseWithAwardID(
        awardId: String,
        firebaseCallBack: Callback<Award>
    ) {
        val ref = rootRef.child("awards").child(awardId)
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val award = getAllFieldAward(snapshot)
                firebaseCallBack.onComplete(award)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    fun getRoleByUid(callback: Callback<String>){
        userRef.child(userUid).child("role").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onComplete(snapshot.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun getAwardOutFirebaseWithParentUid(
        role: String,
        parentUid: String,
        firebaseCallBack: Callback<List<Award>>
    ) {
        val ref = rootRef.child("awards").orderByChild("parentUid").equalTo(parentUid)
        var award = Award()
        val awards = mutableListOf<Award>()
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { awardSnapshot ->
                        award = getAllFieldAward(awardSnapshot)
                        if (role == "child") {
                            if (award.status == 0 || (award.status == 1 && award.childUid == userUid)) {
                                awards.add(award)
                            }
                        } else {
                            awards.add(award)
                        }
                    }
                }
                firebaseCallBack.onComplete(awards)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addAwardInFirebase(nameAward: String, cost: String) {
        val awardId = UUID.randomUUID().toString()
        val ref = rootRef.child("awards").child(awardId)
        ref.child("parentUid").setValue(userUid)
        ref.child("cost").setValue(cost)
        ref.child("awardId").setValue(awardId)
        ref.child("name").setValue(nameAward)
        ref.child("childUid").setValue("")
        ref.child("status").setValue(0)
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

        /*val ref1 = rootRef.child("users").orderByKey().equalTo(uidUser)
        ref1.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.d("Tag",it.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })*/

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
                                    middleMarks.add(semestr.value.toString().toFloat())
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

    fun setFieldDiary(uid: String, field: String, value: Any) {
        val ref = userRef.child(uid).child("diary")
        ref.child(field).setValue(value)
    }

    fun getScheduleFromFirebase(firebaseCallback: Callback<MutableMap<String, List<Lesson>>>) {
        val schedule = mutableMapOf<String, List<Lesson>>()
        val ref = userRef.child("diary").child("schedule")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { dayFirebase ->
                    if (dayFirebase.childrenCount != 0L) {
                        val day = dayFirebase.key.toString()
                        getScheduleDay(userUid, day, object : Callback<List<Lesson>> {
                            override fun onComplete(value: List<Lesson>) {
                                schedule[day] = value
                            }
                        })
                    }
                }
                firebaseCallback.onComplete(schedule)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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
                        Log.d("Tag", it.key.toString() + " " + it.value.toString())
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

    @ExperimentalStdlibApi
    fun getLoginAndPasswordAndUrlDiary(
        uid: String,
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(userUid).child("diary")
        val value = mutableMapOf<String, String>()

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
                                        bytes[i] = byteValues[i].trim { it <= ' ' }
                                            .toByte()
                                        i++
                                    }
                                }
                                val cryptor = AES256JNCryptor()
                                val decrypt =
                                    cryptor.decryptData(
                                        bytes,
                                        secretKey.toCharArray()
                                    )
                                value["password"] = decrypt.decodeToString()
                            } else {
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

    fun setFieldDatabaseTask(taskId: String, field: String, value: Any) {
        val ref = taskRef.child(taskId)
        ref.child(field).setValue(value)

    }

    fun getFieldDatabaseTask(
        taskId: String,
        field: String,
        firebaseCallBack: Callback<String>
    ) {
        val ref = taskRef.child(taskId)
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
        val ref = userRef.child(childUid)
        ref.keepSynced(true)
        getFieldUserDatabase(childUid, "point", object : Callback<String> {
            override fun onComplete(value: String) {
                ref.child("point").setValue(value.toInt() + point)
            }
        })
    }

    fun sendRequestChild(idChild: String, context: Context) {
        //clearAcceptRequest()
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

    fun uploadImageToFirebase(selectedPhotoUri: Uri?, activity: Activity, role: String) {
        Log.d("TAG", "$selectedPhotoUri")
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val fileRef = FirebaseStorage.getInstance().getReference("/image/$filename")


        getFieldUserDatabase(userUid, "profileImageName", object : Callback<String> {
            override fun onComplete(value: String) {
                if (value.isNotEmpty()) {
                    val deleteRef =
                        FirebaseStorage.getInstance().getReference("/image/$value")
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
                    val ref =
                        FirebaseDatabase.getInstance().getReference("/users/$role/$uid")
                    ref.child("profileImageUrl").setValue(it.toString())
                    ref.child("profileImageName").setValue(filename)

                    /*Glide.with(activity).load(it.toString())
                        .into(activity.photoImageviewDrawer)*/
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
                Toast.makeText(context, "Задание успешно добавлено", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun getParent(parentUid: String?, firebaseCallBack: Callback<Parent>) {
        userRef.child(parentUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
        val parent = Parent("", "", "")
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
            if (it.key.toString() == "userUid") {
                parent.userUid = it.value.toString()
            }
            if (it.key.toString() == "username") {
                parent.username = it.value.toString()
            }
        }
        return parent
    }

    fun getChild(childUid: String, firebaseCallBack: Callback<Child>) {
        userRef.child(childUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
        val child = Child("", "", "")
        p0.children.forEach {
            if (it.key.toString() == "acceptName") {
                child.acceptName = it.value.toString()
            }
            if (it.key.toString() == "acceptUid") {
                child.acceptUid = it.value.toString()
            }
            if (it.key.toString() == "userUid") {
                child.userUid = it.value.toString()
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

    fun getTask(taskId: String, firebaseCallBack: Callback<Task>) {
        val ref = taskRef.child("$taskId")
        var task = Task("", "", "", 0, "", "")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                task = getAllFieldsTask(p0)
                firebaseCallBack.onComplete(task)
            }

        })
    }

    fun getAllFieldsTask(p0: DataSnapshot): Task {
        val task = Task("", "", "", 0, "", "")

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
        firebaseCallBack: Callback<List<Task>>
    ) {
        val ref = taskRef.orderByChild("parentUid").equalTo(parentUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks: MutableList<Task> = mutableListOf()
                Log.d("Tag", parentUid)
                if (snapshot.exists()) {
                    snapshot.children.forEach { taskSnapshot ->
                        Log.d("Tag", taskSnapshot.key.toString())
                        taskSnapshot.children.forEach { taskInfo ->
                            if (taskInfo.key.toString() == "status") {
                                if (taskInfo.value.toString() == status.toString()) {
                                    tasks.add(getAllFieldsTask(taskSnapshot))
                                }
                            }
                        }
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getTasksChildUid(
        childUid: String,
        status: Int,
        firebaseCallBack: Callback<List<Task>>
    ) {
        val ref = taskRef.orderByChild("childUid").equalTo(childUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks: MutableList<Task> = mutableListOf()
                if (snapshot.exists()) {
                    snapshot.children.forEach { taskSnapshot ->
                        Log.d("Tag", taskSnapshot.key.toString())
                        taskSnapshot.children.forEach { taskInfo ->
                            if (taskInfo.key.toString() == "status") {
                                if (taskInfo.value.toString() == status.toString()) {
                                    tasks.add(getAllFieldsTask(taskSnapshot))
                                }
                            }
                        }
                    }
                }
                firebaseCallBack.onComplete(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
                    child = getAllFieldsChild(it)
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
                    diaries.add("${it.value}")
                    //diaries.add("${it.key} (${it.value})" )
                }
                firebaseCallBack.onComplete(diaries)
            }

        })
    }
}