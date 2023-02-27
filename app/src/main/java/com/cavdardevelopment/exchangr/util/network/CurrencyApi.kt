package com.cavdardevelopment.exchangr.util.network

import com.cavdardevelopment.exchangr.model.response.ConvertRate
import com.cavdardevelopment.exchangr.model.response.FluctuationResponse
import com.cavdardevelopment.exchangr.model.response.LatestRateResponse
import com.cavdardevelopment.exchangr.model.response.SupportedSymbolsResponse
import com.cavdardevelopment.exchangr.util.network.deserializer.GetFluctuationDeserializer
import com.cavdardevelopment.exchangr.util.network.deserializer.GetLatestRatesDeserializer
import com.cavdardevelopment.exchangr.util.network.deserializer.GetSupportedSymbolsDeserializer
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

interface CurrencyApi {
    @GET("latest?places=2")
    fun getLatestRates(@Query("base") base: String): Call<LatestRateResponse>

//    @GET("latest?base=USD&symbols=TRY&places=2")
    @GET("latest?places=2")
    fun getLatestRates(@Query("base") base: String, @Query("symbols") symbols: String): Call<LatestRateResponse>

    @GET("convert?places=2")
    fun getConvertRates(@Query("from") from: String, @Query("to") to: String): Call<ConvertRate>

    @GET("symbols")
    fun getSupportedSymbols(): Call<SupportedSymbolsResponse>

//    /fluctuation?places=2&start_date=2023-01-10&end_date=2023-02-13&base=PLN&symbols=TRY
    @GET("fluctuation?places=2")
    fun getFluctuation(@Query("start_date") start_date: String, @Query("end_date") end_date: String,  @Query("base") base: String, @Query("symbols") symbols: String): Call<FluctuationResponse>
}

object RetrofitInstance {

    private const val baseUrl = "https://api.exchangerate.host/"

    private fun createGsonConverter(type: Type, typeAdapter: Any): Converter.Factory {
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(type, typeAdapter)
//        val gson = gsonBuilder.create()
//
//        return GsonConverterFactory.create(gson)
        return GsonConverterFactory.create(GsonBuilder().registerTypeAdapter(type, typeAdapter).create())
    }

    val apiFluctuation : CurrencyApi by lazy {
        Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(createGsonConverter(FluctuationResponse::class.java, GetFluctuationDeserializer()))
            .build()
            .create(CurrencyApi::class.java)
    }

    val apiSupportedSymbols : CurrencyApi by lazy {
        Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(createGsonConverter(SupportedSymbolsResponse::class.java, GetSupportedSymbolsDeserializer()))
            .build()
            .create(CurrencyApi::class.java)
    }

    val apiLatestRates : CurrencyApi by lazy {
        Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(createGsonConverter(LatestRateResponse::class.java, GetLatestRatesDeserializer()))
            .build()
            .create(CurrencyApi::class.java)
    }

    val api : CurrencyApi by lazy {
        Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApi::class.java)
    }
}