package com.checkeat.location.util

sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    class Render<T>(val data: T) : ScreenState<T>()
}