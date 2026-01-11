package com.example.kababistanapp

import android.app.Application
import com.google.firebase.FirebaseApp

class KababistanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
