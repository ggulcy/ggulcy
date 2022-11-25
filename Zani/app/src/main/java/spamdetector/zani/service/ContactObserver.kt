package spamdetector.zani.service

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import spamdetector.zani.util.ContactsManager

class ContactObserver(handler: Handler?, var mContext: Context) : ContentObserver(handler) {
    var isRegistered = false

    fun register() { isRegistered = true }
    fun unregister() { isRegistered = false }

    var lastTimeOfCall = 0L
    var lastTimeOfUpdate = 0L
    var thresholdTime: Long = 3000

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        lastTimeOfCall = System.currentTimeMillis()
        if(lastTimeOfCall - lastTimeOfUpdate > thresholdTime) {
            ContactsManager(mContext).addContacts()
            lastTimeOfUpdate = System.currentTimeMillis()
        }
    }
}