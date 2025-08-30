package com.smartbus360.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.smartbus360.app.module.appModule
import com.smartbus360.app.module.databaseModule
import com.smartbus360.app.module.repositoryModule
//import com.smartbus360.app.module.speedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
//    companion object {
//        @SuppressLint("StaticFieldLeak")
//        lateinit var context: Context
//            private set
//    }

    override fun onCreate(){
//        super.onCreate()
////        context = applicationContext
//        startKoin {
//            androidContext(this@MyApplication)
//            modules(appModule)
//        }


            super.onCreate()

            if (GlobalContext.getOrNull() == null) {
                startKoin {
                    androidContext(this@MyApplication)
                    modules(appModule, databaseModule, repositoryModule, )
                }
            }


    }


}
