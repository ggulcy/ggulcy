package spamdetector.zani.contract

import spamdetector.zani.model.data.NoticeInfo
import spamdetector.zani.presenter.BasePresenter

interface NoticeContract {
    interface View {
        fun showLoading()
        fun initList(list: ArrayList<NoticeInfo>)
    }
    interface Presenter : BasePresenter<View> {
        fun getNotice()
    }
}