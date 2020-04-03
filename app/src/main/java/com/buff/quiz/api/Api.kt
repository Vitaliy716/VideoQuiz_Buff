package com.buff.quiz.api

import retrofit2.Call
import com.buff.quiz.models.DefaultResponse
import com.buff.quiz.models.MyResultResponse
import retrofit2.http.*

interface Api {

    @FormUrlEncoded
    @POST("createuser")
    fun createUser(
        @Field("email") email:String,
        @Field("name") name:String,
        @Field("password") password:String,
        @Field("school") school:String
    ):Call<DefaultResponse>

    @GET("buffs/{id}")
    fun getQuiz(@Path("id") id: Int):Call<MyResultResponse>

}