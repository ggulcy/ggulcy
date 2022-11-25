package spamdetector.zani.presenter

import spamdetector.zani.contract.SettingContract
import spamdetector.zani.model.SettingModel

class SettingPresenter:SettingContract.Presenter
{
    private var settingView: SettingContract.View? = null
    private var model : SettingModel = SettingModel()

    override fun saveShowTodayCallList(value: Boolean) {
        model.saveShowTodayCallList(value)
    }

    override fun saveShowPopUpPref(value: Boolean) {
        model.saveShowPopUpPref(value)
    }

    override fun showSwitchPref() {
        val todayCall = model.getTodayCallSwitchPref()
        val showPopUp = model.getShowPopUpPref()
        settingView?.showTodayCallPref(todayCall)
        settingView?.showPopupPref(showPopUp)
    }

    override fun takeView(view: SettingContract.View) {
        settingView=view
    }

    override fun dropView() {
        settingView=null
    }
}