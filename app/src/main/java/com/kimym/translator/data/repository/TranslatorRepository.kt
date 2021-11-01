package com.kimym.translator.data.repository

import com.kimym.translator.data.entity.Country

interface TranslatorRepository {
    suspend fun translate(srcLang: String, targetLang: String, query: String): String

    fun getCountryList(country: Country): MutableList<Country>
}
