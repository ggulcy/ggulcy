package spamdetector.zani.dao

import org.json.JSONArray
import org.json.JSONObject
import spamdetector.zani.model.data.PhoneBookInfo
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL

class SaveClientTaskDivide : MyAsyncTask<PhoneBookInfo, String>() {

    private val MAX_SIZE = 4000

    override fun onPreExecute() {
        preCall?.let { it() }
    }

    override fun doInBackground(arg: PhoneBookInfo?): String? {
        try {
            if (arg == null) {
                return ""
            }
            if (arg.clientList.isNullOrEmpty()) {
                return "success"
            }

            var con: HttpURLConnection?
            var reader: BufferedReader? = null

            val size = arg.clientList!!.size // 예시로 33500 길이의 리스트
            for (i in 0..size/MAX_SIZE) { // 예시로 i = 0,1,2,3
                var end = MAX_SIZE*(i+1) // 예시로 end = 10000, 20000, 30000, 40000
                if (size-end <= 0) {
                    end = size // 예시로 i = 3일 때 end = 40000이므로 end = 33500-(40000-10000) = 3500
                }
                val temp = arg.clientList!!.subList(MAX_SIZE*i, end) // 예시로 i = 3일 때 (30000, 33500) sub list
                val clientList = JSONArray()
                temp.forEach { e ->
                    if (e.number != "")
                        clientList.put("${e.number},${e.name}")
                }

                val url = URL("http://serverpc.iptime.org:5000/client/add")
                con = url.openConnection() as HttpURLConnection?

                if (con == null) {
                    return null
                }

                con.requestMethod = "POST"
                con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
                con.setRequestProperty("Content-Type", "application/json") // JSON 형식으로 전송
                con.setRequestProperty("Accept", "text/json"); // 서버로부터 response 데이터를 json으로 받음
                con.doOutput = true // OutStream 으로 post 데이터를 넘겨줌
                con.doInput = true // InputStream 으로 서버로부터 응답을 받음
                con.connect()

                val outStream: OutputStream = con.outputStream
                val writer = BufferedWriter(OutputStreamWriter(outStream))

                val obj = JSONObject()
                obj.put("clientList", clientList)
                obj.put("memberName", Preferences.getString(PreferencesKey.idKey, ""))
                obj.put("memberNew", arg.memberNew)

                // 버퍼를 생성하고 넣음
                writer.write(obj.toString())
                writer.flush()
                writer.close()

                val stream: InputStream = con.inputStream
                reader = BufferedReader(InputStreamReader(stream))
            }

            return ""
        } catch (e: SocketException) {
            return "network"
        } catch (e: Exception) {
            return "other"
        }
    }

    override fun onPostExecute(result: String?) {
        callback?.let { it(result) }
    }
}