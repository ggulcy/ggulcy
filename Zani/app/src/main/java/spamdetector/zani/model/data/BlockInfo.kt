package spamdetector.zani.model.data

data class BlockInfo (
    var phoneNum: String="",
    var identity: String="",
    var clientName:String="",
    var date:String ="",
    var blockCnt: Int=0
)