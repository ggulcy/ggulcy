package spamdetector.zani.contract

import spamdetector.zani.presenter.BasePresenter

interface SettingContract {
    interface View {
        fun initPref()
        fun showTodayCallPref(value:Boolean)
        fun showPopupPref(value:Boolean)
        fun showImportanceValue()
        fun moveImportanceSetting()
    }
    interface Presenter : BasePresenter<View> {
        fun saveShowTodayCallList(value:Boolean)
        fun saveShowPopUpPref(value:Boolean)
        fun showSwitchPref()
    }
}