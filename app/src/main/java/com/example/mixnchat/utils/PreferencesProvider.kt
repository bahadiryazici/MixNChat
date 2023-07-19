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

    fun putInt(key : String, value : Int ){
        sharedPreferences.edit().putInt(key,value).apply()
    }

    fun getInt(key: String,defaultValue:Int?=0) : Int{
        return sharedPreferences.getInt(key,defaultValue!!)
    }

    fun putFloat(key : String, value : Float ){
        sharedPreferences.edit().putFloat(key,value).apply()
    }
    fun getFloat(key: String,defaultValue:Float?=1f) : Float {
        return sharedPreferences.getFloat(key,defaultValue!!)
    }


    fun putString(key : String, value : String ){
        sharedPreferences.edit().putString(key,value).apply()
    }
    fun getString(key: String,defaultValue:String?="Def") : String? {
        return sharedPreferences.getString(key,defaultValue!!)
    }


    fun putLong(key : String, value : Long ){
        sharedPreferences.edit().putLong(key,value).apply()
    }
    fun getLong(key: String,defaultValue:Long?=1000000) : Long {
        return sharedPreferences.getLong(key,defaultValue!!)
    }
}