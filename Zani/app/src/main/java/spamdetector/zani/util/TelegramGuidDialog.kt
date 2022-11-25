package spamdetector.zani.util

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import kotlinx.android.synthetic.main.dialog_telegram_guid.*
import spamdetector.zani.R

class TelegramGuidDialog :Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Preferences.init(applicationContext)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_telegram_guid)

        telegram_layout.setOnClickListener{
            UtilManager.copyToClip(this, uhDB.text.toString())
            try {
                startActivity(packageManager.getLaunchIntentForPackage("org.telegram.messenger"))
            } catch (e: Exception) {
                val intentPlayStore = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=org.telegram.messenger")
                )
                startActivity(intentPlayStore)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action==MotionEvent.ACTION_OUTSIDE)
        {
            return true
        }
        finish()
        return false
    }

    override fun onBackPressed() {
        finish()
    }
}