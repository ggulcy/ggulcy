package spamdetector.zani.model

import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey

class SettingModel
{
    fun saveShowTodayCallList(value:Boolean)
    {
        Preferences.setBoolean(PreferencesKey.ShowTodayCallListKey, value)
    }

    fun getTodayCallSwitchPref():Boolean = Preferences.getBoolean(PreferencesKey.ShowTodayCallListKey, true)

    fun saveShowPopUpPref(value:Boolean)
    {
        Preferences.setBoolean(PreferencesKey.showPopUpKey, value)
    }

    fun getShowPopUpPref():Boolean = Preferences.getBoolean(PreferencesKey.showPopUpKey, true)

    fun saveBlockPref(type:String, pref:Boolean)
    {
        Preferences.setBoolean(type, pref)
    }
    fun getBlockPref(type:String) :Boolean
    {
        return Preferences.getBoolean(type, false)
    }

}