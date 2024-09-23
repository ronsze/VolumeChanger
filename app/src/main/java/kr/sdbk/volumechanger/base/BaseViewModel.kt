package kr.sdbk.volumechanger.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {
    protected fun<T> MutableStateFlow<T>.set(value: T) = viewModelScope.launch { emit(value) }
}