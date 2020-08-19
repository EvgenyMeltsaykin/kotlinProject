package com.diplom.kotlindiplom.diaries

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
open class Diary {
    val elschool = Elschool()
}