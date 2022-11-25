package spamdetector.zani.presenter

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.BlockModel
import spamdetector.zani.model.MemberModel
import spamdetector.zani.model.SettingModel
import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.util.BlockManager
import spamdetector.zani.util.PreferencesKey

class BlockSettingPresenter(val context: Context) :BlockContract.RegisterPresenter {

    private var registerView: BlockContract.RegisterView? = null
    private var settingModel = SettingModel()
    private var memberModel = MemberModel()
    @RequiresApi(Build.VERSION_CODES.N)
    private var blockManager = BlockManager(context)
    private var blockModel = BlockModel()

    override fun showPref() {
        registerView?.showPref(
            "unknown",
            settingModel.getBlockPref(PreferencesKey.blockUnknownCallKey)
        )
        registerView?.showPref("today", settingModel.getBlockPref(PreferencesKey.blockTodayCallKey))
        registerView?.showPref(
            "specific",
            settingModel.getBlockPref(PreferencesKey.blockSpecificCallKey)
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun specificBlockOn() {
        val callback = { result: ArrayList<BlockInfo>? ->
            if(result!=null)
                for (i: Int in 0 until result!!.size)
                {
                    blockManager.insertBlock(result.get(i).phoneNum)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun specificBlockOff() {
        val callback = { result: ArrayList<BlockInfo>? ->
            if(result!=null)
                for (i: Int in 0 until result!!.size)
                {
                    blockManager.deleteBlock(result.get(i).phoneNum)
                }
        }
        blockModel.requestMemberBlockList(callback)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun savePref(type: String, pref: Boolean) {
        settingModel.saveBlockPref(type, pref)
        if(pref)
            specificBlockOn()
        else
            specificBlockOff()
    }

    override fun takeView(view: BlockContract.RegisterView) {
        registerView = view
    }

    override fun dropView() {
        registerView = null
    }
}