package com.diplom.kotlindiplom.diaries

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.models.ChildForElschool
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import com.diplom.kotlindiplom.models.FunctionsNetwork
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import org.decimal4j.util.DoubleRounder
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.time.LocalDate
import kotlin.collections.HashMap

class Elschool {
    val urlLogin = "https://elschool.ru/Logon/Index"
    val urlDiary = "https://elschool.ru/users/diaries"
    val cabinetText = "каб."
    val keyCookie = "JWToken"
    val url = "elschool.ru"
    fun createDiary() {
        val firebase = FunctionsFirebase()
        Log.d("Tag", "Create")
        firebase.setFieldUserDatabase(firebase.uidUser!!, "diary/login", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/semestrName", "триместр")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/password", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/url", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/marks/dateUpdate", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/schedule/weekUpdate", 0)
    }

    fun deleteDiary() {
        val firebase = FunctionsFirebase()

        firebase.setFieldUserDatabase(firebase.uidUser!!, "diary/semestrName", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/idChild", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/cookie", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/login", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/password", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/url", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/schedule", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/marks", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/marks/dateUpdate", "")
        firebase.setFieldUserDatabase(firebase.uidUser, "diary/schedule/weekUpdate", 0)
    }

    fun login(login: String, password: String): Boolean {
        val cookies: HashMap<String, String> = HashMap()
        var title = ""
        Log.d("Tag","begin"+System.currentTimeMillis().toString())
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
            title = parseDoc.title()
            if (title == "Личный кабинет") {
                val id = parseDoc.text().substringAfter("ID ").substringBefore(" ")
                val firebase = FunctionsFirebase()
                GlobalScope.launch(Dispatchers.IO) {
                    firebase.setFieldDiary(firebase.uidUser!!, "idChild", id)
                    firebase.setFieldDiary(firebase.uidUser,"cookie","")
                    firebase.setFieldDiary(
                        firebase.uidUser,
                        "cookie",
                        document.cookies()[keyCookie].toString()
                    )
                }
                Log.d("Tag","true" + System.currentTimeMillis().toString())
                return true
            } else {
                Log.d("Tag","false1" + System.currentTimeMillis().toString())
                return false
            }
        } catch (e: IOException) {
            Log.d("Tag","IOException" + System.currentTimeMillis().toString())
            e.printStackTrace()
        }
        Log.d("Tag","false2" + System.currentTimeMillis().toString())
        return false
    }

    fun deleteSchedule() {
        val firebase = FunctionsFirebase()
        firebase.getRoleByUid(firebase.uidUser!!, object : Callback<String> {
            override fun onComplete(answer: String) {
                val ref = firebase.rootRef.child("users").child(answer).child(firebase.uidUser)
                    .child("diary").child("schedule")
                ref.child("понедельник").removeValue()
                ref.child("вторник").removeValue()
                ref.child("среда").removeValue()
                ref.child("четверг").removeValue()
                ref.child("пятница").removeValue()
                ref.child("суббота").removeValue()
            }
        })
    }

    @ExperimentalStdlibApi
    fun getSchedule(
        year: Int,
        week: Int,
        id: String = "",
        firebaseCallback: Callback<MutableMap<String, List<Lesson>>>
    ) {
        val firebase = FunctionsFirebase()
        val schedule = mutableMapOf<String, List<Lesson>>()
        firebase.getFieldDiary(firebase.uidUser!!, "cookie",
            object : Callback<String> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: String) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val cookies = hashMapOf<String, String>()
                        cookies[keyCookie] = value
                        try {
                            val document = Jsoup.connect(urlDiary)
                                .cookies(cookies)
                                .get()
                            var scheduleHtml: org.jsoup.nodes.Document
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
                            scheduleHtml = Jsoup.connect("https://elschool.ru/users/diaries/details?rooId=18&instituteId=233&departmentId=91120&pupilId=1588026&year=$year&week=$week&log=false")
                                .cookies(cookies)
                                .get()
                            val dayOfWeekHtml = scheduleHtml.select("tbody")
                            dayOfWeekHtml.forEach { it ->
                                //example понедельник 17.08
                                val dayDate =
                                    it.select("td[class=diary__dayweek ]").select("p").text()
                                        .toString()
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
                            schedule.forEach { (s, list) ->
                                var i = 0
                                val day = s.substringBefore(" ")
                                val date = s.substringAfter(" ")
                                if (day.isNotEmpty()) {
                                    list.forEach {
                                        i++
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/lessonName",
                                            it.name
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/time",
                                            it.time
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/cabinet",
                                            it.cabinet
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/form",
                                            it.form
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/homework",
                                            it.homework
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser,
                                            "diary/schedule/$day/lesson$i/mark",
                                            it.mark
                                        )
                                    }
                                    firebase.setFieldUserDatabase(
                                        firebase.uidUser,
                                        "diary/schedule/$day/date",
                                        date
                                    )
                                }
                            }
                            firebaseCallback.onComplete(schedule)
                        } catch (e: IOException) {
                            firebaseCallback.onComplete(schedule)
                            e.printStackTrace()
                        }
                    }
                }
            })

    }

    fun getChildrenFromFirebase(firebaseCallback: Callback<List<ChildForElschool>>) {
        val firebase = FunctionsFirebase()
        val ref = firebase.parentRef.child(firebase.uidUser!!).child("diary").child("children")
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
        firebase.getFieldDiary(firebase.uidUser!!, "cookie",
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
                            firebase.setFieldDiary(firebase.uidUser, "children", "")
                            var i = 0
                            children.forEach {
                                i++
                                firebase.setFieldDiary(
                                    firebase.uidUser,
                                    "children/child$i/id",
                                    it.id
                                )
                                firebase.setFieldDiary(
                                    firebase.uidUser,
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
        getSchedule(
            selectedYear,
            selectedWeek,
            id,
            object : Callback<MutableMap<String, List<Lesson>>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: MutableMap<String, List<Lesson>>) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(2000)
                        progressBar.isVisible = false
                        showButtons()
                        if (value.isNullOrEmpty()) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMarksFromDiary(
        idChild: String,
        firebaseCallback: Callback<Boolean>
    ) {
        val firebase = FunctionsFirebase()
        firebase.setFieldDiary(firebase.uidUser!!, "marks", "")
        GlobalScope.launch(Dispatchers.IO) {
            firebase.getFieldDiary(firebase.uidUser, "cookie", object : Callback<String> {
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
                            var gradeUrl =
                                scheduleHtml.baseUri().replace("details", "gradesandabsences")
                            Log.d("Tag",gradeUrl)
                            //тест
                            gradeUrl = "https://elschool.ru/users/diaries/gradesandabsences?rooId=18&instituteId=233&departmentId=91120&pupilId=1588026"
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
                                            firebase.uidUser,
                                            "marks/lesson$lessonCount/lessonName",
                                            lessonName
                                        )
                                        var semestrCount = 1
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
                                                    firebase.setFieldDiary(
                                                        firebase.uidUser,
                                                        "marks/dateUpdate",
                                                        LocalDate.now().toString()
                                                    )
                                                    firebase.setFieldDiary(
                                                        firebase.uidUser,
                                                        "marks/lesson$lessonCount/semestr$semestrCount/mark$markCount/date",
                                                        date
                                                    )
                                                    firebase.setFieldDiary(
                                                        firebase.uidUser,
                                                        "marks/lesson$lessonCount/semestr$semestrCount/mark$markCount/value",
                                                        mark
                                                    )
                                                    markCount++
                                                }
                                            }
                                            if (markCountForMiddleMark == 0) {
                                                middleMark = 0f
                                            } else {
                                                middleMark = middleMark / (markCountForMiddleMark)
                                            }
                                            firebase.setFieldDiary(
                                                firebase.uidUser,
                                                "marks/lesson$lessonCount/semestr$semestrCount/middleMark",
                                                DoubleRounder.round(middleMark.toDouble(), 2)
                                            )
                                            semestrCount++
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
                        firebase.getRoleByUid(firebase.uidUser!!, object : Callback<String> {
                            override fun onComplete(answer: String) {
                                if (!end) {
                                    Toast.makeText(
                                        context,
                                        "При загрузке оценок произошла ошибка. Возможно, оценки еще не добавлены.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressBar.isVisible = false
                                    showButtons()
                                }
                                val ref = firebase.rootRef.child("users").child(answer)
                                    .child(firebase.uidUser).child("diary").child("marks")
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
                        })

                    }
                }
            })

    }
}