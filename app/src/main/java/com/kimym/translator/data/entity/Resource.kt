package com.kimym.translator.data.entity

sealed class Resource<T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null
) {
    class Empty<T>(data: T? = null) : Resource<T>(Status.EMPTY, data)
    class Success<T>(data: T) : Resource<T>(Status.SUCCESS, data)
    class Loading<T>(data: T? = null) : Resource<T>(Status.LOADING, data)
}

enum class Status {
    EMPTY,
    SUCCESS,
    LOADING
}
