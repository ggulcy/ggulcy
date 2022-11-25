package spamdetector.zani.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.annotation.RequiresApi
import spamdetector.zani.model.MemberModel
import spamdetector.zani.model.data.CallTypeEnum
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CallLogManager(val context: Context) {
    private val listCount: Int = 30

    private fun getNumberTodayCallLog(number: String) : ArrayList<LogInfo>
    {
        val data = ArrayList<LogInfo>()

        val managedCursor:Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.NUMBER + "= ? ",
            arrayOf(number),
            CallLog.Calls.DATE + " DESC"
        )

        val cachedNameIndex = managedCursor?.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val date = managedCursor?.getColumnIndex(CallLog.Calls.DATE)
        val type: Int? = managedCursor?.getColumnIndex(CallLog.Calls.TYPE)

        loop@while (managedCursor!!.moveToNext()) {
            val callDate = managedCursor.getLong(date!!);
            var cachedName: String? = managedCursor.getString(cachedNameIndex!!)
            if(cachedName==null)
                cachedName = "-"

            val date = Date(callDate)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString:String = format.format(callDate)
            val dateSplit = dateString.split(" ")
            if(!UtilManager.checkToday(dateString))     //오늘 전화가 아니면 break
                break@loop
            val type: String = managedCursor.getString(type!!)
            var callTypeEnum : CallTypeEnum = CallTypeEnum.수신
            when (type.toInt()) {
                CallLog.Calls.OUTGOING_TYPE -> callTypeEnum = CallTypeEnum.발신
                CallLog.Calls.INCOMING_TYPE -> callTypeEnum = CallTypeEnum.수신
                CallLog.Calls.MISSED_TYPE -> callTypeEnum = CallTypeEnum.부재
            }
            data.add(
                LogInfo(
                    memberName = MemberModel().getIdPref(),
                    savedName = cachedName.toString(),
                    callNumber = number,
                    date = dateSplit[0],
                    time = dateSplit[1],
                    delimiter = LogDelimiterEnum.전화기록,
                    callType = callTypeEnum
                )
            )
        }
        return data
    }
    private fun getNumberTodaySmsLog(number: String) : ArrayList<LogInfo>
    {
        val data = ArrayList<LogInfo>()

        val  managedCursor:Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            "address=?",
            arrayOf(number),
            "date DESC"
        )

        val dateIndex = managedCursor?.getColumnIndex(Telephony.Sms.DATE)
        val phoneNumIndex = managedCursor?.getColumnIndex(Telephony.Sms.ADDRESS)
        val typeIndex= managedCursor?.getColumnIndex(Telephony.TextBasedSmsColumns.TYPE);

        loop@while (managedCursor!!.moveToNext()) {
            val date: Long = managedCursor.getLong(dateIndex!!)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString:String = format.format(date)
            val dateSplit = dateString.split(" ")
            val protocol = managedCursor.getInt(typeIndex!!)
            var typeEnum : CallTypeEnum = CallTypeEnum.부재
            when(protocol)
            {
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT -> {
                    typeEnum = CallTypeEnum.발신
                }
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> {
                    typeEnum = CallTypeEnum.수신
                }
                else -> {
                    continue@loop
                }
            }
            if(!UtilManager.checkToday(dateString))
                break@loop
            var phoneNumber = managedCursor.getString(phoneNumIndex!!) //this is phone number rather than address
            val contact: String = getContactNameByNumber(phoneNumber)!! //call the metod that convert phone number to contact name in your contacts

            data.add(
                LogInfo(
                    memberName = MemberModel().getIdPref(),
                    savedName = contact,
                    callNumber = phoneNumber,
                    date = dateSplit[0],
                    time = dateSplit[1],
                    delimiter = LogDelimiterEnum.문자기록,
                    callType = typeEnum
                )
            )
        }
        return data
    }
    fun getTodayLog(number: String) : ArrayList<LogInfo>
    {
        val data = ArrayList<LogInfo>()
        val callLog = ArrayList<LogInfo>(getNumberTodayCallLog(number))
        val messageLog = ArrayList<LogInfo>(getNumberTodaySmsLog(number))

        data.addAll(callLog)
        data.addAll(messageLog)

        val cmp = compareByDescending<LogInfo> { UtilManager.changeStringToDate(it.date, it.time) }
        return data.sortedWith(cmp).toCollection(ArrayList())
    }

    fun getLog() : ArrayList<LogInfo> // 최근 문의 목록 50개 가져오기
    {
        val callCursor:Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )

        val uri = Uri.parse("content://sms/inbox")      //받은 문자만
        val  smsCursor:Cursor? = context.contentResolver.query(
            uri,
            arrayOf(Telephony.Sms.PERSON, Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.TYPE),
            null,
            null,
            "date DESC"
        )

        val data = ArrayList<LogInfo>()
        callCursor!!.moveToFirst()
        smsCursor!!.moveToFirst()
        var toggle = 0
        loop@while (data.size < listCount) { // merge sort 기반으로 양쪽 비교하며 Cursor 옮김
            if (callCursor.isAfterLast) { // call log 50개 이전에 소진
                toggle = 1
                break
            } else if (smsCursor.isAfterLast) { // sms log 50개 이전에 소진
                toggle = 2
                break
            }

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d1 = format.format(callCursor.getLong(2))
            val d2 = format.format(smsCursor.getLong(2))

            if (d1 >= d2) { // call log 1개 추가, 같으면 전화먼저 추가
                val phoneNum = callCursor.getString(1)
                if (phoneNum == (-8).toString() || phoneNum.length < 9) {
                    callCursor.moveToNext()
                    continue@loop
                }
                if(callCursor.getInt(3)==CallLog.Calls.OUTGOING_TYPE)       //발신 타입이면 다음으로 넘김
                {
                    callCursor.moveToNext()
                    continue@loop
                }
                var contact: String? = getContactNameByNumber(phoneNum)
                if(contact == null)
                    contact = "-"
                data.add(
                    LogInfo(
                        memberName = MemberModel().getIdPref(),
                        savedName = contact,
                        callNumber = phoneNum,
                        date = d1.split(" ")[0],
                        time = d1.split(" ")[1],
                        delimiter = LogDelimiterEnum.전화기록
                    )
                )
                callCursor.moveToNext()
            } else { // sms log 1개 추가
                val phoneNum = smsCursor.getString(1)
                if (!checkIsNumber(phoneNum) || phoneNum.length < 9) {
                    smsCursor.moveToNext()
                    continue@loop
                }
                val contact = getContactNameByNumber(phoneNum)
                data.add(
                    LogInfo(
                        memberName = MemberModel().getIdPref(),
                        savedName = "$contact",
                        callNumber = phoneNum,
                        date = d2.split(" ")[0],
                        time = d2.split(" ")[1],
                        delimiter = LogDelimiterEnum.문자기록
                    )
                )
                smsCursor.moveToNext()
            }
        }
        when (toggle) { // data가 덜 채워진 경우
            1 -> { // sms만 남음
                loop@ while (data.size < listCount) {
                    if (smsCursor.isAfterLast) break

                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val d2 = format.format(smsCursor.getLong(2)).split(" ")
                    val phoneNum = smsCursor.getString(1)
                    if (!checkIsNumber(phoneNum) || phoneNum.length < 9) {
                        smsCursor.moveToNext()
                        continue@loop
                    }
                    val contact = getContactNameByNumber(phoneNum)
                    data.add(
                        LogInfo(
                            memberName = MemberModel().getIdPref(),
                            savedName = contact,
                            callNumber = phoneNum,
                            date = d2[0],
                            time = d2[1],
                            delimiter = LogDelimiterEnum.문자기록
                        )
                    )
                    smsCursor.moveToNext()
                }
            }
            2 -> { // call만 남음
                loop@ while (data.size < listCount) {
                    if (callCursor.isAfterLast) break

                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val d1 = format.format(callCursor.getLong(2)).split(" ")
                    val phoneNum = callCursor.getString(1)
                    if (phoneNum == (-8).toString() || phoneNum.length < 9) {
                        callCursor.moveToNext()
                        continue@loop
                    }
                    var contact: String? = callCursor.getString(0)
                    if (contact == null)
                        contact = "-"
                    data.add(
                        LogInfo(
                            memberName = MemberModel().getIdPref(),
                            savedName = contact,
                            callNumber = phoneNum,
                            date = d1[0],
                            time = d1[1],
                            delimiter = LogDelimiterEnum.전화기록
                        )
                    )
                    callCursor.moveToNext()
                }
            }
        }
        callCursor.close()
        smsCursor.close()

        return data
    }
    private fun checkIsNumber(number: String) : Boolean
    {
        number.forEach{ i->
            if(!i.isDigit())    //숫자가 아님
                return false
        }
        return true
    }

    fun getContactNameByNumber(number: String): String {
        val uri :Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(
                number
            )
        );
        var displayName = "-"

        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null);
        val nameIndex = cursor?.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
        if(cursor != null){
            if(cursor.moveToFirst())
                displayName = cursor.getString(nameIndex!!)
            if(displayName.isEmpty())
                displayName = "-"
            cursor.close()
        }
        return displayName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getContactOne(number:String) : LogInfo
    {
        val managedCursor:Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.NUMBER + "= ? ",
            arrayOf(number),
            CallLog.Calls.DATE + " DESC"
        )

        val cachedNameIndex = managedCursor?.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val type: Int? = managedCursor?.getColumnIndex(CallLog.Calls.TYPE)

        loop@while (managedCursor!!.moveToNext()) {
            var cachedName: String? = managedCursor.getString(cachedNameIndex!!)
            if(cachedName==null)
                cachedName = "-"

            val type: String = managedCursor.getString(type!!)
            var callTypeEnum : CallTypeEnum = CallTypeEnum.default
            when (type.toInt()) {
                CallLog.Calls.REJECTED_TYPE -> callTypeEnum = CallTypeEnum.거절
                CallLog.Calls.MISSED_TYPE -> callTypeEnum = CallTypeEnum.부재
                else -> callTypeEnum = CallTypeEnum.수신
            }
            return LogInfo(
                memberName = MemberModel().getIdPref(),
                savedName = cachedName.toString(),
                delimiter = LogDelimiterEnum.전화기록,
                callType = callTypeEnum
            )
        }
        return LogInfo()
    }

    fun getSmsLogId() :Int
    {
        val uriSMS = Uri.parse("content://sms/")
        val cur: Cursor? = context.getContentResolver().query(uriSMS, null, null, null, null)
        return if(cur!!.moveToNext())
            cur.getString(cur.getColumnIndex("_id")).toInt()
        else
            -1
    }
    fun getLatestSmsLog() : LogInfo
    {
        val uriSMS = Uri.parse("content://sms/")
        val cur: Cursor? = context.contentResolver.query(uriSMS, null, null, null, null)
        if(cur!!.moveToNext()) // this will make it point to the first record, which is the last SMS sent
        {
            val callNumber = cur.getString(cur.getColumnIndex("address")) //phone num
            val protocol = cur.getString(cur.getColumnIndex("protocol")) //protocol
            var type = CallTypeEnum.default
            val savedName = getContactNameByNumber(callNumber)

            if (protocol == null)
                type = CallTypeEnum.발신
            else
                type = CallTypeEnum.수신

            return LogInfo(callNumber = callNumber, callType = type, memberName = MemberModel().getIdPref(), savedName = savedName.toString(), delimiter = LogDelimiterEnum.문자기록)
        }
        return LogInfo(callNumber = "", callType = CallTypeEnum.default, memberName = MemberModel().getIdPref(), savedName = "알수없음", delimiter = LogDelimiterEnum.문자기록)
    }

    fun getCallLogId() : Int
    {
        val cur:Cursor? = context.contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)
        return if(cur!!.moveToNext())
            cur.getString(cur.getColumnIndex(CallLog.Calls._ID)).toInt()
        else
            -1
    }

    fun getLatestCallLog() : LogInfo
    {
        val cur:Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.TYPE),
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )

        if(cur!!.moveToNext())
        {
            val callNumber = cur.getString(1)
            var clientName = "-"
            if(!cur.getString(0).isNullOrEmpty())
                clientName = cur.getString(0)
            val type = cur.getInt(2)
            var callType = CallTypeEnum.default
            when(type)
            {
                CallLog.Calls.INCOMING_TYPE -> callType = CallTypeEnum.수신
                CallLog.Calls.OUTGOING_TYPE -> callType = CallTypeEnum.발신
                CallLog.Calls.REJECTED_TYPE -> callType = CallTypeEnum.거절
                CallLog.Calls.MISSED_TYPE -> callType = CallTypeEnum.부재
                else -> return LogInfo(callType = CallTypeEnum.default)     // 이 외의 음성메일, 차단 등등도 log로 떠서 거르려고 default로 체크
            }
            return LogInfo(savedName = clientName, callNumber = callNumber, delimiter = LogDelimiterEnum.전화기록, callType = callType)
        }
        return LogInfo(callNumber = "", delimiter = LogDelimiterEnum.전화기록)
    }
}