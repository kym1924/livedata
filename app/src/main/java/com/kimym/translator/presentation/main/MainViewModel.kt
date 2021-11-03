package com.kimym.translator.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimym.translator.data.entity.Country
import com.kimym.translator.data.entity.Resource
import com.kimym.translator.data.repository.TranslatorRepositoryImpl
import com.kimym.translator.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val translatorRepositoryImpl: TranslatorRepositoryImpl
) : ViewModel() {

    val query = MutableLiveData<String>()

    private val _srcLang = MutableLiveData(Country.KOREA)
    val srcLang: LiveData<Country> = _srcLang

    private val _targetLang = MutableLiveData(Country.USA)
    val targetLang: LiveData<Country> = _targetLang

    private val _swapLang = MutableLiveData<Event<Boolean>>()
    val swapLang: LiveData<Event<Boolean>> = _swapLang

    private val _isSrcBottomSheet = MutableLiveData<Event<Boolean>>()
    val isSrcBottomSheet: LiveData<Event<Boolean>> = _isSrcBottomSheet

    private val _snackBar = MutableLiveData<Event<String>>()
    val snackBar: LiveData<Event<String>> = _snackBar

    val translated = translatedAsLiveData()

    val countryList = Transformations.map(isSrcBottomSheet) { value ->
        getCountryList(value.peekContent())
    }

    fun deleteQuery() {
        query.value = ""
    }

    private fun getCountryList(value: Boolean): MutableList<Country> {
        return translatorRepositoryImpl.getCountryList(
            when (value) {
                true -> requireNotNull(_targetLang.value)
                false -> requireNotNull(_srcLang.value)
            }
        )
    }

    fun setSwap() {
        swapQuery()
        swapLang()
    }

    private fun swapLang() {
        _swapLang.value = Event(true)
        val tempLang = _srcLang.value
        _srcLang.value = _targetLang.value
        _targetLang.value = tempLang
    }

    private fun swapQuery() {
        translated.value?.data?.let { translated ->
            query.value = translated
        }
    }

    fun openBottomSheet(value: Boolean) {
        _isSrcBottomSheet.value = Event(value)
    }

    fun setLanguage(country: Country) {
        when (_isSrcBottomSheet.value?.peekContent()) {
            true -> {
                _srcLang.value = country
                query.value = ""
            }
            false -> _targetLang.value = country
        }
    }

    fun setSnackBar(msg: String) {
        _snackBar.value = Event(msg)
    }

    private fun translatedAsLiveData(): MediatorLiveData<Resource<String>> {
        return MediatorLiveData<Resource<String>>().apply {
            var job: Job? = null
            addSource(query) { query ->
                job?.cancel()
                when (query.isBlank()) {
                    true -> {
                        translated.value = Resource.Empty("")
                    }
                    false -> {
                        translated.value = Resource.Loading()
                        job = translate()
                    }
                }
            }
            addSource(_targetLang) {
                when (query.value.isNullOrBlank()) {
                    false -> {
                        translated.value = Resource.Loading()
                        job = translate()
                    }
                }
            }
        }
    }

    private fun translate(): Job {
        return viewModelScope.launch {
            delay(500)
            val response = translatorRepositoryImpl.translate(
                requireNotNull(_srcLang.value).code,
                requireNotNull(_targetLang.value).code,
                requireNotNull(query.value)
            )
            translated.postValue(Resource.Success(response))
        }
    }
}
