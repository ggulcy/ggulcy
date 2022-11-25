package spamdetector.zani

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import spamdetector.zani.util.NetworkCallback
import spamdetector.zani.util.NetworkCheckDialog
import spamdetector.zani.util.UtilManager

@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity()
{
    private var registerToggle = false
    private lateinit var connectivityManager : ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = checkNetwork()
        if(!result)
            return
        connectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        UtilManager.registerNetworkChangeListener(applicationContext, connectivityManager)
        registerToggle = true
    }
    override fun onResume()
    {
        super.onResume()
        try {
            val result = checkNetwork()
            if(!registerToggle)
            {
                UtilManager.registerNetworkChangeListener(applicationContext, connectivityManager)
                registerToggle = true
            }
        }
        catch(e:Exception)
        {
        }

    }
    override fun onPause() {
        super.onPause()
        try {
            if(registerToggle)
            {
                UtilManager.unRegisterNetworkChangeListener(applicationContext, connectivityManager)
                registerToggle = false
            }
        }
        catch(e:Exception)
        {
        }
    }

    abstract fun initPresenter()
    abstract fun initView()
    fun checkNetwork() :Boolean
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if(!UtilManager.checkNetwork(applicationContext))
            {
                startExitDialog()
                return false
            }
        }
        return true
    }
    private fun startExitDialog()
    {
        val intent = Intent(applicationContext, NetworkCheckDialog::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}