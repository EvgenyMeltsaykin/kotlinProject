package com.diplom.kotlindiplom.diaries

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.models.ChildForElschool
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import com.diplom.kotlindiplom.models.FunctionsNetwork
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.w3c.dom.Document
import java.io.IOException

class Elschool {
    val urlLogin = "https://elschool.ru/Logon/Index"
    val urlDiary = "https://elschool.ru/users/diaries"
    val cabinetText = "каб."
    val url = "elschool.ru"
    fun loginReturnCookies(login: String?, password: String?): HashMap<String, String>? {
        var title = ""
        try {
            val document = Jsoup.connect(urlLogin)
                .data("login", login)
                .data("password", password)
                .data("GoogleAuthCode", "")
                .method(Connection.Method.POST)
                .userAgent("mozilla")
                .execute()
            val parseDoc = document.parse()
            title = parseDoc.title()
            if (title == "Личный кабинет") return document.cookies() as HashMap<String, String>
            else return null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    fun login(login: String, password: String): Boolean {
        val cookies: HashMap<String, String> = HashMap()
        var title = ""
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
                Log.d("Tag",id)
                return true
            }else{
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }
    fun deleteShedule() {
        val firebase = FunctionsFirebase()
        firebase.getRoleByUid(firebase.uidUser!!, object : FirebaseCallback<String> {
            override fun onComplete(answer: String) {
                var role = ""
                if (answer == "child") role = "children"
                else role = "parents"

                val ref = firebase.rootRef.child("users").child(role).child(firebase.uidUser!!)
                    .child("diary").child("shedule")
                ref.child("понедельник").setValue("")
                ref.child("вторник").setValue("")
                ref.child("среда").setValue("")
                ref.child("четверг").setValue("")
                ref.child("пятница").setValue("")
                ref.child("суббота").setValue("")
            }
        })
    }
    @ExperimentalStdlibApi
    fun getShedule(
        year: Int,
        week: Int,
        id: String = "",
        firebaseCallback: FirebaseCallback<MutableMap<String, List<Lesson>>>
    ) {
        val firebase = FunctionsFirebase()
        val shedule = mutableMapOf<String, List<Lesson>>()
        firebase.getLoginAndPasswordAndUrlDiary(firebase.uidUser!!,
            object : FirebaseCallback<Map<String, String>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: Map<String, String>) {
                    GlobalScope.launch(Dispatchers.IO) {

                        val cookies: HashMap<String, String>? = loginReturnCookies(
                            value["login"],
                            value["password"]
                        )
                        if (cookies == null) {
                            firebaseCallback.onComplete(shedule)
                            return@launch
                        }
                        try {
                            val document = Jsoup.connect(urlDiary)
                                .cookies(cookies)
                                .get()
                            val sheduleHtml: org.jsoup.nodes.Document
                            if (id.isEmpty()){
                                sheduleHtml = Jsoup.connect("${document.baseUri()}&year=$year&week=$week&log=false")
                                    .cookies(cookies)
                                    .get()
                            }else{
                                val urlWithoutId = document.baseUri().substringBeforeLast("=")
                                sheduleHtml = Jsoup.connect("${urlWithoutId}=$id&year=$year&week=$week&log=false")
                                    .cookies(cookies)
                                    .get()
                            }
                            val dayOfWeekHtml = sheduleHtml.select("tbody")
                            dayOfWeekHtml.forEach { it ->
                                //example понедельник 17.08
                                val dayDate =
                                    it.select("td[class=diary__dayweek ]").select("p").text()
                                        .toString()
                                val items = it.select("tr[class=diary__lesson]")
                                val lessons = mutableListOf<Lesson>()
                                var cabinetAndTime = ""
                                items.forEach { item ->
                                    var lesson = Lesson()
                                    lesson.name = item.select("div[class=flex-grow-1]").text()
                                    lesson.form = item.select("div[class=lesson-form]").text()
                                    cabinetAndTime =
                                        item.select("div[class=diary__discipline__time]").text()
                                    lesson.cabinet = getCabinet(cabinetAndTime)
                                    lesson.time = getTime(cabinetAndTime)
                                    lesson.homework =
                                        item.select("div[class=diary__homework-text]").text()
                                    lesson.mark = item.select("span[class=diary__mark]").text()
                                    if (lesson.name.isNotEmpty()) {
                                        lessons.add(lesson)
                                    }
                                }
                                shedule[dayDate] = lessons
                            }
                            deleteShedule()
                            shedule.forEach { s, list ->
                                var i = 0
                                val day = s.substringBefore(" ")
                                val date = s.substringAfter(" ")
                                if (day.isNotEmpty()) {
                                    list.forEach {
                                        i++
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/lessonName",
                                            it.name
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/time",
                                            it.time
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/cabinet",
                                            it.cabinet
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/form",
                                            it.form
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/homework",
                                            it.homework
                                        )
                                        firebase.setFieldUserDatabase(
                                            firebase.uidUser!!,
                                            "diary/shedule/$day/lesson$i/mark",
                                            it.mark
                                        )
                                    }
                                    firebase.setFieldUserDatabase(
                                        firebase.uidUser!!,
                                        "diary/shedule/$day/date",
                                        date
                                    )
                                }
                            }
                            firebaseCallback.onComplete(shedule)
                        } catch (e: IOException) {
                            firebaseCallback.onComplete(shedule)
                            e.printStackTrace()
                        }
                    }
                }
            })

    }
    fun getChildrenFromFirebase(firebaseCallback: FirebaseCallback<List<ChildForElschool>>){
        val firebase = FunctionsFirebase()
        val ref = firebase.parentRef.child(firebase.uidUser!!).child("diary").child("children")
        val children = mutableListOf<ChildForElschool>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    p0.children.forEach {
                        val childForElschool = ChildForElschool()
                        it.children.forEach{ childInfo->
                            if (childInfo.key.toString() == "id"){
                                childForElschool.id = childInfo.value.toString()
                            }
                            if (childInfo.key.toString() == "name"){
                                childForElschool.name = childInfo.value.toString()
                            }
                        }
                        children.add(childForElschool)
                    }
                    firebaseCallback.onComplete(children)
                }else{
                    firebaseCallback.onComplete(children)
                }
            }

        })
    }
    @ExperimentalStdlibApi
    fun writeChildrenDiaryInFirebase(firebaseCallback: FirebaseCallback<Boolean>){
        val firebase = FunctionsFirebase()
        val children = mutableListOf<ChildForElschool>()
        firebase.getLoginAndPasswordAndUrlDiary(firebase.uidUser!!,
            object : FirebaseCallback<Map<String, String>> {
                override fun onComplete(value: Map<String, String>) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val cookies: HashMap<String, String>? = loginReturnCookies(
                            value["login"],
                            value["password"]
                        )
                        if (cookies == null) {
                            firebaseCallback.onComplete(false)
                            return@launch
                        }
                        try {
                            val document = Jsoup.connect("https://$url")
                                .cookies(cookies)
                                .get()
                            val childBlockHtml = document.select("div[class=d-flex align-items-center]")
                            childBlockHtml.forEach {
                                val nameChildHtml = it.select("p[class=flex-grow-1]")
                                nameChildHtml.forEach {
                                    val nameChild = it.text()
                                    val urls = it.getElementsByTag("a")
                                    var id = ""
                                    urls.forEach {
                                        id =it.attr("href").substringAfterLast("/")
                                        Log.d("Tag",nameChild)
                                        Log.d("Tag",id)
                                    }
                                    val childForElschool = ChildForElschool(nameChild,id)
                                    children.add(childForElschool)
                                }
                            }
                            firebase.setFieldDiary(firebase.uidUser!!,"children","")
                            var i = 0
                            children.forEach {
                                i++
                                firebase.setFieldDiary(firebase.uidUser!!,"children/child$i/id",it.id)
                                firebase.setFieldDiary(firebase.uidUser!!,"children/child$i/name",it.name)
                            }
                            firebaseCallback.onComplete(true)
                        }catch (e:IOException){
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
        return ""
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
    fun updateShedule(
        id:String = "",
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
        Log.d("Tag",id)
        Toast.makeText(context, "Подождите, идет загрузка расписания", Toast.LENGTH_SHORT).show()
        progressBar.isVisible = true
        hideButtons()
        getShedule(
            selectedYear,
            selectedWeek,
            id,
            object : FirebaseCallback<MutableMap<String, List<Lesson>>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: MutableMap<String, List<Lesson>>) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(2000)
                        progressBar.isVisible = false
                        showButtons()
                        if (value.isNullOrEmpty()) {
                            Toast.makeText(
                                context,
                                "Не удалось загрузить расписание. Авторизуйтесь снова, если Вы недавно сменили пароль ",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Раписание загружено успешно",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            })
    }

    @ExperimentalStdlibApi
    fun getMarks(idChild :String = ""){
        val firebase = FunctionsFirebase()

        firebase.getLoginAndPasswordAndUrlDiary(firebase.uidUser!!,object : FirebaseCallback<Map<String,String>>{
            override fun onComplete(value: Map<String, String>) {
                GlobalScope.launch(Dispatchers.IO) {
                    val cookies: HashMap<String, String>? = loginReturnCookies(
                        value["login"],
                        value["password"]
                    )

                    try {
                        val document = Jsoup.connect(urlDiary)
                            .cookies(cookies)
                            .get()
                        val sheduleHtml: org.jsoup.nodes.Document
                        if (idChild.isEmpty()) {
                            sheduleHtml =
                                Jsoup.connect(document.baseUri())
                                    .cookies(cookies)
                                    .get()
                        } else {
                            val urlWithoutId = document.baseUri().substringBeforeLast("=")
                            sheduleHtml =
                                Jsoup.connect("${urlWithoutId}=$idChild")
                                    .cookies(cookies)
                                    .get()
                        }
                        val gradeUrl = sheduleHtml.baseUri().replace("details","gradesandabsences")
                        val gradeHtml = Jsoup.connect(gradeUrl)
                            .cookies(cookies)
                            .get()
                        val gradeTable = gradeHtml.select("div[class=DivForGradesAndAbsencesTable]").select("tbody")
                        var lessonCount = 1
                        gradeTable.forEach {
                            var lessonHtml = it.select("tr[lesson=\"$lessonCount\"]")
                            while (lessonHtml.isNotEmpty()){
                                lessonHtml = it.select("tr[lesson=\"$lessonCount\"]")
                                lessonHtml.forEach {
                                    val lessonName = it.select("td[class=gradesaa-lesson]").text()
                                    val gradesMark = it.select("td[class=gradesaa-marks]")
                                    var semestrCount = 1
                                    Log.d("Tag",lessonName)
                                    gradesMark.forEach {marksHtml->
                                        Log.d("Tag","Semestr $semestrCount")
                                        val marks = marksHtml.select("span[class=mark-span]")
                                        //Log.d("Tag",marks.text())
                                        var markCount = 1
                                        marks.forEach {
                                            if(it.text().isNotEmpty()){
                                                Log.d("Tag","${it.text()} ${it.attr("data-popover-content").substringBefore("<p>").substringAfterLast(" ")}")
                                                val value = it.text()
                                                val date = it.attr("data-popover-content").substringBefore("<p>").substringAfterLast(" ")
                                                firebase.setFieldDiary(firebase.uidUser!!,"marks/lesson$lessonCount/lessonName",lessonName)
                                                firebase.setFieldDiary(firebase.uidUser!!,"marks/lesson$lessonCount/semestr$semestrCount/mark$markCount/date",date)
                                                firebase.setFieldDiary(firebase.uidUser!!,"marks/lesson$lessonCount/semestr$semestrCount/mark$markCount/value",value)
                                                markCount++
                                            }

                                        }
                                        semestrCount++
                                    }
                                }
                                lessonCount++
                            }
                        }

                    }catch (e:IOException){
                        e.printStackTrace()
                    }

                }
            }
        })
    }
}