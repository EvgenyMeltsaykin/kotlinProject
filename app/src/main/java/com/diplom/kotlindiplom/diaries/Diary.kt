package com.diplom.kotlindiplom.diaries

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
open class Diary {
    val elschool = Elschool()

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun loginInDiary(urlDiary:String, login:String, password:String): Boolean {
        var rightLogin = false
        when (urlDiary) {
            elschool.url -> rightLogin =
                withContext(Dispatchers.IO) { elschool.login(login, password) }
        }
        return rightLogin
    }
}