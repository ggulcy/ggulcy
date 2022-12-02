package spamdetector.zani

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.text.*
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.telegram_guid.*
import spamdetector.zani.adapter.ClientLogListAdapter
import spamdetector.zani.adapter.RecentCallAdapter
import spamdetector.zani.contract.MainContract
import spamdetector.zani.model.LogModel
import spamdetector.zani.model.data.ClientInfo
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.presenter.MainPresenter
import spamdetector.zani.service.CallObserver
import spamdetector.zani.service.ContactObserver
import spamdetector.zani.service.ObserverService
import spamdetector.zani.service.SmsObserver
import spamdetector.zani.util.*
import spamdetector.zani.util.AnimationManager.Companion.animateMove
import spamdetector.zani.util.AnimationManager.Companion.animateResize
import java.lang.ref.WeakReference


class MainActivity : BaseActivity(), MainContract.View, LifecycleObserver {
    companion object {
        const val NEW_MEMBER = "NEW_MEMBER"
        lateinit var activity: Activity
    }

    private lateinit var connectivityManager : ConnectivityManager

    private lateinit var mainPresenter: MainContract.Presenter
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var phoneNum: String

    var searchToggle = 0
    private var distX = 0F
    private var distY = 0F
    var panelHeight = 0

    var first_time = 0L
    var second_time = 0L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startForegroundService(Intent(applicationContext, ObserverService::class.java))

        activity = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        window.setFlags(
//            // 소프트 키 투명화
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//        )
        

        setContentView(R.layout.activity_main)
        connectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        initPresenter()
        initPref()
        initView()
        mainPresenter.getContext(applicationContext)
        mainPresenter.setRecentCallList(applicationContext)
        panelHeight = sliding_layout.panelHeight
        mainPresenter.getMemberInfo()
        recent_call_list.addItemDecoration(RevDecoration(1, Color.BLACK))

        option_btn.setOnClickListener {
            main_drawer.openDrawer(right_drawer)
        }

        number_input.addTextChangedListener(
            UsPhoneNumberFormatter(
                WeakReference<EditText>(
                    number_input
                )
            )
        )
        number_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                search(number_input.text.toString())
            }
            false
        }

        main_drawer.setOnClickListener {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }

        sliding_layout.addPanelSlideListener(PanelEventListener())

        notice_btn.setOnClickListener {
            startActivity(Intent(applicationContext, NoticeActivity::class.java))
        }
        block_btn.setOnClickListener {
            startActivity(Intent(applicationContext, BlockActivity::class.java))
        }
        setting_btn.setOnClickListener {
            startActivity(Intent(applicationContext, SettingActivity::class.java))
        }
        logout_btn.setOnClickListener {
            mainPresenter.doLogout()
        }

        google.setOnClickListener {
            try {
                val hyphenNumber = number_input.text.toString().replace(")", "-")
                val browserIntent = Intent( // 구글 검색 실행
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.google.com/search?q=$hyphenNumber")
                )
                startActivity(browserIntent)
            } catch (e: Exception) {  // 만약 실행이 안된다면 (앱이 없다면)
                val intentPlayStore = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.google.android.googlequicksearchbox")
                ) // 설치 링크를 인텐트에 담아
                startActivity(intentPlayStore) // 플레이스토어로 이동
            }
        }
        facebook.setOnClickListener {
            val hyphenNumber = number_input.text.toString().replace(")", "-")
            UtilManager.copyToClip(this, hyphenNumber) // 클립보드에 전화번호 복사
            val i = packageManager.getLaunchIntentForPackage("com.facebook.katana")
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } else {
                val intentPlayStore = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.facebook.katana")
                )
                startActivity(intentPlayStore)
            }
        }
        kakao.setOnClickListener {
            val hyphenNumber = number_input.text.toString().replace(")", "-")
            UtilManager.copyToClip(this, hyphenNumber)
            val i = packageManager.getLaunchIntentForPackage("com.kakao.talk")
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } else {
                val intentPlayStore = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.kakao.talk")
                )
                startActivity(intentPlayStore)
            }
        }
        mail.setOnClickListener {
            val hyphenNumber = number_input.text.toString().replace(")", "-")
            val i = Intent(Intent.ACTION_SENDTO)
            i.data = Uri.parse("smsto:$hyphenNumber")
            startActivity(i);
        }
        call.setOnClickListener {
            val hyphenNumber = number_input.text.toString().replace(")", "-")
            startActivity(
                Intent(
                    "android.intent.action.CALL",
                    Uri.parse("tel:$hyphenNumber")
                )
            )
        }

        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 300
        animation.startOffset = 20
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = Animation.INFINITE

        expend_btn.startAnimation(animation)

        telegram_layout.setOnClickListener {
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
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith(
                "android.webkit."
            )
        ) {
            val scrcoords = IntArray(2)
            view.getLocationOnScreen(scrcoords)
            val x = ev.rawX + view.getLeft() - scrcoords[0]
            val y = ev.rawY + view.getTop() - scrcoords[1]
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) (this.getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager).hideSoftInputFromWindow(
                this.window.decorView.applicationWindowToken, 0
            )
        }
        return super.dispatchTouchEvent(ev)
    }
    override fun onBackPressed() {
        if (main_drawer.isDrawerOpen(right_drawer)) { // 오른쪽 패널이 열려있으면
            main_drawer.closeDrawer(right_drawer)
            return
        }
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (searchToggle == 1) {
            backToMain()
            return
        }

        second_time = System.currentTimeMillis()
        Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
        if (second_time - first_time < 2000) {
            super.onBackPressed();
            finishAffinity();
        }
        first_time = System.currentTimeMillis()
    }
    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        if (Preferences.getSet(PreferencesKey.CONTACTS, mutableSetOf()).isNullOrEmpty() || intent.getIntExtra(NEW_MEMBER, 0) == -1) // 삭제했다가 다시 깐 유저 or 새로운 유저
            mainPresenter.saveContactsPref(this)
        else if (Preferences.getBoolean(PreferencesKey.autoLoginKey, false))
            mainPresenter.doAutoLogin(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.dropView()
        try {
            UtilManager.unRegisterNetworkChangeListener(applicationContext, connectivityManager)
        }
        catch (e: Exception) { }
    }
    override fun onPause() {
        super.onPause()
    }


    override fun initPresenter() {
        mainPresenter = MainPresenter()
    }
    override fun initView() {
        mainPresenter.takeView(this)
    }
    override fun initPref() {
        Preferences.init(applicationContext)
    }


    override fun showLoading(dialog: LoadingDialog) {
        loadingDialog = dialog
        loadingDialog.show(supportFragmentManager, loadingDialog.tag)
    }
    override fun dismissLoading() {
        loadingDialog.dismiss()
    }


    override fun showRecentCallList(list: ArrayList<LogInfo>) {
        recent_call_list.adapter = RecentCallAdapter(list)
        recent_call_list.visibility = VISIBLE
        recent_call_progress.visibility = GONE
    }
    override fun showRecentCallMsg(msg: String) {
        recentCallMsg.text = msg
        recent_call_progress.visibility = GONE
    }
    override fun showLogout() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
    }
    override fun showMemberInfo(memberInfo: MemberInfo) {
        // 회원 정보 전달받기
        memberName.text = "업체명  :  " + memberInfo.memberId
        licenseDate.text = "제휴 만료일  :  " + memberInfo.member_license
    }
    override fun showClientInfo(clientInfo: ClientInfo?) {
        number_input.animate()
            .scaleX(0.5F)
            .scaleY(0.5F)
            .withEndAction {
                var editLocation = IntArray(2)
                number_input.getLocationOnScreen(editLocation)
                var mainLocation = IntArray(2)
                top_point.getLocationOnScreen(mainLocation)
                distX = -(editLocation[0] - mainLocation[0]).toFloat()
                distY = -(editLocation[1] - mainLocation[1]).toFloat()
                animateMove(number_input, distX, distY, 400L)

                app_icons.visibility = VISIBLE
                no_result_view.visibility = VISIBLE
                main_text.visibility = GONE
                sliding_layout.panelHeight = 0
                main_layout.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorGrayBackground
                    )
                )
                number_input.isFocusableInTouchMode = false
                number_input.isClickable = true
                number_input.clearFocus()
                number_input.setOnClickListener{
                    backToMain()
                }

                if (clientInfo == null) {
                    no_result_view.text = "아무것도 찾지 못했어요."
                    val array = UtilManager.getDeviceTodayCall(applicationContext, phoneNum)
                    client_calls_list.adapter = ClientLogListAdapter(this, array)
                }
                else if(clientInfo?.count==-1) {       //response code가 400일 경우, error 난 경우
                    no_result_view.text = "서버 내부 오류로 검색이 불가합니다."
                    val array = UtilManager.getDeviceTodayCall(applicationContext, phoneNum)
                    client_calls_list.adapter = ClientLogListAdapter(this, array)
                }else {
                    val sorted = if (clientInfo.count == 0) {
                        val array = UtilManager.getDeviceTodayCall(applicationContext, phoneNum)
                        val total = ArrayList<LogInfo>()
                        total.addAll(array)
                        total.addAll(clientInfo.logList!!)
                        UtilManager.sortLogs(total)
                    } else {
                        UtilManager.sortLogs(clientInfo.logList!!)
                    }
                    val count = SpannableString("검색한 결과입니다. (총 " + sorted.size + "건)")
                    count.setSpan(
                        RelativeSizeSpan(0.7f),
                        11,
                        count.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    no_result_view.text = count
                    client_calls_list.adapter = ClientLogListAdapter(this, sorted)
                }
                client_calls_list.visibility = VISIBLE
                loadingDialog.dismiss()
            }
            .start()

        searchToggle = 1
    }
    override fun search(str: String) {
        number_input.setText(str)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 내리기
        inputMethodManager.hideSoftInputFromWindow(number_input.windowToken, 0)

        phoneNum = UtilManager.changeToHyphenNumber(str.replace(")", "").replace("-", ""))
        mainPresenter.doSearch(phoneNum)
    }
    override fun closeBottomPanel() {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED)
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }


    private fun backToMain() {
        app_icons.visibility = INVISIBLE
        no_result_view.visibility = GONE
        main_text.visibility = VISIBLE

        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.HIDDEN)
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED

        sliding_layout.panelHeight = panelHeight
        number_input.isFocusableInTouchMode =true

        distX = 1 / -(distX / 100)
        distY = 1 / -(distY / 100)
        number_input.animate()
            .translationX(distX)
            .translationY(distY)
            .withEndAction {
                animateResize(number_input, 1.0F, 400L)

                main_text.text = "전화번호를 입력하세요."
                main_layout.setBackgroundColor(Color.WHITE)
                client_calls_list.adapter = ClientLogListAdapter(
                    this,
                    ArrayList()
                )
                number_input.isFocusableInTouchMode = true
                number_input.setOnClickListener {}
//                number_input.requestFocus() // 키패드 올리기
//                Selection.setSelection(number_input.text, number_input.length()) // 끝으로 포커스
//                var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.toggleSoftInput(
//                    InputMethodManager.SHOW_FORCED,
//                    InputMethodManager.HIDE_IMPLICIT_ONLY
//                )
            }
            .start()

        searchToggle = 0
    }


    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) { return }
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)  //열릴때
            {
                expend_btn.setImageResource(R.drawable.down_icon)
                textView2.visibility = VISIBLE
//                recent_call_list.visibility = VISIBLE
                recentCallMsg.visibility = VISIBLE
                return
            } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                expend_btn.setImageResource(R.drawable.up_icon)
                recent_call_list.smoothScrollToPosition(0)
                textView2.visibility = INVISIBLE
//                recent_call_list.visibility = INVISIBLE
                recentCallMsg.visibility = INVISIBLE
            }
        }
    }

    private fun setStatusOrNavigationBarColor(isOpenPanel:Boolean){
        //확장과 상관없이 statusBar 는 투명한색
        //패널 확장 여부에 따라 네비게이션바만 투명하게
        if(isOpenPanel){
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.WHITE
            }

            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.navigationBarColor = Color.TRANSPARENT

        }

    }


    private class UsPhoneNumberFormatter(private val mWeakEditText: WeakReference<EditText>): TextWatcher {
        //This TextWatcher sub-class formats entered numbers as 1 (123) 456-7890
        private var mFormatting // this is a flag which prevents the
                = false

        // stack(onTextChanged)
        private var clearFlag = false
        private var mLastStartLocation = 0
        private var mLastBeforeText: String? = null
        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
            if (after == 0 && s.toString() == "1 ") {
                clearFlag = true
            }
            mLastStartLocation = start
            mLastBeforeText = s.toString()
        }

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
            // TODO: Do nothing
        }

        override fun afterTextChanged(s: Editable) {
            // Make sure to ignore calls to afterTextChanged caused by the work
            // done below
            if (!mFormatting) {
                mFormatting = true
                val curPos = mLastStartLocation
                val beforeValue = mLastBeforeText
                val currentValue = s.toString()
                val formattedValue = formatUsNumber(s)
                if (currentValue.length > beforeValue!!.length) {
                    val setCursorPos = (formattedValue.length
                            - (beforeValue.length - curPos))
                    mWeakEditText.get()!!.setSelection(if (setCursorPos < 0) 0 else setCursorPos)
                } else {
                    var setCursorPos = (formattedValue.length
                            - (currentValue.length - curPos))
                    if (setCursorPos > 0 && !Character.isDigit(formattedValue[setCursorPos - 1])) {
                        setCursorPos--
                    }
                    mWeakEditText.get()!!.setSelection(if (setCursorPos < 0) 0 else setCursorPos)
                }
                mFormatting = false
            }
        }

        private fun formatUsNumber(text: Editable): String {
            val formattedString = StringBuilder()
            // Remove everything except digits
            var p = 0
            while (p < text.length) {
                val ch = text[p]
                if (!Character.isDigit(ch)) {
                    text.delete(p, p + 1)
                } else {
                    p++
                }
            }
            // Now only digits are remaining
            val allDigitString = text.toString()
            val totalDigitCount = allDigitString.length
            if (totalDigitCount == 0 || totalDigitCount > 12 && !allDigitString.startsWith("1") || totalDigitCount > 13) {
                // May be the total length of input length is greater than the
                // expected value so we'll remove all formatting
                text.clear()
                text.append(allDigitString)
                return allDigitString
            }
            var alreadyPlacedDigitCount = 0
            // Only '1' is remaining and user pressed backspace and so we clear
            // the edit text.
            if (allDigitString == "1" && clearFlag) {
                text.clear()
                clearFlag = false
                return ""
            }
            if (allDigitString.startsWith("1")) {
                formattedString.append("1 ")
                alreadyPlacedDigitCount++
            }
            // The first 3 numbers beyond '1' must be enclosed in brackets "()"
            if (totalDigitCount - alreadyPlacedDigitCount > 3) {
                formattedString.append(
                    allDigitString.substring(
                        alreadyPlacedDigitCount,
                        alreadyPlacedDigitCount + 3
                    ) + ")"
                )
                alreadyPlacedDigitCount += 3
            }
            // There must be a '-' updated after the last 1 number
            if (totalDigitCount == 11) {
                formattedString.append(
                    allDigitString.substring(
                        alreadyPlacedDigitCount, alreadyPlacedDigitCount + 4
                    ) + "-"
                )
                alreadyPlacedDigitCount += 4
            }
            // There must be a '-' inserted after the next 3 numbers
            else if (totalDigitCount - alreadyPlacedDigitCount > 3) {
                formattedString.append(
                    allDigitString.substring(
                        alreadyPlacedDigitCount,
                        alreadyPlacedDigitCount + 3
                    ) + "-"
                )
                alreadyPlacedDigitCount += 3
            }
            // All the required formatting is done so we'll just copy the
            // remaining digits.
            if (totalDigitCount > alreadyPlacedDigitCount) {
                formattedString.append(
                    allDigitString.substring(alreadyPlacedDigitCount)
                )
            }
            text.clear()
            text.append(formattedString.toString())
            return formattedString.toString()
        }
    }
}