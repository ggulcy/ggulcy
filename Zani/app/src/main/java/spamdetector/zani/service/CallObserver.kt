package spamdetector.zani.service

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import spamdetector.zani.model.LogModel
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.util.CallLogManager
import spamdetector.zani.util.UtilManager

class CallObserver(handler: Handler?, var mContext: Context) : ContentObserver(handler)  {
    var isRegistered = false

    fun register() { isRegistered = true }
    fun unregister() { isRegistered = false }

    val callback = { result: String? ->
        if (result == "network")
            Toast.makeText(mContext, "네트워크 문제로 로그 저장에 실패하였습니다. 신호가 안정적인지 확인하여 주십시오.", Toast.LENGTH_SHORT).show()
        else if (result == "other")
            Toast.makeText(mContext, "서버 통신 문제로 로그 저장에 실패하였습니다", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onChange(selfChange : Boolean)
    {
        val preferenceId = LogModel().getCallLogId()
        val id = CallLogManager(mContext).getCallLogId()
        if(preferenceId < id)        //preference로 저장된 아이디보다 받아온 아이디가 더 높을때만
        {
            LogModel().setCallLogId(id)      //preference update, 저장 실패한 것들은 그냥 넘기기 위해
            if(!UtilManager.checkNetwork(mContext)) //네트워크가 체크, 이 밖에 넣으면 여러번 실행됨
            {
                Toast.makeText(mContext, "네트워크 문제로 로그 저장에 실패하였습니다. 신호가 안정적인지 확인하여 주십시오.", Toast.LENGTH_SHORT).show()
                return
            }
            val log = CallLogManager(mContext).getLatestCallLog()
            if(!log.callNumber.isNullOrEmpty() and (log.delimiter != LogDelimiterEnum.default))
            {
                LogModel().saveLogs(logInfo = log, delimiter = LogDelimiterEnum.전화기록.ordinal, phoneNum = log.callNumber, callback= callback)
            }
        }
    }
}