package spamdetector.zani.dao

import org.json.JSONObject
import spamdetector.zani.model.data.CallTypeEnum
import spamdetector.zani.model.data.ClientInfo
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.util.UtilManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GetClientTask : MyAsyncTask<ArrayList<String>, ClientInfo>() {

    override fun onPreExecute() {
        preCall?.let { it() }
    }

    override fun doInBackground(arg: ArrayList<String>?): ClientInfo? {
        try
        {
            var con: HttpURLConnection? = null
            var reader: BufferedReader? = null

            if (arg.isNullOrEmpty() || arg.size >0 && arg[0].length<10) return null
            val url = URL("http://serverpc.iptime.org:8080/client/one?number=${arg[0]}")
            con = url.openConnection() as HttpURLConnection?

            if (con == null) {
                return null
            }

            con.requestMethod = "GET"
            con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
            con.setRequestProperty("Accept", "text/json") // 서버로부터 response 데이터를 json으로 받음
            con.doInput = true // InputStream 으로 서버로부터 응답을 받음
            con.connect()

            // 서버로부터 설문 문항 수신
            val stream: InputStream = con.inputStream
            reader = BufferedReader(InputStreamReader(stream))
            val builder = StringBuilder()
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }

            val client = ClientInfo()

            if(con.responseCode==400) //이전에
            {
                client.count=-1
                return client
            }
            if (JSONObject(builder.toString()).isNull("data")) return null
            val resultObject = JSONObject(builder.toString()).getJSONObject("data")

            val callListJSONArray = resultObject.getJSONArray("logList")

            val logList = ArrayList<LogInfo>()
            for (i: Int in 0 until callListJSONArray.length()) {
                val elem = callListJSONArray.getJSONObject(i)
                val clientLogInfo = LogInfo()
                clientLogInfo.memberName = elem.getString("memberName")
                clientLogInfo.memberCategory = UtilManager.matchResourceId(elem.getInt("memberCategory"))
                clientLogInfo.savedName = elem.getString("savedName") // 전화번호 형태 이름이면 포맷팅
                val splitted = elem.getString("callDate").split(" ")
                clientLogInfo.date = splitted[0] // YYYY-MM-DD
                clientLogInfo.time = splitted[1] // HH:mm:ss
                when(elem.getInt("delimiter"))
                {
                    1 -> clientLogInfo.delimiter = LogDelimiterEnum.전화기록
                    2 -> clientLogInfo.delimiter = LogDelimiterEnum.문자기록
                    3 -> clientLogInfo.delimiter = LogDelimiterEnum.저장기록
                }
                when(elem.getInt("callType"))
                {
                    1 -> clientLogInfo.callType = CallTypeEnum.수신
                    2 -> clientLogInfo.callType = CallTypeEnum.부재
                    3 -> clientLogInfo.callType = CallTypeEnum.거절
                    4 -> clientLogInfo.callType = CallTypeEnum.default
                    5 -> clientLogInfo.callType = CallTypeEnum.발신
                }

                logList.add(clientLogInfo)
            }
            client.logList = logList
            client.count = resultObject.getInt("count")

            return client
        }
        catch(e:Exception)
        {
            val client = ClientInfo()

            client.count=-1
            return client
        }

    }

    override fun onPostExecute(result: ClientInfo?) {
        callback?.let { it(result) }
    }
}