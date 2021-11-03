package com.kimym.translator.data.repository

import com.kimym.translator.data.api.TranslatorService
import com.kimym.translator.data.entity.Country
import javax.inject.Inject

class TranslatorRepositoryImpl @Inject constructor(
    private val translatorService: TranslatorService
) : TranslatorRepository {
    private val allCountryList by lazy { Country.values().toMutableList() }

    override suspend fun translate(
        srcLang: String,
        targetLang: String,
        query: String
    ): String {
        kotlin.runCatching {
            translatorService.translate(srcLang, targetLang, query)
        }.onSuccess { response ->
            return StringBuilder().apply {
                response.translatedText.forEach { translatedList ->
                    append(translatedList.joinToString(" "))
                    append("\n")
                }
                deleteCharAt(this.length - 1)
            }.toString()
        }
        return ""
    }

    override fun getCountryList(country: Country): MutableList<Country> =
        allCountryList.toMutableList().apply { remove(country) }
}
