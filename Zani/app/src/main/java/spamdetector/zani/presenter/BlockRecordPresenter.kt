package spamdetector.zani.presenter

import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.BlockModel
import spamdetector.zani.model.data.BlockInfo

class BlockRecordPresenter :  BlockContract.RecordPresenter
{
    private var recordView :BlockContract.RecordView? = null
    private var blockModel = BlockModel()

    override fun doGetRecordList() {
        val callback = { result: ArrayList<BlockInfo>? ->
            if(result?.size==0)
                recordView?.showRecordMsg("차단 기록이 없습니다")
            recordView?.showRecordList(result!!)
        }
        blockModel.requestMemberBlockRecords(callback as (ArrayList<BlockInfo>?) -> Unit)
    }

    override fun takeView(view: BlockContract.RecordView) {
        recordView = view
    }

    override fun dropView() {
        recordView = null
    }

}