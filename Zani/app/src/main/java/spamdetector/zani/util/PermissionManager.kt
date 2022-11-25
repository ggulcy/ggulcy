package spamdetector.zani.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {
    companion object {
        const val PERMISSION_READ_PHONE_STATE_CODE = 0x00000001
    }

    private val permissions = arrayListOf<String>()

    fun checkPermission() {
        var statePermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        )
        var logPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        )
        var foregroundPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.FOREGROUND_SERVICE
        )
        var alertPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )
        var answerPhonePermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        var contactsPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        )
        var smsPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        )
        var numbersPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_NUMBERS
        )
        var receiveSMSCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        )
        var callPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        )
        var internetStateCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        var bootCompleteCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        if (statePermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_PHONE_STATE)
        if (logPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_CALL_LOG)
        if (foregroundPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        if (alertPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        if (answerPhonePermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        if (contactsPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_CONTACTS)
        if (smsPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_SMS)
        if (numbersPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        if (receiveSMSCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.RECEIVE_SMS)
        if (callPermissionCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.CALL_PHONE)
        if(internetStateCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        if(bootCompleteCheck != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        if (permissions.size == 0) return

        if (checkNeedRequestPermission()) {
            // 이전에 거부한 경우 필요성 설명 + 권한 요청
//            Toast.makeText(context, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions.toTypedArray(),
                PERMISSION_READ_PHONE_STATE_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions.toTypedArray(),
                PERMISSION_READ_PHONE_STATE_CODE
            )
        }

        // 다른 앱 위에 표시 권한은 Android v11 부터 SYSTEM_ALERT_WINDOW 권한 이용 시 자동 허용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M  // 마시멜로우 이상일 경우
            && !Settings.canDrawOverlays(context)) {        // 오버레이 허용 체크
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivityForResult(intent, PERMISSION_READ_PHONE_STATE_CODE)
        }
    }

    fun checkNeedRequestPermission(): Boolean {
        for (p in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, p))
                return true
        }
        return false
    }
}