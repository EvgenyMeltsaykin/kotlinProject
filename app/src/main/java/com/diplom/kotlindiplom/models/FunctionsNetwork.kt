package com.diplom.kotlindiplom.models

import android.content.Context
import android.net.ConnectivityManager

class FunctionsNetwork {

    fun checkConnect(context:Context?): Boolean{
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}