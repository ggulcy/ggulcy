package spamdetector.zani.model.data

data class ClientInfo (
    var logList: ArrayList<LogInfo>?=null,
    var count: Int=0,
    var member: LogInfo?=null,
    var date: String=""
)