package spamdetector.zani.contract

import spamdetector.zani.presenter.BasePresenter
import android.content.Context
import spamdetector.zani.model.data.ClientInfo
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.model.data.MemberInfo
import spamdetector.zani.util.LoadingDialog

interface MainContract
{
    interface View
    {
        fun showLoading(dialog: LoadingDialog)
        fun dismissLoading()
        fun showClientInfo(clientInfo: ClientInfo?)
        fun showRecentCallList(list: ArrayList<LogInfo>)
        fun showRecentCallMsg(msg: String)
        fun showLogout()
        fun initPref()
        fun showMemberInfo(memberInfo: MemberInfo)
        fun search(str: String)
        fun closeBottomPanel()
    }
    interface Presenter : BasePresenter<View>
    {
        fun setRecentCallList(context: Context)
        fun doAutoLogin(context: Context)
        fun doLogout()
        fun getMemberInfo()
        fun doSearch(number: String)
        fun getContext(context:Context)
        fun saveContacts(context:Context)
        fun saveContactsPref(context: Context)
    }
}