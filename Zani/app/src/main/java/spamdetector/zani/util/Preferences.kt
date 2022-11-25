package spamdetector.zani.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private lateinit var prefs : SharedPreferences

    fun init(context: Context)
    {
        prefs = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun getString(key:String, defaultId: String?): String
    {
        return prefs.getString(key, defaultId).toString();
    }

    fun setString(key:String, str:String)
    {
        prefs.edit().putString(key,str).apply()
    }

    fun getBoolean(key:String, defaultAuto: Boolean): Boolean
    {
        return prefs.getBoolean(key, defaultAuto);
    }

    fun setBoolean(key:String, bool: Boolean)
    {
        prefs.edit().putBoolean(key,bool).apply()
    }

    fun getSet(key: String, defaultSet: Set<String>?): Set<String>?
    {
        return prefs.getStringSet(key, defaultSet)
    }

    fun setSet(key: String, set: Set<String>)
    {
        prefs.edit().putStringSet(key, set).apply()
    }

    fun getInt(key:String) : Int
    {
        return prefs.getInt(key, -1)
    }

    fun setInt(key: String, value:Int)
    {
        prefs.edit().putInt(key, value).apply()
    }
}