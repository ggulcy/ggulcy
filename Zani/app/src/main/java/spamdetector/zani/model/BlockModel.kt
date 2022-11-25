package spamdetector.zani.model

import android.os.Build
import android.telephony.PhoneNumberUtils
import androidx.annotation.RequiresApi
import org.json.JSONObject
import spamdetector.zani.dao.DeleteMemberBlockTask
import spamdetector.zani.dao.GetMemberBlockListTask
import spamdetector.zani.dao.GetMemberBlockRecordsTask
import spamdetector.zani.dao.SaveBlockOneTask
import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.model.data.ResponseInfo
import java.util.*
import kotlin.collections.ArrayList

class BlockModel {
    private var memberModel = MemberModel()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun requestInsertBlock(callback: (ResponseInfo?) -> (Unit), phoneNum:String)
    {
        val memberName = memberModel.getIdPref()
        if (memberName.length == 0)
        {
            return
        }
        val data = JSONObject()
        data.put("memberName", memberName)
        data.put("blockNumber", phoneNum)
        data.put("formatNumber", PhoneNumberUtils.formatNumber(phoneNum, Locale.getDefault().country))

        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/block/insert")

        list.add(data.toString())

        SaveBlockOneTask().execute(list, null, callback)
    }

    fun requestMemberBlockRecords(callback: (ArrayList<BlockInfo>?) -> Unit)
    {
        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/block/get/recordList")
        list.add(memberModel.getIdPref())

        GetMemberBlockRecordsTask().execute(list, null, callback)
    }

    fun requestMemberBlockList(callback: (ArrayList<BlockInfo>?) -> Unit)
    {
        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/block/get/blockList")
        list.add(memberModel.getIdPref())

        GetMemberBlockListTask().execute(list, null, callback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun requestDeleteBlock(callback: (ResponseInfo?) -> Unit, phoneNum:String)
    {
        val memberName = memberModel.getIdPref()
        if (memberName.length == 0)
        {
            return
        }

        val data = JSONObject()
        data.put("name", memberName)
        data.put("number", phoneNum)
        data.put("formatNumber", PhoneNumberUtils.formatNumber(phoneNum, Locale.getDefault().country))

        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/block/delete")
        list.add(data.toString())
        DeleteMemberBlockTask().execute(list, null, callback)
    }
}