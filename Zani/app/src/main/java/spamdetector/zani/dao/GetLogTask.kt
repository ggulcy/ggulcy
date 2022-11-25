package spamdetector.zani.dao

import android.util.Log
import org.json.JSONObject
import spamdetector.zani.model.data.CallTypeEnum
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.util.UtilManager
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GetLogTask : MyAsyncTask<ArrayList<String>, Any>() {

    override fun onPreExecute() {
        preCall?.let { it() }
    }

    override fun doInBackground(arg: ArrayList<String>?): Any? {
        try
        {
            var con: HttpURLConnection? = null
            var reader: BufferedReader? = null

            val url = URL(arg?.get(0))
            con = url.openConnection() as HttpURLConnection?

            if (con == null) {
                return null
            }

            con.requestMethod = "POST"
            con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "text/json")
            con.doInput = true
            con.connect()

            val outStream: OutputStream = con.outputStream
            val writer: BufferedWriter = BufferedWriter(OutputStreamWriter(outStream))
            writer.write(arg?.get(1))
            writer.flush()
            writer.close()

            val stream: InputStream = con.inputStream
            reader = BufferedReader(InputStreamReader(stream))
            val builder = StringBuilder()
            var line: String? = ""
            while (reader.readLine().also { line = it } != null)
            {
                builder.append(line)
            }
            if(con.responseCode==400)
            {
                return "error"
            }
            else if(con.responseCode==200)
            {

                val msg = JSONObject(builder.toString()).getString("msg")
                if(msg == "success")
                    return msg
                if(msg == "no client")
                    return msg

                if (JSONObject(builder.toString()).isNull("data")) return null
                val resultObject = JSONObject(builder.toString()).getJSONArray("data")

                val logList = ArrayList<LogInfo>()
                for (i: Int in 0 until resultObject.length()) {
                    val elem = resultObject.getJSONObject(i)
                    val clientLog = LogInfo()
                    clientLog.memberName = elem.getString("memberName")
                    clientLog.memberCategory = UtilManager.matchResourceId(elem.getInt("memberCategory"))
                    clientLog.savedName = elem.getString("savedName")
                    val splitted = elem.getString("callDate").split(" ")
                    clientLog.date = splitted[0] // YYYY-MM-DD
                    clientLog.time = splitted[1] // HH:mm:ss
                    when(elem.getInt("delimiter"))
                    {
                        1 -> clientLog.delimiter = LogDelimiterEnum.전화기록
                        2 -> clientLog.delimiter = LogDelimiterEnum.문자기록
                        3 -> clientLog.delimiter = LogDelimiterEnum.저장기록
                    }
                    when(elem.getInt("callType"))
                    {
                        1 -> clientLog.callType = CallTypeEnum.수신
                        2 -> clientLog.callType = CallTypeEnum.부재
                        3 -> clientLog.callType = CallTypeEnum.거절
                        4 -> clientLog.callType = CallTypeEnum.default
                        5 -> clientLog.callType = CallTypeEnum.발신
                    }
                    logList.add(clientLog)
                }
                return logList
            }
        }
        catch(e:Exception)
        {
            return "error"
        }
        return "else"
    }

    override fun onPostExecute(result: Any?) {
        callback?.let { it(result) }
    }
}