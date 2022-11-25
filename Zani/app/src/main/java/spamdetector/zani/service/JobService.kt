package spamdetector.zani.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager
import android.util.Log


class JobService : JobService() {
    private lateinit var callReceiver: IncomingCallBroadcastReceiver

    override fun onStartJob(p0: JobParameters?): Boolean {
        callReceiver = IncomingCallBroadcastReceiver()

        val callFilter = IntentFilter()
        callFilter.addAction("android.intent.action.PHONE_STATE")

        val smsFilter = IntentFilter()
        smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED")

        if (!callReceiver.isRegistered) {
            registerReceiver(callReceiver, callFilter)
            callReceiver.register()
        }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
//        if (callReceiver.isRegistered) {
//            unregisterReceiver(callReceiver)
//            callReceiver.unregister()
//        }
//        if (smsReceiver.isRegistered) {
//            unregisterReceiver(smsReceiver)
//            smsReceiver.unregister()
//        }
        return true
    }
}