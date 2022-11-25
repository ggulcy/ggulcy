package spamdetector.zani.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import spamdetector.zani.R
import spamdetector.zani.adapter.CallPopupAdapter
import spamdetector.zani.dao.MyAsyncTask
import spamdetector.zani.model.LogModel
import spamdetector.zani.model.MemberModel
import spamdetector.zani.model.SettingModel
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.model.data.ResponseInfo
import spamdetector.zani.util.CallLogManager
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey
import spamdetector.zani.util.UtilManager
import java.util.*
import kotlin.collections.ArrayList

class IncomingCallBroadcastReceiver: BroadcastReceiver() {

    private var memberModel = MemberModel()
    private var settingModel = SettingModel()

    var isRegistered = true
    var networkOn = true

    fun register() { isRegistered = true }
    fun unregister() { isRegistered = false }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!UtilManager.checkNetwork(context)) // 네트워크에 연결되지 않은 경우
            networkOn = false

        Preferences.init(context)
        if (!memberModel.getIsLogin()) // 로그인하지 않은 경우
            return
        val phoneNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (phoneNum.equals(null)) // 중복 실행 이슈 시, 첫 실행에 NULL 값이 반환됨을 처리 // 두 번째 실행(정상)
            return
        if (settingModel.getBlockPref(PreferencesKey.blockTodayCallKey)) // 오늘 문의 모두 차단
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager).endCall()
                return
            }

        val serviceIntent = Intent(context, CallingService::class.java)
        serviceIntent.putExtra("call_number", phoneNum.toString())
        when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
            TelephonyManager.EXTRA_STATE_RINGING -> { // 전화 울림
                if(!networkOn)
                    serviceIntent.putExtra("network_check", false)
                else
                    serviceIntent.putExtra("network_check", true)
                context.startForegroundService(serviceIntent)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> { // 전화 끊김(수신, 발신, 부재 여부 상관 없이)
                context.stopService(serviceIntent)
            }
        }
    }
}