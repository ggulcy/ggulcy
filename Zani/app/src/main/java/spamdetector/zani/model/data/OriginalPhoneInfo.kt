package spamdetector.zani.model.data

data class OriginalPhoneInfo (
    override var name: String="",
    override var number: String=""
): PhoneInfo(name, number)