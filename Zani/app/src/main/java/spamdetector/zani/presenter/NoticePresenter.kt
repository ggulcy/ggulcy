package spamdetector.zani.presenter

import spamdetector.zani.contract.NoticeContract
import spamdetector.zani.model.NoticeModel
import spamdetector.zani.model.data.NoticeInfo

class NoticePresenter : BasePresenter<NoticeContract.View>, NoticeContract.Presenter {
    private var noticeView: NoticeContract.View? = null

    override fun getNotice() {
        var preCall = {
            noticeView?.showLoading()
        }
        var callback = { arr: ArrayList<NoticeInfo> ->
            noticeView?.initList(arr)
        }
        NoticeModel.requestNoticeList(preCall as () -> Unit, callback as (ArrayList<NoticeInfo>?) -> Unit)
    }

    override fun takeView(view: NoticeContract.View) {
        noticeView = view
    }

    override fun dropView() {
        noticeView = null
    }
}