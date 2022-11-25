package spamdetector.zani.util

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONObject
import spamdetector.zani.dao.MyAsyncTask
import spamdetector.zani.model.ClientModel
import spamdetector.zani.model.MemberModel
import spamdetector.zani.model.data.OriginalPhoneInfo
import spamdetector.zani.model.data.PhoneBookInfo
import spamdetector.zani.util.UtilManager.Companion.changeToHyphenNumber

class ContactsManager(private val context: Context) {

    private var cursor: Cursor

    init {
        val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projections = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.RawContacts.VERSION
        )
        val sortOption = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " desc"

        cursor = context.contentResolver.query(phoneURI, projections, null, null, sortOption)!!
    }

    fun getContacts(): PhoneBookInfo {
        cursor.moveToFirst()
        cursor.moveToPrevious()
        if (!cursor.moveToNext())
            return PhoneBookInfo(null)

        val list = mutableSetOf<OriginalPhoneInfo>()
        var last = ""
        if (!cursor.isBeforeFirst) {
            cursor.moveToFirst()
            cursor.moveToPrevious()
        }
        while(cursor.moveToNext()) {
            if (cursor.isFirst) { // 첫 요소의 ID를 저장
                last = cursor.getString(2)
                Preferences.setString(PreferencesKey.CONTACTS_ID, last)
            }
            val name = cursor.getString(0)
            val number = changeToHyphenNumber(cursor.getString(1))
            if (number == "") continue
            // 개별 전화번호 데이터 생성
            val phoneBook = OriginalPhoneInfo(name, number)
            // 결과목록에 더하기
            list.add(phoneBook)
        }

        return PhoneBookInfo(list.toMutableList(), last.toInt())
    }

    fun processContact(lastId: Int): PhoneBookInfo {
        if (!cursor.moveToNext())
            return PhoneBookInfo(null)

        cursor.moveToFirst()
        return if (cursor.getString(2).toInt() > lastId) { // 추가인 경우
            cursor.moveToPrevious()
            addPhoneBook()
        } else { // 이름 변경인 경우
            cursor.moveToPrevious()
            updateContact()
        }
    }

    fun saveContactsPref() {
        val contacts = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val number = changeToHyphenNumber(cursor.getString(1))
            contacts.add("$number,$name")
        }
        Preferences.setSet(PreferencesKey.CONTACTS, contacts)
    }

    private fun updateContact(): PhoneBookInfo {
        val preContacts = Preferences.getSet(PreferencesKey.CONTACTS, mutableSetOf())?.toMutableSet()
        if (preContacts?.isEmpty()!!) return PhoneBookInfo(null)

        val list = mutableListOf<OriginalPhoneInfo>()
        var last = 0
        while (cursor.moveToNext()) {
            if (cursor.isFirst) {
                last = cursor.getString(2).toInt()
            }

            val name = cursor.getString(0)
            val number = changeToHyphenNumber(cursor.getString(1))
            if (number == "") continue

            if (!preContacts.contains("$number,$name")) {
                list.add(OriginalPhoneInfo(name, number))
                val numberOne = preContacts.filter { it.length >= number.length && it.substring(0, number.length) == number }
                val nameOne = preContacts.filter { it.length > number.length && it.substring(number.length+1) == name }
                preContacts.removeAll(numberOne)
                preContacts.removeAll(nameOne)
                preContacts.add("$number,$name")
                break
            }
        }

        Preferences.setSet(PreferencesKey.CONTACTS, preContacts)
        cursor.moveToFirst() // 공유 cursor 반납
        cursor.moveToPrevious()
        return PhoneBookInfo(list, last)
    }

    private fun addPhoneBook(): PhoneBookInfo {
        val preContacts = Preferences.getSet(PreferencesKey.CONTACTS, mutableSetOf())?.toMutableSet()
        if (preContacts?.isEmpty()!!) return PhoneBookInfo(null)

        val list = mutableListOf<OriginalPhoneInfo>()
        cursor.moveToNext()
        val name = cursor.getString(0)
        val number = changeToHyphenNumber(cursor.getString(1))
        if (number == "") return PhoneBookInfo(null)
        list.add(OriginalPhoneInfo(name, number))
        preContacts.add("$number,$name")

        Preferences.setSet(PreferencesKey.CONTACTS, preContacts)
        Preferences.setString(PreferencesKey.CONTACTS_ID, cursor.getString(2))
        val res = PhoneBookInfo(list, cursor.getString(2).toInt())

        cursor.moveToFirst() // 공유 cursor 반납
        cursor.moveToPrevious()
        return res
    }

    fun addContacts() {
        Preferences.init(context)
        class ContactTask: MyAsyncTask<String, PhoneBookInfo>() {
            override fun onPreExecute() {  }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun doInBackground(arg: String?): PhoneBookInfo? {
                return ContactsManager(context).processContact(MemberModel().getLastId())
            }
            override fun onPostExecute(result: PhoneBookInfo?) {
                if (result?.clientList != null) {
                    val preCall = { }
                    val callback = { result: String ->
                        if (result == "network")
                            Toast.makeText(context, "네트워크 문제로 로그 저장에 실패하였습니다. 신호가 안정적인지 확인하여 주십시오.", Toast.LENGTH_SHORT).show()
                        else if (result == "other")
                            Toast.makeText(context, "서버 통신 문제로 로그 저장에 실패하였습니다", Toast.LENGTH_SHORT).show()
                    }
                    ClientModel().addClientInfoDivide(result, preCall as () -> Unit, callback as (String?) -> Unit)
                }
            }
        }
        ContactTask().execute("", null, null)
    }
}