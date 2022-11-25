package spamdetector.zani.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import spamdetector.zani.R
import spamdetector.zani.model.LogModel
import spamdetector.zani.util.CallLogManager
import spamdetector.zani.util.Preferences

class ObserverService : Service() {
    companion object{
        private var isRunning = false
        fun isServiceRunning(): Boolean {
            return isRunning
        }
    }
    private lateinit var smsObserver: SmsObserver
    private lateinit var callObserver : CallObserver
    private lateinit var contactObserver: ContactObserver

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strId = getString(R.string.observer_channel_id)
            val strTitle = getString(R.string.app_name)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channel = notificationManager.getNotificationChannel(strId)
            if (channel == null) {
                channel = NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            val notification: Notification = NotificationCompat.Builder(this, strId).build()
            startForeground(2, notification)
        }

        Preferences.init(applicationContext)
        if(LogModel().getSmsLogId()==-1)        //저장 안되어 있으면 가져와서 저장해줌
            LogModel().setSmsLogId(CallLogManager(applicationContext).getSmsLogId())
        if(LogModel().getSmsLogId()==-1)        //저장 안되어 있으면 가져와서 저장해줌
            LogModel().setCallLogId(CallLogManager(applicationContext).getCallLogId())

        smsObserver = SmsObserver(Handler(Looper.getMainLooper()), applicationContext)
        callObserver = CallObserver(Handler(Looper.getMainLooper()), applicationContext)
        contactObserver = ContactObserver(Handler(Looper.getMainLooper()), applicationContext)

        if(!smsObserver.isRegistered)
        {
            applicationContext.contentResolver.registerContentObserver(Uri.parse("content://sms/"), true, smsObserver)     //sms observer register
            smsObserver.register()
        }
        if(!callObserver.isRegistered)
        {
            applicationContext.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObserver)     //sms observer register
            callObserver.register()
        }
        if(!contactObserver.isRegistered)
        {
            applicationContext.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactObserver)
            contactObserver.register()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!smsObserver.isRegistered)       //등록되어 있지 않을때만 등록
        {
            applicationContext.contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver)     //sms observer register
            smsObserver.register()
        }
        if(!callObserver.isRegistered)
        {
            applicationContext.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObserver)     //sms observer register
            callObserver.register()
        }
        if(!contactObserver.isRegistered)
        {
            applicationContext.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactObserver)
            contactObserver.register()
        }
        return START_REDELIVER_INTENT       //이전에 전달됐던 intent가 재전달
    }
}