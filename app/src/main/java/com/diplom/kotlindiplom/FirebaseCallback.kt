package com.diplom.kotlindiplom

interface FirebaseCallback<T> {

    fun  onComplete(value: T)

}