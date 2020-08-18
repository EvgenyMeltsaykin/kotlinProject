package com.diplom.kotlindiplom.models

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.diplom.kotlindiplom.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FunctionsApi(val cityId: Int?) {
    val api = ApiService.create()
    val accessToken = "82ec654d82ec654d82ec654d1d829c76b5882ec82ec654ddcb339961b9dc274bcd1e19a"
    val versionVkApi = "5.103"
    fun getNodeCities(editText: AutoCompleteTextView, context: Context, cities:MutableList<City>){

        var citiesString: MutableList<String> = mutableListOf()
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
    fun getNodeSchools (editText: AutoCompleteTextView, context: Context, schools:MutableList<SchoolClass>, cityId : Int?){
        var schoolString: MutableList<String> = mutableListOf()
        if (cityId != -1) {
            var adapterEducational: ArrayAdapter<String>
            val response = api.searchSchool(
                com.diplom.kotlindiplom.childFragments.cityId,
                accessToken,
                versionVkApi,
                editText.text.toString()
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
                            adapterEducational = ArrayAdapter(
                                context,
                                android.R.layout.simple_list_item_1,
                                schoolString
                            )
                            editText.setAdapter(
                                adapterEducational
                            )
                            editText.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) editText.showDropDown() }
                        }
                    }, { error ->
                        error.printStackTrace()
                    }
                )
        }
    }
}