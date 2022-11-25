package spamdetector.zani.dao

import org.json.JSONObject
import spamdetector.zani.model.data.BlockInfo
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GetMemberBlockListTask :MyAsyncTask<ArrayList<String>, ArrayList<BlockInfo>>()
{
    override fun onPreExecute() { }

    override fun doInBackground(arg: ArrayList<String>?): ArrayList<BlockInfo>? {
        var con: HttpURLConnection? = null
        var reader: BufferedReader? = null

        val stringBuilder =
            java.lang.StringBuilder(arg?.get(0))
        stringBuilder.append("?name=${arg?.get(1)}")
        val url = URL(stringBuilder.toString())
        con = url.openConnection() as HttpURLConnection?

        if (con == null) {
            return null
        }

        con.requestMethod = "GET"
        con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "text/json") // 서버로부터 response 데이터를 html로 받음

        con.doInput = true // InputStream 으로 서버로부터 응답을 받음
        con.connect()

        val stream: InputStream = con.inputStream
        reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        var line: String? = ""
        while (reader.readLine().also { line = it } != null)
        {
            builder.append(line)
        }
        val array =ArrayList<BlockInfo>()

        if (con.responseCode == 200) {
            val dataObejct =  JSONObject(builder.toString()).getJSONArray("data")
            for (i: Int in 0 until dataObejct.length())
            {
                array.add(BlockInfo(phoneNum = dataObejct[i].toString()))
            }
        }
        return array
    }

    override fun onPostExecute(result: ArrayList<BlockInfo>?) {
        callback?.let { it(result!!) }
    }
}