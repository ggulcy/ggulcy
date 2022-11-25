package spamdetector.zani.util

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import kotlinx.android.synthetic.main.dialog_check_network.*
import spamdetector.zani.R
import kotlin.system.exitProcess

class NetworkCheckDialog : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Preferences.init(applicationContext)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_check_network)

        close_btn.setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onBackPressed() {
    }
}