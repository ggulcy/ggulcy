package spamdetector.zani.dao

import org.json.JSONObject
import spamdetector.zani.model.data.ResponseInfo
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class LoginTask : MyAsyncTask<ArrayList<String>, ResponseInfo>() {

    override fun onPreExecute() { }

    override fun doInBackground(arg: ArrayList<String>?): ResponseInfo? {
        var con: HttpURLConnection? = null
        var reader: BufferedReader? = null

        val url = URL(arg?.get(0))
        con = url.openConnection() as HttpURLConnection?

        if (con == null) {
            return ResponseInfo("서버 내부 오류로 실행할 수 없습니다.", 500)
        }

        con.requestMethod = "POST"
        con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "text/json") // 서버로부터 response 데이터를 html로 받음

        con.doInput = true // InputStream 으로 서버로부터 응답을 받음

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

        val resultObject = JSONObject(builder.toString())
        if(resultObject.getInt("code")==200)
        {
            val date = resultObject.getString("licenseDate")
            val code = resultObject.getInt("code")
            val new = resultObject.getInt("new")
            val memberName = resultObject.getString("memberName")

            return ResponseInfo(msg=date, code=code, new=new, memberName=memberName)
        }

        return ResponseInfo(msg=resultObject.getString("msg"), code=resultObject.getInt("code"))
    }

    override fun onPostExecute(result: ResponseInfo?) {
        callback?.let { it(result!!) }
    }
}