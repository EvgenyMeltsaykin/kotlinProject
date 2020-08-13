package com.diplom.kotlindiplom.diaries

import android.util.Log
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException

class Elschool {
    val urlLogin = "https://elschool.ru/Logon/Index"
    val urlDiary = "https://elschool.ru/users/diaries"
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

    fun getShedule(year:Int, week: Int, firebaseCallback: FirebaseCallback<MutableMap<String, List<String>>>) {
        val firebase = FunctionsFirebase()
        firebase.getLoginAndPasswordDiary(firebase.uidUser!!,
            object : FirebaseCallback<Map<String, String>> {
                override fun onComplete(value: Map<String, String>) {
                    GlobalScope.launch(Dispatchers.IO) {
                        var cookies: HashMap<String, String>
                        cookies = loginReturnCookies(
                            value["login"],
                            value["password"]
                        ) as HashMap<String, String>
                        if (cookies.isNullOrEmpty()) return@launch
                        val document = Jsoup.connect(urlDiary)
                            .cookies(cookies)
                            .get()
                        val sheduleHtml = Jsoup.connect("${document.baseUri()}&year=$year&week=$week&log=false")
                            .cookies(cookies)
                            .get()
                        val dayOfWeekHtml = sheduleHtml.select("tbody")
                        val shedule = mutableMapOf<String,List<String>>()
                        dayOfWeekHtml.forEach {it->
                            val day = it.select("td[class=diary__dayweek ]").select("p").text().toString().substringBefore(" ")
                            val items = it.select("div[class=d-flex position-relative]")
                            val lessons = mutableListOf<String>()
                            items.forEach {item->
                                lessons.add(item.select("div[class=flex-grow-1]").text())
                            }
                            shedule[day] = lessons
                        }
                        firebaseCallback.onComplete(shedule)
                    }
                }
            })

    }
}