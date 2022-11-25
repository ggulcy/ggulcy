package spamdetector.zani.model

import spamdetector.zani.dao.GetClientTask
import spamdetector.zani.dao.SaveClientTaskDivide
import spamdetector.zani.model.data.ClientInfo
import spamdetector.zani.model.data.PhoneBookInfo

class ClientModel {
    fun requestClientInfo(data:ArrayList<String>, preCall: () -> Unit, callback: (ClientInfo?) -> Unit) {
        GetClientTask().execute(data, preCall, callback)
    }

    fun addClientInfoDivide(phoneBook: PhoneBookInfo, preCall: () -> Unit, callback: (String?) -> Unit) {
        SaveClientTaskDivide().execute(phoneBook, preCall, callback)
    }
}