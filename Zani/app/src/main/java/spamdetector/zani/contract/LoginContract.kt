package spamdetector.zani.contract

import spamdetector.zani.presenter.BasePresenter
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.model.data.ResponseInfo

interface LoginContract {
    interface View
    {
        fun showLoginResult(result : ResponseInfo)
        fun showAutoLoginPref(value:Boolean)
    }
    interface Presenter : BasePresenter<View>
    {
        fun doLogin(memberInfo:MemberInfo)  //login 실행
        fun saveAutoPref(value:Boolean)
        fun savePwPref(pw:String)
    }
}