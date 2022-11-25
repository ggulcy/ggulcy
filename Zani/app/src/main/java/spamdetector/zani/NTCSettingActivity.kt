package spamdetector.zani

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_ntc_setting.*
import spamdetector.zani.R

class NTCSettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ntc_setting)

        ntc_button.setOnClickListener {
            val intent = Intent()
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS";
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
            startActivity(intent)
            finish()
        }
    }
}