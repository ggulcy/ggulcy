package spamdetector.zani.model

import spamdetector.zani.dao.GetNoticeTask
import spamdetector.zani.model.data.NoticeInfo

class NoticeModel { companion object {
    fun requestNoticeList(preCall: (() -> Unit)?, callback: ((ArrayList<NoticeInfo>?) -> Unit)) {
        GetNoticeTask().execute("http://serverpc.iptime.org:8080/notice/all", preCall, callback)
    }
}}