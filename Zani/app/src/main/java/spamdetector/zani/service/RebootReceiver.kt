package spamdetector.zani.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import spamdetector.zani.model.LogModel
import spamdetector.zani.util.CallLogManager
import spamdetector.zani.util.Preferences

class RebootReceiver : BroadcastReceiver(){
    private lateinit var callReceiver: IncomingCallBroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, p1: Intent?) {
        callReceiver = IncomingCallBroadcastReceiver()

        val callFilter = IntentFilter()
        callFilter.addAction("android.intent.action.PHONE_STATE")

        if (!callReceiver.isRegistered) {
            context!!.applicationContext.registerReceiver(callReceiver, callFilter)
            callReceiver.register()
        }
        if(!ObserverService.isServiceRunning())
        {
            context!!.startForegroundService(Intent(context, ObserverService::class.java))
        }
    }
}