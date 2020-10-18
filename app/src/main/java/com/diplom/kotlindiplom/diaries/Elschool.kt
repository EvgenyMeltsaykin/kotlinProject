package com.diplom.kotlindiplom.diaries

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.models.*
import com.diplom.kotlindiplom.models.elschool.SchoolSubjectElschool
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import org.decimal4j.util.DoubleRounder
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class Elschool {
    private val urlLogin = "https://elschool.ru/Logon/Index"
    val urlDiary = "https://elschool.ru/users/diaries"
    val urlMainPage = "https://elschool.ru/users/privateoffice"
    private val cabinetText = "каб."
    val keyCookie = "JWToken"
    val url = "elschool.ru"
    private fun roleDiaryToRoleDatabase(role: String): String {
        if (role.decapitalize(Locale.ROOT) == "сотрудник ОО") {
            return "employee";
        }
        if (role.decapitalize(Locale.ROOT) == "учитель") {
            return "teacher";
        }
        if (role.decapitalize(Locale.ROOT) == "классный руководитель") {
            return "classTeacher";
        }
        if (role.decapitalize(Locale.ROOT) == "учащийся") {
            return "child";
        }
        if (role.decapitalize(Locale.ROOT) == "родитель") {
            return "parent";
        }
        return ""
    }

    fun createDiary() {
        val firebase = FunctionsFirebase()
        firebase.setFieldDiary(firebase.userUid, "login", "")
        firebase.setFieldDiary(firebase.userUid, "semestrName", "триместр")
        firebase.setFieldDiary(firebase.userUid, "password", "")
        firebase.setFieldDiary(firebase.userUid, "url", "")
        firebase.setFieldDiary(firebase.userUid, "marks/dateUpdate", "")
        firebase.setFieldDiary(firebase.userUid, "schedule/weekUpdate", 0)
    }

    fun deleteDiary() {
        val firebase = FunctionsFirebase()

        firebase.setFieldDiary(firebase.userUid, "semestrName", "")
        firebase.setFieldDiary(firebase.userUid, "idChild", "")
        firebase.setFieldDiary(firebase.userUid, "roleDiary", "")
        firebase.setFieldDiary(firebase.userUid, "cookie", "")
        firebase.setFieldDiary(firebase.userUid, "login", "")
        firebase.setFieldDiary(firebase.userUid, "password", "")
        firebase.setFieldDiary(firebase.userUid, "url", "")
        firebase.setFieldDiary(firebase.userUid, "schedule", "")
        firebase.setFieldDiary(firebase.userUid, "marks", "")
        firebase.setFieldDiary(firebase.userUid, "marks/dateUpdate", "")
        firebase.setFieldDiary(firebase.userUid, "schedule/weekUpdate", 0)
        firebase.diaryRef.child("children").removeValue()
        firebase.diaryRef.child("roles").removeValue()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getRoleFromDiary(document: org.jsoup.nodes.Document) {
        val roleTable = document.select("div[class=col-12 col-xl d-flex flex-column]")
            .select("div[class=border-block p-3 mb-3 flex-grow-1]").select("tbody").select("td")
            .select("td")
        var i = 0
        val firebase = FunctionsFirebase()
        var role = ""
        val roleInDiary = RoleDiary()
        roleTable.forEach {
            if (i % 2 == 0) {
                if (role.isEmpty()) {
                    role = it.text()
                }
                roleInDiary.name = it.text()
            } else {
                roleInDiary.state = it.text()
                roleInDiary.roleInDatabase = roleDiaryToRoleDatabase(roleInDiary.name)
                firebase.setNewRoleDiary(i / 2, roleInDiary)
            }
            i++
        }
        val roleDatabase = roleDiaryToRoleDatabase(role)
        firebase.setFieldDiary(firebase.userUid, "roleDiary", roleDatabase)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun login(login: String, password: String): Boolean {
        val cookies: HashMap<String, String> = HashMap()
        try {
            val document = Jsoup.connect(urlLogin)
                .data("login", login)
                .data("password", password)
                .data("GoogleAuthCode", "")
                .cookies(cookies)
                .method(Connection.Method.POST)
                .userAgent("mozilla")
                .execute()
            val parseDoc = document.parse()

            val title = parseDoc.title()
            if (title == "Личный кабинет") {
                getRoleFromDiary(parseDoc)
                val id = parseDoc.text().substringAfter("ID ").substringBefore(" ")
                val firebase = FunctionsFirebase()
                GlobalScope.launch(Dispatchers.IO) {
                    firebase.setFieldDiary(firebase.userUid, "idChild", id)
                    firebase.setFieldDiary(firebase.userUid, "cookie", "")
                    firebase.setFieldDiary(
                        firebase.userUid,
                        "cookie",
                        document.cookies()[keyCookie].toString()
                    )
                }
                return true
            } else {
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteSchedule() {
        val firebase = FunctionsFirebase()
        val ref = firebase.userRef.child(firebase.userUid)
            .child("diary").child("schedule")
        ref.child("понедельник").removeValue()
        ref.child("вторник").removeValue()
        ref.child("среда").removeValue()
        ref.child("четверг").removeValue()
        ref.child("пятница").removeValue()
        ref.child("суббота").removeValue()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSeeMarks(scheduleHtml: org.jsoup.nodes.Document, cookies: HashMap<String, String>) {
        //scheduleHtml example
        //https://elschool.ru/users/diaries/details?rooId=18&instituteId=233&departmentId=123400&pupilId=1588026&year=2020&week=42&log=false
        //tempUri example
        //https://elschool.ru/users/diaries/confirmregularmarksbydaterange?rooId=18&instituteId=233&departmentId=123400&pupilId=1588026&year=2020&week=42&log=false
        var tempUri = scheduleHtml.baseUri().replace("details", "confirmregularmarksbydaterange")
        val year = tempUri.substringAfter("year=").substringBefore("&")
        val week = tempUri.substringAfter("week=").substringBefore("&")
        if (year.isNotEmpty()) {
            val startYearDate = LocalDate.of(year.toInt(), 1, 1)
            val dateWithWeek = startYearDate.plusWeeks((week).toLong() - 1)
            val countDay = dateWithWeek.dayOfWeek.ordinal
            val startDate = dateWithWeek.minusDays(countDay.toLong())
            val endDate = dateWithWeek.plusDays((6 - countDay).toLong())
            //tempUri example
            //https://elschool.ru/users/diaries/confirmregularmarksbydaterange?rooId=18&instituteId=233&departmentId=123400&pupilId=1588026
            tempUri = tempUri.substringBefore("&year=")
            //
            val resultUri = "$tempUri&startDate=$startDate&endDate=$endDate"
            //Просмотр оценки
            val document = Jsoup.connect(resultUri)
                .ignoreContentType(true)
                .cookies(cookies)
                .userAgent("mozilla")
                .method(Connection.Method.GET)
                .get()
        }

    }

    @ExperimentalStdlibApi
    fun getScheduleFromElschool(
        year: Int,
        week: Int,
        id: String = "",
        firebaseCallback: Callback<Boolean>
    ) {
        val firebase = FunctionsFirebase()
        val schedule = mutableMapOf<String, List<Lesson>>()
        firebase.getFieldDiary(firebase.userUid, "cookie",
            object : Callback<String> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: String) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val cookies = hashMapOf<String, String>()
                        cookies[keyCookie] = value
                        try {
                            val document = Jsoup.connect(urlDiary)
                                .cookies(cookies)
                                .method(Connection.Method.GET)
                                .get()
                            val scheduleHtml: org.jsoup.nodes.Document
                            if (id.isEmpty()) {
                                scheduleHtml =
                                    Jsoup.connect("${document.baseUri()}&year=$year&week=$week&log=false")
                                        .cookies(cookies)
                                        .get()
                            } else {
                                val urlWithoutId = document.baseUri().substringBeforeLast("=")
                                scheduleHtml =
                                    Jsoup.connect("${urlWithoutId}=$id&year=$year&week=$week&log=false")
                                        .cookies(cookies)
                                        .get()
                            }
                            //тест
                            //scheduleHtml = Jsoup.connect("https://elschool.ru/users/diaries/details?rooId=18&instituteId=233&departmentId=91120&pupilId=1588026&year=2020&week=3&log=false").cookies(cookies).get()
                            val dayOfWeekHtml = scheduleHtml.select("tbody")
                            dayOfWeekHtml.forEach { it ->
                                //example понедельник 17.08
                                var dayDate: String
                                dayDate = it.select("td[class=diary__dayweek]").select("p").text()
                                    .toString()
                                if (dayDate.isEmpty()) {
                                    dayDate =
                                        it.select("td[class=diary__dayweek  diary__dayweek_today]")
                                            .select("p").text()
                                            .toString()
                                }
                                val items = it.select("tr[class=diary__lesson]")
                                val lessons = mutableListOf<Lesson>()
                                var cabinetAndTime = ""
                                items.forEach { item ->
                                    val lesson = Lesson()
                                    lesson.name = item.select("div[class=flex-grow-1]").text()
                                    lesson.form = item.select("div[class=lesson-form]").text()
                                    cabinetAndTime =
                                        item.select("div[class=diary__discipline__time]").text()
                                    lesson.cabinet = getCabinet(cabinetAndTime)
                                    lesson.time = getTime(cabinetAndTime)
                                    lesson.homework =
                                        item.select("div[class=diary__homework-text]").text()
                                    lesson.mark = item.select("span[class=diary__mark]").text()
                                    if (lesson.mark.isEmpty()) {
                                        lesson.mark =
                                            item.select("span[class=diary__mark diary__mark-not-seen-before]")
                                                .text()
                                    }
                                    if (lesson.name.isNotEmpty()) {
                                        lessons.add(lesson)
                                    }
                                }
                                schedule[dayDate] = lessons
                            }
                            deleteSchedule()
                            firebaseCallback.onComplete(true)
                            schedule.forEach { (s, list) ->
                                var i = 0
                                val day = s.substringBefore(" ")
                                val date = s.substringAfter(" ")
                                if (day.isNotEmpty()) {
                                    list.forEach {
                                        i++
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/lessonName",
                                            it.name
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/time",
                                            it.time
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/cabinet",
                                            it.cabinet
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/form",
                                            it.form
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/homework",
                                            it.homework
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.userUid,
                                            "diary/schedule/$day/lesson$i/mark",
                                            it.mark
                                        )
                                    }
                                    firebase.setFieldUserDatabase(
                                        firebase.userUid,
                                        "diary/schedule/$day/date",
                                        date
                                    )
                                }
                            }
                            firebase.getFieldDiary(
                                firebase.userUid,
                                "roleDiary",
                                object : Callback<String> {
                                    override fun onComplete(value: String) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && value == "parent") {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                setSeeMarks(scheduleHtml, cookies)
                                            }
                                        }
                                    }
                                })

                        } catch (e: IOException) {
                            firebaseCallback.onComplete(false)
                            e.printStackTrace()
                        }
                    }
                }
            })

    }

    fun getChildrenFromFirebase(firebaseCallback: Callback<List<ChildForElschool>>) {
        val firebase = FunctionsFirebase()
        val ref = firebase.userRef.child(firebase.userUid).child("diary").child("children")
        val children = mutableListOf<ChildForElschool>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val childForElschool = ChildForElschool()
                        it.children.forEach { childInfo ->
                            if (childInfo.key.toString() == "id") {
                                childForElschool.id = childInfo.value.toString()
                            }
                            if (childInfo.key.toString() == "name") {
                                childForElschool.name = childInfo.value.toString()
                            }
                        }
                        children.add(childForElschool)
                    }
                }
                firebaseCallback.onComplete(children)
            }

        })
    }

    @ExperimentalStdlibApi
    fun writeChildrenDiaryInFirebase(firebaseCallback: Callback<Boolean>) {
        val firebase = FunctionsFirebase()
        val children = mutableListOf<ChildForElschool>()
        firebase.getFieldDiary(firebase.userUid, "cookie",
            object : Callback<String> {
                override fun onComplete(value: String) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val cookies = hashMapOf<String, String>()
                        cookies[keyCookie] = value
                        try {
                            val document = Jsoup.connect("https://$url")
                                .cookies(cookies)
                                .get()
                            val childBlockHtml =
                                document.select("div[class=d-flex align-items-center]")
                            childBlockHtml.forEach {
                                val nameChildHtml = it.select("p[class=flex-grow-1]")
                                nameChildHtml.forEach {
                                    val nameChild = it.text()
                                    val urls = it.getElementsByTag("a")
                                    var id = ""
                                    urls.forEach {
                                        id = it.attr("href").substringAfterLast("/")
                                    }
                                    val childForElschool = ChildForElschool(nameChild, id)
                                    children.add(childForElschool)
                                }
                            }
                            firebase.setFieldDiary(firebase.userUid, "children", "")
                            var i = 0
                            children.forEach {
                                i++
                                firebase.setFieldDiary(
                                    firebase.userUid,
                                    "children/child$i/id",
                                    it.id
                                )
                                firebase.setFieldDiary(
                                    firebase.userUid,
                                    "children/child$i/name",
                                    it.name
                                )
                            }
                            firebaseCallback.onComplete(true)
                        } catch (e: IOException) {
                            firebaseCallback.onComplete(false)
                            e.printStackTrace()
                        }
                    }
                }
            })
    }

    private fun getTime(cabinetAndTime: String?): String {
        if (cabinetAndTime?.substringBefore(" ") == cabinetText) {
            //example каб. 119 10:15 - 10:55
            return cabinetAndTime.substringAfter(" ").substringAfter(" ")
        } else {
            return cabinetAndTime!!
        }
    }

    private fun getCabinet(cabinetAndTime: String?): String {
        if (cabinetAndTime?.substringBefore(" ") == cabinetText) {
            //example каб. 119 10:15 - 10:55
            return cabinetAndTime.substringAfter(" ").substringBefore(" ")
        } else {
            return ""
        }
    }

    @ExperimentalStdlibApi
    fun updateSchedule(
        id: String = "",
        selectedYear: Int,
        selectedWeek: Int,
        context: Context,
        progressBar: ProgressBar,
        hideButtons: () -> Unit,
        showButtons: () -> Unit
    ) {
        val network = FunctionsNetwork()
        if (!network.checkConnect(context)) {
            Toast.makeText(
                context,
                "Обновить расписание не удалось. Нет соединения с интернетом",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        Toast.makeText(context, "Подождите, идет загрузка расписания", Toast.LENGTH_SHORT).show()
        progressBar.isVisible = true
        hideButtons()
        getScheduleFromElschool(
            selectedYear,
            selectedWeek,
            id,
            object : Callback<Boolean> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: Boolean) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(2000)
                        progressBar.isVisible = false
                        showButtons()
                        if (!value) {
                            Toast.makeText(
                                context,
                                "Не удалось загрузить расписание. Сайт не отвечает.Если вы недавно сменили пароль, авторизуйтесь заново.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Раписание загружено успешно",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            })
    }
    fun setTeacherLessonDatabase(lesson:List<SchoolSubjectElschool>, day:String){
        val firebase = FunctionsFirebase()
        firebase.diaryRef.child("schedule").child(day).removeValue()
        lesson.forEach {
            val ref = firebase.diaryRef.child("schedule").child(day).push()
            ref.keepSynced(true)
            ref.setValue(it)
        }

    }
    fun getTeacherScheduleFromDiary(day:String,date:String) {
        val firebase = FunctionsFirebase()
        firebase.getFieldDiary(firebase.userUid, "cookie", object : Callback<String> {
            override fun onComplete(value: String) {
                GlobalScope.launch(Dispatchers.IO) {
                    val cookies = hashMapOf<String, String>()
                    cookies[keyCookie] = value
                    try {
                        val document =
                            Jsoup.connect("https://elschool.ru/MenuToLayout/MenuToLayout/ScheduleJSON?date=$date")
                                .ignoreContentType(true)
                                .cookies(cookies)
                                .userAgent("mozilla")
                                .method(Connection.Method.POST)
                                .post()

                        val tempAnswer = document.select("body").text().toString().replace("\\", "")
                        val jsonAnswer = tempAnswer.substringAfter("\",").substringAfter(":").substringBeforeLast("}")
                        val schoolSubjects = arrayListOf<SchoolSubjectElschool>()
                        val klaxon = Klaxon()
                        JsonReader(StringReader(jsonAnswer)).use { reader ->
                            reader.beginArray {
                                while (reader.hasNext()) {
                                    val subject = klaxon.parse<SchoolSubjectElschool>(reader)
                                    schoolSubjects.add(subject!!)
                                }
                                setTeacherLessonDatabase(schoolSubjects,day)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("Tag",e.toString())
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMarksFromDiary(
        idChild: String,
        firebaseCallback: Callback<Boolean>
    ) {
        val firebase = FunctionsFirebase()
        firebase.setFieldDiary(firebase.userUid, "marks", "")
        GlobalScope.launch(Dispatchers.IO) {
            firebase.getFieldDiary(firebase.userUid, "cookie", object : Callback<String> {
                override fun onComplete(value: String) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val cookies = hashMapOf<String, String>()
                        cookies[keyCookie] = value
                        try {
                            val document = Jsoup.connect(urlDiary)
                                .cookies(cookies)
                                .get()
                            val scheduleHtml: org.jsoup.nodes.Document
                            if (idChild.isEmpty()) {
                                scheduleHtml =
                                    Jsoup.connect(document.baseUri())
                                        .cookies(cookies)
                                        .get()
                            } else {
                                val urlWithoutId = document.baseUri().substringBeforeLast("=")
                                scheduleHtml =
                                    Jsoup.connect("${urlWithoutId}=$idChild")
                                        .cookies(cookies)
                                        .get()
                            }
                            val gradeUrl =
                                scheduleHtml.baseUri().replace("details", "gradesandabsences")
                            Log.d("Tag", gradeUrl)
                            //тест
                            //gradeUrl = "https://elschool.ru/users/diaries/gradesandabsences?rooId=18&instituteId=233&departmentId=91120&pupilId=1588026"
                            val gradeHtml = Jsoup.connect(gradeUrl)
                                .cookies(cookies)
                                .get()
                            val gradeTable =
                                gradeHtml.select("div[class=DivForGradesAndAbsencesTable]")
                                    .select("tbody")
                            var lessonCount = 1
                            var add = false
                            gradeTable.forEach {
                                var i = 0
                                //var lessonHtml = it.select("tr[lesson=\"$lessonCount\"]")
                                do {
                                    i++
                                    val lessonHtml = it.select("tr[lesson=\"$lessonCount\"]")
                                    lessonHtml.forEach {
                                        val lessonName =
                                            it.select("td[class=gradesaa-lesson]").text()
                                        val gradesMark =
                                            it.select("td[class=gradesaa-marks]")
                                        firebase.setFieldDiary(
                                            firebase.userUid,
                                            "marks/$lessonCount/lessonName",
                                            lessonName
                                        )
                                        var semesterCount = 1
                                        gradesMark.forEach { marksHtml ->
                                            val marks =
                                                marksHtml.select("span[class=mark-span]")
                                            var markCountForMiddleMark = 0
                                            var markCount = 1
                                            var middleMark = 0f
                                            marks.forEach {
                                                if (it.text().isNotEmpty()) {
                                                    val mark = it.text()
                                                    if (mark.isDigitsOnly()) {
                                                        middleMark += mark.toInt()
                                                        markCountForMiddleMark++
                                                        Log.d(
                                                            "Tag",
                                                            "$lessonName markcount=" + markCountForMiddleMark.toString() + " middleMark = $middleMark"
                                                        )
                                                    }
                                                    val date =
                                                        it.attr("data-popover-content")
                                                            .substringBefore("<p>")
                                                            .substringAfterLast(" ")
                                                    add = true
                                                    val nowDate = Calendar.getInstance().time
                                                    val formatter = SimpleDateFormat(
                                                        "yyyy-MM-dd HH:mm",
                                                        Locale.getDefault()
                                                    ).format(nowDate)
                                                    firebase.setFieldDiary(
                                                        firebase.userUid,
                                                        "marks/dateUpdate",
                                                        formatter
                                                    )
                                                    firebase.setFieldDiary(
                                                        firebase.userUid,
                                                        "marks/$lessonCount/semestr$semesterCount/mark$markCount/date",
                                                        date
                                                    )
                                                    firebase.setFieldDiary(
                                                        firebase.userUid,
                                                        "marks/$lessonCount/semestr$semesterCount/mark$markCount/value",
                                                        mark
                                                    )
                                                    markCount++
                                                }
                                            }
                                            if (markCountForMiddleMark == 0) {
                                                middleMark = 0f
                                            } else {
                                                middleMark /= (markCountForMiddleMark)
                                            }
                                            firebase.setFieldDiary(
                                                firebase.userUid,
                                                "marks/$lessonCount/semestr$semesterCount/middleMark",
                                                DoubleRounder.round(middleMark.toDouble(), 2)
                                            )
                                            semesterCount++
                                        }

                                    }
                                    lessonCount++
                                } while (lessonHtml.isNotEmpty())
                                firebaseCallback.onComplete(add)
                                return@launch
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            firebaseCallback.onComplete(false)
                        }
                    }

                }
            })


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalStdlibApi
    fun getMarks(
        idChild: String = "",
        context: Context,
        progressBar: ProgressBar,
        hideButtons: () -> Unit,
        showButtons: () -> Unit
    ) {
        val firebase = FunctionsFirebase()
        Toast.makeText(context, "Подождите, идет загрузка оценок", Toast.LENGTH_SHORT).show()
        progressBar.isVisible = true
        hideButtons()
        getMarksFromDiary(
            idChild,
            object : Callback<Boolean> {
                override fun onComplete(end: Boolean) {
                    GlobalScope.launch(Dispatchers.Main) {
                        if (!end) {
                            Toast.makeText(
                                context,
                                "При загрузке оценок произошла ошибка. Возможно, оценки еще не добавлены.",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.isVisible = false
                            showButtons()
                        }
                        val ref =
                            firebase.userRef.child(firebase.userUid).child("diary").child("marks")
                        ref.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                p0: DataSnapshot,
                                p1: String?
                            ) {
                                progressBar.isVisible = false
                                showButtons()
                            }

                            override fun onChildChanged(
                                p0: DataSnapshot,
                                p1: String?
                            ) {
                                return
                            }

                            override fun onChildRemoved(p0: DataSnapshot) {
                                return
                            }

                            override fun onChildMoved(
                                p0: DataSnapshot,
                                p1: String?
                            ) {
                                return
                            }

                            override fun onCancelled(p0: DatabaseError) {
                                return
                            }

                        })
                    }
                }
            })

    }


}