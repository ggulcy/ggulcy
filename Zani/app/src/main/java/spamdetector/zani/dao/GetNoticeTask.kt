package spamdetector.zani.dao

import org.json.JSONObject
import spamdetector.zani.model.data.NoticeInfo
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class GetNoticeTask : MyAsyncTask<String, ArrayList<NoticeInfo>>() {

    override fun onPreExecute() {
        preCall?.let { it() }
    }

    override fun doInBackground(arg: String?): ArrayList<NoticeInfo>? {
        var con: HttpURLConnection? = null
        var reader: BufferedReader? = null

        val url = URL(arg)
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

        var noticeArray = ArrayList<NoticeInfo>()
        val resultObject = JSONObject(builder.toString()).getJSONArray("data")

        for (i: Int in 0 until resultObject.length()) {
            var result = resultObject.getJSONObject(i)

            var notice = NoticeInfo()
            notice.title = result.getString("title")
            notice.content = result.getString("content")
            notice.date = result.getString("date").split("T")[0]

            noticeArray.add(notice)
        }

        return noticeArray
    }

    override fun onPostExecute(result: ArrayList<NoticeInfo>?) {
        if (result != null)
            callback?.let { it(result) }
    }
}