package com.diplom.kotlindiplom.models

import android.R
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.diplom.kotlindiplom.ApiService
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.models.apiResponse.cities.City
import com.diplom.kotlindiplom.models.apiResponse.schoolClass.SchoolClass
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FunctionsApi() {
    private val api = ApiService.create()
    private val accessToken =
        "82ec654d82ec654d82ec654d1d829c76b5882ec82ec654ddcb339961b9dc274bcd1e19a"
    private val versionVkApi = "5.124"
    fun getNodeCities(text: String, cities: MutableList<City>, callback: Callback<List<String>>) {

        val citiesString: MutableList<String> = mutableListOf()
        val response = api.searchCity(
            0,
            1,
            20,
            1,
            accessToken,
            versionVkApi,
            text
        )
        response.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                cities.clear()
                citiesString.clear()
                result.response?.items?.forEach {
                    cities.add(it)
                    citiesString.add(it.title.toString())
                }
                callback.onComplete(citiesString)

            }, { error ->
                error.printStackTrace()
            })
    }

    fun getNodeSchools(
        schools: MutableList<SchoolClass>,
        text: String,
        cityId: Int,
        callback: Callback<List<String>>
    ) {
        val schoolString: MutableList<String> = mutableListOf()
        if (cityId != -1) {
            val response = api.searchSchool(
                0,
                cityId,
                100,
                accessToken,
                versionVkApi,
                text
            )
            response.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { result ->
                        schools.clear()
                        schoolString.clear()
                        result.response?.items?.forEach {
                            schools.add(it)
                            schoolString.add(it.title.toString())
                        }
                        callback.onComplete(schoolString)
                    }, { error ->
                        error.printStackTrace()
                    }
                )

        }
    }
}