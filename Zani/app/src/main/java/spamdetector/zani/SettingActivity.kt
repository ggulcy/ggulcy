package spamdetector.zani

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_setting.*
import spamdetector.zani.util.Preferences
import spamdetector.zani.contract.SettingContract
import spamdetector.zani.presenter.SettingPresenter

class SettingActivity : BaseActivity(), SettingContract.View {
    private lateinit var presenter:SettingContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        Preferences.init(applicationContext)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, )


        initPresenter()
        presenter.showSwitchPref()
        showImportanceValue()

        today_question_switch.setOnCheckedChangeListener{_, isChecked->
            presenter.saveShowTodayCallList(isChecked)
        }
        popup_switch.setOnCheckedChangeListener{_, isChecked->
            presenter.saveShowPopUpPref(isChecked)
        }
        back_btn.setOnClickListener {
            finish()
        }
        move_importance_setting.setOnClickListener{
            moveImportanceSetting()
        }
    }
    override fun onBackPressed() {
        finish()
    }

    override fun initPresenter() {
        presenter=SettingPresenter()
        initView()
    }

    override fun initView() {
        presenter.takeView(this)
    }

    override fun initPref() {
        Preferences.init(applicationContext)
    }

    override fun showTodayCallPref(value: Boolean) {
        today_question_switch.isChecked = value
    }

    override fun showPopupPref(value: Boolean) {
        popup_switch.isChecked=value
    }

    override fun showImportanceValue() {
        val value = NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        if(value)
            alarm_importance_value.text = "ON"
        else
            alarm_importance_value.text = "OFF"


    }

    override fun moveImportanceSetting() {
        val intent = Intent();
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS";
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
        startActivity(intent)
    }

    override fun onResume() {
        showImportanceValue()
        super.onResume()
    }


    override fun onDestroy()
    {
        super.onDestroy()
        presenter.dropView()
    }
}