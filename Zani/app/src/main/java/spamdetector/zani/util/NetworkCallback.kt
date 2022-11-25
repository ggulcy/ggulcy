package spamdetector.zani.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network

object NetworkCallback : ConnectivityManager.NetworkCallback() {
    private lateinit var context: Context
    fun initContext(context: Context)
    {
        this.context = context
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        val intent = Intent(context,NetworkCheckDialog::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
    }
}