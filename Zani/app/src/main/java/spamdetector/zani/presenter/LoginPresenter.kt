package spamdetector.zani.presenter

import spamdetector.zani.contract.LoginContract
import spamdetector.zani.model.MemberModel
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.model.data.ResponseInfo

class LoginPresenter : BasePresenter<LoginContract.View>,LoginContract.Presenter {

    private var loginView: LoginContract.View? = null
    private val memberModel = MemberModel()

    override fun doLogin(memberInfo: MemberInfo) {
        val callback = { result: ResponseInfo ->
            loginView?.showLoginResult(result)
            if (result.code == 200) {
                memberModel.saveLicenseDatePreference(result.msg)
                memberModel.saveIdPreference(result.memberName!!)
                memberModel.savePwPref(memberInfo.memberPw)
                memberModel.saveIsLogin(true)
                memberModel.saveLastId(result.new)
            }
            else
                memberModel.saveIsLogin(false)
        }
        memberModel.requestLogin(memberInfo, callback as (ResponseInfo?) -> Unit)
    }
    override fun saveAutoPref(value: Boolean) {
        memberModel.saveAutoLoginPref(value)
    }
    override fun savePwPref(pw: String) {
        memberModel.savePwPref(pw)
    }

    override fun takeView(view: LoginContract.View) {
        loginView = view
    }
    override fun dropView() {
        loginView = null
    }
}