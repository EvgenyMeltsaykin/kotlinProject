package com.diplom.kotlindiplom.models

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.diplom.kotlindiplom.ApiService
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.models.apiResponse.cities.City
import com.diplom.kotlindiplom.models.apiResponse.schoolClass.SchoolClass
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FunctionsApi() {
    private val api = ApiService.create()
    private val accessToken =
        "82ec654d82ec654d82ec654d1d829c76b5882ec82ec654ddcb339961b9dc274bcd1e19a"
    val versionVkApi = "5.103"
    fun getNodeCities(editText: AutoCompleteTextView, context: Context, cities: MutableList<City>) {

        val citiesString: MutableList<String> = mutableListOf()
        var adapterCity: ArrayAdapter<String>
        val response = api.searchCity(
            1,
            1,
            accessToken,
            versionVkApi,
            "${editText.text}"
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
                adapterCity = ArrayAdapter(
                    context,
                    R.layout.simple_list_item_1,
                    citiesString
                )
                editText.setAdapter(adapterCity)
                editText.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) editText.showDropDown() }
            }, { error ->
                error.printStackTrace()
            })
    }

    fun getNodeSchools(
        schools: MutableList<SchoolClass>,
        text:String,
        cityId: Int,
        callback: Callback<List<String>>
    ){
        val schoolString: MutableList<String> = mutableListOf()
        if (cityId != -1) {
            val response = api.searchSchool(
                cityId,
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