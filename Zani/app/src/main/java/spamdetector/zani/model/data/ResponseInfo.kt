package spamdetector.zani.model.data

data class ResponseInfo (
    var msg: String,
    var code: Int,
    var new: Int=0,
    var memberName: String?=""
)