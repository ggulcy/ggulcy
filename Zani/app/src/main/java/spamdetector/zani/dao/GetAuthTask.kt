package spamdetector.zani.dao

import android.content.Context
import org.json.JSONObject
import spamdetector.zani.R
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GetAuthTask(private val context: Context) : MyAsyncTask<String, String>() {

    override fun onPreExecute() {
        preCall?.let { it() }
    }

    override fun doInBackground(arg: String?): String? {
        if (arg == null || arg == "") {
            return "잘못된 접근입니다"
        }

        var con: HttpURLConnection? = null
        var reader: BufferedReader? = null

        val url = URL("http://serverpc.iptime.org:8080/member/auth")

        con = url.openConnection() as HttpURLConnection?

        if (con == null) {
            return null
        }

        con.requestMethod = "POST";
        con.setRequestProperty("Cache-Control", "no-cache"); // cache 설정
        con.setRequestProperty("Content-Type", "application/json"); // JSON 형식으로 전송
        con.setRequestProperty("Accept", "text/json"); // 서버로부터 response 데이터를 json으로 받음
        con.doOutput = true; // OutStream 으로 post 데이터를 넘겨줌
        con.doInput = true; // InputStream 으로 서버로부터 응답을 받음
        con.connect();

        var obj = JSONObject()
        obj.put("number", arg)

        // 버퍼를 생성하고 넣음
        val outStream: OutputStream = con.outputStream
        val writer = BufferedWriter(OutputStreamWriter(outStream))
        writer.write(obj.toString())
        writer.flush()
        writer.close()

        // 서버로부터 데이터를 받음
        val stream: InputStream = con.inputStream
        reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        var line: String? = ""
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
        }

        val msg = JSONObject(builder.toString()).getString("msg")
        val version = JSONObject(builder.toString()).getString("data")
        return if (msg == "auth fail") "인증되지 않은 기기 입니다"
        else if (version == context.getString(R.string.app_version)) "success"
        else "최신 버전으로 업데이트가 필요합니다"
    }

    override fun onPostExecute(result: String?) {
        if (result != null)
            callback?.let { it(result) }
    }
}