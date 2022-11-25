package spamdetector.zani.model.data

data class LogInfo(
    var memberName: String="",
    var memberCategory: Int=0,
    var savedName: String="알수없음",
    var callNumber: String="",
    var date: String="",
    var time: String="",
    var delimiter: LogDelimiterEnum = LogDelimiterEnum.default,
    var callType : CallTypeEnum = CallTypeEnum.default
)