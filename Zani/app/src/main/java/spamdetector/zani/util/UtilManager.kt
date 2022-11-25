package spamdetector.zani.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.PhoneLookup
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import spamdetector.zani.R
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class UtilManager { companion object {
    private const val standardHour = 8    //기준시간은 8시
    fun changeToHyphenNumber(number: String): String {
        var temp = number
        if (temp[0] == '+') {
            val len = temp.split(" ")[0].length
            if (temp.length > len)
                temp = temp.substring(len)
        }

        var nums = temp.replace(("[^\\d.]").toRegex(), "") // 숫자만 추출
        if (nums.isEmpty()) return number
        if (nums[0] == '1') {
            nums = "0$nums"
        }
        return PhoneNumberUtils.formatNumber(nums, Locale.getDefault().country)
//        if (nums.length == 9)
//            return nums.substring(0, 2) + "-" + nums.substring(2, 5) + "-" + nums.substring(5, 9)
//        if (nums.length == 10)
//            return nums.substring(0, 3) + "-" + nums.substring(3, 6) + "-" + nums.substring(6, 10)
//        if (nums.length == 11)
//            return nums.substring(0, 3) + "-" + nums.substring(3, 7) + "-" + nums.substring(7, 11)

//        return number
    }

    fun insertHyphen(str: String?): String {
        if (str!!.length == 3) { // 010 -> 010)
            return "$str)"
        } else if (str.length > 3) {
            var onlyNumber = str.replace(")", "").replace("-", "")

            if (onlyNumber.length == 6) return "$str-" // 010)000 -> 010)000-
            if (onlyNumber.length == 11) { // 010)000-00000 -> 010)0000-0000
                return "${onlyNumber.substring(0, 3)})${onlyNumber.substring(3, 7)}-${
                    onlyNumber.substring(
                        7
                    )
                }"
            }
        }

        return str
    }

    fun deleteHyphen(str: String?): String {
        var onlyNumber = str!!.replace(")", "").replace("-", "")
        if (onlyNumber.length == 10) { // 010)0000-000 -> 010)000-0000
            return "${onlyNumber.substring(0, 3)})${onlyNumber.substring(3, 6)}-${
                onlyNumber.substring(
                    6
                )
            }"
        } else if (onlyNumber.length == 6 && str.length == 7) { // 010)000 -> 010)00
            return str.substring(0, 6)
        } else if (str.length == 3) { // 010 -> 01
            return str.substring(0, 2)
        }

        return str
    }

    fun changeToCleanNumber(str: String): String {
        return str.replace(")", "-")
    }

    fun matchResourceId(category: Int): Int {
        return when (category) {
            0 -> R.drawable.category_gm
            1 -> R.drawable.category_uh
            2 -> R.drawable.category_kb
            3 -> R.drawable.category_rd
            4 -> R.drawable.category_am
            5 -> R.drawable.category_op
            6 -> R.drawable.category_tg
            7 -> R.drawable.category_ft
            8 -> R.drawable.category_hp
            9 -> R.drawable.category_hg
            10 -> R.drawable.category_bs
            else -> R.drawable.category_etc
        }
    }

    fun copyToClip(context: Context, string: String) {
        val clipboard: ClipboardManager =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("number", string)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "클립보드에 복사했습니다", Toast.LENGTH_SHORT).show()
    }

    fun getContactName(context: Context, phoneNumber: String?): String? {
        val cr = context.contentResolver
        val uri: Uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val cursor =
            cr.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)
                ?: return null
        var contactName: String? = null
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME))
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        return contactName
    }

    fun checkToday(dateString: String): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            val zoneId = ZoneId.of("Asia/Seoul")
            var covertToLocalDate = changeToLocalDateTime(dateString)
            var convertDate = covertToLocalDate!!.atZone(ZoneId.of("Asia/Seoul"))
            val now = LocalDateTime.now()
            if(isBeforeStandardTime())      //당일 0시~ 8시 사이면 전날 새벽 8시~현재 시간까지 true
            {
                val standard = now.atZone(zoneId).minusDays(1).with(LocalTime.of(8,0,0,0))
                return convertDate!!.isAfter(standard)
            }
            else                            //새벽 8시 이후이면 다음날 00시 이전까지 true
            {
                val today = now.atZone(zoneId).with(LocalTime.of(8,0,0,0))
                val tomorrow = now.atZone(zoneId).plusDays(1).with(LocalTime.of(0,0,0,0))
                return (convertDate!!.isBefore(tomorrow) and convertDate.isAfter(today))
            }
        }
       return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeToLocalDateTime(dateString: String) : LocalDateTime?
    {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isBeforeStandardTime() : Boolean        //아침 8시 이전 true, 아침 8시 이후 false
    {
        val today : LocalDateTime= LocalDateTime.now()
        val zoneTime = today.atZone(ZoneId.of("Asia/Seoul"))
        return ((zoneTime.hour < standardHour) and (zoneTime.hour >=0))
    }


    fun checkLicense(dateString: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd") // 오늘 문의인지 아닌지
        val licenseTime = dateFormat.parse(dateString)
        val today = Calendar.getInstance()
        val dDay = (today.time.time - licenseTime.time) / (60 * 60 * 24 * 1000)
        return dDay <= 0L
    }

    fun sortLogs(data: ArrayList<LogInfo>): ArrayList<LogInfo> {
        var tmp = ArrayList<LogInfo>()
        tmp.addAll(data)
        val cmp = compareByDescending<LogInfo> { SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${it.date} ${it.time}") }       //정렬후
        tmp = tmp.sortedWith(cmp).distinctBy { listOf(it.memberName, it.callNumber, it.delimiter) }.toCollection(ArrayList())

        var callOrMessage = ArrayList<LogInfo>()
        var notCallOrMessage = ArrayList<LogInfo>()

        for(i in tmp)
        {
            if((i.delimiter==LogDelimiterEnum.전화기록) or (i.delimiter==LogDelimiterEnum.문자기록))
            {
                callOrMessage.add(i)
            }
            else
                notCallOrMessage.add(i)
        }
        return (callOrMessage + notCallOrMessage).toCollection(ArrayList())
    }

    fun setSpannableString(view: TextView, context:Context)
    {
        val blackList = context.resources.getStringArray(R.array.black_list)
        val foreGroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(context,R.color.BlackListColor))
        val flag =  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

        for(i in blackList)
        {
            val spannableString = SpannableString(view.text.toString())
            val start = view.text.toString().indexOf(i)
            if(start>-1)       //문자열 있음
            {
                val end = view.text.toString().length
                spannableString.setSpan(foreGroundColorSpan, 0, end, flag)
                view.text = spannableString
            }
        }
    }
    fun getDeviceTodayCall(context:Context, number:String) : ArrayList<LogInfo>
    {
        var deviceLog=CallLogManager(context).getTodayLog(number.replace("-", ""))
        return sortLogs(deviceLog)

    }

    fun registerNetworkChangeListener(context:Context, connectivityManager: ConnectivityManager) { //네트워크가 끊기는것을 감지
        val callback = NetworkCallback
        callback.initContext(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            connectivityManager.registerDefaultNetworkCallback(callback)
        else
        {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, callback)
        }
    }

    fun unRegisterNetworkChangeListener(context:Context, connectivityManager: ConnectivityManager)
    {
        val callback = NetworkCallback
        callback.initContext(context)
        connectivityManager.unregisterNetworkCallback(callback)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetwork(context:Context) :Boolean     //네트워크가 끊겨있는지 검사
    {
        var result = false
        val connectivityManager = context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(capabilities!=null)
        {
            if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            {
                result = true
            }
        }
        return result
    }
    fun changeStringToDate(date: String, time: String) : Date?
    {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return input.parse("$date $time")
    }
} }