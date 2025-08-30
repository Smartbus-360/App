package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LanguageViewModel(private val repository: PreferencesRepository) : ViewModel() {
//    fun setLanguage(language: String, context: Context) {
//        repository.setSelectedLanguage(language)
//        repository.setLocale(language, context) // Apply the locale change
//    }
//
//    fun viewLanguage(language: String, context: Context) {
////        repository.setSelectedLanguage(language)
//        repository.setLocale(language, context) // Apply the locale change
//    }


    fun completeLanguageSelection() {
        repository.setLangSelectCompleted()
    }

}