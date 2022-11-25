package spamdetector.zani.presenter

import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.BlockModel
import spamdetector.zani.model.data.BlockInfo

class BlockListPresenter : BlockContract.ListPresenter {
    private var listView:BlockContract.ListView? = null
    private var blockModel = BlockModel()

    override fun doGetBlockList() {
        val callback = { result: ArrayList<BlockInfo>? ->
            if(result?.size==0)
                listView?.showBlockMsg("차단 목록이 없습니다")
            listView?.showBlockList(result!!)
        }
        blockModel.requestMemberBlockList(callback as (ArrayList<BlockInfo>?) -> Unit)

    }

    override fun takeView(view: BlockContract.ListView) {
        listView = view
    }

    override fun dropView() {
       listView = null
    }

}