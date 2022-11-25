package spamdetector.zani

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import spamdetector.zani.contract.LoginContract
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.model.data.ResponseInfo
import spamdetector.zani.presenter.LoginPresenter
import spamdetector.zani.util.*


class LoginActivity : BaseActivity(), LoginContract.View {
    private lateinit var loginPresenter: LoginPresenter

    var first_time: Long = 0
    var second_time: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initPresenter()
        initView()

        loginButton.setOnClickListener {
            val id = idInput.text.toString()
            val pw = pwInput.text.toString()

            val memberInfo = MemberInfo(
                memberId = id,
                memberPw = pw,
            )
            if(id.isNotEmpty() &&pw.isNotEmpty())
            {
                loginPresenter.doLogin(memberInfo)
            }
            else{
                val message = "아이디, 비번을 입력해 주세요"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        auto_login_check.setOnCheckedChangeListener { compoundButton, b ->
            loginPresenter.saveAutoPref(b)
        }

        telegram_btn.setOnClickListener {
            startActivity(Intent(applicationContext, TelegramGuidDialog::class.java))
        }
    }
    override fun onBackPressed() {
        second_time = System.currentTimeMillis();
        Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
        if (second_time - first_time < 2000) {
            super.onBackPressed();
            finishAffinity();
        }
        first_time = System.currentTimeMillis();
    }
    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.dropView()
    }


    override fun initPresenter() { loginPresenter = LoginPresenter() }
    override fun initView() { loginPresenter.takeView(this) }


    override fun showLoginResult(result : ResponseInfo) {
        when(result.code)
        {
            200 ->
            {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra(MainActivity.NEW_MEMBER, result.new)
                startActivity(intent)
                finish()
            }
            else ->
            {
                val message =result.msg
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun showAutoLoginPref(value: Boolean) {
        auto_login_check.isChecked = value
    }
}