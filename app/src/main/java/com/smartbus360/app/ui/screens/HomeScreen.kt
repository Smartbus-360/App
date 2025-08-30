package com.smartbus360.app.ui.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.smartbus360.app.navigation.NavGraphAfterOnboard
import com.smartbus360.app.viewModels.LanguageViewModel

@Composable
fun HomeScreen( languageViewModel: LanguageViewModel) {
    Column {
//        Text("Welcome to the Home Screen!")
    }
    NavGraphAfterOnboard()
}