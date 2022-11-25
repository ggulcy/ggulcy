package spamdetector.zani.presenter

import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.data.BlockInfo

class BlockPresenter : BlockContract.Presenter {

    private var blockView: BlockContract.View? = null

    override fun findAll() : ArrayList<BlockInfo> {
        var list = ArrayList<BlockInfo>()
        return list
    }

    override fun takeView(view: BlockContract.View) {
        blockView = view
    }

    override fun dropView() {
        blockView = null
    }
}