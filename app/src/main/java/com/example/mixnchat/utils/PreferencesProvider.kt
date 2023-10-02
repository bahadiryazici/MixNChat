package com.example.mixnchat.utils

import android.content.Context

class PreferencesProvider(context: Context) {
    private val sharedPreferences=context.getSharedPreferences(Constants.PREFERENCE_NAME,0)

    fun putBoolean(key : String, value : Boolean ){
        sharedPreferences.edit().putBoolean(key,value).apply()
    }
    fun getBoolean(key: String,defaultValue:Boolean?=false) : Boolean{
        return sharedPreferences.getBoolean(key,defaultValue!!)
    }
}