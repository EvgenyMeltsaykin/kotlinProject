package com.diplom.kotlindiplom.diaries

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException

class Elschool  {
    val urlLogin = "https://elschool.ru/Logon/Index"
    val url = "elschool.ru"
    fun login(login: String, password: String): Boolean {
        val cookies: HashMap<String, String> = HashMap()
        var title = ""
        try {
            val document = Jsoup.connect(urlLogin)
                .data("login",login)
                .data("password",password)
                .data("GoogleAuthCode","")
                .cookies(cookies)
                .method(Connection.Method.POST)
                .userAgent("mozilla")
                .execute()

            title = document.parse().title()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return title == "Личный кабинет"

    }
}