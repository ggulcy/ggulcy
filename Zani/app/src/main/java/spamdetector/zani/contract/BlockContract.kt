package spamdetector.zani.contract

import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.model.data.ResponseInfo
import spamdetector.zani.presenter.BasePresenter

interface BlockContract {
    interface View {
    }
    interface Presenter : BasePresenter<View> {
        fun findAll() : ArrayList<BlockInfo>
    }
//==============================================================
    interface RegisterView
    {
        fun showPref(type:String, pref: Boolean)
    }
    interface RegisterPresenter:BasePresenter<RegisterView>
    {
        fun savePref(type:String, pref:Boolean)
        fun showPref()
        fun specificBlockOn()
        fun specificBlockOff()
    }
//==============================================================
    interface ListView
    {
        fun showBlockList(array:ArrayList<BlockInfo>)
        fun showBlockMsg(msg:String)
    }
    interface ListPresenter :BasePresenter<ListView>
    {
        fun doGetBlockList()
    }
//==============================================================
    interface RecordView
    {
        fun showRecordList(array:ArrayList<BlockInfo>)
        fun showRecordMsg(msg:String)
    }
    interface RecordPresenter: BasePresenter<RecordView>
    {
        fun doGetRecordList()
    }
}