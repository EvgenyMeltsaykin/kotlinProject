package com.diplom.kotlindiplom.diaries

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException


class Elschool {
    val urlLogin = "https://elschool.ru/Logon/Index"
    val urlDiary = "https://elschool.ru/users/diaries"
    val cabinetText = "каб."
    val monday = "понедельник"
    val url = "elschool.ru"
    fun loginReturnCookies(login: String?, password: String?): MutableMap<String, String>? {
        var title = ""
        try {
            val document = Jsoup.connect(urlLogin)
                .data("login", login)
                .data("password", password)
                .data("GoogleAuthCode", "")
                .method(Connection.Method.POST)
                .userAgent("mozilla")
                .execute()

            title = document.parse().title()
            if (title == "Личный кабинет") return document.cookies()
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

            title = document.parse().title()
            Log.d("Tag",title)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return title == "Личный кабинет"
    }

    fun getShedule(year:Int, week: Int, firebaseCallback: FirebaseCallback<MutableMap<String, List<Lesson>>>) {
        val firebase = FunctionsFirebase()
        val shedule = mutableMapOf<String,List<Lesson>>()
        firebase.getLoginAndPasswordDiary(firebase.uidUser!!,
            object : FirebaseCallback<Map<String, String>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: Map<String, String>) {
                    GlobalScope.launch(Dispatchers.IO) {
                        var cookies: HashMap<String, String>
                        cookies = loginReturnCookies(
                            value["login"],
                            value["password"]
                        ) as HashMap<String, String>
                        if (cookies.isNullOrEmpty()) return@launch
                        try {
                            val document = Jsoup.connect(urlDiary)
                                .cookies(cookies)
                                .get()
                            val sheduleHtml = Jsoup.connect("${document.baseUri()}&year=$year&week=$week&log=false")
                                .cookies(cookies)
                                .get()
                            val dayOfWeekHtml = sheduleHtml.select("tbody")
                            dayOfWeekHtml.forEach {it->
                                //example понедельник 17.08
                                val dayDate = it.select("td[class=diary__dayweek ]").select("p").text().toString()
                                val items = it.select("tr[class=diary__lesson]")
                                val lessons = mutableListOf<Lesson>()
                                var cabinetAndTime = ""
                                items.forEach {item->
                                    var lesson = Lesson()
                                    lesson.name = item.select("div[class=flex-grow-1]").text()
                                    lesson.form = item.select("div[class=lesson-form]").text()
                                    cabinetAndTime = item.select("div[class=diary__discipline__time]").text()
                                    lesson.cabinet = getCabinet(cabinetAndTime)
                                    lesson.time = getTime(cabinetAndTime)
                                    lesson.homework = item.select("div[class=diary__homework-text]").text()
                                    lesson.mark = item.select("span[class=diary__mark]").text()
                                    if(lesson.name.isNotEmpty()){
                                        lessons.add(lesson)
                                    }
                                }
                                shedule[dayDate] = lessons
                            }
                            shedule.forEach { s, list ->
                                var i = 0
                                val day = s.substringBefore(" ")
                                val date = s.substringAfter(" ")
                                if(day.isNotEmpty()){
                                    list.forEach {
                                        i++
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/lessonName",it.name)
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/time",it.time)
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/cabinet",it.cabinet)
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/form",it.form)
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/homework",it.homework)
                                        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/lesson$i/mark",it.mark)
                                    }
                                    firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule/$day/date",date)
                                }
                            }
                            firebaseCallback.onComplete(shedule)
                        }catch (e:IOException){
                            firebaseCallback.onComplete(shedule)
                            e.printStackTrace()
                        }
                    }
                }
            })

    }

    private fun getTime(cabinetAndTime: String?): String {
        if (cabinetAndTime?.substringBefore(" ") == cabinetText){
            //example каб. 119 10:15 - 10:55
            return cabinetAndTime.substringAfter(" ").substringAfter(" ")
        }else{
            return cabinetAndTime!!
        }
        return ""
    }

    private fun getCabinet(cabinetAndTime: String?): String {
        if (cabinetAndTime?.substringBefore(" ") == cabinetText){
            //example каб. 119 10:15 - 10:55
            return cabinetAndTime.substringAfter(" ").substringBefore(" ")
        }else{
            return ""
        }
    }

    fun updateShedule(selectedYear:Int,selectedWeek:Int,context:Context){
        val diary = Diary()
        val firebase = FunctionsFirebase()
        Toast.makeText(context,"Подождите, идет загрузка расписания",Toast.LENGTH_SHORT).show()
        firebase.setFieldDatabase(firebase.uidUser!!,"diary/shedule","")
        diary.elschool.getShedule(selectedYear,selectedWeek,object : FirebaseCallback<MutableMap<String, List<Lesson>>>{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onComplete(value: MutableMap<String, List<Lesson>>) {
                GlobalScope.launch(Dispatchers.Main){
                    if(value.isNullOrEmpty()){
                        Toast.makeText(context,"Не удалось загрузить расписание",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context,"Раписание загружено успешно",Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }
}