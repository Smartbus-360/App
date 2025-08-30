package com.smartbus360.app.module

import android.content.Context
import androidx.room.Room
import com.smartbus360.app.data.database.AlertStatusRepository
import com.smartbus360.app.data.database.AppDatabase
import com.smartbus360.app.data.olaMaps.RouteViewModel
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.utility.NetworkMonitor
import com.smartbus360.app.viewModels.AdvertisementBannerViewModel
import com.smartbus360.app.viewModels.AlertStatusViewModel
import com.smartbus360.app.viewModels.BusLocationScreenViewModel
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.LoginViewModel
import com.smartbus360.app.viewModels.MainScreenViewModel
import com.smartbus360.app.viewModels.NetworkViewModel
import com.smartbus360.app.viewModels.NotificationViewModel
import com.smartbus360.app.viewModels.OnboardingViewModel
import com.smartbus360.app.viewModels.ReachDateTimeViewModel
import com.smartbus360.app.viewModels.RoleSelectionViewModel
import com.smartbus360.app.viewModels.SnappingViewModel
import com.smartbus360.app.viewModels.SplashViewModel
import com.smartbus360.app.viewModels.StudentScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { PreferencesRepository(androidContext()) }
//    single {  NetworkMonitor(androidContext()) }
    viewModel { SplashViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { RoleSelectionViewModel(get()) }
    viewModel { LoginViewModel(androidContext(), get()) }
    viewModel { MainScreenViewModel(get()) }
    viewModel { BusLocationScreenViewModel(get()) }
    viewModel { StudentScreenViewModel(get()) }
    viewModel { SnappingViewModel() }
    viewModel { RouteViewModel() }
    viewModel { AlertStatusViewModel(get())}
    viewModel { AdvertisementBannerViewModel(get()) }
    single { NetworkMonitor(get()) }
    viewModel { NetworkViewModel(get()) }
    viewModel {NotificationViewModel(get())}
    viewModel { ReachDateTimeViewModel(get()) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    single { get<AppDatabase>().alertStatusDao() }
}

val repositoryModule = module {
    single { AlertStatusRepository(get()) }
}