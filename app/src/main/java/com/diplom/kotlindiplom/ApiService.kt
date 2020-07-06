package com.diplom.kotlindiplom

import com.diplom.kotlindiplom.models.CitiesResponse
import com.diplom.kotlindiplom.models.SearchCitiesResponse
import com.diplom.kotlindiplom.models.SearchCountryResponse
import com.diplom.kotlindiplom.models.SearchSchoolClassResponse
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("method/database.getCities")
    fun searchCity(
        @Query("country_id") countryId: Int,
        @Query("need_all") needAll: Int,
        @Query("access_token") accessToken: String,
        @Query("v") version: String,
        @Query("q") q: String): Observable<SearchCitiesResponse>

    @GET ("method/database.getCountries")
    fun searchCountry(
        @Query("need_all")needAll: Int,
        @Query("access_token") accessToken: String,
        @Query("v") version: String):Observable<SearchCountryResponse>

    @GET ("method/database.getSchools")
    fun searchSchool(
        @Query("city_id")cityId: Int?,
        @Query("access_token") accessToken: String,
        @Query("v") version: String,
        @Query("q") q: String):Observable<SearchSchoolClassResponse>


    companion object Factory {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.vk.com/")
                .build()

            return retrofit.create(ApiService::class.java);
        }
    }
}