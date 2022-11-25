package spamdetector.zani.presenter

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import spamdetector.zani.contract.MainContract
import spamdetector.zani.dao.MyAsyncTask
import spamdetector.zani.model.*
import spamdetector.zani.model.data.*
import spamdetector.zani.util.*
import java.lang.Exception

class MainPresenter :BasePresenter<MainContract.View>,MainContract.Presenter{
    private var mainView: MainContract.View? = null
    private var settingModel = SettingModel()
    private var memberModel = MemberModel()
    private var clientModel = ClientModel()
    private var log = ArrayList<LogInfo>()
    private lateinit var context: Context

    override fun setRecentCallList(context: Context) {
        if(!settingModel.getTodayCallSwitchPref()) // 설정이 꺼진 경우 early return
        {
            mainView?.showRecentCallMsg("전화 문의보기 설정이 꺼져 있습니다")
            return
        }
        class RecentCallTask: MyAsyncTask<String, ArrayList<LogInfo>>() {
            override fun onPreExecute() {  }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun doInBackground(arg: String?): ArrayList<LogInfo>? {
                return CallLogManager(context).getLog()
            }
            override fun onPostExecute(result: ArrayList<LogInfo>?) {
                log = result!!
                when(result.size)
                {
                    0-> mainView?.showRecentCallMsg("문의 목록이 없습니다")
                    else -> mainView?.showRecentCallList(result)
                }
            }
        }
        RecentCallTask().execute("", null, null)
    }

    override fun doAutoLogin(context: Context) {
        val id = memberModel.getPhoneNumber()
        val pw = memberModel.getPwPref()
        if(id.isNotEmpty() && pw.isNotEmpty()) {
            val callback = { result: ResponseInfo ->
                if (result.code == 200) { // 자동 로그인 성공 시 전화번호부 비교
                    memberModel.saveLicenseDatePreference(result.msg)
                    memberModel.saveIdPreference(result.memberName!!)
                    memberModel.savePwPref(pw)
                    setRecentCallList(context)
                } else { // 라이센스 만료 시 자동 로그아웃
                    Toast.makeText(context, result.msg, Toast.LENGTH_SHORT).show()
                    doLogout()
                }
            }
            memberModel.requestLogin(MemberInfo(id, pw), callback as (ResponseInfo?) -> Unit)
        }
    }

    override fun doLogout() {
        val callback = {
            mainView?.showLogout()
            memberModel.saveIsLogin(false)
        }
        memberModel.requestLogout(callback as () -> Unit)
    }

    override fun getMemberInfo() {
        val memberInfo = MemberInfo(memberId = memberModel.getIdPref(), member_license = "${memberModel.getLicenseDatePrefByFormat()} (${memberModel.getLicenseDatePref()})")
        mainView?.showMemberInfo(memberInfo)
    }

    override fun doSearch(number: String) {
        val data = ArrayList<String>()
        data.add(number)

        val preCall = { mainView?.showLoading(LoadingDialog("고객 정보를 불러올게요")) }
        val callback = { result: ClientInfo? -> mainView?.showClientInfo(result) }
        clientModel.requestClientInfo(data, preCall as () -> Unit, callback as (ClientInfo?) -> Unit)
    }

    override fun getContext(context:Context) { this.context = context }

    override fun saveContacts(context: Context) {
        val phoneBook = ContactsManager(context).getContacts()
        val pre = {}
        val back = { result: String ->
            if (result == "network") {
                Toast.makeText(context, "네트워크 문제로 로그 저장에 실패하였습니다. 신호가 안정적인지 확인하여 주십시오.", Toast.LENGTH_SHORT).show()
                Preferences.setSet(PreferencesKey.CONTACTS, mutableSetOf())
            } else if (result == "other") {
                Toast.makeText(context, "서버 통신 문제로 로그 저장에 실패하였습니다", Toast.LENGTH_SHORT).show()
                Preferences.setSet(PreferencesKey.CONTACTS, mutableSetOf())
            }
            mainView?.dismissLoading()
            setRecentCallList(context)
        }
        clientModel.addClientInfoDivide(phoneBook, pre, back as (String?) -> Unit)
    }

    override fun saveContactsPref(context: Context) {
        class ContactTask: MyAsyncTask<String, String>() {
            override fun onPreExecute() {
                mainView?.showLoading(LoadingDialog("어플 보안상 최초 한 번만 진행합니다.\n잠시만 기다려주세요.\n(1~3분 소요예정)"))
            }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun doInBackground(arg: String?): String? {
                try {
                    ContactsManager(context).saveContactsPref()
                } catch (e: Exception) {
                    return ""
                }
                return "success"
            }
            override fun onPostExecute(result: String?) {
                if (result == "") {
                    Toast.makeText(context, "연락처 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                    mainView?.dismissLoading()
                    return
                }
                saveContacts(context)
            }
        }
        ContactTask().execute("", null, null)
    }

    override fun takeView(view: MainContract.View) { mainView = view }
    override fun dropView() { mainView = null }
}