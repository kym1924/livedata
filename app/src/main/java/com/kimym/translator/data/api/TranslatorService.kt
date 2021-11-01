package com.kimym.translator.data.api

import com.kimym.translator.data.entity.TranslatedText
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TranslatorService {
    @Headers("Authorization: KakaoAK ****")
    @GET("/v2/translation/translate")
    suspend fun translate(
        @Query("src_lang") src_lang: String,
        @Query("target_lang") target_lang: String,
        @Query("query") query: String
    ): TranslatedText
}
