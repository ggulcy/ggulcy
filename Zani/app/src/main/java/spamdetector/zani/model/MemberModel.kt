package spamdetector.zani.model

import org.json.JSONObject
import spamdetector.zani.dao.*
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.model.data.ResponseInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemberModel {
    fun requestLogin(
        memberInfo: MemberInfo,
        loginCallback: (ResponseInfo?) -> (Unit)
    ) // 서버로 login 요청 후 결과 값 반환
    {
        val data = JSONObject()
        data.put("id", memberInfo.memberId)
        data.put("pw", memberInfo.memberPw)

        val callback = { result: ResponseInfo? ->
            loginCallback(result)
        }
        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/member/login")
        list.add(data.toString())

        LoginTask().execute(list, null, callback)
    }
    fun requestLogout(logoutCallback: () -> (Unit)) {
        Preferences.setBoolean(PreferencesKey.autoLoginKey, false)
        logoutCallback()
    }

    fun saveIdPreference(memberId: String) { Preferences.setString(PreferencesKey.idKey, memberId) }
    fun getIdPref(): String { return Preferences.getString(PreferencesKey.idKey, "") }

    fun saveLicenseDatePreference(date: String) { Preferences.setString(PreferencesKey.licenseDateKey, date) }
    fun getLicenseDatePref(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val licenseDate = Preferences.getString(PreferencesKey.licenseDateKey, "")
        var licenseTime = dateFormat.parse(licenseDate)
        var today = Calendar.getInstance()
        var dDay = (today.time.time - licenseTime.time) / (60 * 60 * 24 * 1000)

        return "D$dDay"
    }
    fun getLicenseDatePrefByFormat(): String { return Preferences.getString(PreferencesKey.licenseDateKey, "") }

    fun saveAutoLoginPref(value: Boolean) { Preferences.setBoolean(PreferencesKey.autoLoginKey, value) }
    fun getAutoLoginPref(): Boolean { return Preferences.getBoolean(PreferencesKey.autoLoginKey, false) }

    fun savePwPref(pw: String) { Preferences.setString(PreferencesKey.pwKey, pw) }
    fun getPwPref(): String { return Preferences.getString(PreferencesKey.pwKey, "") }

    fun saveIsLogin(value: Boolean) { Preferences.setBoolean(PreferencesKey.loggedInKey, value) }
    fun getIsLogin(): Boolean { return Preferences.getBoolean(PreferencesKey.loggedInKey, false) }

    fun getPhoneNumber():String { return Preferences.getString(PreferencesKey.PHONE_NUMBER, "") }

    fun saveLastId(value: Int) { Preferences.setString(PreferencesKey.CONTACTS_ID, value.toString()) }
    fun getLastId(): Int { return Preferences.getString(PreferencesKey.CONTACTS_ID, "0").toInt() }
}