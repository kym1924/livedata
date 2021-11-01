package com.kimym.translator.data.entity

import com.google.gson.annotations.SerializedName

data class TranslatedText(
    @SerializedName("translated_text")
    val translatedText: List<List<String>>
)
