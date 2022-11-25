package spamdetector.zani

import android.Manifest
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import spamdetector.zani.dao.GetAuthTask
import spamdetector.zani.service.JobService
import spamdetector.zani.util.PermissionManager
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey


class SplashActivity : BaseActivity() {
    private lateinit var pm: PermissionManager
    private var jobScheduler : JobScheduler? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Preferences.init(applicationContext)

        pm = PermissionManager(this)
        pm.checkPermission()
        changedDialer()
        changedNTC()
        scheduleJob(applicationContext)
    }

    override fun initPresenter() {}

    override fun initView() {}

    // 권한 체크 비동기로 실행
    // 모든 권한 응답이 끝난 후 인증 과정 실행
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var number = Preferences.getString(PreferencesKey.PHONE_NUMBER, "")
        var tm: TelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && isDefault() && !(pm.checkNeedRequestPermission())
        ) {
            if (number.length < 10 || number.length > 16) {
                number = tm.line1Number
                if (number.startsWith("+82")) {
                    number = number.replace("+82", "0")
                }
                if (number.length < 10 || number.length > 15) {
                    Toast.makeText(applicationContext, "기기의 번호를 정상적으로 받아오지 못했습니다. 재시도 해주십시오.", Toast.LENGTH_LONG).show()
                    finishAffinity()
                }
                Preferences.setString(PreferencesKey.PHONE_NUMBER, number) // 기기에 기기전화번호 저장
            }
        } else {
            Toast.makeText(applicationContext, "권한을 모두 허용하지 않으면 이용할 수 없습니다", Toast.LENGTH_LONG).show()
            finishAffinity()
        }

        var callback = { result: String? ->
            if (result == "success") {
                if (Preferences.getBoolean(PreferencesKey.autoLoginKey, false)) // 자동로그인 중이라면
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                else startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else {
                Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                finishAffinity()
            }
        }
        GetAuthTask(this).execute(number, preCall = null, callback = callback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isDefault(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            return rm.isRoleHeld(RoleManager.ROLE_DIALER) && rm.isRoleAvailable(RoleManager.ROLE_DIALER)
        }

        val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val name = tm.defaultDialerPackage
        return name == packageName
    }

    private fun changedDialer() {
        if (intent.resolveActivity(packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm = getSystemService(ROLE_SERVICE) as RoleManager
                var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode != Activity.RESULT_OK) {
                        Toast.makeText(this, "[ 설정 ]에서 해당 앱을 기본 전화 앱으로 설정해주십시오.", Toast.LENGTH_SHORT).show()
                        finishAffinity()
                    }
                }
                resultLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                startActivityForResult(intent, 200)
            }
        } else {
            Toast.makeText(this, "요청이 실패하였습니다. [ 설정 ]에서 해당 앱을 기본 전화 앱으로 설정한 후 재시도 해주십시오.", Toast.LENGTH_SHORT).show()
            finishAffinity()
        }
    }

    private fun changedNTC() {
        val value = NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        if (value)
            startActivity(Intent(this, NTCSettingActivity::class.java))
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun scheduleJob(context: Context) {
        if (jobScheduler == null) {
            jobScheduler = context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        }
        val componentName = ComponentName(
            context,
            JobService::class.java
        )
        val jobInfo = JobInfo.Builder(
            1,
            componentName
        ) // setOverrideDeadline runs it immediately - you must have at least one constraint
            // https://stackoverflow.com/questions/51064731/firing-jobservice-without-constraints
            .setOverrideDeadline(100)
            .setPersisted(false).build()
        jobScheduler!!.schedule(jobInfo)
    }

}